package app.notegamut.platform
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
actual fun loadImageBitmapFromPath(path: String): ImageBitmap? =
    BitmapFactory.decodeFile(path)?.asImageBitmap()
actual fun saveImageBitmapToPath(bmp: ImageBitmap, path: String): Boolean = try {
    val f = File(path); f.parentFile?.mkdirs()
    FileOutputStream(f).use { out -> bmp.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out) }
    true
} catch (_: Throwable) { false }
