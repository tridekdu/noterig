package app.notegamut
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
fun main() = application {
    val root = File(System.getProperty("user.dir"), "segments").apply { mkdirs() }
    Window(onCloseRequest = ::exitApplication, title = "Infinite Canvas") {
        InfiniteCanvasScreen(rootDir = root, tileSize = 384)
    }
}
