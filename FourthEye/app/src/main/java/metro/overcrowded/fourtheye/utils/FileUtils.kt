package metro.overcrowded.fourtheye.utils

import android.content.Context
import android.graphics.Bitmap
import java.io.File

object FileUtils {
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String): File {
        val file = File(context.cacheDir, filename)
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return file
    }

    fun saveTextToFile(context: Context, filename: String, text: String): File {
        val file = File(context.cacheDir, filename)
        file.writeText(text)
        return file
    }
}