package app.notegamut.platform
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File
actual fun loadImageBitmapFromPath(path: String): ImageBitmap? =
    try { Image.makeFromEncoded(File(path).readBytes()).toComposeImageBitmap() } catch (_: Throwable) { null }
actual fun saveImageBitmapToPath(bmp: ImageBitmap, path: String): Boolean = try {
    val img = Image.makeFromBitmap(bmp.asSkiaBitmap())
    val data = img.encodeToData(EncodedImageFormat.PNG, 100) ?: return false
    File(path).apply { parentFile?.mkdirs() }.writeBytes(data.bytes)
    true
} catch (_: Throwable) { false }
