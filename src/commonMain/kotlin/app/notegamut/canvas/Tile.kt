package app.notegamut.canvas
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import java.util.ArrayDeque
private const val MAX_UNDO = 16
class Tile(val id: TileId, initial: ImageBitmap) {
    private val stack = ArrayDeque<ImageBitmap>().apply { push(initial) }
    val image: ImageBitmap get() = stack.first()
    val version = mutableStateOf(0)
    fun bump() { version.value = version.value + 1 }
    fun snapshot(copy: (ImageBitmap) -> ImageBitmap) {
        stack.push(copy(stack.first())); while (stack.size > MAX_UNDO) stack.removeLast()
    }
    fun undo() { if (stack.size > 1) stack.removeFirst() }
}
