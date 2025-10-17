package app.notegamut.canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.geometry.Offset
import app.notegamut.platform.loadImageBitmapFromPath
import app.notegamut.platform.saveImageBitmapToPath
import java.io.File
class TileStore(private val rootDir: File, private val tileSize: Int) {
    init { rootDir.mkdirs() }
    fun pathOf(id: TileId) = File(rootDir, id.fileName())
    fun tryLoad(id: TileId): Tile? {
        val bmp = loadImageBitmapFromPath(pathOf(id).absolutePath) ?: return null
        return Tile(id, ensureSize(bmp))
    }
    fun create(id: TileId, alloc: (Int, Int) -> ImageBitmap): Tile = Tile(id, alloc(tileSize, tileSize))
    fun save(tile: Tile) { saveImageBitmapToPath(tile.image, pathOf(tile.id).absolutePath) }
    private fun ensureSize(src: ImageBitmap): ImageBitmap =
        if (src.width == tileSize && src.height == tileSize) src
        else ImageBitmap(tileSize, tileSize).also { dst -> Canvas(dst).drawImage(src, Offset.Zero, Paint()) }
}
