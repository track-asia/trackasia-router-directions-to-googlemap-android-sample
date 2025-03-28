package com.maps.route.data

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.maps.route.model.Route

internal class DrawRouteBuilder private constructor(builder: BuildRoute) {

    private val alpha: Float
    private val pathWidth: Int
    private val pathColor: Int
    private val bitmapDescriptor: BitmapDescriptor
    private val googleMap: GoogleMap
    private val polylineList: MutableList<Polyline> = mutableListOf()

    companion object {
        private const val DEFAULT_MARKER_ALPHA = 1f
        private const val DEFAULT_PATH_WIDTH = 5
        private const val DEFAULT_PATH_COLOR = Color.RED
    }

    init {
        googleMap = builder.googleMap
        alpha = builder.alpha
        pathWidth = builder.pathWidth
        pathColor = builder.pathColor
        bitmapDescriptor = builder.bitmapDescriptor
    }

    fun drawPath(routes: List<Route>) {
        for (route in routes) {
            route.overviewPolyline?.points?.let { encodedPath ->
                val decodedPath = PolyUtil.decode(encodedPath)
                val polylineOptions = PolylineOptions()
                    .addAll(decodedPath)
                    .width(pathWidth.toFloat())
                    .color(getColorWithAlpha(pathColor, alpha))
                
                val polyline = googleMap.addPolyline(polylineOptions)
                polylineList.add(polyline)
            }
        }
    }

    fun removePaths() {
        for (polyline in polylineList) {
            polyline.remove()
        }
        polylineList.clear()
    }

    private fun getColorWithAlpha(color: Int, alpha: Float): Int {
        val alphaValue = (alpha * 255).toInt()
        return Color.argb(alphaValue, Color.red(color), Color.green(color), Color.blue(color))
    }

    class BuildRoute(internal val googleMap: GoogleMap) {
        internal var bitmapDescriptor: BitmapDescriptor
        internal var pathWidth: Int
        internal var pathColor: Int
        internal var alpha: Float

        fun withColor(pathColor: Int): BuildRoute {
            this.pathColor = pathColor
            return this
        }

        fun withWidth(pathWidth: Int): BuildRoute {
            this.pathWidth = pathWidth
            return this
        }

        fun withMarkerIcon(bitmapDescriptor: BitmapDescriptor): BuildRoute {
            this.bitmapDescriptor = bitmapDescriptor
            return this
        }

        fun withAlpha(alpha: Float): BuildRoute {
            this.alpha = alpha
            return this
        }

        fun build(): DrawRouteBuilder {
            return DrawRouteBuilder(this)
        }

        init {
            pathWidth = DEFAULT_PATH_WIDTH
            pathColor = DEFAULT_PATH_COLOR
            alpha = DEFAULT_MARKER_ALPHA
            bitmapDescriptor =
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        }
    }
}
