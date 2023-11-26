package com.queueshub.utils

import android.graphics.*
import android.util.Log
import android.util.Rational
import android.util.Size
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import androidx.camera.core.Logger
import com.queueshub.utils.ImageUtil
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Utility class for image related operations.
 */
@RequiresApi(21) // TODO(b/200306659): Remove and replace with annotation on package-info.java
object ImageUtil {
    private const val TAG = "ImageUtil"

    /**
     * Converts a [Size] to an float array of vertexes.
     */
    fun sizeToVertexes(size: Size): FloatArray {
        return floatArrayOf(
            0f, 0f, size.width.toFloat(), 0f, size.width.toFloat(), size.height.toFloat(), 0f,
            size.height.toFloat()
        )
    }

    /**
     * Returns the min value.
     */
    fun min(value1: Float, value2: Float, value3: Float, value4: Float): Float {
        return Math.min(Math.min(value1, value2), Math.min(value3, value4))
    }

    /**
     * Rotates aspect ratio based on rotation degrees.
     */
    fun getRotatedAspectRatio(
        @IntRange(from = 0, to = 359) rotationDegrees: Int,
        aspectRatio: Rational
    ): Rational? {
        return if (rotationDegrees == 90 || rotationDegrees == 270) {
            ImageUtil.inverseRational(aspectRatio)
        } else Rational(aspectRatio.numerator, aspectRatio.denominator)
    }

    /**
     * Converts JPEG [ImageProxy] to JPEG byte array.
     */
    fun jpegImageToJpegByteArray(image: ImageProxy): ByteArray {
        require(image.format == ImageFormat.JPEG) { "Incorrect image format of the input image proxy: " + image.format }
        val planes = image.planes
        val buffer = planes[0].buffer
        val data = ByteArray(buffer.capacity())
        buffer.rewind()
        buffer[data]
        return data
    }

    /**
     * Converts JPEG [ImageProxy] to JPEG byte array. The input JPEG image will be cropped
     * by the specified crop rectangle and compressed by the specified quality value.
     */
    @Throws(ImageUtil.CodecFailedException::class)
    fun jpegImageToJpegByteArray(
        image: ImageProxy,
        cropRect: Rect, @IntRange(from = 1, to = 100) jpegQuality: Int
    ): ByteArray {
        require(image.format == ImageFormat.JPEG) { "Incorrect image format of the input image proxy: " + image.format }
        var data = ImageUtil.jpegImageToJpegByteArray(image)
        data = ImageUtil.cropJpegByteArray(data, cropRect, jpegQuality)
        return data
    }

    /**
     * Converts YUV_420_888 [ImageProxy] to JPEG byte array. The input YUV_420_888 image
     * will be cropped if a non-null crop rectangle is specified. The output JPEG byte array will
     * be compressed by the specified quality value.
     */
    @Throws(ImageUtil.CodecFailedException::class)
    fun yuvImageToJpegByteArray(
        image: ImageProxy,
        cropRect: Rect?, @IntRange(from = 1, to = 100) jpegQuality: Int
    ): ByteArray {
        require(image.format == ImageFormat.YUV_420_888) { "Incorrect image format of the input image proxy: " + image.format }
        return ImageUtil.nv21ToJpeg(
            ImageUtil.yuv_420_888toNv21(image),
            image.width,
            image.height,
            cropRect,
            jpegQuality
        )
    }

    /** [android.media.Image] to NV21 byte array.  */
    fun yuv_420_888toNv21(image: ImageProxy): ByteArray {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        yBuffer.rewind()
        uBuffer.rewind()
        vBuffer.rewind()
        val ySize = yBuffer.remaining()
        var position = 0
        // TODO(b/115743986): Pull these bytes from a pool instead of allocating for every image.
        val nv21 = ByteArray(ySize + image.width * image.height / 2)

        // Add the full y buffer to the array. If rowStride > 1, some padding may be skipped.
        for (row in 0 until image.height) {
            yBuffer[nv21, position, image.width]
            position += image.width
            yBuffer.position(
                Math.min(ySize, yBuffer.position() - image.width + yPlane.rowStride)
            )
        }
        val chromaHeight = image.height / 2
        val chromaWidth = image.width / 2
        val vRowStride = vPlane.rowStride
        val uRowStride = uPlane.rowStride
        val vPixelStride = vPlane.pixelStride
        val uPixelStride = uPlane.pixelStride

        // Interleave the u and v frames, filling up the rest of the buffer. Use two line buffers to
        // perform faster bulk gets from the byte buffers.
        val vLineBuffer = ByteArray(vRowStride)
        val uLineBuffer = ByteArray(uRowStride)
        for (row in 0 until chromaHeight) {
            vBuffer[vLineBuffer, 0, Math.min(vRowStride, vBuffer.remaining())]
            uBuffer[uLineBuffer, 0, Math.min(uRowStride, uBuffer.remaining())]
            var vLineBufferPosition = 0
            var uLineBufferPosition = 0
            for (col in 0 until chromaWidth) {
                nv21[position++] = vLineBuffer[vLineBufferPosition]
                nv21[position++] = uLineBuffer[uLineBufferPosition]
                vLineBufferPosition += vPixelStride
                uLineBufferPosition += uPixelStride
            }
        }
        return nv21
    }

    /** Crops JPEG byte array with given [android.graphics.Rect].  */
    @Throws(ImageUtil.CodecFailedException::class)
    private fun cropJpegByteArray(
        data: ByteArray, cropRect: Rect,
        @IntRange(from = 1, to = 100) jpegQuality: Int
    ): ByteArray {
        val bitmap: Bitmap
        try {
            val decoder = BitmapRegionDecoder.newInstance(
                data, 0, data.size,
                false
            )
            bitmap = decoder.decodeRegion(cropRect, BitmapFactory.Options())
            decoder.recycle()
        } catch (e: IllegalArgumentException) {
            throw ImageUtil.CodecFailedException(
                "Decode byte array failed with illegal argument.$e",
                ImageUtil.CodecFailedException.FailureType.DECODE_FAILED
            )
        } catch (e: IOException) {
            throw ImageUtil.CodecFailedException(
                "Decode byte array failed.",
                ImageUtil.CodecFailedException.FailureType.DECODE_FAILED
            )
        }
        if (bitmap == null) {
            throw ImageUtil.CodecFailedException(
                "Decode byte array failed.",
                ImageUtil.CodecFailedException.FailureType.DECODE_FAILED
            )
        }
        val out = ByteArrayOutputStream()
        val success = bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out)
        if (!success) {
            throw ImageUtil.CodecFailedException(
                "Encode bitmap failed.",
                ImageUtil.CodecFailedException.FailureType.ENCODE_FAILED
            )
        }
        bitmap.recycle()
        return out.toByteArray()
    }

    /** True if the given aspect ratio is meaningful.  */
    fun isAspectRatioValid(aspectRatio: Rational): Boolean {
        return aspectRatio != null && aspectRatio.toFloat() > 0 && !aspectRatio.isNaN
    }

    /** True if the given aspect ratio is meaningful and has effect on the given size.  */
    fun isAspectRatioValid(
        sourceSize: Size,
        aspectRatio: Rational?
    ): Boolean {
        return (aspectRatio != null && aspectRatio.toFloat() > 0f && ImageUtil.isCropAspectRatioHasEffect(
            sourceSize,
            aspectRatio
        )
                && !aspectRatio.isNaN)
    }

    /**
     * Calculates crop rect with the specified aspect ratio on the given size. Assuming the rect is
     * at the center of the source.
     */
    fun computeCropRectFromAspectRatio(
        sourceSize: Size,
        aspectRatio: Rational
    ): Rect? {
        if (!ImageUtil.isAspectRatioValid(aspectRatio)) {
            Log.w("TAG", "Invalid view ratio.")
            return null
        }
        val sourceWidth = sourceSize.width
        val sourceHeight = sourceSize.height
        val srcRatio = sourceWidth / sourceHeight.toFloat()
        var cropLeft = 0
        var cropTop = 0
        var outputWidth = sourceWidth
        var outputHeight = sourceHeight
        val numerator = aspectRatio.numerator
        val denominator = aspectRatio.denominator
        if (aspectRatio.toFloat() > srcRatio) {
            outputHeight = Math.round(sourceWidth / numerator.toFloat() * denominator)
            cropTop = (sourceHeight - outputHeight) / 2
        } else {
            outputWidth = Math.round(sourceHeight / denominator.toFloat() * numerator)
            cropLeft = (sourceWidth - outputWidth) / 2
        }
        return Rect(cropLeft, cropTop, cropLeft + outputWidth, cropTop + outputHeight)
    }

    /**
     * Calculates crop rect based on the dispatch resolution and rotation degrees info.
     *
     *
     *  The original crop rect is calculated based on camera sensor buffer. On some devices,
     * the buffer is rotated before being passed to users, in which case the crop rect also
     * needs additional transformations.
     *
     *
     *  There are two most common scenarios: 1) exif rotation is 0, or 2) exif rotation
     * equals output rotation. 1) means the HAL rotated the buffer based on target
     * rotation. 2) means HAL no-oped on the rotation. Theoretically only 1) needs
     * additional transformations, but this method is also generic enough to handle all possible
     * HAL rotations.
     */
    fun computeCropRectFromDispatchInfo(
        surfaceCropRect: Rect,
        surfaceToOutputDegrees: Int, dispatchResolution: Size,
        dispatchToOutputDegrees: Int
    ): Rect {
        // There are 3 coordinate systems: surface, dispatch and output. Surface is where
        // the original crop rect is defined. We need to figure out what HAL
        // has done to the buffer (the surface->dispatch mapping) and apply the same
        // transformation to the crop rect.
        // The surface->dispatch mapping is calculated by inverting a dispatch->surface mapping.
        val matrix = Matrix()
        // Apply the dispatch->surface rotation.
        matrix.setRotate((dispatchToOutputDegrees - surfaceToOutputDegrees).toFloat())
        // Apply the dispatch->surface translation. The translation is calculated by
        // compensating for the offset caused by the dispatch->surface rotation.
        val vertexes = ImageUtil.sizeToVertexes(dispatchResolution)
        matrix.mapPoints(vertexes)
        val left = ImageUtil.min(
            vertexes[0],
            vertexes[2], vertexes[4], vertexes[6]
        )
        val top = ImageUtil.min(
            vertexes[1],
            vertexes[3], vertexes[5], vertexes[7]
        )
        matrix.postTranslate(-left, -top)
        // Inverting the dispatch->surface mapping to get the surface->dispatch mapping.
        matrix.invert(matrix)

        // Apply the surface->dispatch mapping to surface crop rect.
        val dispatchCropRectF = RectF()
        matrix.mapRect(dispatchCropRectF, RectF(surfaceCropRect))
        dispatchCropRectF.sort()
        val dispatchCropRect = Rect()
        dispatchCropRectF.round(dispatchCropRect)
        return dispatchCropRect
    }

    @Throws(ImageUtil.CodecFailedException::class)
    private fun nv21ToJpeg(
        nv21: ByteArray, width: Int, height: Int,
        cropRect: Rect?, @IntRange(from = 1, to = 100) jpegQuality: Int
    ): ByteArray {
        val out = ByteArrayOutputStream()
        val yuv = YuvImage(nv21, ImageFormat.YUV_420_888, width, height, null)
        val success = yuv.compressToJpeg(
            cropRect ?: Rect(0, 0, width, height),
            jpegQuality, out
        )
        if (!success) {
            throw ImageUtil.CodecFailedException(
                "YuvImage failed to encode jpeg.",
                ImageUtil.CodecFailedException.FailureType.ENCODE_FAILED
            )
        }
        return out.toByteArray()
    }

    private fun isCropAspectRatioHasEffect(
        sourceSize: Size,
        aspectRatio: Rational
    ): Boolean {
        val sourceWidth = sourceSize.width
        val sourceHeight = sourceSize.height
        val numerator = aspectRatio.numerator
        val denominator = aspectRatio.denominator
        return (sourceHeight != Math.round(sourceWidth / numerator.toFloat() * denominator)
                || sourceWidth != Math.round(sourceHeight / denominator.toFloat() * numerator))
    }

    private fun inverseRational(rational: Rational?): Rational? {
        return if (rational == null) {
            rational
        } else Rational( /*numerator=*/
            rational.denominator,  /*denominator=*/
            rational.numerator
        )
    }

    /**
     * Checks whether the image's crop rectangle is the same as the source image size.
     */
    fun shouldCropImage(image: ImageProxy): Boolean {
        return ImageUtil.shouldCropImage(
            image.width, image.height, image.cropRect.width(),
            image.cropRect.height()
        )
    }

    /**
     * Checks whether the image's crop rectangle is the same as the source image size.
     */
    fun shouldCropImage(
        sourceWidth: Int, sourceHeight: Int, cropRectWidth: Int,
        cropRectHeight: Int
    ): Boolean {
        return sourceWidth != cropRectWidth || sourceHeight != cropRectHeight
    }

    /** Exception for error during transcoding image.  */
    class CodecFailedException : Exception {
        enum class FailureType {
            ENCODE_FAILED, DECODE_FAILED, UNKNOWN
        }

        var failureType: ImageUtil.CodecFailedException.FailureType
            private set

        internal constructor(message: String) : super(message) {
            failureType = ImageUtil.CodecFailedException.FailureType.UNKNOWN
        }

        internal constructor(
            message: String,
            failureType: ImageUtil.CodecFailedException.FailureType
        ) : super(message) {
            this.failureType = failureType
        }
    }
}