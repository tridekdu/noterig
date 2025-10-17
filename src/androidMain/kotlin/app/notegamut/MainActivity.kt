package app.notegamut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import java.io.File

class MainActivity : ComponentActivity() {

    private var onyx: OnyxBridge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable Boox raw drawing if SDK/device present
        onyx = OnyxBridge(window.decorView).also { it.enable() }

        val dir = File(getExternalFilesDir(null), "segments").apply { mkdirs() }
        setContent { InfiniteCanvasScreen(rootDir = dir, tileSize = 384) }
    }

    override fun onDestroy() {
        onyx?.disable()
        onyx = null
        super.onDestroy()
    }
}
