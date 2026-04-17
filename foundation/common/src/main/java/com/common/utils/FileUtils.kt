package com.common.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

/**
 * 通用文件操作工具类
 * 涵盖了 Uri 转换、文件创建、删除、大小计算、MIME 类型获取等常用功能。
 */
object FileUtils {

    private const val TAG = "FileUtils"

    /**
     * 将 Uri 转换为 File
     * - FileProvider 的 content:// URI：直接用原始文件路径
     * - 其他 content:// URI：通过 contentResolver 拷贝
     * - file:// URI：直接返回
     *
     * @param context 上下文
     * @param uri 目标 Uri
     * @param destDir 目标文件夹，默认使用外部缓存目录
     * @return 转换后的 File 对象，失败返回 null
     */
    fun uriToFile(context: Context, uri: Uri, destDir: File = context.externalCacheDir ?: context.cacheDir): File? {
        LogUtils.d("ljx_file", "uriToFile start - uri: $uri")
        return try {
            // FileProvider 的 content:// URI，直接用原始文件
            if (uri.scheme == "content" && uri.authority?.contains("fileprovider") == true) {
                val path = uri.path
                LogUtils.d("ljx_file", "FileProvider URI, path: $path")
                val originalFile = File(path)
                LogUtils.d("ljx_file", "file exists: ${originalFile.exists()}, size: ${originalFile.length()}")
                if (originalFile.exists()) {
                    return originalFile
                }
            }

            // 其他 content:// URI，通过 contentResolver 拷贝
            val fileName = getFileName(context, uri) ?: "temp_file_${System.currentTimeMillis()}"
            LogUtils.d("ljx_file", "content URI, fileName: $fileName")
            val destFile = File(destDir, fileName)
            val inputStream = context.contentResolver.openInputStream(uri)
            LogUtils.d("ljx_file", "inputStream: ${inputStream != null}")
            if (inputStream != null && copyStream(inputStream, FileOutputStream(destFile))) {
                LogUtils.d("ljx_file", "copied file size: ${destFile.length()}")
                destFile
            } else {
                null
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "uriToFile error: ${e.message}")
            null
        }
    }

    /**
     * 获取 Uri 对应的文件名
     */
    fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        return cursor.getString(nameIndex)
                    }
                }
            }
        }
        return uri.path?.let { path ->
            val cut = path.lastIndexOf('/')
            if (cut != -1) path.substring(cut + 1) else path
        }
    }

    /**
     * 获取文件的 MIME 类型
     */
    fun getMimeType(file: File?): String {
        if (file == null || !file.exists()) return "application/octet-stream"
        val extension = file.extension
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }

    /**
     * 获取 Uri 的 MIME 类型
     */
    fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri) ?: "application/octet-stream"
    }

    /**
     * 拷贝流数据
     */
    fun copyStream(input: InputStream, output: FileOutputStream): Boolean {
        val bis = BufferedInputStream(input)
        val bos = BufferedOutputStream(output)
        return try {
            val buffer = ByteArray(8192)
            var read: Int
            while (bis.read(buffer).also { read = it } != -1) {
                bos.write(buffer, 0, read)
            }
            bos.flush()
            true
        } catch (e: Exception) {
            LogUtils.e(TAG, "copyStream error: ${e.message}")
            false
        } finally {
            try { bos.close() } catch (_: Exception) {}
            try { bis.close() } catch (_: Exception) {}
        }
    }

    /**
     * 格式化文件大小 (如: 1.2 MB)
     */
    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    /**
     * 递归删除文件或文件夹
     */
    fun deleteFile(file: File?): Boolean {
        if (file == null || !file.exists()) return false
        if (file.isDirectory) {
            val children = file.listFiles()
            if (children != null) {
                for (child in children) {
                    deleteFile(child)
                }
            }
        }
        return file.delete()
    }

    /**
     * 确保文件夹存在，不存在则创建
     */
    fun ensureDir(dir: File?): Boolean {
        if (dir == null) return false
        if (!dir.exists()) {
            return dir.mkdirs()
        }
        return dir.isDirectory
    }

    /**
     * 创建一个临时文件
     *
     * @param prefix 前缀
     * @param suffix 后缀 (如 .jpg)
     */
    fun createTempFile(context: Context, prefix: String = "temp_", suffix: String = ""): File {
        val dir = File(context.externalCacheDir ?: context.cacheDir, "temp")
        ensureDir(dir)
        return File(dir, "${prefix}${System.currentTimeMillis()}${suffix}")
    }
}
