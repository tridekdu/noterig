package app.notegamut.canvas
data class TileId(val x:Int,val y:Int,val z:Int=0){fun fileName()="%d_%d_%d.png".format(x,y,z)}
