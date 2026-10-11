package com.findit.dashboard.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Shows the item's photo, or the gallery icon when there is no photo yet. */
@Composable
fun PhotoImage(
    photoPath: String?,
    modifier: Modifier = Modifier,
    placeholderBackground: Color = Color(0xFFE8F1F8),
    placeholderIconSize: Dp = 40.dp
) {
    val photo by produceState<ImageBitmap?>(null, photoPath) {
        // Decoding a photo is slow: do it on a background thread.
        value = if (photoPath.isNullOrEmpty()) null
        else withContext(Dispatchers.IO) { loadPhoto(photoPath) }
    }

    val bitmap = photo
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Item photo",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier.background(placeholderBackground), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(android.R.drawable.ic_menu_gallery),
                contentDescription = "Item photo",
                modifier = Modifier.size(placeholderIconSize)
            )
        }
    }
}

private fun loadPhoto(path: String): ImageBitmap? {
    return try {
        if (!File(path).exists()) return null
        // Shrink the 12MP camera photo so it does not use too much memory.
        val options = BitmapFactory.Options().apply { inSampleSize = 4 }
        val bitmap = BitmapFactory.decodeFile(path, options) ?: return null

        // Camera photos store their rotation in EXIF, so turn the picture upright.
        val degrees = when (
            ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) {
            bitmap.asImageBitmap()
        } else {
            val matrix = Matrix().apply { postRotate(degrees) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                .asImageBitmap()
        }
    } catch (e: Exception) {
        null
    }
}
