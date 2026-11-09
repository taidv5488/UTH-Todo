package com.example.bt4_oop

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.content.Intent
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.bt4_oop.ui.theme.BT4OOPTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.firestore.SetOptions
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import android.content.pm.PackageManager
import android.os.Build


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM_TOKEN", "Không lấy được token", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM_TOKEN", token)
            }
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle().apply { putString("screen_name", "main") }
        analytics.logEvent("test_event", bundle)
        enableEdgeToEdge()
        setContent {
            BT4OOPTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = ScreenBg
                ) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// MÀU SẮC & GIAO DIỆN (LIGHT / DARK / AUTO)
// ---------------------------------------------------------------

val Primary = Color(0xFF2F5BEA)
val ScreenBg = Color(0xFFF4F4F7)
val TextMuted = Color(0xFF6B6F7B)
val TextDark = Color(0xFF1C1C1E)
val DangerRed = Color(0xFFD93025)
val SwitchGreen = Color(0xFF34C759)

data class AppColors(
    val primary: Color,
    val screenBg: Color,
    val cardBg: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val divider: Color,
    val isDark: Boolean
)

@Composable
fun rememberAppColors(state: TodoState): AppColors {
    val isDark = when (state.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            hour !in 6..<18 // Từ 18h00 tối đến 6h00 sáng là Chế độ Tối (DARK)
        }
    }

    val accents = listOf(Color(0xFF2F5BEA), Color(0xFF0B8A5F), Color(0xFFC8440F), Color(0xFF7C3AED))
    val primaryColor = accents.getOrElse(state.accentIndex) { Color(0xFF2F5BEA) }

    return if (isDark) {
        AppColors(
            primary = primaryColor,
            screenBg = Color(0xFF121212),       // Đen tuyền cho Nền Tối
            cardBg = Color(0xFF1E1E1E),         // Nền thẻ màu tối
            textPrimary = Color.White,          // Chữ trắng
            textMuted = Color(0xFFA0A5B5),       // Chữ phụ màu mờ sáng
            divider = Color(0xFF2C2C2E),
            isDark = true
        )
    } else {
        AppColors(
            primary = primaryColor,
            screenBg = Color(0xFFF4F4F7),       // Nền sáng trắng/xám nhạt
            cardBg = Color.White,               // Nền thẻ màu trắng
            textPrimary = Color(0xFF1C1C1E),    // Chữ đen
            textMuted = Color(0xFF6B6F7B),       // Chữ phụ màu xám
            divider = Color(0xFFEDEDF0),
            isDark = false
        )
    }
}

// ---------------------------------------------------------------
// DỮ LIỆU & ENUM
// ---------------------------------------------------------------

enum class Category(val label: String, val bg: Color, val fg: Color) {
    STUDY("Học tập", Color(0xFFE3EAFD), Primary),
    LIFE("Cuộc sống", Color(0xFFFFE6D5), Color(0xFFB5470C))
}

enum class DayOption(val label: String) {
    TODAY("Hôm nay"),
    TOMORROW("Ngày mai"),
    THIS_WEEK("Tuần này")
}

enum class Priority(val label: String) {
    LOW("Thấp"),
    MEDIUM("Vừa"),
    HIGH("Cao")
}

enum class NotiType { DUE_SOON, OVERDUE, SUCCESS, REMINDER }

enum class ThemeMode { LIGHT, DARK, AUTO }

data class SubTask(val id: Int, val title: String, val done: Boolean = false)

data class TodoTask(
    val id: Int,
    val title: String,
    val note: String = "",
    val category: Category,
    val day: DayOption,
    val hour: Int,
    val minute: Int,
    val priority: Priority = Priority.MEDIUM,
    val reminder: Boolean = true,
    val done: Boolean = false,
    val subtasks: List<SubTask> = emptyList()
) {
    val timeText: String get() = "%02d:%02d".format(hour, minute)
}

data class AppNotification(
    val id: Int,
    val type: NotiType,
    val title: String,
    val message: String,
    val time: String,
    val unread: Boolean
)

// Mô tả nội dung của một trang onboarding
data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

// Danh sách 3 trang onboarding
val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.b,
        title = "Mọi việc, gọn trong một chỗ",
        description = "Ghi lại bài tập, việc nhà, lịch hẹn. Không còn quên việc nào."
    ),
    OnboardingPage(
        imageRes = R.drawable.c,
        title = "Chia theo Học tập và Cuộc sống",
        description = "Mỗi việc có nhóm riêng, nhìn là biết hôm nay cần lo gì."
    ),
    OnboardingPage(
        imageRes = R.drawable.d,
        title = "Nhắc đúng lúc.",
        description = "Bật thông báo để UTH Todo nhắc bạn trước hạn chót."
    )
)

// Trạng thái dùng chung cho toàn app tích hợp Cloud Firestore & Analytics
class TodoState {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    val tasks = mutableStateListOf<TodoTask>()

    val notifications = mutableStateListOf(
        AppNotification(1, NotiType.DUE_SOON, "Sắp đến hạn", "Ôn chương 3 – Cấu trúc dữ liệu lúc 14:00.", "13:45", true),
        AppNotification(2, NotiType.OVERDUE, "Đã quá hạn", "Bài đọc tham khảo môn Mạng máy tính chưa xong.", "10:10", true),
        AppNotification(3, NotiType.SUCCESS, "Làm tốt lắm!", "Bạn đã xong 2 việc trong sáng nay.", "11:30", false),
        AppNotification(4, NotiType.REMINDER, "Nhắc việc", "Họp nhóm đồ án trong tuần này.", "Hôm qua", false)
    )

    var themeMode by mutableStateOf(ThemeMode.LIGHT)
    var accentIndex by mutableIntStateOf(0)
    var remindEnabled by mutableStateOf(true)
    var soundEnabled by mutableStateOf(true)
    var dailySummary by mutableStateOf(false)

    var userName by mutableStateOf("Văn Tài")
    var avatarUriString by mutableStateOf<String?>(null)

    fun syncWithFirestore() {
        val uid = auth.currentUser?.uid ?: return
        listenerRegistration?.remove()

        // Lắng nghe thay đổi thông tin profile User
        db.collection("users").document(uid).addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                doc.getString("name")?.let { if (it.isNotBlank()) userName = it }
                doc.getString("avatarUri")?.let { avatarUriString = it }
            }
        }

        val userTasksRef = db.collection("users").document(uid).collection("tasks")
        listenerRegistration = userTasksRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            if (snapshot.isEmpty) {
                // Đẩy dữ liệu mẫu ban đầu lên Firestore nếu user chưa có task nào
                val sampleTasks = listOf(
                    TodoTask(1, "Tập thể dục 30 phút", category = Category.LIFE, day = DayOption.TODAY, hour = 6, minute = 30, done = true),
                    TodoTask(
                        2, "Nộp bài tập Lập trình di động",
                        note = "Nộp file .zip lên hệ thống của trường. Nhớ kèm ảnh chụp màn hình app.",
                        category = Category.STUDY, day = DayOption.TODAY, hour = 9, minute = 0, done = true,
                        priority = Priority.HIGH,
                        subtasks = listOf(
                            SubTask(1, "Hoàn thiện màn hình đăng nhập", true),
                            SubTask(2, "Chụp ảnh màn hình app", true),
                            SubTask(3, "Nén file và nộp bài", true)
                        )
                    ),
                    TodoTask(3, "Ôn chương 3 – Cấu trúc dữ liệu", category = Category.STUDY, day = DayOption.TODAY, hour = 14, minute = 0),
                    TodoTask(4, "Đi chợ mua đồ cho tuần", category = Category.LIFE, day = DayOption.TODAY, hour = 17, minute = 30),
                    TodoTask(5, "Gọi điện về nhà", category = Category.LIFE, day = DayOption.TOMORROW, hour = 21, minute = 0),
                    TodoTask(6, "Họp nhóm đồ án", category = Category.STUDY, day = DayOption.THIS_WEEK, hour = 20, minute = 0, priority = Priority.HIGH)
                )
                sampleTasks.forEach { task ->
                    userTasksRef.document(task.id.toString()).set(taskToMap(task))
                }
            } else {
                val list = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val note = doc.getString("note") ?: ""
                    val categoryName = doc.getString("category") ?: "STUDY"
                    val dayName = doc.getString("day") ?: "TODAY"
                    val hour = doc.getLong("hour")?.toInt() ?: 12
                    val minute = doc.getLong("minute")?.toInt() ?: 0
                    val priorityName = doc.getString("priority") ?: "MEDIUM"
                    val reminder = doc.getBoolean("reminder") ?: true
                    val done = doc.getBoolean("done") ?: false

                    val subtasks = (doc.get("subtasks") as? List<*>)?.mapNotNull { item ->
                        val m = item as? Map<*, *> ?: return@mapNotNull null
                        SubTask(
                            id = (m["id"] as? Number)?.toInt() ?: 0,
                            title = m["title"] as? String ?: return@mapNotNull null,
                            done = m["done"] as? Boolean ?: false
                        )
                    } ?: emptyList()

                    val category = try { Category.valueOf(categoryName) } catch (_: Exception) { Category.STUDY }
                    val day = try { DayOption.valueOf(dayName) } catch (_: Exception) { DayOption.TODAY }
                    val priority = try { Priority.valueOf(priorityName) } catch (_: Exception) { Priority.MEDIUM }

                    TodoTask(
                        id = id, title = title, note = note, category = category,
                        day = day, hour = hour, minute = minute, priority = priority,
                        reminder = reminder, done = done, subtasks = subtasks
                    )
                }
                tasks.clear()
                tasks.addAll(list.sortedBy { it.id })
            }
        }
    }

    private fun taskToMap(task: TodoTask): Map<String, Any> {
        return hashMapOf(
            "id" to task.id,
            "title" to task.title,
            "note" to task.note,
            "category" to task.category.name,
            "day" to task.day.name,
            "hour" to task.hour,
            "minute" to task.minute,
            "priority" to task.priority.name,
            "reminder" to task.reminder,
            "done" to task.done,
            "subtasks" to task.subtasks.map { mapOf("id" to it.id, "title" to it.title, "done" to it.done) },
            "updatedAt" to System.currentTimeMillis()
        )
    }

    fun addTask(task: TodoTask) {
        val newId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
        val taskWithId = task.copy(id = newId)
        tasks.add(taskWithId)
        TaskApi.createAsync(taskWithId)

        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("tasks")
            .document(taskWithId.id.toString())
            .set(taskToMap(taskWithId))
    }

    fun updateUserProfile(name: String, avatarUri: String?) {
        userName = name
        avatarUriString = avatarUri

        val uid = auth.currentUser?.uid ?: return
        val updates = hashMapOf<String, Any?>(
            "name" to name,
            "avatarUri" to avatarUri,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).set(updates, SetOptions.merge())
    }

    fun updateTask(task: TodoTask) {
        val i = tasks.indexOfFirst { it.id == task.id }
        if (i >= 0) tasks[i] = task
        TaskApi.patchAsync(task)

        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("tasks")
            .document(task.id.toString())
            .set(taskToMap(task))
    }

    fun deleteTask(id: Int) {
        tasks.removeAll { it.id == id }
        TaskApi.deleteAsync(id)

        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("tasks")
            .document(id.toString())
            .delete()
    }

    fun toggle(id: Int) {
        val i = tasks.indexOfFirst { it.id == id }
        if (i >= 0) {
            val updated = tasks[i].copy(done = !tasks[i].done)
            tasks[i] = updated
            TaskApi.patchAsync(updated)

            val uid = auth.currentUser?.uid ?: return
            db.collection("users").document(uid).collection("tasks")
                .document(id.toString())
                .update("done", updated.done)
        }
    }

    fun markAllRead() {
        for (i in notifications.indices) notifications[i] = notifications[i].copy(unread = false)
    }
}

// ---------------------------------------------------------------
// ĐIỀU HƯỚNG
// ---------------------------------------------------------------

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Main : Screen("main_screen")
    object Detail : Screen("detail_screen")
    object Third : Screen("third_screen")
    object Home : Screen("home_screen")
    object Dashboard : Screen("dashboard_screen")
    object NewTask : Screen("new_task_screen")
    object Profile : Screen("profile_screen")
    object TaskDetail : Screen("task_detail/{taskId}?from={from}") {
        fun createRoute(id: Int, from: String) = "task_detail/$id?from=$from"
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val todoState = remember { TodoState() }

    LaunchedEffect(Unit) {
        todoState.syncWithFirestore()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // Màn 1: Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Màn 2: Onboarding 1
        composable(Screen.Main.route) {
            OnboardingScreen(
                page = onboardingPages[0],
                onNext = { navController.navigate(Screen.Detail.route) }
            )
        }

        // Màn 3: Onboarding 2
        composable(Screen.Detail.route) {
            OnboardingScreen(
                page = onboardingPages[1],
                onNext = { navController.navigate(Screen.Third.route) }
            )
        }

        // Màn 4: Onboarding 3
        composable(Screen.Third.route) {
            OnboardingScreen(
                page = onboardingPages[2],
                onNext = { navController.navigate(Screen.Home.route) }
            )
        }

        // Màn 5: Home (Đăng nhập Google hoặc Dùng thử)
        composable(Screen.Home.route) {
            val context = LocalContext.current
            val auth = FirebaseAuth.getInstance()
            HomeScreen(
                onGoogleSignIn = {
                    Toast.makeText(context, "Đăng nhập bằng Google đang phát triển", Toast.LENGTH_SHORT).show()
                },
                onGuestLogin = {
                    auth.signInAnonymously()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                if (user != null) {
                                    val userData = hashMapOf<String, Any>(
                                        "uid" to user.uid,
                                        "isAnonymous" to user.isAnonymous,
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                    // Lưu thông tin người dùng vào Realtime Database
                                    RealtimeDatabaseManager.getUserRef(user.uid)
                                        .updateChildren(userData)
                                        .addOnSuccessListener {
                                            Log.d("REALTIME_DB", "Lưu người dùng thành công")
                                        }
                                        .addOnFailureListener { error ->
                                            Log.e("REALTIME_DB", "Lỗi lưu người dùng", error)
                                        }
                                    FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(user.uid)
                                        .set(userData)

                                    val analytics = FirebaseAnalytics.getInstance(context)
                                    val bundle = Bundle().apply {
                                        putString(FirebaseAnalytics.Param.METHOD, "anonymous")
                                    }
                                    analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
                                }

                                todoState.syncWithFirestore()
                                Toast.makeText(
                                    context,
                                    "Đăng nhập khách thành công",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            } else {
                                val err = task.exception?.localizedMessage ?: "Đăng nhập thất bại"
                                Toast.makeText(
                                    context,
                                    "Lỗi: $err",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            )
        }

        // Màn 6: Dashboard (4 Tab chính)
        composable(Screen.Dashboard.route) {
            TodoShell(
                state = todoState,
                onAddClick = { navController.navigate(Screen.NewTask.route) },
                onOpenProfile = { navController.navigate(Screen.Profile.route) },
                onOpenTask = { id, from -> navController.navigate(Screen.TaskDetail.createRoute(id, from)) }
            )
        }

        // Màn 7: Tạo công việc mới
        composable(Screen.NewTask.route) {
            NewTaskScreen(
                state = todoState,
                onCancel = { navController.popBackStack() },
                onSave = { newTask ->
                    todoState.addTask(newTask)
                    navController.popBackStack()
                }
            )
        }

        // Màn 8: Hồ sơ cá nhân
        composable(Screen.Profile.route) {
            ProfileScreen(
                state = todoState,
                onBack = { navController.popBackStack() },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Màn 9: Chi tiết công việc
        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType },
                navArgument("from") { type = NavType.StringType; defaultValue = "home" }
            )
        ) { entry ->
            val id = entry.arguments?.getInt("taskId") ?: -1
            val from = entry.arguments?.getString("from") ?: "home"
            TaskDetailScreenV2(
                state = todoState,
                taskId = id,
                backLabel = if (from == "list") "Danh sách" else "Trang chủ",
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ---------------------------------------------------------------
// MÀN HÌNH INTRO & LOGIN
// ---------------------------------------------------------------

@Composable
fun SplashScreen(onNavigateToMain: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(key1 = true) {
        delay(1000.milliseconds)
        onNavigateToMain()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF2196F3))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.a),
            contentDescription = "avatar",
            modifier = Modifier.size(130.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Việc học, việc nhà - gọn gàng mỗi ngày",
            color = Color.White,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Phiên bản 2.1.0",
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
fun OnboardingScreen(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        AppHeader()

        Spacer(modifier = Modifier.height(50.dp))
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.title,
            modifier = Modifier.size(300.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = page.title,
            fontSize = 40.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(25.dp))
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = Color.Gray
        )

        if (onNext != null) {
            Spacer(modifier = Modifier.weight(1f))
            NextButton(onClick = onNext)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AppHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.a),
            contentDescription = "avatar",
            modifier = Modifier.size(50.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "UTH",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Todo",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F5BEA)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Bỏ qua",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun NextButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        shape = RoundedCornerShape(25.dp)
    ) {
        Text(
            text = "Tiếp theo",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onGoogleSignIn: () -> Unit = {},
    onGuestLogin: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.a),
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Chào mừng đến với UTH Todo!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Đăng nhập để lưu công việc và đồng bộ trên mọi thiết bị của bạn.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(25.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "🌐 Đăng nhập bằng Google",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGuestLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F5BEA)),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "Dùng thử (Tài khoản khách)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ---------------------------------------------------------------
// KHUNG CHÍNH: NỘI DUNG 4 TAB + THANH ĐIỀU HƯỚNG DƯỚI
// ---------------------------------------------------------------

@Composable
fun TodoShell(
    state: TodoState,
    onAddClick: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenTask: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val colors = rememberAppColors(state)

    Column(modifier = modifier.fillMaxSize().background(colors.screenBg)) {
        Box(modifier = Modifier.weight(1f)) {
            when (tab) {
                0 -> HomeTab(
                    state = state,
                    colors = colors,
                    onOpenProfile = onOpenProfile,
                    onSeeAll = { tab = 1 },
                    onOpenNotifications = { tab = 2 },
                    onOpenTask = { onOpenTask(it, "home") }
                )
                1 -> ListTab(state = state, colors = colors, onOpenTask = { onOpenTask(it, "list") })
                2 -> NotificationsTab(state = state, colors = colors)
                else -> SettingsTab(state = state, colors = colors, onOpenProfile = onOpenProfile)
            }
        }
        BottomBar(selected = tab, colors = colors, onSelect = { tab = it }, onAdd = onAddClick)
    }
}

@Composable
fun BottomBar(selected: Int, colors: AppColors, onSelect: (Int) -> Unit, onAdd: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(color = colors.cardBg, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(Icons.Default.Home, "Trang chủ", selected == 0, colors, Modifier.weight(1f)) { onSelect(0) }
                NavItem(Icons.AutoMirrored.Filled.List, "Danh sách", selected == 1, colors, Modifier.weight(1f)) { onSelect(1) }
                Spacer(modifier = Modifier.weight(1f))
                NavItem(Icons.Default.Notifications, "Thông báo", selected == 2, colors, Modifier.weight(1f)) { onSelect(2) }
                NavItem(Icons.Default.Settings, "Cài đặt", selected == 3, colors, Modifier.weight(1f)) { onSelect(3) }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            shape = CircleShape,
            containerColor = colors.primary,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-22).dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm việc")
        }
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = if (selected) colors.primary else colors.textMuted
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = color, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

// ---------------------------------------------------------------
// THÀNH PHẦN DÙNG LẠI
// ---------------------------------------------------------------

@Composable
fun CategoryChip(category: Category) {
    Text(
        text = category.label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = category.fg,
        modifier = Modifier
            .background(category.bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
fun CheckCircle(checked: Boolean, primaryColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .then(
                if (checked) Modifier.background(primaryColor)
                else Modifier.border(1.5.dp, Color(0xFFC7C7CC), CircleShape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun TaskRow(task: TodoTask, showDay: Boolean, colors: AppColors, onToggle: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckCircle(task.done, colors.primary, onToggle)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (task.done) Color.Gray else colors.textPrimary,
                textDecoration = if (task.done) TextDecoration.LineThrough else null
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryChip(task.category)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showDay) "${task.day.label}, ${task.timeText}" else task.timeText,
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }
        if (showDay && task.priority == Priority.HIGH && !task.done) {
            Text(
                "Gấp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DangerRed,
                modifier = Modifier.background(Color(0xFFFFE0E0), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        if (!showDay) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = colors.textMuted)
        }
    }
}

@Composable
fun Avatar(
    size: Int,
    avatarUri: String? = null,
    name: String = "Văn Tài",
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val bitmap = remember(avatarUri) {
        if (!avatarUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(avatarUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val initials = name.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .takeLast(2)
        .joinToString("")
        .ifEmpty { "VT" }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(0xFFE3EAFD))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials,
                color = Color(0xFF1D4ED8),
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.32f).sp
            )
        }
    }
}

fun todayLabel(): String {
    val c = Calendar.getInstance()
    val days = arrayOf("Chủ nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
    return "${days[c.get(Calendar.DAY_OF_WEEK) - 1]}, ${c.get(Calendar.DAY_OF_MONTH)} tháng ${c.get(Calendar.MONTH) + 1}"
}

fun formatTime12(hour: Int, minute: Int): String {
    val suffix = if (hour < 12) "SA" else "CH"
    val h = if (hour % 12 == 0) 12 else hour % 12
    return "%02d:%02d %s".format(h, minute, suffix)
}

// ---------------------------------------------------------------
// TAB 1: TRANG CHỦ
// ---------------------------------------------------------------

@Composable
fun HomeTab(
    state: TodoState,
    colors: AppColors,
    onOpenProfile: () -> Unit,
    onSeeAll: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenTask: (Int) -> Unit
) {
    val today = state.tasks.filter { it.day == DayOption.TODAY }
    val doneCount = today.count { it.done }
    val progress = if (today.isEmpty()) 0f else doneCount.toFloat() / today.size
    val hasUnread = state.notifications.any { it.unread }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(48, avatarUri = state.avatarUriString, name = state.userName, onClick = onOpenProfile)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(todayLabel(), fontSize = 13.sp, color = colors.textMuted)
                    Text("Chào ${state.userName}!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.cardBg).clickable(onClick = onOpenNotifications),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, "Thông báo", tint = colors.textPrimary)
                    if (hasUnread) {
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(10.dp).size(8.dp).clip(CircleShape).background(DangerRed)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().background(colors.primary, RoundedCornerShape(20.dp)).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tiến độ hôm nay", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text("$doneCount/${today.size} việc", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = when {
                            today.isNotEmpty() && doneCount == today.size -> "Tuyệt vời, xong hết rồi!"
                            progress >= 0.5f -> "Đã hơn nửa chặng, cố lên!"
                            else -> "Bắt đầu thôi nào!"
                        },
                        color = Color.White, fontSize = 13.sp
                    )
                }
                ProgressRing(progress)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryCard(Category.STUDY, state.tasks.count { it.category == Category.STUDY && !it.done }, colors, Modifier.weight(1f))
                CategoryCard(Category.LIFE, state.tasks.count { it.category == Category.LIFE && !it.done }, colors, Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Việc hôm nay", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.weight(1f))
                Text("Xem tất cả", fontSize = 14.sp, color = colors.primary, fontWeight = FontWeight.Medium, modifier = Modifier.clickable(onClick = onSeeAll))
            }
        }

        items(today, key = { it.id }) { task ->
            TaskRow(task, showDay = false, colors = colors, onToggle = { state.toggle(task.id) }, onClick = { onOpenTask(task.id) })
        }
    }
}

@Composable
fun ProgressRing(progress: Float) {
    Box(modifier = Modifier.size(68.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 8.dp.toPx()
            val inset = strokeW / 2
            val arcSize = Size(size.width - strokeW, size.height - strokeW)
            val stroke = Stroke(width = strokeW, cap = StrokeCap.Round)
            drawArc(Color.White.copy(alpha = 0.3f), 0f, 360f, false, Offset(inset, inset), arcSize, style = stroke)
            drawArc(Color.White, -90f, 360f * progress, false, Offset(inset, inset), arcSize, style = stroke)
        }
        Text("${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun CategoryCard(category: Category, remaining: Int, colors: AppColors, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(colors.cardBg, RoundedCornerShape(16.dp)).padding(14.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(category.bg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (category == Category.STUDY) Icons.AutoMirrored.Filled.List else Icons.Default.Home,
                null, tint = category.fg, modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(category.label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
        Text("$remaining việc còn lại", fontSize = 13.sp, color = colors.textMuted)
    }
}

// ---------------------------------------------------------------
// TAB 2: DANH SÁCH
// ---------------------------------------------------------------

@Composable
fun ListTab(state: TodoState, colors: AppColors, onOpenTask: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(-1) }

    val filtered = state.tasks.filter { t ->
        t.title.contains(query, ignoreCase = true) &&
                (filter == -1 || t.category.ordinal == filter)
    }
    val pending = filtered.filter { !it.done }
    val done = filtered.filter { it.done }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Danh sách", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.weight(1f))
                Text("${state.tasks.count { !it.done }} việc chưa xong", fontSize = 13.sp, color = colors.textMuted)
            }
        }
        item {
            val fieldBg = if (colors.isDark) Color(0xFF2C2C2E) else Color(0xFFE9E9EE)
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Tìm công việc", color = colors.textMuted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.textMuted) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill("Tất cả", filter == -1, colors) { filter = -1 }
                FilterPill("Học tập", filter == 0, colors) { filter = 0 }
                FilterPill("Cuộc sống", filter == 1, colors) { filter = 1 }
            }
        }

        if (pending.isNotEmpty()) {
            item { SectionLabel("CẦN LÀM", colors) }
            items(pending, key = { it.id }) { TaskRow(it, showDay = true, colors = colors, onToggle = { state.toggle(it.id) }, onClick = { onOpenTask(it.id) }) }
        }
        if (done.isNotEmpty()) {
            item { SectionLabel("ĐÃ XONG", colors) }
            items(done, key = { it.id }) { TaskRow(it, showDay = true, colors = colors, onToggle = { state.toggle(it.id) }, onClick = { onOpenTask(it.id) }) }
        }
        if (pending.isEmpty() && done.isEmpty()) {
            item {
                Text(
                    "Không tìm thấy công việc nào.",
                    color = colors.textMuted, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                )
            }
        }
    }
}

@Composable
fun SectionLabel(text: String, colors: AppColors) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.padding(top = 6.dp))
}

@Composable
fun FilterPill(label: String, selected: Boolean, colors: AppColors, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) colors.textPrimary else colors.cardBg,
        border = if (selected) null else BorderStroke(1.dp, colors.divider)
    ) {
        Text(
            label,
            fontSize = 14.sp, fontWeight = FontWeight.Bold,
            color = if (selected) (if (colors.isDark) Color.Black else Color.White) else colors.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// ---------------------------------------------------------------
// MÀN: VIỆC MỚI
// ---------------------------------------------------------------

@Composable
fun NewTaskScreen(
    state: TodoState,
    onCancel: () -> Unit,
    onSave: (TodoTask) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppColors(state)
    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(Category.STUDY) }
    var day by rememberSaveable { mutableStateOf(DayOption.TODAY) }
    var hour by rememberSaveable { mutableIntStateOf(19) }
    var minute by rememberSaveable { mutableIntStateOf(0) }
    var reminder by rememberSaveable { mutableStateOf(true) }
    var priority by rememberSaveable { mutableStateOf(Priority.MEDIUM) }

    val context = LocalContext.current
    val canSave = title.isNotBlank()

    fun save() {
        if (!canSave) return
        onSave(
            TodoTask(
                id = 0, title = title.trim(), note = note.trim(), category = category, day = day,
                hour = hour, minute = minute, priority = priority, reminder = reminder
            )
        )
    }

    Column(modifier = modifier.fillMaxSize().background(colors.screenBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hủy", color = colors.primary, fontSize = 16.sp, modifier = Modifier.clickable(onClick = onCancel))
            Text("Việc mới", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = colors.textPrimary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Text(
                "Lưu", fontSize = 16.sp,
                color = if (canSave) colors.primary else colors.textMuted,
                modifier = Modifier.clickable(enabled = canSave) { save() }
            )
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp))) {
                val transparentColors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
                TextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Tên công việc", color = colors.textMuted) },
                    placeholder = { Text("Ví dụ: Ôn chương 4 – Giải thuật", color = colors.textMuted) },
                    colors = transparentColors, modifier = Modifier.fillMaxWidth()
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                TextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Ghi chú", color = colors.textMuted) },
                    placeholder = { Text("Thêm ghi chú (không bắt buộc)", color = colors.textMuted) },
                    colors = transparentColors, modifier = Modifier.fillMaxWidth()
                )
            }

            FieldLabel("Nhóm", colors)
            ChoiceRow(Category.entries, category, colors, { it.label }) { category = it }

            FieldLabel("Ngày", colors)
            ChoiceRow(DayOption.entries, day, colors, { it.label }) { day = it }

            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp))) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Giờ", fontSize = 16.sp, color = colors.textPrimary, modifier = Modifier.weight(1f))
                    Text(
                        formatTime12(hour, minute),
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        modifier = Modifier
                            .background(colors.divider, RoundedCornerShape(10.dp))
                            .clickable {
                                TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, false).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Nhắc tôi", fontSize = 16.sp, color = colors.textPrimary)
                        Text("Trước 15 phút", fontSize = 12.sp, color = colors.textMuted)
                    }
                    AppSwitch(reminder) { reminder = it }
                }
            }

            FieldLabel("Mức ưu tiên", colors)
            Row(
                modifier = Modifier.fillMaxWidth().background(colors.divider, RoundedCornerShape(12.dp)).padding(3.dp)
            ) {
                Priority.entries.forEach { p ->
                    val sel = p == priority
                    Text(
                        p.label,
                        textAlign = TextAlign.Center,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                        color = if (sel) colors.textPrimary else colors.textMuted,
                        modifier = Modifier
                            .weight(1f)
                            .background(if (sel) colors.cardBg else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { priority = p }
                            .padding(vertical = 10.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { save() },
            enabled = canSave,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                disabledContainerColor = colors.divider,
                disabledContentColor = colors.textMuted
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)
        ) {
            Text("Lưu công việc", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun FieldLabel(text: String, colors: AppColors) {
    Text(
        text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textMuted,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
    )
}

@Composable
fun <T> ChoiceRow(options: List<T>, selected: T, colors: AppColors, label: (T) -> String, onSelect: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { opt ->
            val sel = opt == selected
            Text(
                label(opt),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (sel) colors.primary else colors.textPrimary,
                modifier = Modifier
                    .weight(1f)
                    .background(if (sel) (if (colors.isDark) Color(0xFF1E284A) else Color(0xFFE3EAFD)) else colors.cardBg, RoundedCornerShape(12.dp))
                    .border(1.dp, if (sel) colors.primary else colors.divider, RoundedCornerShape(12.dp))
                    .clickable { onSelect(opt) }
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
fun AppSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = SwitchGreen,
            checkedThumbColor = Color.White,
            checkedBorderColor = Color.Transparent,
            uncheckedTrackColor = Color(0xFFD1D1D6),
            uncheckedThumbColor = Color.White,
            uncheckedBorderColor = Color.Transparent
        )
    )
}

// ---------------------------------------------------------------
// TAB 3: THÔNG BÁO
// ---------------------------------------------------------------

@Composable
fun NotificationsTab(state: TodoState, colors: AppColors) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Thông báo", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Text("Đánh dấu đã đọc", color = colors.primary, fontSize = 14.sp, modifier = Modifier.clickable { state.markAllRead() })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.cardBg)
        ) {
            state.notifications.forEachIndexed { index, n ->
                NotificationRow(n, colors)
                if (index < state.notifications.lastIndex) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                }
            }
        }
    }
}

@Composable
fun NotificationRow(n: AppNotification, colors: AppColors) {
    val (icon: ImageVector, bg: Color, fg: Color) = when (n.type) {
        NotiType.DUE_SOON, NotiType.REMINDER -> Triple(Icons.Default.Notifications, Color(0xFFE3EAFD), colors.primary)
        NotiType.OVERDUE -> Triple(Icons.Default.Warning, Color(0xFFFFE0E0), DangerRed)
        NotiType.SUCCESS -> Triple(Icons.Default.Check, Color(0xFFDDF5E8), Color(0xFF1E8E5A))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (n.unread) (if (colors.isDark) Color(0xFF252A38) else Color(0xFFF3F6FF)) else colors.cardBg)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(bg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp)) }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(n.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary, modifier = Modifier.weight(1f))
                Text(n.time, fontSize = 12.sp, color = colors.textMuted)
            }
            Text(n.message, fontSize = 13.sp, color = colors.textMuted)
        }
        if (n.unread) {
            Box(
                Modifier.padding(start = 10.dp, top = 6.dp).size(8.dp).clip(CircleShape).background(colors.primary)
            )
        }
    }
}

// ---------------------------------------------------------------
// TAB 4: CÀI ĐẶT
// ---------------------------------------------------------------

@Composable
fun SettingsTab(state: TodoState, colors: AppColors, onOpenProfile: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Cài đặt", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Avatar(40, avatarUri = state.avatarUriString, name = state.userName, onClick = onOpenProfile)
        }

        SettingsHeader("GIAO DIỆN", colors)
        Column(modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp)).padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeOption("Sáng", state.themeMode == ThemeMode.LIGHT, colors, Modifier.weight(1f), { state.themeMode = ThemeMode.LIGHT }) {
                    MiniScreen(Color(0xFFF4F4F7), Color(0xFFD5D5DA), Color.White)
                }
                ThemeOption("Tối", state.themeMode == ThemeMode.DARK, colors, Modifier.weight(1f), { state.themeMode = ThemeMode.DARK }) {
                    MiniScreen(Color(0xFF121212), Color(0xFF555558), Color(0xFF2C2C2E))
                }
                ThemeOption("Tự động", state.themeMode == ThemeMode.AUTO, colors, Modifier.weight(1f), { state.themeMode = ThemeMode.AUTO }) {
                    Row(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f).fillMaxSize()) { MiniScreen(Color(0xFFF4F4F7), Color(0xFFD5D5DA), Color.White) }
                        Box(Modifier.weight(1f).fillMaxSize()) { MiniScreen(Color(0xFF121212), Color(0xFF555558), Color(0xFF2C2C2E)) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Màu chủ đạo", fontSize = 16.sp, color = colors.textPrimary, modifier = Modifier.weight(1f))
                val accents = listOf(Color(0xFF2F5BEA), Color(0xFF0B8A5F), Color(0xFFC8440F), Color(0xFF7C3AED))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    accents.forEachIndexed { i, c ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .then(if (state.accentIndex == i) Modifier.border(2.dp, c, CircleShape).padding(3.dp) else Modifier)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { state.accentIndex = i }
                        )
                    }
                }
            }
        }

        SettingsHeader("THÔNG BÁO", colors)
        Column(modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp))) {
            ToggleRow("Nhắc việc", "Báo trước hạn chót 15 phút", state.remindEnabled, colors) { state.remindEnabled = it }
            RowDivider(colors)
            ToggleRow("Âm thanh", "Phát âm khi có thông báo", state.soundEnabled, colors) { state.soundEnabled = it }
            RowDivider(colors)
            ToggleRow("Tổng kết cuối ngày", "Gửi lúc 21:00 mỗi tối", state.dailySummary, colors) { state.dailySummary = it }
        }

        SettingsHeader("CHUNG", colors)
        Column(modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp))) {
            ValueRow("Ngôn ngữ", "Tiếng Việt", colors)
            RowDivider(colors)
            ValueRow("Giới thiệu UTH Todo", "Phiên bản 1.0", colors)
        }
    }
}

@Composable
fun SettingsHeader(text: String, colors: AppColors) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
}

@Composable
fun RowDivider(colors: AppColors) {
    Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp).background(colors.divider))
}

@Composable
fun ToggleRow(title: String, subtitle: String, checked: Boolean, colors: AppColors, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = colors.textPrimary)
            Text(subtitle, fontSize = 12.sp, color = colors.textMuted)
        }
        AppSwitch(checked, onChange)
    }
}

@Composable
fun ValueRow(title: String, value: String, colors: AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 15.sp, color = colors.textMuted)
    }
}

@Composable
fun ThemeOption(
    label: String,
    selected: Boolean,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    preview: @Composable () -> Unit
) {
    Column(modifier = modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.divider, RoundedCornerShape(12.dp))
        ) { preview() }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 13.sp, color = if (selected) colors.primary else colors.textMuted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun MiniScreen(bg: Color, bar: Color, card: Color) {
    Column(Modifier.fillMaxSize().background(bg).padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.width(24.dp).height(5.dp).background(bar, RoundedCornerShape(3.dp)))
        Box(Modifier.fillMaxWidth().height(12.dp).background(card, RoundedCornerShape(4.dp)))
        Box(Modifier.fillMaxWidth().height(12.dp).background(card, RoundedCornerShape(4.dp)))
    }
}

// ---------------------------------------------------------------
// MÀN: HỒ SƠ
// ---------------------------------------------------------------

@Composable
fun ProfileScreen(
    state: TodoState,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppColors(state)
    val context = LocalContext.current

    var showEditNameDialog by rememberSaveable { mutableStateOf(false) }
    var tempName by rememberSaveable { mutableStateOf(state.userName) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            state.updateUserProfile(name = state.userName, avatarUri = uri.toString())
            Toast.makeText(context, "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show()
        }
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Đổi tên hiển thị", color = colors.textPrimary) },
            text = {
                Column {
                    TextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Tên mới", color = colors.textMuted) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            state.updateUserProfile(name = tempName.trim(), avatarUri = state.avatarUriString)
                            Toast.makeText(context, "Đã cập nhật tên thành công", Toast.LENGTH_SHORT).show()
                        }
                        showEditNameDialog = false
                    }
                ) {
                    Text("Lưu", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Hủy", color = colors.textMuted)
                }
            },
            containerColor = colors.cardBg
        )
    }

    val total = state.tasks.size
    val doneCount = state.tasks.count { it.done }
    val pending = total - doneCount
    val percent = if (total == 0) 0 else doneCount * 100 / total

    val weekValues = listOf(40, 26, 0, 0, 0, 0, 0)
    val labels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val todayIndex = 1

    Column(modifier = modifier.fillMaxSize().background(colors.screenBg).padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart).clickable(onClick = onBack),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                Text(" Trang chủ", color = colors.primary, fontSize = 15.sp)
            }
            Text("Hồ sơ", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = colors.textPrimary)
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val displayEmail = currentUser?.email ?: (if (currentUser != null) "ID: ${currentUser.uid.take(12)}..." else "taidv5488@ut.edu.vn")

        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Avatar(
                    size = 90,
                    avatarUri = state.avatarUriString,
                    name = state.userName,
                    onClick = { launcher.launch("image/*") }
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Đổi ảnh",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    tempName = state.userName
                    showEditNameDialog = true
                }
            ) {
                Text(state.userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Sửa tên",
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(displayEmail, fontSize = 13.sp, color = colors.textMuted)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (currentUser != null) "✓ Đã kết nối Firebase (${if (currentUser.isAnonymous) "Khách" else "Tài khoản"})" else "✓ Chưa đăng nhập",
                fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E8E5A),
                modifier = Modifier.background(Color(0xFFDDF5E8), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("$doneCount", "Việc đã xong", colors, Modifier.weight(1f))
            StatCard("$pending", "Đang chờ", colors, Modifier.weight(1f))
            StatCard("$percent%", "Hoàn thành", colors, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))
        Column(modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row {
                Text("Tuần này", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary, modifier = Modifier.weight(1f))
                Text("Việc xong mỗi ngày", fontSize = 13.sp, color = colors.textMuted)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                labels.forEachIndexed { i, label ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val h = if (weekValues[i] == 0) 6 else weekValues[i]
                        val color = when {
                            weekValues[i] == 0 -> colors.divider
                            i == todayIndex -> colors.primary
                            else -> Color(0xFFB0BFF7)
                        }
                        Box(Modifier.width(24.dp).height(h.dp).background(color, RoundedCornerShape(5.dp)))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            label, fontSize = 11.sp,
                            fontWeight = if (i == todayIndex) FontWeight.Bold else FontWeight.Normal,
                            color = if (i == todayIndex) colors.textPrimary else colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.cardBg, RoundedCornerShape(16.dp))
                .clickable(onClick = onLogout)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = DangerRed, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun StatCard(value: String, label: String, colors: AppColors, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(colors.cardBg, RoundedCornerShape(14.dp)).padding(12.dp)) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        Text(label, fontSize = 12.sp, color = colors.textMuted)
    }
}
// ---------------------------------------------------------------
// MÀN: CHI TIẾT CÔNG VIỆC
// ---------------------------------------------------------------

@Composable
fun TaskDetailScreen(
    state: TodoState,
    taskId: Int,
    backLabel: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppColors(state)
    val task = state.tasks.find { it.id == taskId }

    if (task == null) {
        Column(
            modifier = modifier.fillMaxSize().background(colors.screenBg).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Công việc không tồn tại hoặc đã bị xóa.", color = colors.textMuted)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) {
                Text("Quay lại", color = Color.White)
            }
        }
        return
    }

    var newSubtaskTitle by rememberSaveable { mutableStateOf("") }
    var noteText by rememberSaveable(task.id) { mutableStateOf(task.note) }

    Column(modifier = modifier.fillMaxSize().background(colors.screenBg)) {
        // Thanh tiêu đề
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onBack),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(backLabel, color = colors.primary, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.weight(1f))
            Text("Chi tiết công việc", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = colors.textPrimary)
            Spacer(modifier = Modifier.weight(1f))

            Text(
                "Xóa",
                color = DangerRed,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    state.deleteTask(taskId)
                    onBack()
                }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Thông báo khi công việc đã hoàn thành
            if (task.done) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Công việc đã hoàn thành. Bỏ tích để mở lại và chỉnh sửa.",
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Thẻ tiêu đề công việc
            Column(
                modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp)).padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CheckCircle(task.done, colors.primary) { state.toggle(task.id) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        task.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.done) Color.Gray else colors.textPrimary,
                        textDecoration = if (task.done) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryChip(task.category)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${task.day.label}, ${task.timeText}", fontSize = 13.sp, color = colors.textMuted)
                    Spacer(modifier = Modifier.weight(1f))
                    if (task.priority == Priority.HIGH) {
                        Text(
                            "Gấp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DangerRed,
                            modifier = Modifier.background(Color(0xFFFFE0E0), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            FieldLabel("Ghi chú", colors)
            Column(modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp))) {
                TextField(
                    value = noteText,
                    onValueChange = {
                        if (!task.done) {
                            noteText = it
                            state.updateTask(task.copy(note = it))
                        }
                    },
                    enabled = !task.done,
                    placeholder = { Text("Thêm ghi chú...", color = colors.textMuted) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        disabledTextColor = colors.textMuted,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FieldLabel("Việc nhỏ (Sub-tasks)", colors)
            Column(
                modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp)).padding(16.dp)
            ) {
                if (task.subtasks.isEmpty()) {
                    Text("Chưa có công việc nhỏ nào.", fontSize = 13.sp, color = colors.textMuted)
                } else {
                    val subDone = task.subtasks.count { it.done }
                    Text(
                        "Tiến độ: $subDone/${task.subtasks.size} hoàn thành",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    task.subtasks.forEach { sub ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CheckCircle(sub.done, colors.primary) {
                                if (!task.done) {
                                    val updated = task.subtasks.map {
                                        if (it.id == sub.id) it.copy(done = !it.done) else it
                                    }
                                    val allDone = updated.isNotEmpty() && updated.all { it.done }
                                    state.updateTask(task.copy(subtasks = updated, done = if (allDone) true else task.done))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                sub.title,
                                fontSize = 15.sp,
                                color = if (sub.done) Color.Gray else colors.textPrimary,
                                textDecoration = if (sub.done) TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (!task.done) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = newSubtaskTitle,
                            onValueChange = { newSubtaskTitle = it },
                            placeholder = { Text("Thêm việc nhỏ...", color = colors.textMuted) },
                            singleLine = true,
                            enabled = !task.done,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                disabledTextColor = colors.textMuted,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newSubtaskTitle.isNotBlank() && !task.done) {
                                    val nextSubId = (task.subtasks.maxOfOrNull { it.id } ?: 0) + 1
                                    val updated = task.subtasks + SubTask(nextSubId, newSubtaskTitle.trim(), false)
                                    state.updateTask(task.copy(subtasks = updated))
                                    newSubtaskTitle = ""
                                }
                            },
                            enabled = !task.done && newSubtaskTitle.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text("Thêm", color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------
// PREVIEWS
// ---------------------------------------------------------------

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    BT4OOPTheme {
        SplashScreen(onNavigateToMain = {}, modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    BT4OOPTheme {
        OnboardingScreen(page = onboardingPages[0], onNext = {})
    }
}
