package app.notegamut

import android.view.View

/**
 * Best-effort bridge for Onyx Boox raw drawing.
 * Uses reflection so we don't depend on specific 1.4.x vs 1.5.x callback APIs.
 * If the SDK or device is missing, all calls no-op.
 */
class OnyxBridge(private val targetView: View) {

    private var touchHelper: Any? = null
    private var touchHelperClass: Class<*>? = null

    fun enable() {
        try {
            touchHelperClass = Class.forName("com.onyx.android.sdk.pen.TouchHelper")
            val cls = touchHelperClass!!

            // Try create(View, RawInputCallback) first
            touchHelper = try {
                val cbClass = Class.forName("com.onyx.android.sdk.pen.RawInputCallback")
                val ctor = cls.getMethod("create", View::class.java, cbClass)
                // Use a dynamic proxy that ignores all methods to avoid API drift
                val proxy = java.lang.reflect.Proxy.newProxyInstance(
                    cbClass.classLoader, arrayOf(cbClass)
                ) { _, _, _ -> null }
                ctor.invoke(null, targetView, proxy)
            } catch (_: Throwable) {
                // Fallback: create(View)
                val m = cls.getMethod("create", View::class.java)
                m.invoke(null, targetView)
            }

            // Open raw drawing session
            runCatching { cls.getMethod("openRawDrawing").invoke(touchHelper) }

            // Enable raw drawing
            runCatching {
                cls.getMethod("setRawDrawingEnabled", Boolean::class.javaPrimitiveType)
                    .invoke(touchHelper, true)
            }

            // Optional latency flags if available in this SDK
            runCatching {
                cls.getMethod("setStrokeStyle", Int::class.javaPrimitiveType)
                    .invoke(touchHelper, 0) // 0 = normal
            }
        } catch (_: Throwable) {
            // Not an Onyx device or SDK not on classpath. Safe no-op.
        }
    }

    fun disable() {
        val cls = touchHelperClass ?: return
        val helper = touchHelper ?: return
        runCatching {
            cls.getMethod("setRawDrawingEnabled", Boolean::class.javaPrimitiveType)
                .invoke(helper, false)
        }
        runCatching { cls.getMethod("closeRawDrawing").invoke(helper) }
        touchHelper = null
        touchHelperClass = null
    }
}
