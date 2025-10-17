package app.notegamut
object OnyxInputRouter {
    @Volatile var onDown: ((x: Float, y: Float, p: Float) -> Unit)? = null
    @Volatile var onMove: ((x: Float, y: Float, p: Float) -> Unit)? = null
    @Volatile var onUp:   (() -> Unit)? = null
}
