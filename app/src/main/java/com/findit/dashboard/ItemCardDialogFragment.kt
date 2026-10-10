package com.findit.dashboard

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import com.findit.dashboard.ui.theme.DashboardFindItTheme
import com.findit.dashboard.viewmodel.DashboardViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CardMode { ADD, UPDATE, DETAILS }

class ItemCardDialogFragment : BottomSheetDialogFragment() {

    private val viewModel: DashboardViewModel by activityViewModels()

    companion object {
        private const val ARG_MODE = "arg_mode"
        private const val ARG_PHOTO_PATH = "arg_photo_path"
        private const val ARG_NAME = "arg_name"
        private const val ARG_LOCATION = "arg_location"

        // Used right after a photo is captured in TakePictureActivity
        fun forAdd(photoPath: String): ItemCardDialogFragment {
            return newInstance(CardMode.ADD, photoPath, "", "")
        }

        // Used when editing an existing item (e.g. tapped from a grid card later)
        fun forUpdate(photoPath: String?, name: String, location: String): ItemCardDialogFragment {
            return newInstance(CardMode.UPDATE, photoPath, name, location)
        }

        // Used for "See Details" — same card, read-only
        fun forDetails(photoPath: String?, name: String, location: String): ItemCardDialogFragment {
            return newInstance(CardMode.DETAILS, photoPath, name, location)
        }

        private fun newInstance(
            mode: CardMode,
            photoPath: String?,
            name: String,
            location: String
        ): ItemCardDialogFragment {
            val fragment = ItemCardDialogFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_MODE, mode.name)
                putString(ARG_PHOTO_PATH, photoPath)
                putString(ARG_NAME, name)
                putString(ARG_LOCATION, location)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mode = CardMode.valueOf(arguments?.getString(ARG_MODE) ?: CardMode.ADD.name)
        val photoPath = arguments?.getString(ARG_PHOTO_PATH)
        val initialName = arguments?.getString(ARG_NAME).orEmpty()
        val initialLocation = arguments?.getString(ARG_LOCATION).orEmpty()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                DashboardFindItTheme(dynamicColor = false) {
                    ItemCard(
                        mode = mode,
                        photoPath = photoPath,
                        initialName = initialName,
                        initialLocation = initialLocation,
                        onCloseClick = { dismiss() },
                        onSaveClick = { enteredName, enteredLocation ->
                            handleSave(mode, photoPath, enteredName, enteredLocation)
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val bottomSheet = (dialog as? BottomSheetDialog)
            ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    private fun handleSave(
        mode: CardMode,
        photoPath: String?,
        enteredName: String,
        enteredLocation: String
    ) {
        if (mode == CardMode.DETAILS) {
            dismiss()
            return
        }

        val nameTrimmed = enteredName.trim()
        val locationTrimmed = enteredLocation.trim()

        if (nameTrimmed.isEmpty() || locationTrimmed.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in name and location", Toast.LENGTH_SHORT).show()
            return
        }

        val timestamp = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date())
        val newItem = DashboardItem(
            title = nameTrimmed,
            location = "Location: $locationTrimmed",
            date = timestamp,
            photoPath = photoPath
        )

        viewModel.addItem(newItem)

        Toast.makeText(requireContext(), "$nameTrimmed saved!", Toast.LENGTH_SHORT).show()

        if (activity is TakePictureActivity) {
            activity?.finish()
        }

        dismiss()
    }
}

@Composable
fun ItemCard(
    mode: CardMode,
    photoPath: String?,
    initialName: String,
    initialLocation: String,
    onCloseClick: () -> Unit,
    onSaveClick: (name: String, location: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var nameState by remember(initialName) { mutableStateOf(initialName) }
    var locationState by remember(initialLocation) { mutableStateOf(initialLocation) }
    val focusManager = LocalFocusManager.current

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onCloseClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (mode) {
                        CardMode.ADD -> "Add Item"
                        CardMode.UPDATE -> "Update Item"
                        CardMode.DETAILS -> "Item Details"
                    },
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Photo Preview
            val photoBitmap = remember(photoPath) {
                if (!photoPath.isNullOrEmpty()) {
                    val file = File(photoPath)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    } else null
                } else null
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.LightGray)
            ) {
                if (photoBitmap != null) {
                    Image(
                        bitmap = photoBitmap,
                        contentDescription = "Item photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                        contentDescription = "Item photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Item Name
            Text(
                text = "Item Name",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            BasicTextField(
                value = nameState,
                onValueChange = { nameState = it },
                enabled = mode != CardMode.DETAILS,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(10.dp)),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (nameState.isEmpty()) {
                            Text(
                                text = "My Keys",
                                color = Color(0xFF999999),
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Location
            Text(
                text = "Location",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            BasicTextField(
                value = locationState,
                onValueChange = { locationState = it },
                enabled = mode != CardMode.DETAILS,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(10.dp)),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp, end = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (locationState.isEmpty()) {
                                Text(
                                    text = "Above the mini table",
                                    color = Color(0xFF999999),
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(36.dp)
                                .background(Color(0xFF1B4965), CircleShape)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_btn_speak_now),
                                contentDescription = "Voice location",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Record Message (Optional)
            Text(
                text = "Record Message (Optional)",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(10.dp))
                    .padding(start = 12.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Start recording yourself!",
                    color = Color(0xFF999999),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xFF1B4965), CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_media_play),
                        contentDescription = "Play recording",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xFF1B4965), CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_edit),
                        contentDescription = "Edit recording",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Save / Update / Close Button
            Button(
                onClick = { onSaveClick(nameState, locationState) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B4965),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = when (mode) {
                        CardMode.ADD -> "Save"
                        CardMode.UPDATE -> "Update"
                        CardMode.DETAILS -> "Close"
                    },
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@ComposePreview(showBackground = true)
@Composable
fun ItemCardAddPreview() {
    DashboardFindItTheme(dynamicColor = false) {
        ItemCard(
            mode = CardMode.ADD,
            photoPath = null,
            initialName = "",
            initialLocation = "",
            onCloseClick = {},
            onSaveClick = { _, _ -> }
        )
    }
}

@ComposePreview(showBackground = true)
@Composable
fun ItemCardDetailsPreview() {
    DashboardFindItTheme(dynamicColor = false) {
        ItemCard(
            mode = CardMode.DETAILS,
            photoPath = null,
            initialName = "My Keys",
            initialLocation = "Above the mini table",
            onCloseClick = {},
            onSaveClick = { _, _ -> }
        )
    }
}
