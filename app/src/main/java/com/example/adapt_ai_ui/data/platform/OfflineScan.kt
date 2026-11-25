package com.example.adapt_ai_ui.data.platform

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun readScanImage(resolver: ContentResolver, uri: Uri): Bitmap =
    withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                val scale = (1600f / maxOf(info.size.width, info.size.height)).coerceAtMost(1f)
                decoder.setTargetSize(maxOf(1, (info.size.width * scale).toInt()), maxOf(1, (info.size.height * scale).toInt()))
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            require(options.outWidth > 0 && options.outHeight > 0)
            var sample = 1
            while (maxOf(options.outWidth, options.outHeight) / sample > 1600) sample *= 2
            options.inJustDecodeBounds = false
            options.inSampleSize = sample
            requireNotNull(resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) })
        }
    }

internal suspend fun recognizeScan(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    try {
        recognizer.process(InputImage.fromBitmap(bitmap, 0)).addOnCompleteListener { result ->
            recognizer.close()
            when {
                result.isSuccessful -> continuation.resume(result.result.text.take(12_000))
                else -> continuation.resumeWithException(result.exception ?: IllegalStateException("Recognition stopped"))
            }
        }
    } catch (failure: Exception) {
        recognizer.close()
        continuation.resumeWithException(failure)
    }
}

internal suspend fun encodeScanImage(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
    val data = ByteArrayOutputStream()
    for (quality in listOf(86, 70, 55, 40, 25)) {
        data.reset()
        check(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, data))
        if (data.size() <= 490_000) return@withContext Base64.encodeToString(data.toByteArray(), Base64.NO_WRAP)
    }
    error("Scan is too large to save")
}

internal suspend fun decodeScanImage(encoded: String): Bitmap = withContext(Dispatchers.Default) {
    val bytes = Base64.decode(encoded, Base64.NO_WRAP)
    requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
}
