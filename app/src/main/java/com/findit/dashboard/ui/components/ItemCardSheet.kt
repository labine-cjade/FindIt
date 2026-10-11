package com.findit.dashboard.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.findit.dashboard.R
import com.findit.dashboard.locationText
import com.findit.dashboard.ui.theme.ConfirmBlue
import com.findit.dashboard.ui.theme.FieldBorder
import com.findit.dashboard.ui.theme.LabelGray
import com.findit.dashboard.ui.theme.Navy
import com.findit.dashboard.ui.theme.OutlineGray
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/** The same bottom sheet is used for Add, Details and Update. */
enum class CardMode { ADD, DETAILS, UPDATE }

/** Date text saved with an item, for example "Feb 23, 2026 11:43 AM". */
fun currentTimestamp(): String =
    SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date())

private val RecordRed = Color(0xFFD32F2F)

/**
 * Bottom sheet for one item.
 *
 * - ADD: form after taking a photo (button "Save").
 * - DETAILS: read-only view with a playable recorded message and an "Update" button.
 * - UPDATE: the same form, filled with the item (button "Update Item", then a confirm dialog).
 *
 * [initialLocation] is the plain location text (without "Location: ").
 * [onSave] is called with the new name, location and voice message file (or null).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCardSheet(
    initialMode: CardMode,
    photoPath: String?,
    initialName: String,
    initialLocation: String,
    initialAudioPath: String?,
    dateText: String,
    onDismiss: () -> Unit,
    onSave: (name: String, location: String, audioPath: String?) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var mode by remember { mutableStateOf(initialMode) }
    var name by remember { mutableStateOf(initialName) }
    var location by remember { mutableStateOf(initialLocation) }
    var showConfirm by remember { mutableStateOf(false) }

    // Voice message state
    val recorder = remember { VoiceMessageRecorder(context) }
    var audioPath by remember { mutableStateOf(initialAudioPath) }
    var newAudioPath by remember { mutableStateOf<String?>(null) } // recorded in this sheet, not saved yet
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    // When the sheet closes: stop the mic/player and throw away a recording that was never saved.
    DisposableEffect(Unit) {
        onDispose {
            recorder.release()
            if (!saved) newAudioPath?.let { File(it).delete() }
        }
    }

    fun close() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    fun startRecording() {
        recorder.stopPlaying()
        isPlaying = false
        // A new recording replaces one that was recorded earlier in this same sheet.
        newAudioPath?.let { File(it).delete() }
        newAudioPath = null
        val file = File(context.filesDir, "message_${System.currentTimeMillis()}.m4a")
        if (recorder.startRecording(file)) {
            newAudioPath = file.absolutePath
            isRecording = true
        } else {
            file.delete()
            Toast.makeText(context, "Could not start the microphone", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopRecording() {
        isRecording = false
        if (recorder.stopRecording()) {
            audioPath = newAudioPath
        } else {
            newAudioPath?.let { File(it).delete() }
            newAudioPath = null
            Toast.makeText(context, "Recording was too short", Toast.LENGTH_SHORT).show()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startRecording()
        else Toast.makeText(context, "Microphone permission is needed to record", Toast.LENGTH_SHORT).show()
    }

    fun onRecordClick() {
        if (isRecording) {
            stopRecording()
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startRecording()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun onPlayClick() {
        val path = audioPath ?: return
        if (isPlaying) {
            recorder.stopPlaying()
            isPlaying = false
        } else if (recorder.startPlaying(path) { isPlaying = false }) {
            isPlaying = true
        } else {
            Toast.makeText(context, "Cannot play this message", Toast.LENGTH_SHORT).show()
        }
    }

    // Speech-to-text for the location box
    val startLocationVoice = rememberVoiceInput("Say the location...") { spoken ->
        location = spoken
    }

    fun doSave() {
        saved = true
        // If the message was replaced, the old file is not needed anymore.
        if (audioPath != initialAudioPath) initialAudioPath?.let { File(it).delete() }
        onSave(name.trim(), location.trim(), audioPath)
    }

    fun onSaveClick() {
        if (isRecording) stopRecording()
        if (name.isBlank() || location.isBlank()) {
            Toast.makeText(context, "Please fill in name and location", Toast.LENGTH_SHORT).show()
        } else if (mode == CardMode.UPDATE) {
            showConfirm = true
        } else {
            doSave()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
        ) {
            // Header: X + title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { close() }
                )
                Text(
                    text = when (mode) {
                        CardMode.ADD -> "Add Item"
                        CardMode.DETAILS -> "Details"
                        CardMode.UPDATE -> "Update Item"
                    },
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            // Photo (with the "Update Photo" badge while updating)
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .height(if (mode == CardMode.DETAILS) 180.dp else 170.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                PhotoImage(
                    photoPath = photoPath,
                    modifier = Modifier.fillMaxSize(),
                    placeholderIconSize = 64.dp
                )
                if (mode == CardMode.UPDATE) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xB0102A43))
                            .clickable {
                                Toast.makeText(context, "Photo update coming soon", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Update Photo", color = Color.White, fontSize = 12.sp)
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_edit),
                            contentDescription = "Edit photo",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(14.dp)
                        )
                    }
                }
            }

            if (mode == CardMode.DETAILS) {
                // ---------- DETAILS ----------
                Text(
                    text = name,
                    color = Color.Black,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Last Seen",
                    color = LabelGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = locationText(location),
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Recorded Message",
                    color = LabelGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 14.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .height(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (audioPath != null) {
                            WaveformBars(seed = audioPath.hashCode(), barCount = 32)
                        } else {
                            Text(text = "No recorded message", color = LabelGray, fontSize = 13.sp)
                        }
                    }
                    CircleIconButton(
                        iconRes = if (isPlaying) android.R.drawable.ic_media_pause
                        else android.R.drawable.ic_media_play,
                        description = "Play recorded message",
                        onClick = { onPlayClick() },
                        size = 36.dp,
                        background = if (audioPath != null) Navy else Navy.copy(alpha = 0.3f),
                        enabled = audioPath != null
                    )
                }
                Text(
                    text = dateText,
                    color = LabelGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                SheetButton(text = "Update") {
                    // See Details -> Update: switch the same sheet to the editable form
                    recorder.stopPlaying()
                    isPlaying = false
                    mode = CardMode.UPDATE
                }
            } else {
                // ---------- ADD / UPDATE form ----------
                Text(
                    text = "Item Name",
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                FindItTextField(
                    value = name,
                    onValueChange = { name = it },
                    hint = "My Keys",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                )

                Text(
                    text = "Location",
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 14.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FindItTextField(
                        value = location,
                        onValueChange = { location = it },
                        hint = "Above the mini table",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.weight(1f)
                    )
                    CircleIconButton(
                        iconRes = R.drawable.ic_mic,
                        description = "Set location by voice",
                        onClick = { startLocationVoice() },
                        size = 46.dp,
                        iconPadding = 13.dp,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                Row(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "Record Message",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(Optional)",
                        color = LabelGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .height(46.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(1.dp, FieldBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        when {
                            isRecording -> Text(
                                text = "Recording... tap the red button to stop",
                                color = RecordRed,
                                fontSize = 12.sp
                            )
                            audioPath != null -> WaveformBars(seed = audioPath.hashCode(), barCount = 22)
                            else -> Text(
                                text = "Start recording yourself!",
                                color = Color(0xFF999999),
                                fontSize = 13.sp
                            )
                        }
                    }
                    // Play the recorded message
                    CircleIconButton(
                        iconRes = if (isPlaying) android.R.drawable.ic_media_pause
                        else android.R.drawable.ic_media_play,
                        description = "Play recorded message",
                        onClick = { onPlayClick() },
                        size = 32.dp,
                        iconPadding = 9.dp,
                        background = if (audioPath != null && !isRecording) Navy else Navy.copy(alpha = 0.3f),
                        enabled = audioPath != null && !isRecording
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Record button: mic when idle, red stop square while recording
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(RecordRed)
                                .clickable { onRecordClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color.White, RoundedCornerShape(2.dp))
                            )
                        }
                    } else {
                        CircleIconButton(
                            iconRes = R.drawable.ic_mic,
                            description = "Record message",
                            onClick = { onRecordClick() },
                            size = 32.dp,
                            iconPadding = 7.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
                SheetButton(text = if (mode == CardMode.ADD) "Save" else "Update Item") {
                    onSaveClick()
                }
            }
        }
    }

    if (showConfirm) {
        ConfirmChangesDialog(
            onNo = { showConfirm = false },
            onYes = {
                showConfirm = false
                doSave()
            }
        )
    }
}

/** The dark pill button at the bottom of the sheet. */
@Composable
private fun SheetButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

/** Small bars that look like a sound wave. The same file always gets the same bars. */
@Composable
private fun WaveformBars(seed: Int, barCount: Int) {
    val heights = remember(seed, barCount) {
        val random = Random(seed)
        List(barCount) { random.nextInt(6, 28) }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(h.dp)
                    .background(Navy)
            )
        }
    }
}

/** "Do you want to save changes?" shown before an update is saved. */
@Composable
private fun ConfirmChangesDialog(onNo: () -> Unit, onYes: () -> Unit) {
    Dialog(onDismissRequest = onNo) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ConfirmBlue,
            shadowElevation = 8.dp,
            modifier = Modifier.width(280.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Navy),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "?", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Do you want to save changes?",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 14.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, OutlineGray, RoundedCornerShape(16.dp))
                            .clickable(onClick = onNo),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No", color = Color.Black, fontSize = 13.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Navy)
                            .clickable(onClick = onYes),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Yes", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
