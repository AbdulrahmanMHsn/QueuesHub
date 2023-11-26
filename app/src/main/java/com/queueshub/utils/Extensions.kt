package com.queueshub.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, executor)
    }
}

val Context.executor: Executor
    get() = ContextCompat.getMainExecutor(this)


fun Context.setLocale(locale: Locale): Context {
    Locale.setDefault(locale)
    val config = resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}

fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    bitmap.scaleBitmapDown(300)
    close()
    return bitmap
}

fun Bitmap.scaleBitmapDown(maxDimension: Int): Bitmap {
    val originalWidth = width
    val originalHeight = height
    var resizedWidth = maxDimension
    var resizedHeight = maxDimension
    if (originalHeight > originalWidth) {
        resizedHeight = maxDimension
        resizedWidth = (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
    } else if (originalWidth > originalHeight) {
        resizedWidth = maxDimension
        resizedHeight = (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
    } else {
        resizedHeight = maxDimension
        resizedWidth = maxDimension
    }
    return Bitmap.createScaledBitmap(this, resizedWidth, resizedHeight, false)
}

fun Bitmap.toFile(context: Context): File {

    val file = File(
        context.cacheDir, String.format("%s.png", UUID.randomUUID().toString().replace("-", ""))
    )
    file.createNewFile()
    val bos = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 60, bos)
    val bitmapData = bos.toByteArray()

    val fos = FileOutputStream(file)
    fos.write(bitmapData)
    fos.flush()
    fos.close()
    return file
}

fun File.toBitmap(): Bitmap {

    val filePath: String = path
    return BitmapFactory.decodeFile(filePath)
}

fun <T : Any> NavHostController.putArgs(args: Pair<String, T>) {
    val key = args.first
    val value = args.second
    currentBackStackEntry?.arguments?.apply {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Short -> putShort(key, value)
            is Long -> putLong(key, value)
            is Byte -> putByte(key, value)
            is Boolean -> putBoolean(key, value)
            is ByteArray -> putByteArray(key, value)
            is Char -> putChar(key, value)
            is CharArray -> putCharArray(key, value)
            is CharSequence -> putCharSequence(key, value)
            is Float -> putFloat(key, value)
            is Bundle -> putBundle(key, value)
            // is Serializable -> putSerializable(key, value)
            is Parcelable -> putParcelable(key, value)
            else -> throw IllegalStateException("Type ${value.javaClass.canonicalName} is not supported now")
        }
    }
}

fun String.isValidEgyptianPhoneNumber() =
    (startsWith("010") || startsWith("011") || startsWith("015") || startsWith("012")) && length == 11

fun createPartFromString(stringData: String): RequestBody {
    return stringData.toRequestBody("text/plain".toMediaTypeOrNull())
}
fun String.ArabicToEnglish():String {
    var result = ""
    var en = '0'
    for (ch in this) {
        en = ch
        when (ch) {
            '٠' -> en = '0'
            '١' -> en = '1'
            '٢' -> en = '2'
            '٣' -> en = '3'
            '٤' -> en = '4'
            '٥' -> en = '5'
            '٦' -> en = '6'
            '٧' -> en = '7'
            '٨' -> en = '8'
            '٩' -> en = '9'
        }
        result = "${result}$en"
    }
    return result
}