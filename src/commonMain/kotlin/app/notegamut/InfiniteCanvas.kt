package app.notegamut

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.key.Key as ComposeKey
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import app.notegamut.canvas.*
import app.notegamut.draw.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.floor
import java.io.File

@Composable
fun InfiniteCanvasScreen(rootDir: File, tileSize: Int = 384) {
    val vp = remember { Viewport() }
    val scope = rememberCoroutineScope()
    val state = remember { TiledState(TileStore(rootDir, tileSize), tileSize, vp, scope) }
    var tool by remember { mutableStateOf(Tool.Pen) }
    var panMode by remember { mutableStateOf(true) } // toggle with Space
    val focusRequester = remember { FocusRequester() }

    // Observe to force recomposition on invalidations
    val tick = state.invalidateTick.value

    Box(
        Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .pointerInput(tool) {
                awaitPointerEventScope {
                    var mode: String? = null   // "draw" | "pan" | null
                    var last = Offset.Zero

                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        val first = changes.firstOrNull() ?: continue
                        val pos = first.position

                        when (event.type) {
                            PointerEventType.Press -> {
                                // Decide mode once at press
                                val anyStylus = changes.any { it.type == PointerType.Stylus }
                                val isMouse = changes.any { it.type == PointerType.Mouse }
                                mode = when {
                                    anyStylus -> "draw"                                       // Android pen
                                    isMouse && event.buttons.isTertiaryPressed -> "pan"        // Desktop middle
                                    isMouse && event.buttons.isPrimaryPressed -> "draw"        // Desktop left
                                    changes.any { it.type == PointerType.Touch } -> "pan"      // Android finger
                                    else -> null
                                }
                                last = pos
                                if (mode == "draw") {
                                    state.beginStroke(tool, pos.x, pos.y)
                                    state.requestInvalidate()
                                }
                                changes.forEach { it.consume() }
                            }

                            PointerEventType.Move -> {
                                val delta = pos - last
                                if (mode == "draw") {
                                    state.addPoint(pos.x, pos.y)
                                    state.requestInvalidate()
                                } else if (mode == "pan") {
                                    vp.offX -= delta.x
                                    vp.offY -= delta.y
                                    // Viewport is state-backed; grid and labels update immediately
                                }
                                last = pos
                                changes.forEach { it.consume() }
                            }

                            PointerEventType.Release, PointerEventType.Exit, PointerEventType.Unknown -> {
                                if (mode == "draw") {
                                    state.endStroke()
                                    state.requestInvalidate()
                                }
                                mode = null
                                changes.forEach { it.consume() }
                            }
                        }
                    }
                }
            }
    ) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Canvas(Modifier.fillMaxSize().background(Color(0xFFF8F8F8))) {
            // Grid with darker lines on tile borders
            val light = Color(0xFFDDDDDD)
            val dark = Color(0xFF9E9E9E)
            val grid = 24f

            // vertical lines
            var x = (-(vp.offX % grid) + grid) % grid
            while (x <= size.width) {
                val worldX = x + vp.offX
                val onTileBorder = (worldX % tileSize) in 0f..1f
                drawLine(
                    if (onTileBorder) dark else light,
                    Offset(x, 0f),
                    Offset(x, size.height),
                    if (onTileBorder) 2f else 1f
                )
                x += grid
            }
            // horizontal lines
            var y = (-(vp.offY % grid) + grid) % grid
            while (y <= size.height) {
                val worldY = y + vp.offY
                val onTileBorder = (worldY % tileSize) in 0f..1f
                drawLine(
                    if (onTileBorder) dark else light,
                    Offset(0f, y),
                    Offset(size.width, y),
                    if (onTileBorder) 2f else 1f
                )
                y += grid
            }

            // Tiles
            val (xs, ys) = vp.visibleTileRange(size.width, size.height, tileSize)
            for (tx in xs) for (ty in ys) {
                val id = TileId(tx, ty, 0)
                val t = state.tile(id) ?: continue
                val _observe = t.version.value // observe per-tile changes
                val sx = tx * tileSize - vp.offX
                val sy = ty * tileSize - vp.offY
                drawImage(t.image, topLeft = Offset(sx, sy))
            }

            // Live stroke overlay during drag
            state.current?.let { s ->
                val w = if (s.tool == Tool.Pen) 3f else 8f
                val col = if (s.tool == Tool.Pen) Color.Black else Color.White
                for (i in 1 until s.pts.size) {
                    val a = s.pts[i - 1]; val b = s.pts[i]
                    drawLine(col, Offset(a.x, a.y), Offset(b.x, b.y), w)
                }
            }
        }

// Canvas grid and tiles already read vp.offX/vp.offY each draw

// Debug labels: compute without remember
        val (xs, ys) = vp.visibleTileRange(width = 4096f, height = 4096f, tileSize)
        Box(Modifier.fillMaxSize()) {
            for (tx in xs) for (ty in ys) {
                val sx = tx * tileSize - vp.offX
                val sy = ty * tileSize - vp.offY
                Box(Modifier.offset { IntOffset(sx.toInt() + 8, sy.toInt() + 8) }) {
                    Text(text = "${tx}_${ty}_0", color = Color(0xFF555555))
                }
            }
        }

        Button(
            onClick = { tool = if (tool == Tool.Pen) Tool.Eraser else Tool.Pen },
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) { Text(if (tool == Tool.Pen) "Pen" else "Eraser") }

        Text(
            text = if (panMode) "Pan mode: SPACE" else "Draw mode",
            color = if (panMode) Color(0xFF1565C0) else Color(0xFF444444),
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        )
    }
}

// ----- state and IO -----
private class TiledState(
    private val store: TileStore,
    private val tileSize: Int,
    private val vp: Viewport,
    private val scope: CoroutineScope
) {
    private val tiles = androidx.compose.runtime.mutableStateMapOf<TileId, Tile>()
    private val dirty = mutableSetOf<TileId>()
    private val touchedThisStroke = mutableSetOf<TileId>()
    val invalidateTick = mutableStateOf(0)
    var current: Stroke? = null

    fun requestInvalidate() { invalidateTick.value = invalidateTick.value + 1 }

    fun tile(id: TileId): Tile? = tiles[id] ?: store.tryLoad(id)?.also { tiles[id] = it }

    fun beginStroke(tool: Tool, x: Float, y: Float) {
        current = Stroke(tool, mutableListOf(StrokePoint(x, y, 1f)))
        touchedThisStroke.clear()
    }
    fun addPoint(x: Float, y: Float) { current?.pts?.add(StrokePoint(x, y, 1f)) }
    fun endStroke() { current?.let { commitStroke(it); saveDirtyAsync() }; current = null; touchedThisStroke.clear() }

    private fun commitStroke(s: Stroke) {
        if (s.pts.size < 2) return
        for (i in 1 until s.pts.size) {
            val a = s.pts[i - 1]; val b = s.pts[i]
            drawSegment(a, b, s.tool)
        }
    }

    private fun drawSegment(a: StrokePoint, b: StrokePoint, tool: Tool) {
        val wx0 = a.x + vp.offX; val wy0 = a.y + vp.offY
        val wx1 = b.x + vp.offX; val wy1 = b.y + vp.offY
        val tid0 = TileId((wx0 / tileSize).toInt(), (wy0 / tileSize).toInt(), 0)
        val tid1 = TileId((wx1 / tileSize).toInt(), (wy1 / tileSize).toInt(), 0)

        fun local(tid: TileId, x: Float, y: Float) = (x - tid.x * tileSize) to (y - tid.y * tileSize)
        fun copy(src: androidx.compose.ui.graphics.ImageBitmap) =
            androidx.compose.ui.graphics.ImageBitmap(src.width, src.height).also {
                androidx.compose.ui.graphics.Canvas(it).drawImage(
                    src,
                    androidx.compose.ui.geometry.Offset.Zero,
                    androidx.compose.ui.graphics.Paint()
                )
            }
        fun applyOn(tile: Tile, id: TileId, ax: Float, ay: Float, bx: Float, by: Float) {
            if (touchedThisStroke.add(id)) tile.snapshot(::copy)
            val c = androidx.compose.ui.graphics.Canvas(tile.image)
            val p = androidx.compose.ui.graphics.Paint().apply {
                color = if (tool == Tool.Pen) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.Transparent
                blendMode = if (tool == Tool.Pen) androidx.compose.ui.graphics.BlendMode.SrcOver else androidx.compose.ui.graphics.BlendMode.Clear
                isAntiAlias = true
                strokeWidth = if (tool == Tool.Pen) 3f else 8f
            }
            c.drawLine(
                androidx.compose.ui.geometry.Offset(ax, ay),
                androidx.compose.ui.geometry.Offset(bx, by),
                p
            )
            tiles[id] = tile
            tile.bump()
            dirty += id
        }

        val tA = tiles.getOrPut(tid0) { store.tryLoad(tid0) ?: store.create(tid0) { w, h -> androidx.compose.ui.graphics.ImageBitmap(w, h) } }
        val (ax, ay) = local(tid0, wx0, wy0); val (bx, by) = local(tid0, wx1, wy1)
        applyOn(tA, tid0, ax, ay, bx, by)

        if (tid1 != tid0) {
            val tB = tiles.getOrPut(tid1) { store.tryLoad(tid1) ?: store.create(tid1) { w, h -> androidx.compose.ui.graphics.ImageBitmap(w, h) } }
            val (ax2, ay2) = local(tid1, wx0, wy0); val (bx2, by2) = local(tid1, wx1, wy1)
            applyOn(tB, tid1, ax2, ay2, bx2, by2)
        }
        requestInvalidate()
    }

    private fun saveDirtyAsync() {
        val ids = dirty.toList()
        dirty.clear()
        if (ids.isEmpty()) return
        scope.launch(Dispatchers.IO) { ids.forEach { id -> tiles[id]?.let { store.save(it) } } }
    }
}
