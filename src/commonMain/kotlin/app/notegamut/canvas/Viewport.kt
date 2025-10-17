package app.notegamut.canvas
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.floor

class Viewport {
    var offX by mutableStateOf(0f)
    var offY by mutableStateOf(0f)
    var scale by mutableStateOf(1f)

    fun visibleTileRange(width: Float, height: Float, tile: Int): Pair<IntRange, IntRange> {
        val tx0 = floor(offX / tile).toInt() - 1
        val ty0 = floor(offY / tile).toInt() - 1
        val tx1 = tx0 + (width / tile).toInt() + 3
        val ty1 = ty0 + (height / tile).toInt() + 3
        return (tx0..tx1) to (ty0..ty1)
    }
}
