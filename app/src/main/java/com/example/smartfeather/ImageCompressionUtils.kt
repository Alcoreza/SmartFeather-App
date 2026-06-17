package com.example.smartfeather

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object ImageCompressionUtils {
    fun compressImageForUpload(
        context: Context,
        sourceUri: Uri,
        maxSize: Int = 1280,
        quality: Int = 78
    ): Uri {
        val bitmap = decodeBitmap(context, sourceUri)
        val resized = resizeBitmap(bitmap, maxSize)

        val file = File.createTempFile(
            "smartfeather-upload-",
            ".jpg",
            context.cacheDir
        )

        FileOutputStream(file).use { output ->
            resized.compress(Bitmap.CompressFormat.JPEG, quality, output)
        }

        if (resized !== bitmap) {
            resized.recycle()
        }

        bitmap.recycle()

        return Uri.fromFile(file)
    }

    private fun decodeBitmap(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
        } else {
            context.contentResolver.openInputStream(uri).use { input ->
                BitmapFactory.decodeStream(input)
                    ?: error("Unable to decode selected image.")
            }
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val scale = if (width >= height) {
            maxSize.toFloat() / width.toFloat()
        } else {
            maxSize.toFloat() / height.toFloat()
        }

        val targetWidth = (width * scale).roundToInt()
        val targetHeight = (height * scale).roundToInt()

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}