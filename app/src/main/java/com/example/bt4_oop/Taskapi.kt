package com.example.bt4_oop

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// ---------------------------------------------------------------
// LỚP GỌI API MOCK:  https://share.tryapps.store/mock/teach/tasks
//   GET    /tasks/{id}   -> mở chi tiết một việc
//   PATCH  /tasks/{id}   -> sửa / đánh dấu hoàn thành / việc nhỏ
//   DELETE /tasks/{id}   -> xóa công việc
// Chỉ dùng HttpURLConnection + org.json (có sẵn trong Android), không cần thêm thư viện.
// ---------------------------------------------------------------

/** Dữ liệu trả về của GET /tasks/{id} (đúng 10 trường như response mẫu). */
data class ApiSubTask(val title: String, val done: Boolean)

data class ApiTask(
    val id: Int,
    val title: String,
    val note: String,
    val category: String,   // "study" | "life"
    val date: String,       // "2026-10-20"
    val time: String,       // "09:00"
    val priority: String,   // "low" | "medium" | "high"
    val remind: Boolean,
    val done: Boolean,
    val subtasks: List<ApiSubTask>
)

object TaskApi {
    private const val TAG = "TASK_API"
    private const val BASE_URL = "https://share.tryapps.store/mock/teach/tasks"

    // Panel API ghi "Authorization: Bearer $MOCK_API_TOKEN". Nếu server không đòi token thì để trống.
    private const val MOCK_API_TOKEN = ""

    // Scope riêng để PATCH/DELETE vẫn chạy tiếp dù màn hình đã đóng (vd: vừa xóa xong là quay lại).
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ---------- GET (danh sách) ----------
    suspend fun listTasks(): Result<List<ApiTask>> = withContext(Dispatchers.IO) {
        runCatching {
            val (code, body) = request("GET", BASE_URL)
            Log.d(TAG, "GET /tasks -> $code")
            if (code !in 200..299) error("HTTP $code")
            parseList(body)
        }.onFailure { Log.e(TAG, "GET list lỗi", it) }
    }

    // ---------- POST ----------
    suspend fun createTask(task: TodoTask): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val (code, _) = request("POST", BASE_URL, task.toCreateJson().toString())
            Log.d(TAG, "POST /tasks -> $code")
            if (code !in 200..299) error("HTTP $code")
            code
        }.onFailure { Log.e(TAG, "POST lỗi", it) }
    }

    fun createAsync(task: TodoTask) {
        scope.launch { createTask(task) }
    }

    // ---------- GET ----------
    suspend fun getTask(id: Int): Result<ApiTask> = withContext(Dispatchers.IO) {
        runCatching {
            val (code, body) = request("GET", "$BASE_URL/$id")
            Log.d(TAG, "GET /tasks/$id -> $code")
            if (code !in 200..299) error("HTTP $code")
            parseTask(JSONObject(body))
        }.onFailure { Log.e(TAG, "GET lỗi", it) }
    }

    // ---------- PATCH ----------
    suspend fun patchTask(task: TodoTask): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val (code, _) = request("PATCH", "$BASE_URL/${task.id}", task.toPatchJson().toString())
            Log.d(TAG, "PATCH /tasks/${task.id} -> $code")
            if (code !in 200..299) error("HTTP $code")
            code
        }.onFailure { Log.e(TAG, "PATCH lỗi", it) }
    }

    /** Gọi và quên: dùng trong UI, không chờ kết quả. */
    fun patchAsync(task: TodoTask) {
        scope.launch { patchTask(task) }
    }

    // ---------- DELETE ----------
    suspend fun deleteTask(id: Int): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val (code, _) = request("DELETE", "$BASE_URL/$id")
            Log.d(TAG, "DELETE /tasks/$id -> $code")
            if (code !in 200..299) error("HTTP $code")
            code
        }.onFailure { Log.e(TAG, "DELETE lỗi", it) }
    }

    fun deleteAsync(id: Int) {
        scope.launch { deleteTask(id) }
    }

    // ---------- Chi tiết kỹ thuật ----------
    private fun request(method: String, url: String, jsonBody: String? = null): Pair<Int, String> {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("Accept", "application/json")
            if (MOCK_API_TOKEN.isNotBlank()) {
                conn.setRequestProperty("Authorization", "Bearer $MOCK_API_TOKEN")
            }
            if (jsonBody != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.outputStream.use { it.write(jsonBody.toByteArray(Charsets.UTF_8)) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            return code to text
        } finally {
            conn.disconnect()
        }
    }

    private fun parseList(body: String): List<ApiTask> {
        val t = body.trim()
        val arr = if (t.startsWith("[")) JSONArray(t) else JSONObject(t).let {
            it.optJSONArray("data") ?: it.optJSONArray("tasks") ?: it.optJSONArray("items") ?: JSONArray()
        }
        return (0 until arr.length()).map { parseTask(arr.getJSONObject(it)) }
    }

    /** POST /tasks: gửi đủ các trường như response mẫu (date tính theo "Hôm nay / Ngày mai / Tuần này"). */
    private fun TodoTask.toCreateJson(): JSONObject = toPatchJson().apply {
        put("id", id)
        put("date", dateFor(day))
    }

    private fun dateFor(day: DayOption): String {
        val c = java.util.Calendar.getInstance()
        when (day) {
            DayOption.TODAY -> {}
            DayOption.TOMORROW -> c.add(java.util.Calendar.DAY_OF_YEAR, 1)
            DayOption.THIS_WEEK -> { // chủ nhật sắp tới (khớp dữ liệu mẫu: 20/10 -> 25/10)
                val untilSunday = (java.util.Calendar.SATURDAY - c.get(java.util.Calendar.DAY_OF_WEEK) + 1) % 7
                c.add(java.util.Calendar.DAY_OF_YEAR, if (untilSunday == 0) 7 else untilSunday)
            }
        }
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(c.time)
    }

    private fun parseTask(o: JSONObject): ApiTask {
        val subs = o.optJSONArray("subtasks") ?: JSONArray()
        val subList = (0 until subs.length()).map { i ->
            val s = subs.getJSONObject(i)
            ApiSubTask(s.optString("title"), s.optBoolean("done"))
        }
        return ApiTask(
            id = o.optInt("id"),
            title = o.optString("title"),
            note = o.optString("note"),
            category = o.optString("category"),
            date = o.optString("date"),
            time = o.optString("time"),
            priority = o.optString("priority"),
            remind = o.optBoolean("remind"),
            done = o.optBoolean("done"),
            subtasks = subList
        )
    }

    /** PATCH là cập nhật một phần: chỉ gửi các trường màn Chi tiết có thể thay đổi. */
    private fun TodoTask.toPatchJson(): JSONObject = JSONObject().apply {
        put("title", title)
        put("note", note)
        put("category", category.name.lowercase())
        put("time", timeText)
        put("priority", priority.name.lowercase())
        put("remind", reminder)
        put("done", done)
        put("subtasks", JSONArray().apply {
            subtasks.forEach { put(JSONObject().put("title", it.title).put("done", it.done)) }
        })
    }
}