package app.notegamut.platform
import androidx.compose.ui.graphics.ImageBitmap
expect fun loadImageBitmapFromPath(path:String):ImageBitmap?
expect fun saveImageBitmapToPath(bmp:ImageBitmap,path:String):Boolean
