package com.example.bt4_oop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Màn "Chi tiết công việc" theo thiết kế mới (Tuần 04).
// Dùng lại TodoState, AppColors, CategoryChip, CheckCircle, FieldLabel... đã có trong MainActivity.kt.

@Composable
private fun plainFieldColors(colors: AppColors) = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedTextColor = colors.textPrimary,
    unfocusedTextColor = colors.textPrimary,
    disabledTextColor = colors.textPrimary,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent
)

@Composable
private fun StatusChip(text: String, bg: Color, fg: Color) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, colors: AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = colors.textMuted, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, color = colors.textMuted, modifier = Modifier.weight(1f))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
    }
}

@Composable
fun TaskDetailScreenV2(
    state: TodoState,
    taskId: Int,
    backLabel: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppColors(state)
    val task = state.tasks.find { it.id == taskId }

    // State đặt trước khi return sớm để thứ tự remember luôn ổn định
    var editing by remember { mutableStateOf(false) }
    var titleDraft by remember(task?.id) { mutableStateOf(task?.title ?: "") }
    var noteDraft by remember(task?.id) { mutableStateOf(task?.note ?: "") }
    var newSubtask by remember { mutableStateOf("") }
    var editingSub by remember { mutableStateOf<SubTask?>(null) }
    var subDraft by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var apiStatus by remember { mutableStateOf("GET /tasks/$taskId …") }

    // "Mở khi bấm một việc" -> GET /mock/teach/tasks/{id}
    LaunchedEffect(taskId) {
        TaskApi.getTask(taskId)
            .onSuccess { apiStatus = "GET /tasks/$taskId → 200 OK (${it.title})" }
            .onFailure { apiStatus = "GET /tasks/$taskId → lỗi: ${it.message}" }
    }

    if (task == null) {
        Column(
            modifier = modifier.fillMaxSize().background(colors.screenBg).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text("Công việc không tồn tại hoặc đã bị xóa.", color = colors.textMuted)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) {
                Text("Quay lại", color = Color.White)
            }
        }
        return
    }

    // PATCH / DELETE được gọi bên trong TodoState (updateTask, toggle, deleteTask)
    fun saveTask(t: TodoTask) = state.updateTask(t)
    fun toggleTask(t: TodoTask) = state.toggle(t.id)
    fun removeTask(id: Int) = state.deleteTask(id)

    // ---- Hộp thoại xác nhận xóa công việc ----
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa công việc?", color = colors.textPrimary) },
            text = { Text("Công việc \"${task.title}\" sẽ bị xóa vĩnh viễn.", color = colors.textMuted) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    removeTask(task.id)
                    onBack()
                }) { Text("Xóa", color = DangerRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy", color = colors.textMuted) }
            },
            containerColor = colors.cardBg
        )
    }

    // ---- Hộp thoại sửa việc nhỏ (icon bút chì) ----
    editingSub?.let { sub ->
        AlertDialog(
            onDismissRequest = { editingSub = null },
            title = { Text("Sửa việc nhỏ", color = colors.textPrimary) },
            text = {
                TextField(
                    value = subDraft,
                    onValueChange = { subDraft = it },
                    singleLine = true,
                    colors = plainFieldColors(colors),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (subDraft.isNotBlank()) {
                        val updated = task.subtasks.map {
                            if (it.id == sub.id) it.copy(title = subDraft.trim()) else it
                        }
                        saveTask(task.copy(subtasks = updated))
                    }
                    editingSub = null
                }) { Text("Lưu", color = colors.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { editingSub = null }) { Text("Hủy", color = colors.textMuted) }
            },
            containerColor = colors.cardBg
        )
    }

    val subDone = task.subtasks.count { it.done }
    val subTotal = task.subtasks.size
    val fraction = if (subTotal == 0) 0f else subDone.toFloat() / subTotal

    Column(modifier = modifier.fillMaxSize().background(colors.screenBg)) {
        // ---------- Thanh trên: < Danh sách ............ Sửa ----------
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
            Text(
                text = if (editing) "Lưu" else "Sửa",
                color = colors.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    if (editing) {
                        if (titleDraft.isNotBlank()) {
                            saveTask(task.copy(title = titleDraft.trim(), note = noteDraft.trim()))
                        }
                        editing = false
                    } else {
                        titleDraft = task.title
                        noteDraft = task.note
                        editing = true
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(apiStatus, fontSize = 11.sp, color = colors.textMuted)
            Spacer(modifier = Modifier.height(8.dp))

            // ---------- Chips: nhóm · ưu tiên · đã xong ----------
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                CategoryChip(task.category)
                StatusChip(
                    "Ưu tiên ${task.priority.label.lowercase()}",
                    if (task.priority == Priority.HIGH) Color(0xFFFFE0E0) else colors.divider,
                    if (task.priority == Priority.HIGH) DangerRed else colors.textMuted
                )
                if (task.done) StatusChip("Đã xong", Color(0xFFDDF5E8), Color(0xFF1E8E5A))
            }

            // ---------- Tiêu đề lớn ----------
            Spacer(modifier = Modifier.height(10.dp))
            if (editing) {
                Column(modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp))) {
                    TextField(
                        value = titleDraft,
                        onValueChange = { titleDraft = it },
                        label = { Text("Tên công việc", color = colors.textMuted) },
                        colors = plainFieldColors(colors),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Text(
                    text = task.title,
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.done) Color.Gray else colors.textPrimary,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null
                )
            }

            // ---------- Hạn chót / Nhắc nhở ----------
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp))) {
                InfoRow(Icons.Default.DateRange, "Hạn chót", "${task.day.label}, ${task.timeText}", colors)
                Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp).background(colors.divider))
                InfoRow(Icons.Default.Notifications, "Nhắc nhở", if (task.reminder) "Trước 15 phút" else "Tắt", colors)
            }

            // ---------- Ghi chú ----------
            FieldLabel("Ghi chú", colors)
            Column(modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp))) {
                if (editing) {
                    TextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        placeholder = { Text("Thêm ghi chú...", color = colors.textMuted) },
                        colors = plainFieldColors(colors),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = task.note.ifBlank { "Chưa có ghi chú." },
                        fontSize = 15.sp,
                        color = if (task.note.isBlank()) colors.textMuted else colors.textPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // ---------- Việc nhỏ + thanh tiến độ ----------
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Việc nhỏ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.weight(1f))
                Text("$subDone/$subTotal", fontSize = 13.sp, color = colors.textMuted)
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(5.dp).background(colors.divider, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(5.dp)
                        .background(colors.primary, RoundedCornerShape(3.dp))
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth().background(colors.cardBg, RoundedCornerShape(16.dp))) {
                task.subtasks.forEach { sub ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CheckCircle(sub.done, colors.primary) {
                            val updated = task.subtasks.map { if (it.id == sub.id) it.copy(done = !it.done) else it }
                            val allDone = updated.isNotEmpty() && updated.all { it.done }
                            saveTask(task.copy(subtasks = updated, done = allDone))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            sub.title,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            color = if (sub.done) Color.Gray else colors.textPrimary,
                            textDecoration = if (sub.done) TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.Edit, "Sửa việc nhỏ", tint = colors.textMuted,
                            modifier = Modifier.size(20.dp).clickable {
                                subDraft = sub.title
                                editingSub = sub
                            }
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Icon(
                            Icons.Default.Delete, "Xóa việc nhỏ", tint = DangerRed,
                            modifier = Modifier.size(20.dp).clickable {
                                val updated = task.subtasks.filter { it.id != sub.id }
                                val allDone = updated.isNotEmpty() && updated.all { it.done }
                                saveTask(task.copy(subtasks = updated, done = if (updated.isEmpty()) task.done else allDone))
                            }
                        )
                    }
                    Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp).background(colors.divider))
                }

                // Hàng "+ Thêm việc nhỏ ........ Thêm"
                val canAdd = newSubtask.isNotBlank()
                fun addSub() {
                    if (!canAdd) return
                    val nextId = (task.subtasks.maxOfOrNull { it.id } ?: 0) + 1
                    // Thêm việc nhỏ chưa xong => công việc không còn "xong" nữa
                    saveTask(
                        task.copy(
                            subtasks = task.subtasks + SubTask(nextId, newSubtask.trim(), false),
                            done = false
                        )
                    )
                    newSubtask = ""
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    TextField(
                        value = newSubtask,
                        onValueChange = { newSubtask = it },
                        placeholder = { Text("Thêm việc nhỏ", color = colors.textMuted) },
                        singleLine = true,
                        colors = plainFieldColors(colors),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Thêm",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        color = if (canAdd) colors.primary else colors.textMuted,
                        modifier = Modifier.clickable(enabled = canAdd) { addSub() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ---------- Nút dưới cùng ----------
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (task.done) {
                Button(
                    onClick = { toggleTask(task) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.divider),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardBg, contentColor = colors.textPrimary),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text("Đánh dấu chưa xong", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
            } else {
                Button(
                    onClick = { toggleTask(task) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text("Đánh dấu hoàn thành", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Xóa công việc",
                color = DangerRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { showDeleteDialog = true }.padding(4.dp)
            )
        }
    }
}