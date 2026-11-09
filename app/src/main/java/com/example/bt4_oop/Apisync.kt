package com.example.bt4_oop

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// ---------------------------------------------------------------
// ĐỒNG BỘ DANH SÁCH TỪ API MOCK  ->  TodoState
//   GET /mock/teach/tasks  trả về mảng 6 việc (id, title, note, category, date, time,
//   priority, remind, done, subtasks). Hàm dưới đây đổ chúng vào state.tasks để
//   Trang chủ / Danh sách / Chi tiết hiển thị đúng dữ liệu của API.
// ---------------------------------------------------------------

private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

/** Thay cho syncWithFirestore(): tải danh sách từ API, nếu lỗi mạng thì quay về Firestore. */
fun TodoState.syncWithApi() {
    syncScope.launch {
        TaskApi.listTasks()
            .onSuccess { list ->
                if (list.isEmpty()) {
                    syncWithFirestore()
                    return@onSuccess
                }
                // API chỉ có trường "date" (yyyy-MM-dd). Lấy ngày sớm nhất làm "hôm nay" của dữ liệu mẫu:
                // cùng ngày -> Hôm nay, +1 -> Ngày mai, còn lại -> Tuần này.
                val base = list.mapNotNull { parseDateMillis(it.date) }.minOrNull()
                val mapped = list.map { it.toTodoTask(base) }.sortedBy { it.id }
                tasks.clear()
                tasks.addAll(mapped)
                Log.d("TASK_API", "Đã tải ${mapped.size} việc từ API")
            }
            .onFailure { syncWithFirestore() }
    }
}

private fun parseDateMillis(date: String): Long? = try {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date)?.time
} catch (_: Exception) {
    null
}

fun ApiTask.toTodoTask(baseMillis: Long?): TodoTask {
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 12
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val dayOffset = if (baseMillis == null) 0L else {
        val d = parseDateMillis(date) ?: baseMillis
        Math.round((d - baseMillis) / 86_400_000.0)
    }
    val day = when {
        dayOffset <= 0L -> DayOption.TODAY
        dayOffset == 1L -> DayOption.TOMORROW
        else -> DayOption.THIS_WEEK
    }

    return TodoTask(
        id = id,
        title = title,
        note = note,
        category = if (category.equals("life", ignoreCase = true)) Category.LIFE else Category.STUDY,
        day = day,
        hour = hour,
        minute = minute,
        priority = when (priority.lowercase()) {
            "low" -> Priority.LOW
            "high" -> Priority.HIGH
            else -> Priority.MEDIUM
        },
        reminder = remind,
        done = done,
        subtasks = subtasks.mapIndexed { i, s -> SubTask(i + 1, s.title, s.done) }
    )
}