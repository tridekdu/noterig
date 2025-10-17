package app.notegamut.draw
enum class Tool{Pen,Eraser}
data class StrokePoint(val x:Float,val y:Float,val pressure:Float)
data class Stroke(val tool: Tool, val pts: MutableList<StrokePoint>)