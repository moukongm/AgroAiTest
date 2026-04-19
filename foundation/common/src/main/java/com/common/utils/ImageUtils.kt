package com.common.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    private const val MAX_FILE_SIZE_BYTES = 7_864_320
    private const val DEFAULT_MAX_DIMENSION = 1024
    private const val DEFAULT_JPEG_QUALITY = 80
    private const val TAG = "ImageUtils"

    /**
     * 压缩图片，确保其大小不超过指定的最大值（默认10MB）
     * @param bitmap 原始图片
     * @param maxSizeBytes 最大文件大小（字节）
     * @return 压缩后的图片
     */
    fun compressBitmap(bitmap: Bitmap, maxSizeBytes: Int = MAX_FILE_SIZE_BYTES): Bitmap {
        var quality = 100
        var compressedBitmap = bitmap
        var byteArrayOutputStream = ByteArrayOutputStream()
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

        // 如果图片已经小于最大大小，直接返回
        if (byteArrayOutputStream.size() <= maxSizeBytes) {
            Log.d(TAG, "图片大小已经符合要求：${byteArrayOutputStream.size()} bytes")
            return compressedBitmap
        }

        // 首先尝试降低质量
        while (byteArrayOutputStream.size() > maxSizeBytes && quality > 10) {
            byteArrayOutputStream.reset()
            quality -= 10
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            Log.d(TAG, "压缩质量：$quality，大小：${byteArrayOutputStream.size()} bytes")
        }

        // 如果降低质量后仍然太大，则缩小尺寸
        var scale = 1.0f
        while (byteArrayOutputStream.size() > maxSizeBytes && scale > 0.1f) {
            scale -= 0.1f
            val newWidth = (compressedBitmap.width * scale).toInt()
            val newHeight = (compressedBitmap.height * scale).toInt()

            // 确保尺寸不为0
            if (newWidth <= 0 || newHeight <= 0) break

            val scaledBitmap = Bitmap.createScaledBitmap(compressedBitmap, newWidth, newHeight, true)

            // 如果不是原始图片，释放之前的缩放图片
            if (compressedBitmap != bitmap) {
                compressedBitmap.recycle()
            }

            compressedBitmap = scaledBitmap
            byteArrayOutputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            Log.d(TAG, "缩放比例：$scale，大小：${byteArrayOutputStream.size()} bytes")
        }

        Log.d(TAG, "最终图片大小：${byteArrayOutputStream.size()} bytes")
        return compressedBitmap
    }

    /**
     * 压缩图片文件，统一用于上传前的本地文件瘦身。
     */
    @JvmStatic
    @JvmOverloads
    fun compressImageFile(
        file: File,
        maxDimension: Int = DEFAULT_MAX_DIMENSION,
        quality: Int = DEFAULT_JPEG_QUALITY
    ): File? {
        if (!file.exists() || maxDimension <= 0) return null

        return try {
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null
            }

            val bitmap = BitmapFactory.decodeFile(
                file.absolutePath,
                BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
                }
            ) ?: return null

            val needsScale = bitmap.width > maxDimension || bitmap.height > maxDimension
            val scaledBitmap = if (needsScale) {
                val ratio = minOf(
                    maxDimension.toFloat() / bitmap.width,
                    maxDimension.toFloat() / bitmap.height
                )
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt().coerceAtLeast(1),
                    (bitmap.height * ratio).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                bitmap
            }

            try {
                FileOutputStream(file).use { fos ->
                    scaledBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        quality.coerceIn(10, 100),
                        fos
                    )
                    fos.flush()
                }
                file
            } finally {
                bitmap.recycle()
                if (scaledBitmap !== bitmap) {
                    scaledBitmap.recycle()
                }
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "compressImageFile error: ${e.message}")
            null
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var inSampleSize = 1
        if (width <= maxDimension && height <= maxDimension) {
            return inSampleSize
        }
        var halfWidth = width / 2
        var halfHeight = height / 2
        while ((halfWidth / inSampleSize) >= maxDimension && (halfHeight / inSampleSize) >= maxDimension) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    /**
     * 将Bitmap转换为Base64编码的字符串
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val compressedBitmap = compressBitmap(bitmap, MAX_FILE_SIZE_BYTES)
        val byteArrayOutputStream = ByteArrayOutputStream()
        var quality = 85
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        while (byteArrayOutputStream.size() > MAX_FILE_SIZE_BYTES && quality > 30) {
            byteArrayOutputStream.reset()
            quality -= 10
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        }
        val byteArray = byteArrayOutputStream.toByteArray()
        if (compressedBitmap != bitmap) {
            compressedBitmap.recycle()
        }
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * 将Bitmap转换为Base64编码的Data URL
     */
    fun bitmapToBase64DataUrl(bitmap: Bitmap): String {
        val base64 = bitmapToBase64(bitmap)
        return "data:image/jpeg;base64,$base64"
    }
}
