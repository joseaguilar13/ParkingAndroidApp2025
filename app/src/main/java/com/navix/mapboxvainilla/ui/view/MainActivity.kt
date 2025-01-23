package com.navix.mapboxvainilla.ui.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.QueriedRenderedFeature
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
//import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
//import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
//import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.navix.mapboxvainilla.Activity2
import com.navix.mapboxvainilla.Constants
import com.navix.mapboxvainilla.PermissionManager
import com.navix.mapboxvainilla.R
import com.navix.mapboxvainilla.databinding.ActivityMainBinding
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.TextJustify
import com.mapbox.maps.extension.style.expressions.dsl.generated.has
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.google.gson.JsonParser





class MainActivity :
    BaseActivity(),
    SensorEventListener {
    private var context: Context = this
    private lateinit var binding: ActivityMainBinding
    private lateinit var event: ParseObject
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private lateinit var buildingsGeoJson: JSONObject
    private lateinit var routesGeoJson: JSONObject
    private lateinit var routesGeoJson2: JSONObject
    private lateinit var routesGeoJson3: JSONObject
    private lateinit var routesGeoJson4: JSONObject
    private lateinit var routesGeoJson5: JSONObject
    private lateinit var routesGeoJson6: JSONObject
    private lateinit var routesGeoJson7: JSONObject
    private lateinit var routesGeoJson8: JSONObject
    private lateinit var routesGeoJson9: JSONObject
    private var selectedFeatureId: String? = null
    private var index = 0
    private var labelGlobal: String? = null

    private val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            }
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH,
            )
        }

    override fun onAccuracyChanged(
        sensor: Sensor,
        accuracy: Int,
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askPermissions()
        initializeBinding()
        initializeSensorCompass()
        ininitData()

    }

    fun clicke(view: View){

        Toast.makeText(context, "Ohmain", Toast.LENGTH_LONG).show()


    }

    fun close(view: View){

        // Toast.makeText(context, "Close", Toast.LENGTH_LONG).show()
        //binding.mapView.annotations.cleanup()
        val viewAnnotationManager = binding.mapView.viewAnnotationManager
        val viewAnnotation = viewAnnotationManager.removeAllViewAnnotations()

    }

    fun clicke2(view: View){

        //Toast.makeText(context, "Ohmain2", Toast.LENGTH_LONG).show()

        val Intent = Intent(this, Activity2::class.java)
        Intent.putExtra("no", labelGlobal)
        startActivity(Intent)


        binding.mapView.mapboxMap.addOnMapClickListener { point ->
            clickFeatureInMap(point)
            true
        }


    }


    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
    }

    private fun initializeSensorCompass() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI,
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI,
            )
        }
    }

    private fun initializeBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun askPermissions() {
        PermissionManager.requestPermissions(
            this,
            permissions,
            {
                // All permissions granted, proceed
                // ...
            },
            {
                // At least one permission denied, handle accordingly
                // ...
            },
        )
    }

    private fun ininitData() {
        CoroutineScope(Dispatchers.IO).launch {
            event = getEventsData()!!
            Log.i("EventData", event.toString())
            CoroutineScope(Dispatchers.Main).launch {
                configureMapBox()
            }
        }
    }

    private fun getEventsData(): ParseObject? {
        val query = ParseQuery.getQuery<ParseObject>("Geojsons")
        query.whereEqualTo("active", true)
        // LiygcKybef
        // dwmK9Jn66G
        query.whereEqualTo("name", Constants.PARKING_PLUS_ROUTES)

        return try {
            query.first as ParseObject
        } catch (pe: ParseException) {
            println(pe.message)
            println(pe.code)
            null
        }
    }

    private fun configureMapBox() {
        val initialCameraOptions =
            CameraOptions
                .Builder()
                .center(
                    Point.fromLngLat(
                        -100.43810221870,
                        20.83518096920,
                    ),
                    /*Point.fromLngLat(
                        event.getParseGeoPoint("location")!!.longitude,
                        event.getParseGeoPoint("location")!!.latitude,
                    ),*/
                ).zoom(17.0)
                .build()

        binding.mapView.mapboxMap.setCamera(initialCameraOptions)


        prepareLoadGeoJson()

        Toast.makeText(context, "Select the slot you want to go from Entrance", Toast.LENGTH_LONG).show()



        // addViewAnnotationToPoint(Point.fromLngLat(-100.43810221870, 20.83518096920,))

    }



    private fun addViewAnnotationToPoint(point: Point) {
        // Define the view annotation

        val viewAnnotationManager = binding.mapView.viewAnnotationManager

        val viewAnnotation = viewAnnotationManager.addViewAnnotation(
            // Specify the layout resource id
            resId = R.layout.menu,
            // Set any view annotation options

            options = viewAnnotationOptions {
                // View annotation is placed at the specific geo coordinate
                geometry(point)
            }
        )
    }

    private fun prepareLoadGeoJson() {
        if (event.getJSONObject("geoJson") != null) {
            buildingsGeoJson = JSONObject()
            routesGeoJson = JSONObject()
            routesGeoJson2 = JSONObject()
            routesGeoJson3 = JSONObject()
            routesGeoJson4 = JSONObject()
            routesGeoJson5 = JSONObject()
            routesGeoJson6 = JSONObject()
            routesGeoJson7 = JSONObject()
            routesGeoJson8 = JSONObject()
            routesGeoJson9 = JSONObject()

            val geoJson: JSONObject = event.getJSONObject("geoJson")!!

            buildingsGeoJson.put("type", "FeatureCollection")
            buildingsGeoJson.put("generator", "JOSM")

            routesGeoJson.put("type", "FeatureCollection")
            routesGeoJson.put("generator", "JOSM")

            routesGeoJson2.put("type", "FeatureCollection")
            routesGeoJson2.put("generator", "JOSM")

            routesGeoJson3.put("type", "FeatureCollection")
            routesGeoJson3.put("generator", "JOSM")

            routesGeoJson4.put("type", "FeatureCollection")
            routesGeoJson4.put("generator", "JOSM")

            routesGeoJson5.put("type", "FeatureCollection")
            routesGeoJson5.put("generator", "JOSM")

            routesGeoJson6.put("type", "FeatureCollection")
            routesGeoJson6.put("generator", "JOSM")

            routesGeoJson7.put("type", "FeatureCollection")
            routesGeoJson7.put("generator", "JOSM")

            routesGeoJson8.put("type", "FeatureCollection")
            routesGeoJson8.put("generator", "JOSM")

            routesGeoJson9.put("type", "FeatureCollection")
            routesGeoJson9.put("generator", "JOSM")

            val buildingsFeatures = JSONArray()
            val routeFeatures = JSONArray()
            val routeFeatures2 = JSONArray()
            val routeFeatures3 = JSONArray()
            val routeFeatures4 = JSONArray()
            val routeFeatures5 = JSONArray()
            val routeFeatures6 = JSONArray()
            val routeFeatures7 = JSONArray()
            val routeFeatures8 = JSONArray()
            val routeFeatures9 = JSONArray()

            geoJson.getJSONArray("features").let { features ->
                for (i in 0 until features.length()) {
                    val feature = features.getJSONObject(i)
                    val properties = feature.optJSONObject("properties")

                    if (properties != null) {
                        properties.optString("poi_tag").let { poi_tag ->
                            // Log.i("Salida", poi_tag)
                            if (poi_tag.startsWith("Ruta04")) {
                                routeFeatures.put(feature)
                            }
                            else if (poi_tag.startsWith("Ruta45")) {
                                routeFeatures2.put(feature)
                            }
                            else if (poi_tag.startsWith("Ruta07")) {
                                routeFeatures3.put(feature)

                            }
                            else if (poi_tag.startsWith("Ruta58")) {
                                routeFeatures4.put(feature)

                            }
                            else if (poi_tag.startsWith("Ruta80")) {
                                routeFeatures5.put(feature)

                            }
                            else if (poi_tag.startsWith("Ruta133")) {
                                routeFeatures6.put(feature)

                            }
                            else if (poi_tag.startsWith("Ruta68")) {
                                routeFeatures7.put(feature)

                            }
                            else if (poi_tag.startsWith("Ruta108")) {
                                routeFeatures8.put(feature)

                            }
                            else if (poi_tag.startsWith("Ruta115")) {
                                routeFeatures9.put(feature)



                            } else {

                                buildingsFeatures.put(feature)
                            }
                        }
                    } else {
                        buildingsFeatures.put(feature)
                    }
                }
            }

            routesGeoJson.put("features", routeFeatures)
            routesGeoJson2.put("features", routeFeatures2)
            routesGeoJson3.put("features", routeFeatures3)
            routesGeoJson4.put("features", routeFeatures4)
            routesGeoJson5.put("features", routeFeatures5)
            routesGeoJson6.put("features", routeFeatures6)
            routesGeoJson7.put("features", routeFeatures7)
            routesGeoJson8.put("features", routeFeatures8)
            routesGeoJson9.put("features", routeFeatures9)
            buildingsGeoJson.put("features", buildingsFeatures)

            loadFloor()
        }
    }


    private fun loadFloor() {
        // Configuración inicial de la cámara para vista 3D
        binding.mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .zoom(18.0)
                .pitch(60.0)  // Este ángulo da el efecto de vista "desde atrás"
                .build()
        )

        binding.mapView.mapboxMap.loadStyle(
            style(style = Style.STANDARD) {
                +geoJsonSource("indoor-building") {
                    data(buildingsGeoJson.toString())
                }

                // Capa 3D para edificios usando una capa fill con traslación en Z
                +fillLayer("building-3d", "indoor-building") {
                    fillColor("#F2F2F6")
                    fillOpacity(0.8)
                    fillTranslate(listOf(0.0, -20.0))  // Simular altura
                    fillAntialias(true)
                    filter(Expression.eq(
                        Expression.get("label"),
                        Expression.literal("building")
                    ))
                }

                // Capa 3D para áreas que comienzan con A
                +fillLayer("area-3d", "indoor-building") {
                    fillColor("#F2F2F6")
                    fillOpacity(0.8)
                    fillTranslate(listOf(0.0, -15.0))  // Simular altura menor
                    fillAntialias(true)
                    filter(Expression.all(
                        Expression.has("label"),
                        Expression.match(
                            Expression.get("label"),
                            Expression.literal("^A.*"),
                            Expression.literal(true),
                            Expression.literal(false)
                        )
                    ))
                }

                // Capa de sombra para building
                +fillLayer(
                    "building-shadow-layer",
                    "indoor-building",
                ) {
                    fillColor("#000000")
                    fillOpacity(0.02)  // Muy transparente para la sombra
                    fillTranslate(listOf(3.5, 3.5))
                    filter(Expression.eq(
                        Expression.get("label"),
                        Expression.literal("building")
                    ))
                }

                // Fill layer principal para building
                +fillLayer(
                    "building-fill-layer",
                    "indoor-building",
                ) {
                    fillColor("#F2F2F6")
                    fillOpacity(0.05)  // 95% transparente
                    fillTranslate(listOf(0.5, 0.5))
                    filter(Expression.eq(
                        Expression.get("label"),
                        Expression.literal("building")
                    ))
                }

                // Capa de highlight para building
                +fillLayer(
                    "building-highlight-layer",
                    "indoor-building",
                ) {
                    fillColor("#FFFFFF")
                    fillOpacity(0.02)  // Muy transparente para el highlight
                    fillTranslate(listOf(-1.5, -1.5))
                    filter(Expression.eq(
                        Expression.get("label"),
                        Expression.literal("building")
                    ))
                }

                // Capa de sombra para otros labels
                +fillLayer(
                    "shadow-layer",
                    "indoor-building",
                ) {
                    fillColor("#000000")
                    fillOpacity(0.12)
                    fillTranslate(listOf(3.5, 3.5))
                    filter(Expression.all(
                        Expression.has("label"),
                        Expression.neq(
                            Expression.get("label"),
                            Expression.literal("building")
                        )
                    ))
                }

                // Fill layer principal para otros
                +fillLayer(
                    "fill-layer",
                    "indoor-building",
                ) {
                    fillColor("#F2F2F6")
                    fillOpacity(0.95)
                    fillTranslate(listOf(0.5, 0.5))
                    filter(Expression.all(
                        Expression.has("label"),
                        Expression.neq(
                            Expression.get("label"),
                            Expression.literal("building")
                        )
                    ))
                }

                // Capa de highlight para otros
                +fillLayer(
                    "highlight-layer",
                    "indoor-building",
                ) {
                    fillColor("#FFFFFF")
                    fillOpacity(0.1)
                    fillTranslate(listOf(-1.5, -1.5))
                    filter(Expression.all(
                        Expression.has("label"),
                        Expression.neq(
                            Expression.get("label"),
                            Expression.literal("building")
                        )
                    ))
                }

                // Bordes principales para todos excepto building
                +lineLayer(
                    "linelayer",
                    "indoor-building",
                ) {
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                    lineOpacity(0.7)
                    lineWidth(0.5)
                    lineColor("#50667f")
                    filter(Expression.neq(
                        Expression.get("label"),
                        Expression.literal("building")
                    ))
                }

                // Borde 3D para building - Sombra
                +lineLayer(
                    "building-border-shadow",
                    "indoor-building",
                ) {
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                    lineOpacity(0.3)
                    lineWidth(2.0)
                    lineColor("#000000")
                    lineTranslate(listOf(3.0, 3.0))
                    filter(Expression.eq(
                        Expression.get("label"),
                        Expression.literal("building")
                    ))
                }

                // Borde 3D para building - Línea principal
                +lineLayer(
                    "building-border-main",
                    "indoor-building",
                ) {
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                    lineOpacity(0.9)
                    lineWidth(1.5)
                    lineColor("#50667f")
                    filter(Expression.eq(
                        Expression.get("label"),
                        Expression.literal("building")
                    ))
                }

                // Borde 3D para building - Highlight
                +lineLayer(
                    "building-border-highlight",
                    "indoor-building",
                ) {
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                    lineOpacity(0.4)
                    lineWidth(1.0)
                    lineColor("#FFFFFF")
                    lineTranslate(listOf(-1.0, -1.0))
                    filter(Expression.eq(
                        Expression.get("label"),
                        Expression.literal("building")
                    ))
                }

                // Línea superior para efecto 3D general
                +lineLayer(
                    "top-line-layer",
                    "indoor-building",
                ) {
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                    lineOpacity(0.4)
                    lineWidth(1.5)
                    lineColor("#FFFFFF")
                    lineTranslate(listOf(0.0, -1.5))
                    filter(Expression.all(
                        Expression.has("label"),
                        Expression.neq(
                            Expression.get("label"),
                            Expression.literal("building")
                        )
                    ))
                }

                // Etiquetas
                +symbolLayer(
                    "indoor-building-label",
                    "indoor-building",
                ) {
                    textField("{label}")
                    textColor(Color.BLACK)
                    textOpacity(0.5)
                    textAllowOverlap(true)
                    textSize(12.0)
                }
            }
        )

        binding.mapView.mapboxMap.addOnMapClickListener { point ->
            clickFeatureInMap(point)
            true
        }
    }


    // Nueva función para actualizar la cámara cuando el usuario se mueve
    private fun updateNavigationCamera(userLocation: Point, bearing: Double) {
        binding.mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(userLocation)
                .zoom(18.0)
                .pitch(60.0)
                .bearing(bearing)  // Esto hará que el mapa rote según la dirección del usuario
                .build()
        )
    }

    /*
        private fun loadFloor() {
            binding.mapView.mapboxMap.loadStyle(
                style(style = Style.STANDARD) {
                    +geoJsonSource("indoor-building") {
                        data(buildingsGeoJson.toString())
                    }
                    +fillLayer(
                        "fill-layer",
                        "indoor-building",
                    ) {
                        fillColor("#FAFAFA")  // Instagram background color
                        fillOpacity(0.95)
                    }
                    +lineLayer(
                        "linelayer",
                        "indoor-building",
                    ) {
                        lineCap(LineCap.ROUND)
                        lineJoin(LineJoin.ROUND)
                        lineOpacity(0.8)
                        lineWidth(0.7)
                        lineColor("#C13584")  // Instagram gradient purple
                    }
                    // Añadimos una capa de línea adicional para el efecto gradiente
                    +lineLayer(
                        "linelayer-accent",
                        "indoor-building",
                    ) {
                        lineCap(LineCap.ROUND)
                        lineJoin(LineJoin.ROUND)
                        lineOpacity(0.4)
                        lineWidth(1.2)
                        lineColor("#E1306C")  // Instagram gradient pink
                    }
                    +symbolLayer(
                        "indoor-building-label",
                        "indoor-building",
                    ) {
                        textField("{label}")
                        textColor("#262626")  // Instagram text color
                        textOpacity(0.9)
                        textSize(12.0)
                        textHaloColor("#FFFFFF")
                        textHaloWidth(0.8)
                        textFont(listOf("Roboto Medium", "Arial Unicode MS Bold"))  // Instagram-like font
                    }
                },
            )

            binding.mapView.mapboxMap.addOnMapClickListener { point ->
                clickFeatureInMap(point)
                true
            }
        }
    */



    /*private fun loadFloor() {
        binding.mapView.mapboxMap.loadStyle(
            style(style = Style.STANDARD) {
                +geoJsonSource("indoor-building") {
                    data(buildingsGeoJson.toString())
                }
                +fillLayer(
                    "fill-layer",
                    "indoor-building",
                ) {
                    fillColor("#E6E4E0")
                }
                +lineLayer(
                    "linelayer",
                    "indoor-building",
                ) {
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                    lineOpacity(0.7)
                    lineWidth(0.5)
                    lineColor("#50667f")
                }
                +symbolLayer(
                    "indoor-building-label",
                    "indoor-building",
                ) {
                    textField("{label}")
                    textColor(Color.BLACK)
                    textOpacity(0.5)
                }
            },
        )

        binding.mapView.mapboxMap.addOnMapClickListener { point ->
            clickFeatureInMap(point)
            true
        }
    }*/

    /* private fun loadFloor() {
         binding.mapView.mapboxMap.loadStyle(
             style(style = Style.STANDARD) {
                 // Base GeoJSON source
                 +geoJsonSource("indoor-building") {
                     data(buildingsGeoJson.toString())
                 }

                 // Main fill layer for buildings
                 +fillLayer(
                     "fill-layer",
                     "indoor-building",
                 ) {
                     fillColor("#FFFFFF")  // White base
                     fillOpacity(0.9)
                 }

                 // Secondary fill layer for depth effect
                 +fillLayer(
                     "fill-layer-shadow",
                     "indoor-building",
                 ) {
                     fillColor("#E8E8E8")  // Light gray for shadow
                     fillOpacity(0.4)
                 }

                 // Outline layer
                 +lineLayer(
                     "linelayer",
                     "indoor-building",
                 ) {
                     lineCap(LineCap.ROUND)
                     lineJoin(LineJoin.ROUND)
                     lineOpacity(0.8)
                     lineWidth(1.0)  // Slightly thicker lines
                     lineColor("#4A90E2")  // Modern blue color
                     lineBlur(0.5)
                 }

                 // Main labels
                 +symbolLayer(
                     "indoor-building-label",
                     "indoor-building",
                 ) {
                     textField("{label}")
                     textColor("#2C3E50")  // Darker blue-gray for better readability
                     textOpacity(0.9)
                     textSize(12.0)
                     // Add text halo for better contrast
                     textHaloColor("#FFFFFF")
                     textHaloWidth(1.0)
                     textHaloBlur(1.0)
                     // Text positioning
                     textAllowOverlap(false)
                     textAnchor(TextAnchor.CENTER)
                     textJustify(TextJustify.CENTER)
                 }
             }
         )
     }
 */





    private fun loadStyleInMap() {
    }



    private fun clickFeatureInMap(point: Point) {
        // Handle clicks on the map here
        val pixel = binding.mapView.mapboxMap.pixelForCoordinate(point)
        val renderedQueryGeometry = RenderedQueryGeometry(listOf(pixel))
        val renderedQueryOptions = RenderedQueryOptions(listOf("fill-layer"), null)
        binding.mapView.mapboxMap.queryRenderedFeatures(
            renderedQueryGeometry,
            renderedQueryOptions,
        ) { features ->
            // Log.i("Features", it.toString())
            if (features.error == null) {
                if (!features.value.isNullOrEmpty()) {
                    val feature = features.value!![0]

                    val label = feature.queriedFeature.feature.getProperty("label") ?: "Unknown label"
                    labelGlobal = "$label"


                    if (label != "Unknown label") {
                        Toast.makeText(context, "$label ", Toast.LENGTH_SHORT).show()

                        addViewAnnotationToPoint(point)





                        loadRoute(label.toString(), feature, point)
                    }
                }
            }
        }
    }

    private fun loadRoute(
        label: String,
        feature: QueriedRenderedFeature,
        point: Point,
    ) {
        // if (label == "\"A4\"" || label == "\"entrance\"") {
        if (label == "\"A4\"" ) {
            val clickedFeatureId = feature.layers[0]

            if (selectedFeatureId == null) {
                loadFirstRouteElement(clickedFeatureId)
                loadMarker(point)


                /*  Handler(Looper.getMainLooper()).postDelayed({

                         // loadMarker(-100.4683509, 20.83612)
                      //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()

                      loadMarker(Point.fromLngLat(-100.4383475, 20.8362200))
                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8362200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)



                  }, 1000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8362000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8362000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 2000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8361800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8361800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 3000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8361600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8361600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 4000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8361400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8361400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 5000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8361200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8361200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 6000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8361000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8361000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 7000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8360800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8360800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 8000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8360600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8360600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 9000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8360400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8360400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 10000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8360200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8360200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 11000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8360000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8360000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 12000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8359800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8359800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 13000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8359600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8359600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 14000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8359400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8359400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 15000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8359200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8359200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 16000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8359000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8359000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 17000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8358800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8358800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 18000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8358600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8358600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 19000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8358400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8358400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 20000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8358200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8358200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 21000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8358000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8358000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 22000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8357800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8357800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 23000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8357600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8357600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 24000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8357400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8357400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 25000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8357200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8357200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 26000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8357000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8357000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 27000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8356800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8356800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 28000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8356600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8356600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 29000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8356400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8356400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 30000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8356200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8356200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 31000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8356000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8356000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 32000); // Millisecond 1000 = 1 sec



                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8355800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8355800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 33000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8355600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8355600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 34000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8355400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8355400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 35000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8355200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8355200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 36000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8355000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8355000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 37000); // Millisecond 1000 = 1 sec



                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8354800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8354800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 38000); // Millisecond 1000 = 1 sec



                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8354600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8354600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 39000); // Millisecond 1000 = 1 sec



                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8354400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8354400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 40000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8354200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8354200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 41000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8354000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8354000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 42000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8353800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8353800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 43000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8353600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8353600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 44000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8353400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8353400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 45000); // Millisecond 1000 = 1 sec



                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8353200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8353200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 46000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8353000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8353000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 47000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8352800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8352800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 48000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8352600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8352600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 49000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8352400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8352400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 50000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8352200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8352200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 51000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8352000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8352000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 52000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8351800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8351800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 53000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8351600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8351600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 54000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8351400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8351400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 55000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8351200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8351200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 56000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8351000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8351000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 57000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8350800))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8350800))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 58000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8350600))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8350600))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 59000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8350400))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8350400))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 60000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8350200))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8350200))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 61000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8350000))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8350000))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 62000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383475, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383475, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 63000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383400, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383400, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 64000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383300, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383300, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 65000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383200, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383200, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 66000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383100, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383100, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 67000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4383000, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4383000, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 68000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4382800, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4382800, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 69000); // Millisecond 1000 = 1 sec

                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4382600, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4382600, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 70000); // Millisecond 1000 = 1 sec



                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4382500, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4382500, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 71000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4382400, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4382400, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 72000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4382300, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4382300, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 73000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4382200, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4382200, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 74000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4382100, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4382100, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 75000); // Millisecond 1000 = 1 sec


                  Handler(Looper.getMainLooper()).postDelayed({

                      // loadMarker(-100.4683509, 20.83612)
                      // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                      binding.mapView.annotations.cleanup()
                      loadMarker(Point.fromLngLat(-100.4382000, 20.8349700))

                      val cameraOptions =
                          CameraOptions
                              .Builder()
                              .center(Point.fromLngLat(-100.4382000, 20.8349700))
                              .build()
                      binding.mapView.mapboxMap.setCamera(cameraOptions)


                  }, 76000); // Millisecond 1000 = 1 sec
  */


                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43880582498, 20.83624375665)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43880582498, 20.83624375665))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43880582498, 20.83624375665))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 1000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43880663729, 20.83614227951)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43880663729, 20.83614227951))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43880663729, 20.83614227951))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 2000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43880663729, 20.83596994535)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43880663729, 20.83596994535))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43880663729, 20.83596994535))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 3000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43880663729, 20.83580115384)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43880663729, 20.83580115384))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43880663729, 20.83580115384))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 4000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43880690806, 20.83563590501)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43880690806, 20.83563590501))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43880690806, 20.83563590501))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 5000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43880744959, 20.83546230496)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43880744959, 20.83546230496))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43880744959, 20.83546230496))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 6000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43880799113, 20.83529224758)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43880799113, 20.83529224758))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43880799113, 20.83529224758))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 7000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43880744959, 20.83511510427)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43880744959, 20.83511510427))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43880744959, 20.83511510427))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 8000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43880799113, 20.83499312832)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43880799113, 20.83499312832))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43880799113, 20.83499312832))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 9000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43866529633, 20.8349926222)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43866529633, 20.8349926222))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43866529633, 20.8349926222))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 10000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43850283545, 20.83499211607)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43850283545, 20.83499211607))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43850283545, 20.83499211607))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 11000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4383893836, 20.83499236914)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4383893836, 20.83499236914))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4383893836, 20.83499236914))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 12000); // Millisecond 1000 = 1 sec

















            } else {
                // Clear existing route element
                clearRouteElement(selectedFeatureId!!)
                selectedFeatureId = null
                binding.mapView.annotations.cleanup()
            }
        }


        //if (label == "\"A54\"" ) {
        if (label == "\"A45\"" ) {
            val clickedFeatureId = feature.layers[0]

            if (selectedFeatureId == null) {
                loadSecondRouteElement(clickedFeatureId)
                loadMarker(point)


                /* Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()

                     loadMarker(Point.fromLngLat(-100.4383475, 20.8362200))
                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8362200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)



                 }, 1000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8362000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8362000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 2000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8361800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8361800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 3000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8361600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8361600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 4000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8361400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8361400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 5000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8361200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8361200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 6000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8361000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8361000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 7000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8360800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8360800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 8000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8360600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8360600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 9000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8360400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8360400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 10000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8360200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8360200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 11000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8360000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8360000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 12000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8359800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8359800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 13000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8359600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8359600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 14000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8359400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8359400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 15000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8359200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8359200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 16000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8359000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8359000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 17000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8358800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8358800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 18000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8358600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8358600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 19000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8358400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8358400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 20000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8358200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8358200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 21000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8358000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8358000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 22000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8357800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8357800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 23000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8357600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8357600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 24000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8357400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8357400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 25000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8357200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8357200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 26000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8357000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8357000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 27000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8356800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8356800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 28000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8356600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8356600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 29000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8356400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8356400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 30000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8356200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8356200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 31000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8356000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8356000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 32000); // Millisecond 1000 = 1 sec



                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8355800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8355800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 33000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8355600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8355600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 34000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8355400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8355400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 35000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8355200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8355200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 36000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8355000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8355000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 37000); // Millisecond 1000 = 1 sec



                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8354800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8354800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 38000); // Millisecond 1000 = 1 sec



                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8354600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8354600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 39000); // Millisecond 1000 = 1 sec



                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8354400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8354400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 40000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8354200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8354200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 41000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8354000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8354000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 42000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8353800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8353800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 43000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8353600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8353600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 44000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8353400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8353400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 45000); // Millisecond 1000 = 1 sec



                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8353200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8353200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 46000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8353000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8353000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 47000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8352800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8352800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 48000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8352600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8352600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 49000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8352400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8352400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 50000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8352200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8352200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 51000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8352000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8352000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 52000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8351800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8351800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 53000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8351600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8351600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 54000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8351400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8351400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 55000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8351200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8351200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 56000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8351000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8351000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 57000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8350800))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8350800))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 58000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8350600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8350600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 59000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8350400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8350400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 60000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8350200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8350200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 61000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8350000))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8350000))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 62000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8349700))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8349700))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 63000); // Millisecond 1000 = 1 sec


                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8349600))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8349600))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 64000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8349400))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8349400))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 65000); // Millisecond 1000 = 1 sec

                 Handler(Looper.getMainLooper()).postDelayed({

                     // loadMarker(-100.4683509, 20.83612)
                     // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                     binding.mapView.annotations.cleanup()
                     loadMarker(Point.fromLngLat(-100.4383475, 20.8349200))

                     val cameraOptions =
                         CameraOptions
                             .Builder()
                             .center(Point.fromLngLat(-100.4383475, 20.8349200))
                             .build()
                     binding.mapView.mapboxMap.setCamera(cameraOptions)


                 }, 66000); // Millisecond 1000 = 1 sec

 */


                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43885050254, 20.83620756981)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43885050254, 20.83620756981))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43885050254, 20.83620756981))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 1000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884833639, 20.83609116208)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884833639, 20.83609116208))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884833639, 20.83609116208))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 2000); // Millisecond 1000 = 1 sec


                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884820006, 20.83603042669)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884820006, 20.83603042669))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884820006, 20.83603042669))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 3000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884806468, 20.8359652636)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884806468, 20.8359652636))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884806468, 20.8359652636))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 4000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884860621, 20.83588251276)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884860621, 20.83588251276))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884860621, 20.83588251276))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 5000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884914775, 20.8358006476)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884914775, 20.8358006476))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884914775, 20.8358006476))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 6000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4388487416, 20.83571536606)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4388487416, 20.83571536606))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4388487416, 20.83571536606))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 7000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884928313, 20.83563717019)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884928313, 20.83563717019))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884928313, 20.83563717019))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 6000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884968928, 20.83556023958)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884968928, 20.83556023958))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884968928, 20.83556023958))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 9000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884982467, 20.83548773751)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884982467, 20.83548773751))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884982467, 20.83548773751))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 10000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884982467, 20.83540827621)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884982467, 20.83540827621))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884982467, 20.83540827621))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 11000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884982467, 20.83533602712)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884982467, 20.83533602712))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884982467, 20.83533602712))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 12000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4388503662, 20.83525466778)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4388503662, 20.83525466778))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4388503662, 20.83525466778))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 13000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4388495539, 20.83517381452)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4388495539, 20.83517381452))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4388495539, 20.83517381452))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 14000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43885077249, 20.83504854894)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43885077249, 20.83504854894))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43885077249, 20.83504854894))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 15000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43885023096, 20.83488810746)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43885023096, 20.83488810746))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43885023096, 20.83488810746))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 16000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884968942, 20.83472336375)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884968942, 20.83472336375))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884968942, 20.83472336375))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 17000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43885050172, 20.83455811373)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43885050172, 20.83455811373))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43885050172, 20.83455811373))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 18000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43884996019, 20.83436401431)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43884996019, 20.83436401431))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43884996019, 20.83436401431))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 19000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43878037278, 20.83436350819)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43878037278, 20.83436350819))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43878037278, 20.83436350819))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 20000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43870591154, 20.83436325512)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43870591154, 20.83436325512))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43870591154, 20.83436325512))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 21000); // Millisecond 1000 = 1 sec



















            } else {
                // Clear existing route element
                clearRouteElement(selectedFeatureId!!)
                selectedFeatureId = null
                binding.mapView.annotations.cleanup()
            }
        }



        //if (label == "\"A115\"" ) {
        if (label == "\"A7\"" ) {
            val clickedFeatureId = feature.layers[0]

            if (selectedFeatureId == null) {
                loadThirdRouteElement(clickedFeatureId)
                loadMarker(point)


                /*
                                Handler(Looper.getMainLooper()).postDelayed({

                                    // loadMarker(-100.4683509, 20.83612)
                                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                                    binding.mapView.annotations.cleanup()

                                    loadMarker(Point.fromLngLat(-100.43824300819, 20.83621551594))
                                    val cameraOptions =
                                        CameraOptions
                                            .Builder()
                                            .center(Point.fromLngLat(-100.43824300819, 20.83621551594))
                                            .build()
                                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                                }, 1000); // Millisecond 1000 = 1 sec

                                Handler(Looper.getMainLooper()).postDelayed({

                                    // loadMarker(-100.4683509, 20.83612)
                                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                                    binding.mapView.annotations.cleanup()
                                    loadMarker(Point.fromLngLat(-100.43824089006, 20.83619526674))

                                    val cameraOptions =
                                        CameraOptions
                                            .Builder()
                                            .center(Point.fromLngLat(-100.43824089006, 20.83619526674))
                                            .build()
                                    binding.mapView.mapboxMap.setCamera(cameraOptions)


                                }, 2000); // Millisecond 1000 = 1 sec

                                Handler(Looper.getMainLooper()).postDelayed({

                                    // loadMarker(-100.4683509, 20.83612)
                                    // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                                    binding.mapView.annotations.cleanup()
                                    loadMarker(Point.fromLngLat(-100.43823823727, 20.83616990622))

                                    val cameraOptions =
                                        CameraOptions
                                            .Builder()
                                            .center(Point.fromLngLat(-100.43823823727, 20.83616990622))
                                            .build()
                                    binding.mapView.mapboxMap.setCamera(cameraOptions)


                                }, 3000); // Millisecond 1000 = 1 sec



                                Handler(Looper.getMainLooper()).postDelayed({

                                    // loadMarker(-100.4683509, 20.83612)
                                    // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                                    binding.mapView.annotations.cleanup()
                                    loadMarker(Point.fromLngLat(-100.43823551037, 20.83614383711))

                                    val cameraOptions =
                                        CameraOptions
                                            .Builder()
                                            .center(Point.fromLngLat(-100.43823551037, 20.83614383711))
                                            .build()
                                    binding.mapView.mapboxMap.setCamera(cameraOptions)


                                }, 4000); // Millisecond 1000 = 1 sec



                                Handler(Looper.getMainLooper()).postDelayed({

                                    // loadMarker(-100.4683509, 20.83612)
                                    // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                                    binding.mapView.annotations.cleanup()
                                    loadMarker(Point.fromLngLat(-100.43823244831, 20.83611456397))

                                    val cameraOptions =
                                        CameraOptions
                                            .Builder()
                                            .center(Point.fromLngLat(-100.43823244831, 20.83611456397))
                                            .build()
                                    binding.mapView.mapboxMap.setCamera(cameraOptions)


                                }, 5000); // Millisecond 1000 = 1 sec


                                Handler(Looper.getMainLooper()).postDelayed({

                                    // loadMarker(-100.4683509, 20.83612)
                                    // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                                    binding.mapView.annotations.cleanup()
                                    loadMarker(Point.fromLngLat(-100.43823095947, 20.83610033071))

                                    val cameraOptions =
                                        CameraOptions
                                            .Builder()
                                            .center(Point.fromLngLat(-100.43823095947, 20.83610033071))
                                            .build()
                                    binding.mapView.mapboxMap.setCamera(cameraOptions)


                                }, 6000); // Millisecond 1000 = 1 sec



                                Handler(Looper.getMainLooper()).postDelayed({

                                    // loadMarker(-100.4683509, 20.83612)
                                    // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                                    binding.mapView.annotations.cleanup()
                                    loadMarker(Point.fromLngLat(-100.4382285267, 20.83607707351))

                                    val cameraOptions =
                                        CameraOptions
                                            .Builder()
                                            .center(Point.fromLngLat(-100.4382285267, 20.83607707351))
                                            .build()
                                    binding.mapView.mapboxMap.setCamera(cameraOptions)


                                }, 7000); // Millisecond 1000 = 1 sec

                                Handler(Looper.getMainLooper()).postDelayed({

                                    // loadMarker(-100.4683509, 20.83612)
                                    // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                                    binding.mapView.annotations.cleanup()
                                    loadMarker(Point.fromLngLat(-100.43822663884, 20.83605902565))

                                    val cameraOptions =
                                        CameraOptions
                                            .Builder()
                                            .center(Point.fromLngLat(-100.43822663884, 20.83605902565))
                                            .build()
                                    binding.mapView.mapboxMap.setCamera(cameraOptions)


                                }, 8000); // Millisecond 1000 = 1 sec

                                Handler(Looper.getMainLooper()).postDelayed({

                                    // loadMarker(-100.4683509, 20.83612)
                                    // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                                    binding.mapView.annotations.cleanup()
                                    loadMarker(Point.fromLngLat(-100.43822334652, 20.83602755113))

                                    val cameraOptions =
                                        CameraOptions
                                            .Builder()
                                            .center(Point.fromLngLat(-100.43822334652, 20.83602755113))
                                            .build()
                                    binding.mapView.mapboxMap.setCamera(cameraOptions)


                                }, 9000); // Millisecond 1000 = 1 sec

                                Handler(Looper.getMainLooper()).postDelayed({

                                    // loadMarker(-100.4683509, 20.83612)
                                    // Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                                    binding.mapView.annotations.cleanup()
                                    loadMarker(Point.fromLngLat(-100.43822111326, 20.83600620123))

                                    val cameraOptions =
                                        CameraOptions
                                            .Builder()
                                            .center(Point.fromLngLat(-100.43822111326, 20.83600620123))
                                            .build()
                                    binding.mapView.mapboxMap.setCamera(cameraOptions)


                                }, 10000); // Millisecond 1000 = 1 sec
                */


                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876331425, 20.83624666672)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876331425, 20.83624666672))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876331425, 20.83624666672))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 1000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876412656, 20.83618833635)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876412656, 20.83618833635))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876412656, 20.83618833635))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 2000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876426194, 20.83610773665)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876426194, 20.83610773665))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876426194, 20.83610773665))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 3000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876480348, 20.83602435324)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876480348, 20.83602435324))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876480348, 20.83602435324))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 4000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876493886, 20.83593818612)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876493886, 20.83593818612))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876493886, 20.83593818612))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 5000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876534501, 20.83585163935)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876534501, 20.83585163935))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876534501, 20.83585163935))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 6000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876588655, 20.83576673744)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876588655, 20.83576673744))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876588655, 20.83576673744))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 7000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876602193, 20.83567981098)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876602193, 20.83567981098))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876602193, 20.83567981098))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 8000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876642809, 20.83559364366)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876642809, 20.83559364366))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876642809, 20.83559364366))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 9000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876656347, 20.83550684364)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876656347, 20.83550684364))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876656347, 20.83550684364))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 10000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876696962, 20.83541979051)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876696962, 20.83541979051))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876696962, 20.83541979051))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 11000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43876724039, 20.83540055783)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43876724039, 20.83540055783))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43876724039, 20.83540055783))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 12000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865961006, 20.83540017824)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865961006, 20.83540017824))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865961006, 20.83540017824))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 13000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43853126597, 20.83539979865)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43853126597, 20.83539979865))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43853126597, 20.83539979865))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 14000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43840454648, 20.83539979865)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43840454648, 20.83539979865))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43840454648, 20.83539979865))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 15000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43825630093, 20.8353987864)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43825630093, 20.8353987864))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43825630093, 20.8353987864))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 16000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43811428305, 20.83539916599)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43811428305, 20.83539916599))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43811428305, 20.83539916599))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 17000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43802993878, 20.83539903946)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43802993878, 20.83539903946))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43802993878, 20.83539903946))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 18000); // Millisecond 1000 = 1 sec











            } else {
                // Clear existing route element
                clearRouteElement(selectedFeatureId!!)
                selectedFeatureId = null
                binding.mapView.annotations.cleanup()
            }
        }


        //if (label == "\"A115\"" ) {
        if (label == "\"A58\"" ) {
            val clickedFeatureId = feature.layers[0]

            if (selectedFeatureId == null) {
                loadFourthRouteElement(clickedFeatureId)
                loadMarker(point)

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43872845279, 20.83625267683)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43872845279, 20.83625267683))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43872845279, 20.83625267683))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 1000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43872892663, 20.83621123823)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43872892663, 20.83621123823))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43872892663, 20.83621123823))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 2000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43872933278, 20.8361718241)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43872933278, 20.8361718241))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43872933278, 20.8361718241))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 3000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43872926509, 20.83612949977)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43872926509, 20.83612949977))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43872926509, 20.83612949977))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 4000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43872980663, 20.83608692237)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43872980663, 20.83608692237))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43872980663, 20.83608692237))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 5000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873028047, 20.83604415516)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873028047, 20.83604415516))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873028047, 20.83604415516))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 6000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873048355, 20.83600233691)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873048355, 20.83600233691))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873048355, 20.83600233691))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 7000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873061893, 20.83596013906)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873061893, 20.83596013906))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873061893, 20.83596013906))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 8000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873068662, 20.83591749834)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873068662, 20.83591749834))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873068662, 20.83591749834))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 9000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4387308897, 20.83587492088)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4387308897, 20.83587492088))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4387308897, 20.83587492088))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 10000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873129585, 20.83583190055)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873129585, 20.83583190055))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873129585, 20.83583190055))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 11000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4387317697, 20.83578976591)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4387317697, 20.83578976591))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4387317697, 20.83578976591))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 12000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873210816, 20.83574725168)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873210816, 20.83574725168))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873210816, 20.83574725168))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 13000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.438732582, 20.83570505376)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.438732582, 20.83570505376))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.438732582, 20.83570505376))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 14000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873264969, 20.83566209664)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873264969, 20.83566209664))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873264969, 20.83566209664))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 15000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873285277, 20.83562002523)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873285277, 20.83562002523))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873285277, 20.83562002523))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 16000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873332661, 20.83557801707)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873332661, 20.83557801707))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873332661, 20.83557801707))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 17000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873339431, 20.83553657828)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873339431, 20.83553657828))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873339431, 20.83553657828))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 18000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873407123, 20.83549469663)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873407123, 20.83549469663))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873407123, 20.83549469663))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 19000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43873407123, 20.83548432111)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43873407123, 20.83548432111))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43873407123, 20.83548432111))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 20000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43869122217, 20.83548438438)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43869122217, 20.83548438438))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43869122217, 20.83548438438))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 21000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43863889623, 20.83548470071)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43863889623, 20.83548470071))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43863889623, 20.83548470071))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 22000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4385762811, 20.83548444764)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4385762811, 20.83548444764))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4385762811, 20.83548444764))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 23000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43851691519, 20.83548400479)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43851691519, 20.83548400479))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43851691519, 20.83548400479))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 24000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43845443544, 20.83548394152)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43845443544, 20.83548394152))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43845443544, 20.83548394152))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 25000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43838755572, 20.83548362519)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43838755572, 20.83548362519))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43838755572, 20.83548362519))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 26000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43831424524, 20.83548330887)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43831424524, 20.83548330887))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43831424524, 20.83548330887))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 27000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43825447318, 20.83548299254)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43825447318, 20.83548299254))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43825447318, 20.83548299254))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 28000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43818915037, 20.83548273948)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43818915037, 20.83548273948))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43818915037, 20.83548273948))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 29000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4381149599, 20.83548235989)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4381149599, 20.83548235989))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4381149599, 20.83548235989))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 30000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43804435711, 20.83548217009)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43804435711, 20.83548217009))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43804435711, 20.83548217009))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 31000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43797517586, 20.83548210683)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43797517586, 20.83548210683))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43797517586, 20.83548210683))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 32000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43791344072, 20.83548166397)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43791344072, 20.83548166397))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43791344072, 20.83548166397))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 33000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43789624695, 20.8354816007)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43789624695, 20.8354816007))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43789624695, 20.8354816007))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 34000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43789705925, 20.83545648436)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43789705925, 20.83545648436))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43789705925, 20.83545648436))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 35000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4378974654, 20.83542213126)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4378974654, 20.83542213126))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4378974654, 20.83542213126))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 36000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4378983454, 20.83538366591)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4378983454, 20.83538366591))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4378983454, 20.83538366591))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 37000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43789875155, 20.83534285972)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43789875155, 20.83534285972))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43789875155, 20.83534285972))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 38000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43789868386, 20.83530199026)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43789868386, 20.83530199026))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43789868386, 20.83530199026))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 39000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43789881925, 20.83526137385)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43789881925, 20.83526137385))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43789881925, 20.83526137385))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 40000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43789888694, 20.83521987172)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43789888694, 20.83521987172))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43789888694, 20.83521987172))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 41000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43789963155, 20.83517995121)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43789963155, 20.83517995121))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43789963155, 20.83517995121))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 42000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790017309, 20.8351411062)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790017309, 20.8351411062))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790017309, 20.8351411062))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 43000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790037616, 20.83510510813)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790037616, 20.83510510813))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790037616, 20.83510510813))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 44000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790051155, 20.8350644284)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790051155, 20.8350644284))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790051155, 20.8350644284))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 45000); // Millisecond 1000 = 1 sec


                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790166231, 20.83498414442)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790166231, 20.83498414442))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790166231, 20.83498414442))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 46000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790227154, 20.83494270548)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790227154, 20.83494270548))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790227154, 20.83494270548))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 47000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790301615, 20.83490246856)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790301615, 20.83490246856))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790301615, 20.83490246856))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 48000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790308384, 20.83486147245)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790308384, 20.83486147245))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790308384, 20.83486147245))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 49000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790362538, 20.83481984367)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790362538, 20.83481984367))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790362538, 20.83481984367))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 50000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790416692, 20.83477891081)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790416692, 20.83477891081))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790416692, 20.83477891081))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 51000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790443768, 20.83473639629)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790443768, 20.83473639629))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790443768, 20.83473639629))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 52000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790477614, 20.83469445114)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790477614, 20.83469445114))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790477614, 20.83469445114))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 53000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790558845, 20.83465168354)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790558845, 20.83465168354))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790558845, 20.83465168354))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 54000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790612998, 20.8346100547)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790612998, 20.8346100547))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790612998, 20.8346100547))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 55000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790633306, 20.83456817279)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790633306, 20.83456817279))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790633306, 20.83456817279))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 56000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4379068746, 20.83452717659)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4379068746, 20.83452717659))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4379068746, 20.83452717659))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 57000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790700998, 20.83448523139)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790700998, 20.83448523139))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790700998, 20.83448523139))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 58000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790734844, 20.83444328618)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790734844, 20.83444328618))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790734844, 20.83444328618))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 59000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790721306, 20.83440039197)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790721306, 20.83440039197))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790721306, 20.83440039197))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 60000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790707767, 20.83436047124)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790707767, 20.83436047124))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790707767, 20.83436047124))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 61000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4379076869, 20.83432080356)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4379076869, 20.83432080356))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4379076869, 20.83432080356))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 62000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790788998, 20.83428107261)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790788998, 20.83428107261))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790788998, 20.83428107261))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 63000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790795767, 20.83423817835)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790795767, 20.83423817835))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790795767, 20.83423817835))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 64000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790809305, 20.83419642287)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790809305, 20.83419642287))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790809305, 20.83419642287))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 65000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790836382, 20.83415643882)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790836382, 20.83415643882))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790836382, 20.83415643882))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 66000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790816075, 20.8341213895)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790816075, 20.8341213895))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790816075, 20.8341213895))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 67000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790904074, 20.83408108911)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790904074, 20.83408108911))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790904074, 20.83408108911))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 68000); // Millisecond 1000 = 1 sec



            } else {
                // Clear existing route element
                clearRouteElement(selectedFeatureId!!)
                selectedFeatureId = null
                binding.mapView.annotations.cleanup()
            }
        }


        if (label == "\"A80\"" ) {
            val clickedFeatureId = feature.layers[0]

            if (selectedFeatureId == null) {
                loadFifthRouteElement(clickedFeatureId)
                loadMarker(point)


                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43869555446, 20.83625229724)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43869555446, 20.83625229724))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43869555446, 20.83625229724))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 1000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.438696096, 20.83621370557)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.438696096, 20.83621370557))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.438696096, 20.83621370557))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 2000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43869656984, 20.83617669551)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43869656984, 20.83617669551))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43869656984, 20.83617669551))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 3000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43869697599, 20.83613557322)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43869697599, 20.83613557322))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43869697599, 20.83613557322))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 4000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43869731445, 20.83609818356)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43869731445, 20.83609818356))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43869731445, 20.83609818356))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 5000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43869792368, 20.83605636532)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43869792368, 20.83605636532))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43869792368, 20.83605636532))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 6000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43869846522, 20.83601480014)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43869846522, 20.83601480014))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43869846522, 20.83601480014))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 7000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4386994806, 20.83597374107)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4386994806, 20.83597374107))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4386994806, 20.83597374107))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 8000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43870015752, 20.8359333779)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43870015752, 20.8359333779))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43870015752, 20.8359333779))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 9000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43870042829, 20.83589206574)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43870042829, 20.83589206574))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43870042829, 20.83589206574))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 10000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43870103751, 20.83585265153)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43870103751, 20.83585265153))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43870103751, 20.83585265153))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 11000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43870090213, 20.83581374343)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43870090213, 20.83581374343))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43870090213, 20.83581374343))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 12000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43870184982, 20.83577439246)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43870184982, 20.83577439246))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43870184982, 20.83577439246))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 13000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43870272982, 20.83573649659)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43870272982, 20.83573649659))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43870272982, 20.83573649659))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 14000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43870333904, 20.83569315989)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43870333904, 20.83569315989))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43870333904, 20.83569315989))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 15000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43870388058, 20.83565108848)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43870388058, 20.83565108848))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43870388058, 20.83565108848))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 16000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4387036775, 20.83560889054)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4387036775, 20.83560889054))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4387036775, 20.83560889054))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 17000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43870415135, 20.8355671987)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43870415135, 20.8355671987))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43870415135, 20.8355671987))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 18000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4387045575, 20.8355265191)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4387045575, 20.8355265191))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4387045575, 20.8355265191))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 19000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43870476058, 20.83550829868)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43870476058, 20.83550829868))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43870476058, 20.83550829868))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 20000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43866103152, 20.83550798235)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43866103152, 20.83550798235))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43866103152, 20.83550798235))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 21000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43860166561, 20.83550798235)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43860166561, 20.83550798235))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43860166561, 20.83550798235))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 22000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43853356743, 20.83550779256)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43853356743, 20.83550779256))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43853356743, 20.83550779256))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 23000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43846208464, 20.83550741296)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43846208464, 20.83550741296))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43846208464, 20.83550741296))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 24000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43838660803, 20.83550728643)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43838660803, 20.83550728643))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43838660803, 20.83550728643))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 25000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43831668216, 20.8355071599)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43831668216, 20.8355071599))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43831668216, 20.8355071599))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 26000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43827153157, 20.83550671705)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43827153157, 20.83550671705))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43827153157, 20.83550671705))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 27000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43820796875, 20.83550614766)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43820796875, 20.83550614766))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43820796875, 20.83550614766))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 28000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4381328306, 20.83550564153)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4381328306, 20.83550564153))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4381328306, 20.83550564153))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 29000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43805586476, 20.83550500888)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43805586476, 20.83550500888))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43805586476, 20.83550500888))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 30000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43798004968, 20.8355042497)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43798004968, 20.8355042497))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43798004968, 20.8355042497))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 31000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43790193308, 20.83550361704)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43790193308, 20.83550361704))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43790193308, 20.83550361704))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 32000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43786192709, 20.83550368031)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43786192709, 20.83550368031))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43786192709, 20.83550368031))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 33000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43786192709, 20.83547786805)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43786192709, 20.83547786805))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43786192709, 20.83547786805))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 34000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43786219786, 20.83543541699)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43786219786, 20.83543541699))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43786219786, 20.83543541699))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 35000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43786240093, 20.83539321898)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43786240093, 20.83539321898))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43786240093, 20.83539321898))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 36000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43786240093, 20.83535019851)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43786240093, 20.83535019851))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43786240093, 20.83535019851))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 37000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43786226555, 20.83530717803)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43786226555, 20.83530717803))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43786226555, 20.83530717803))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 38000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4378618594, 20.83526637182)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4378618594, 20.83526637182))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4378618594, 20.83526637182))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 39000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43786192709, 20.83522518602)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43786192709, 20.83522518602))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43786192709, 20.83522518602))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 40000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43786138555, 20.83518646755)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43786138555, 20.83518646755))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43786138555, 20.83518646755))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 41000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43786091171, 20.8351643879)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43786091171, 20.8351643879))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43786091171, 20.8351643879))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 42000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43782077033, 20.83516445116)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43782077033, 20.83516445116))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43782077033, 20.83516445116))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 43000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43775720751, 20.83516432463)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43775720751, 20.83516432463))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43775720751, 20.83516432463))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 44000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43768213705, 20.83516388177)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43768213705, 20.83516388177))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43768213705, 20.83516388177))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 45000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43760821735, 20.8351641981)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43760821735, 20.8351641981))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43760821735, 20.8351641981))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 46000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43753070998, 20.8351640083)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43753070998, 20.8351640083))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43753070998, 20.8351640083))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 47000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4374599718, 20.83516381851)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4374599718, 20.83516381851))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4374599718, 20.83516381851))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 48000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4373878121, 20.8351640083)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4373878121, 20.8351640083))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4373878121, 20.8351640083))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 49000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43734069844, 20.8351640083)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43734069844, 20.8351640083))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43734069844, 20.8351640083))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 50000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43734049537, 20.83511845715)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43734049537, 20.83511845715))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43734049537, 20.83511845715))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 51000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43734015738, 20.83504905532)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43734015738, 20.83504905532))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43734015738, 20.83504905532))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 52000); // Millisecond 1000 = 1 sec



            } else {
                // Clear existing route element
                clearRouteElement(selectedFeatureId!!)
                selectedFeatureId = null
                binding.mapView.annotations.cleanup()
            }
        }

        if (label == "\"A133\"" ) {
            val clickedFeatureId = feature.layers[0]

            if (selectedFeatureId == null) {
                loadSixthRouteElement(clickedFeatureId)
                loadMarker(point)


                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43864661319, 20.83625160139)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43864661319, 20.83625160139))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43864661319, 20.83625160139))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 1000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43864725616, 20.83620709434)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43864725616, 20.83620709434))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43864725616, 20.83620709434))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 2000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43864789927, 20.83618751384)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43864789927, 20.83618751384))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43864789927, 20.83618751384))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 3000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43864864388, 20.83614601196)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43864864388, 20.83614601196))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43864864388, 20.83614601196))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 4000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43864898234, 20.83610273864)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43864898234, 20.83610273864))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43864898234, 20.83610273864))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 5000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43864911773, 20.83606199592)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43864911773, 20.83606199592))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43864911773, 20.83606199592))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 6000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43864938849, 20.8360221389)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43864938849, 20.8360221389))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43864938849, 20.8360221389))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 7000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865013311, 20.83598209207)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865013311, 20.83598209207))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865013311, 20.83598209207))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 8000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865121618, 20.83594489216)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865121618, 20.83594489216))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865121618, 20.83594489216))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 9000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865223156, 20.83590364327)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865223156, 20.83590364327))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865223156, 20.83590364327))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 10000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865311156, 20.83586378621)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865311156, 20.83586378621))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865311156, 20.83586378621))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 11000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865426232, 20.83582418219)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865426232, 20.83582418219))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865426232, 20.83582418219))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 12000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865487155, 20.83578071899)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865487155, 20.83578071899))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865487155, 20.83578071899))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 13000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865575155, 20.83573826802)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865575155, 20.83573826802))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865575155, 20.83573826802))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 14000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865642847, 20.83569581703)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865642847, 20.83569581703))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865642847, 20.83569581703))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 15000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865703769, 20.83565716195)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865703769, 20.83565716195))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865703769, 20.83565716195))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 16000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865717308, 20.83561376196)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865717308, 20.83561376196))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865717308, 20.83561376196))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 17000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43865744391, 20.83556036611)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43865744391, 20.83556036611))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43865744391, 20.83556036611))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 18000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43859002265, 20.83555998652)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43859002265, 20.83555998652))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43859002265, 20.83555998652))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 19000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43848753691, 20.83555985999)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43848753691, 20.83555985999))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43848753691, 20.83555985999))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 20000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43836393126, 20.83556036611)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43836393126, 20.83556036611))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43836393126, 20.83556036611))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 21000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43825075019, 20.83555922733)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43825075019, 20.83555922733))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43825075019, 20.83555922733))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 22000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43810913846, 20.83555859468)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43810913846, 20.83555859468))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43810913846, 20.83555859468))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 23000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43796847441, 20.8355574559)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43796847441, 20.8355574559))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43796847441, 20.8355574559))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 24000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43787316403, 20.83555808856)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43787316403, 20.83555808856))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43787316403, 20.83555808856))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 25000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43787235173, 20.83561312937)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43787235173, 20.83561312937))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43787235173, 20.83561312937))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 26000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43779396449, 20.83561211726)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43779396449, 20.83561211726))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43779396449, 20.83561211726))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 27000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43770352794, 20.83561161113)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43770352794, 20.83561161113))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43770352794, 20.83561161113))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 28000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43762987901, 20.83561135807)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43762987901, 20.83561135807))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43762987901, 20.83561135807))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 29000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43754323321, 20.83560983971)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43754323321, 20.83560983971))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43754323321, 20.83560983971))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 30000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43742409523, 20.83560832134)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43742409523, 20.83560832134))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43742409523, 20.83560832134))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 31000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4373266187, 20.83560730909)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4373266187, 20.83560730909))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4373266187, 20.83560730909))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 32000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43723618215, 20.83560705603)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43723618215, 20.83560705603))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43723618215, 20.83560705603))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 33000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43715955477, 20.83560553767)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43715955477, 20.83560553767))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43715955477, 20.83560553767))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 34000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43716280399, 20.83554530909)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43716280399, 20.83554530909))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43716280399, 20.83554530909))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 35000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43716551167, 20.83544788046)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43716551167, 20.83544788046))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43716551167, 20.83544788046))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 36000); // Millisecond 1000 = 1 sec




            } else {
                // Clear existing route element
                clearRouteElement(selectedFeatureId!!)
                selectedFeatureId = null
                binding.mapView.annotations.cleanup()
            }
        }



        if (label == "\"A68\"" ) {
            val clickedFeatureId = feature.layers[0]

            if (selectedFeatureId == null) {
                loadSeventhRouteElement(clickedFeatureId)
                loadMarker(point)



                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43857702578, 20.83624805855)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43857702578, 20.83624805855))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43857702578, 20.83624805855))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 1000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4385777027, 20.83618605881)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4385777027, 20.83618605881))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4385777027, 20.83618605881))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 2000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43857919206, 20.8361200102)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43857919206, 20.8361200102))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43857919206, 20.8361200102))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 3000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43858027513, 20.83604662269)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43858027513, 20.83604662269))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43858027513, 20.83604662269))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 4000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43858108744, 20.83598664735)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43858108744, 20.83598664735))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43858108744, 20.83598664735))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 5000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43858217051, 20.83590769243)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43858217051, 20.83590769243))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43858217051, 20.83590769243))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 6000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43858244128, 20.83586998638)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43858244128, 20.83586998638))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43858244128, 20.83586998638))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 7000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43858325358, 20.83580646812)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43858325358, 20.83580646812))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43858325358, 20.83580646812))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 8000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43858514896, 20.83574421514)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43858514896, 20.83574421514))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43858514896, 20.83574421514))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 9000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4385856905, 20.83568449274)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4385856905, 20.83568449274))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4385856905, 20.83568449274))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 10000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4385873151, 20.83562451725)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4385873151, 20.83562451725))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4385873151, 20.83562451725))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 11000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43850310622, 20.83562325195)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43850310622, 20.83562325195))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43850310622, 20.83562325195))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 12000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43842052194, 20.83562274583)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43842052194, 20.83562274583))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43842052194, 20.83562274583))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 13000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43832710693, 20.8356222397)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43832710693, 20.8356222397))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43832710693, 20.8356222397))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 14000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43823504577, 20.83562148052)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43823504577, 20.83562148052))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43823504577, 20.83562148052))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 15000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43815110765, 20.83562046828)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43815110765, 20.83562046828))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43815110765, 20.83562046828))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 16000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43809045559, 20.83562046828)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43809045559, 20.83562046828))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43809045559, 20.83562046828))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 17000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43800678824, 20.83561996215)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43800678824, 20.83561996215))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43800678824, 20.83561996215))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 18000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43792989009, 20.83561996215)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43792989009, 20.83561996215))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43792989009, 20.83561996215))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 19000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43792772395, 20.83567993764)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43792772395, 20.83567993764))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43792772395, 20.83567993764))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 20000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43783918277, 20.83567943152)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43783918277, 20.83567943152))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43783918277, 20.83567943152))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 21000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43775118313, 20.83567867233)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43775118313, 20.83567867233))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43775118313, 20.83567867233))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 22000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43766778654, 20.83567791315)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43766778654, 20.83567791315))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43766778654, 20.83567791315))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 23000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43757220539, 20.83567740703)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43757220539, 20.83567740703))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43757220539, 20.83567740703))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 24000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43748095654, 20.83567639478)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43748095654, 20.83567639478))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43748095654, 20.83567639478))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 25000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43739593534, 20.8356756356)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43739593534, 20.8356756356))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43739593534, 20.8356756356))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 26000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43728600348, 20.83567487642)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43728600348, 20.83567487642))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43728600348, 20.83567487642))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 27000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43718933926, 20.83567386417)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43718933926, 20.83567386417))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43718933926, 20.83567386417))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 28000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43706397362, 20.83567335805)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43706397362, 20.83567335805))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43706397362, 20.83567335805))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 29000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43695999866, 20.83567259887)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43695999866, 20.83567259887))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43695999866, 20.83567259887))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 30000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43684302683, 20.83567234581)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43684302683, 20.83567234581))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43684302683, 20.83567234581))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 31000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4367395934, 20.83567133356)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4367395934, 20.83567133356))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4367395934, 20.83567133356))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 32000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43666431986, 20.83567057438)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43666431986, 20.83567057438))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43666431986, 20.83567057438))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 33000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4365663018, 20.8356698152)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4365663018, 20.8356698152))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4365663018, 20.8356698152))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 34000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43656900948, 20.83559693358)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43656900948, 20.83559693358))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43656900948, 20.83559693358))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 35000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43656982179, 20.83553012541)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43656982179, 20.83553012541))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43656982179, 20.83553012541))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 36000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43657090486, 20.83547293354)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43657090486, 20.83547293354))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43657090486, 20.83547293354))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 37000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43657252947, 20.83539625591)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43657252947, 20.83539625591))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43657252947, 20.83539625591))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 38000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43657252947, 20.83531173333)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43657252947, 20.83531173333))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43657252947, 20.83531173333))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 39000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43657280024, 20.83523404337)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43657280024, 20.83523404337))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43657280024, 20.83523404337))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 40000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.436573071, 20.83517179015)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.436573071, 20.83517179015))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.436573071, 20.83517179015))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 42000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43657496638, 20.83510903078)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43657496638, 20.83510903078))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43657496638, 20.83510903078))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 43000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43657713253, 20.83505107956)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43657713253, 20.83505107956))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43657713253, 20.83505107956))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 44000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43657794483, 20.83497895684)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43657794483, 20.83497895684))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43657794483, 20.83497895684))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 45000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43657875714, 20.83491999332)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43657875714, 20.83491999332))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43657875714, 20.83491999332))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 46000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4365790279, 20.8348481236)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4365790279, 20.8348481236))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4365790279, 20.8348481236))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 47000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4365790279, 20.8347846049)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4365790279, 20.8347846049))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4365790279, 20.8347846049))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 48000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43658011098, 20.83472412294)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43658011098, 20.83472412294))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43658011098, 20.83472412294))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 49000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43658200635, 20.83465073474)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43658200635, 20.83465073474))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43658200635, 20.83465073474))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 50000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43658336019, 20.8346006283)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43658336019, 20.8346006283))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43658336019, 20.8346006283))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 51000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43658525557, 20.8345206604)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43658525557, 20.8345206604))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43658525557, 20.8345206604))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 52000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43664969838, 20.8345206604)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43664969838, 20.8345206604))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43664969838, 20.8345206604))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 53000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43670601815, 20.83452141959)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43670601815, 20.83452141959))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43670601815, 20.83452141959))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 54000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43670493508, 20.83445663545)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43670493508, 20.83445663545))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43670493508, 20.83445663545))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 55000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43670547662, 20.83439286352)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43670547662, 20.83439286352))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43670547662, 20.83439286352))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 56000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43670574739, 20.83432706707)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43670574739, 20.83432706707))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43670574739, 20.83432706707))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 57000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43670574739, 20.83426582573)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43670574739, 20.83426582573))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43670574739, 20.83426582573))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 58000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43670655969, 20.83420584968)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43670655969, 20.83420584968))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43670655969, 20.83420584968))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 59000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43670791353, 20.83413600412)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43670791353, 20.83413600412))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43670791353, 20.83413600412))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 60000); // Millisecond 1000 = 1 sec


            } else {
                // Clear existing route element
                clearRouteElement(selectedFeatureId!!)
                selectedFeatureId = null
                binding.mapView.annotations.cleanup()
            }
        }

        if (label == "\"A108\"" ) {
            val clickedFeatureId = feature.layers[0]

            if (selectedFeatureId == null) {
                loadEighthRouteElement(clickedFeatureId)
                loadMarker(point)



                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43848496462, 20.83624590754)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43848496462, 20.83624590754))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43848496462, 20.83624590754))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 1000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43848496462, 20.83619516897)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43848496462, 20.83619516897))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43848496462, 20.83619516897))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 2000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43848645384, 20.83612671617)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43848645384, 20.83612671617))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43848645384, 20.83612671617))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 3000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43848740153, 20.83605497355)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43848740153, 20.83605497355))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43848740153, 20.83605497355))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 4000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43848861999, 20.83599107578)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43848861999, 20.83599107578))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43848861999, 20.83599107578))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 5000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43848902614, 20.83592110452)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43848902614, 20.83592110452))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43848902614, 20.83592110452))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 6000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43848929691, 20.83585416996)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43848929691, 20.83585416996))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43848929691, 20.83585416996))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 7000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4384902446, 20.83579368842)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4384902446, 20.83579368842))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4384902446, 20.83579368842))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 8000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43849010921, 20.83571739055)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43849010921, 20.83571739055))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43849010921, 20.83571739055))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 9000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43849078613, 20.8356774069)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43849078613, 20.8356774069))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43849078613, 20.8356774069))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 10000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4384332479, 20.83567690078)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4384332479, 20.83567690078))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4384332479, 20.83567690078))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 11000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43838505118, 20.83567664772)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43838505118, 20.83567664772))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43838505118, 20.83567664772))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 12000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43832412835, 20.83567639466)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43832412835, 20.83567639466))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43832412835, 20.83567639466))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 13000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43826036246, 20.835675762)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43826036246, 20.835675762))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43826036246, 20.835675762))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 14000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43817804894, 20.8356744967)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43817804894, 20.8356744967))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43817804894, 20.8356744967))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 15000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4381393291, 20.83567361099)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4381393291, 20.83567361099))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4381393291, 20.83567361099))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 16000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43808652932, 20.83567272527)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43808652932, 20.83567272527))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43808652932, 20.83567272527))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 17000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43803196954, 20.83567183956)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43803196954, 20.83567183956))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43803196954, 20.83567183956))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 18000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43798796972, 20.83567183956)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43798796972, 20.83567183956))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43798796972, 20.83567183956))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 19000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43798702203, 20.8357043579)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43798702203, 20.8357043579))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43798702203, 20.8357043579))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 20000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43798634511, 20.83573459869)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43798634511, 20.83573459869))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43798634511, 20.83573459869))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 21000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43794369913, 20.83573383951)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43794369913, 20.83573383951))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43794369913, 20.83573383951))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 22000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43788765013, 20.83573168849)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43788765013, 20.83573168849))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43788765013, 20.83573168849))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 23000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43782456115, 20.83573029666)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43782456115, 20.83573029666))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43782456115, 20.83573029666))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 24000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43774414302, 20.83573017013)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43774414302, 20.83573017013))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43774414302, 20.83573017013))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 25000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43766372489, 20.83573042319)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43766372489, 20.83573042319))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43766372489, 20.83573042319))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 26000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43759941746, 20.83573080278)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43759941746, 20.83573080278))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43759941746, 20.83573080278))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 27000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43753984847, 20.83573092931)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43753984847, 20.83573092931))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43753984847, 20.83573092931))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 28000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4374835287, 20.8357313089)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4374835287, 20.8357313089))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4374835287, 20.8357313089))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 29000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43742802123, 20.83573118237)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43742802123, 20.83573118237))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43742802123, 20.83573118237))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 30000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43734611387, 20.83573118237)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43734611387, 20.83573118237))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43734611387, 20.83573118237))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 31000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43729751099, 20.8357313089)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43729751099, 20.8357313089))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43729751099, 20.8357313089))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 32000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43723157895, 20.8357313089)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43723157895, 20.8357313089))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43723157895, 20.8357313089))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 33000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43717146843, 20.83573105584)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43717146843, 20.83573105584))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43717146843, 20.83573105584))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 34000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43711108714, 20.8357313089)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43711108714, 20.8357313089))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43711108714, 20.8357313089))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 35000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4370536843, 20.83573143543)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4370536843, 20.83573143543))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4370536843, 20.83573143543))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 36000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43698815841, 20.83573156196)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43698815841, 20.83573156196))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43698815841, 20.83573156196))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 37000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4369245279, 20.83573118237)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4369245279, 20.83573118237))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4369245279, 20.83573118237))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 38000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43686035585, 20.83573118237)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43686035585, 20.83573118237))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43686035585, 20.83573118237))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 39000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43679889149, 20.83573118237)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43679889149, 20.83573118237))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43679889149, 20.83573118237))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 40000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4367341779, 20.83573168849)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4367341779, 20.83573168849))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4367341779, 20.83573168849))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 41000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4366747443, 20.83573219461)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4366747443, 20.83573219461))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4366747443, 20.83573219461))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 42000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43660718765, 20.83573244768)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43660718765, 20.83573244768))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43660718765, 20.83573244768))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 43000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43654924327, 20.83573219461)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43654924327, 20.83573219461))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43654924327, 20.83573219461))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 44000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43647816664, 20.83573232115)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43647816664, 20.83573232115))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43647816664, 20.83573232115))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 45000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43641954534, 20.83573257421)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43641954534, 20.83573257421))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43641954534, 20.83573257421))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 46000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43635429022, 20.8357329538)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43635429022, 20.8357329538))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43635429022, 20.8357329538))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 47000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43631353962, 20.8357329538)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43631353962, 20.8357329538))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43631353962, 20.8357329538))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 48000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4362729244, 20.83573333339)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4362729244, 20.83573333339))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4362729244, 20.83573333339))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 49000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627305978, 20.83569550077)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627305978, 20.83569550077))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627305978, 20.83569550077))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 50000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627333055, 20.83563577835)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627333055, 20.83563577835))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627333055, 20.83563577835))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 51000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627387209, 20.83558845591)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627387209, 20.83558845591))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627387209, 20.83558845591))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 52000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627387209, 20.83555201509)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627387209, 20.83555201509))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627387209, 20.83555201509))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 53000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627400747, 20.83551810487)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627400747, 20.83551810487))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627400747, 20.83551810487))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 54000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627400747, 20.83546584769)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627400747, 20.83546584769))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627400747, 20.83546584769))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 55000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627387209, 20.83543864359)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627387209, 20.83543864359))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627387209, 20.83543864359))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 56000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627414286, 20.83540448028)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627414286, 20.83540448028))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627414286, 20.83540448028))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 57000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627414286, 20.83537094963)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627414286, 20.83537094963))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627414286, 20.83537094963))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 58000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627441362, 20.83532944752)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627441362, 20.83532944752))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627441362, 20.83532944752))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 59000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627481978, 20.83529616991)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627481978, 20.83529616991))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627481978, 20.83529616991))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 60000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627495516, 20.83526200657)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627495516, 20.83526200657))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627495516, 20.83526200657))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 61000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627522593, 20.83522885548)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627522593, 20.83522885548))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627522593, 20.83522885548))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 62000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627495516, 20.83518823905)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627495516, 20.83518823905))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627495516, 20.83518823905))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 63000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627481978, 20.83515508793)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627481978, 20.83515508793))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627481978, 20.83515508793))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 64000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627495516, 20.83511447149)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627495516, 20.83511447149))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627495516, 20.83511447149))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 65000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4362754967, 20.83506752847)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4362754967, 20.83506752847))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4362754967, 20.83506752847))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 66000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627563208, 20.83503146712)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627563208, 20.83503146712))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627563208, 20.83503146712))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 67000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627563208, 20.83500021394)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627563208, 20.83500021394))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627563208, 20.83500021394))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 68000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43627617362, 20.8349606097)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43627617362, 20.8349606097))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43627617362, 20.8349606097))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 69000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.436276309, 20.83492113198)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.436276309, 20.83492113198))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.436276309, 20.83492113198))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 70000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.436276309, 20.83488747468)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.436276309, 20.83488747468))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.436276309, 20.83488747468))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 71000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43622811227, 20.83488747468)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43622811227, 20.83488747468))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43622811227, 20.83488747468))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 72000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4361837063, 20.8348879808)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4361837063, 20.8348879808))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4361837063, 20.8348879808))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 73000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43613036498, 20.83488785427)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43613036498, 20.83488785427))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43613036498, 20.83488785427))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 74000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43606835908, 20.83488823387)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43606835908, 20.83488823387))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43606835908, 20.83488823387))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 75000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43602950385, 20.83488899305)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43602950385, 20.83488899305))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43602950385, 20.83488899305))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 76000); // Millisecond 1000 = 1 sec



            } else {
                // Clear existing route element
                clearRouteElement(selectedFeatureId!!)
                selectedFeatureId = null
                binding.mapView.annotations.cleanup()
            }
        }

        if (label == "\"A115\"" ) {
            val clickedFeatureId = feature.layers[0]

            if (selectedFeatureId == null) {
                loadNinethRouteElement(clickedFeatureId)
                loadMarker(point)


                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43839642357, 20.83624249135)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43839642357, 20.83624249135))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43839642357, 20.83624249135))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 1000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43839831895, 20.8361976997)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43839831895, 20.8361976997))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43839831895, 20.8361976997))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 2000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43839913126, 20.83614379787)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43839913126, 20.83614379787))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43839913126, 20.83614379787))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 3000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43839967279, 20.83607521851)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43839967279, 20.83607521851))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43839967279, 20.83607521851))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 4000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43840156817, 20.83602840233)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43840156817, 20.83602840233))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43840156817, 20.83602840233))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 5000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4384012974, 20.83597829635)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4384012974, 20.83597829635))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4384012974, 20.83597829635))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 6000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43840156817, 20.83592819035)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43840156817, 20.83592819035))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43840156817, 20.83592819035))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 7000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43840292201, 20.83587302311)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43840292201, 20.83587302311))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43840292201, 20.83587302311))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 8000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43840346355, 20.83583076196)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43840346355, 20.83583076196))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43840346355, 20.83583076196))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 9000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43840508815, 20.83576876205)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43840508815, 20.83576876205))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43840508815, 20.83576876205))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 10000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43840617123, 20.83572346414)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43840617123, 20.83572346414))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43840617123, 20.83572346414))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 11000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43833956227, 20.83572219883)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43833956227, 20.83572219883))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43833956227, 20.83572219883))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 12000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43826266412, 20.83572194577)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43826266412, 20.83572194577))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43826266412, 20.83572194577))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 13000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43817222757, 20.83572093353)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43817222757, 20.83572093353))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43817222757, 20.83572093353))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 14000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43809262174, 20.83572194577)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43809262174, 20.83572194577))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43809262174, 20.83572194577))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 15000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43802844969, 20.83572093353)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43802844969, 20.83572093353))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43802844969, 20.83572093353))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 16000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43802899123, 20.83575054166)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43802899123, 20.83575054166))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43802899123, 20.83575054166))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 17000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43802899123, 20.8357723049)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43802899123, 20.8357723049))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43802899123, 20.8357723049))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 18000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43797077608, 20.83577179878)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43797077608, 20.83577179878))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43797077608, 20.83577179878))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 19000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43791581015, 20.83577179878)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43791581015, 20.83577179878))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43791581015, 20.83577179878))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 20000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43784676428, 20.83577129266)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43784676428, 20.83577129266))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43784676428, 20.83577129266))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 21000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4377769061, 20.83577129266)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4377769061, 20.83577129266))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4377769061, 20.83577129266))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 22000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43770542331, 20.83577129266)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43770542331, 20.83577129266))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43770542331, 20.83577129266))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 23000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43763123285, 20.83577205184)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43763123285, 20.83577205184))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43763123285, 20.83577205184))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 24000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4375670608, 20.8357723049)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4375670608, 20.8357723049))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4375670608, 20.8357723049))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 25000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43749205803, 20.8357723049)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43749205803, 20.8357723049))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43749205803, 20.8357723049))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 26000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43743005213, 20.83577205184)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43743005213, 20.83577205184))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43743005213, 20.83577205184))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 27000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43735992318, 20.83577255796)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43735992318, 20.83577255796))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43735992318, 20.83577255796))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 28000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43729737575, 20.8357723049)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43729737575, 20.8357723049))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43729737575, 20.8357723049))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 29000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.437234016, 20.83577255796)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.437234016, 20.83577255796))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.437234016, 20.83577255796))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 30000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43716849012, 20.83577306408)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43716849012, 20.83577306408))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43716849012, 20.83577306408))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 31000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43709375811, 20.83577255796)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43709375811, 20.83577255796))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43709375811, 20.83577255796))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 32000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43701767227, 20.83577331715)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43701767227, 20.83577331715))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43701767227, 20.83577331715))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 33000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43694998024, 20.83577357021)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43694998024, 20.83577357021))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43694998024, 20.83577357021))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 34000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43688228821, 20.83577407633)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43688228821, 20.83577407633))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43688228821, 20.83577407633))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 35000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43680376545, 20.83577382327)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43680376545, 20.83577382327))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43680376545, 20.83577382327))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 36000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43674121801, 20.83577382327)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43674121801, 20.83577382327))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43674121801, 20.83577382327))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 37000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43666513217, 20.83577483551)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43666513217, 20.83577483551))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43666513217, 20.83577483551))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 38000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43658715095, 20.83577483551)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43658715095, 20.83577483551))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43658715095, 20.83577483551))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 39000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43650050515, 20.83577584775)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43650050515, 20.83577584775))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43650050515, 20.83577584775))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 40000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4364125055, 20.83577508857)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4364125055, 20.83577508857))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4364125055, 20.83577508857))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 41000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43633723197, 20.83577610082)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43633723197, 20.83577610082))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43633723197, 20.83577610082))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 42000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.4362562723, 20.83577635388)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.4362562723, 20.83577635388))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.4362562723, 20.83577635388))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 43000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43619724484, 20.83577635388)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43619724484, 20.83577635388))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43619724484, 20.83577635388))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 44000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43620022329, 20.83572852536)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43620022329, 20.83572852536))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43620022329, 20.83572852536))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 45000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43620130637, 20.83567715397)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43620130637, 20.83567715397))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43620130637, 20.83567715397))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 46000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43620238944, 20.83562831317)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43620238944, 20.83562831317))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43620238944, 20.83562831317))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 47000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43620211867, 20.83559465603)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43620211867, 20.83559465603))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43620211867, 20.83559465603))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 48000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43620238944, 20.83555821521)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43620238944, 20.83555821521))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43620238944, 20.83555821521))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 49000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43620320174, 20.83553392133)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43620320174, 20.83553392133))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43620320174, 20.83553392133))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 50000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43613767586, 20.83553417439)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43613767586, 20.83553417439))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43613767586, 20.83553417439))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 51000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43609083297, 20.83553493357)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43609083297, 20.83553493357))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43609083297, 20.83553493357))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 52000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43601691327, 20.83553518664)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43601691327, 20.83553518664))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43601691327, 20.83553518664))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 53000); // Millisecond 1000 = 1 sec

                Handler(Looper.getMainLooper()).postDelayed({

                    // loadMarker(-100.43597007038, 20.83553645194)
                    //Toast.makeText(context, "Hola", Toast.LENGTH_SHORT).show()
                    binding.mapView.annotations.cleanup()

                    loadMarker(Point.fromLngLat(-100.43597007038, 20.83553645194))
                    val cameraOptions =
                        CameraOptions
                            .Builder()
                            .center(Point.fromLngLat(-100.43597007038, 20.83553645194))
                            .build()
                    binding.mapView.mapboxMap.setCamera(cameraOptions)



                }, 54000); // Millisecond 1000 = 1 sec





            } else {
                // Clear existing route element
                clearRouteElement(selectedFeatureId!!)
                selectedFeatureId = null
                binding.mapView.annotations.cleanup()
            }
        }





    }

    private fun loadMarker(point: Point) {
        bitmapFromDrawableRes(
            this@MainActivity,
            R.drawable.ic_action_navigation,
        )?.let {
            val annotationApi = binding.mapView.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
            // Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions =
                PointAnnotationOptions()
                    // Define a geographic coordinate.
                    .withPoint(point)
                    // Specify the bitmap you assigned to the point annotation
                    // The bitmap will be added to map style automatically.
                    .withIconImage(it)
            // Add the resulting pointAnnotation to the map.
            pointAnnotationManager.create(pointAnnotationOptions)
        }
    }

    private fun bitmapFromDrawableRes(
        context: Context,
        @DrawableRes resourceId: Int,
    ) = convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap =
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888,
                )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun loadFirstRouteElement(buildingFeatureId: String) {
        // Access the first feature in routesGeoJson
        val firstRouteFeature = routesGeoJson.optJSONArray("features")?.optJSONObject(0)

        if (firstRouteFeature != null) {
            // Add the route feature to the map with a specific source (e.g., "route-source")
            binding.mapView.mapboxMap.getStyle { style ->
                style.addSource(
                    geoJsonSource("route-source-$index") {
                        data(firstRouteFeature.toString())
                    },
                )
                style.addLayer(
                    lineLayer(
                        "route-layer-$index",
                        "route-source-$index",
                    ) {
                        lineWidth(2.0)
                        lineColor(Color.RED)
                    },
                )
            }
            selectedFeatureId = buildingFeatureId // Remember the clicked building ID
        }
    }

    private fun loadSecondRouteElement(buildingFeatureId: String) {
        // Access the first feature in routesGeoJson
        val firstRouteFeature = routesGeoJson2.optJSONArray("features")?.optJSONObject(0)

        if (firstRouteFeature != null) {
            // Add the route feature to the map with a specific source (e.g., "route-source")
            binding.mapView.mapboxMap.getStyle { style ->
                style.addSource(
                    geoJsonSource("route-source-$index") {
                        data(firstRouteFeature.toString())
                    },
                )
                style.addLayer(
                    lineLayer(
                        "route-layer-$index",
                        "route-source-$index",
                    ) {
                        lineWidth(2.0)
                        lineColor(Color.BLUE)
                    },
                )
            }
            selectedFeatureId = buildingFeatureId // Remember the clicked building ID
        }
    }

    private fun loadThirdRouteElement(buildingFeatureId: String) {
        // Access the first feature in routesGeoJson
        val firstRouteFeature = routesGeoJson3.optJSONArray("features")?.optJSONObject(0)

        if (firstRouteFeature != null) {
            // Add the route feature to the map with a specific source (e.g., "route-source")
            binding.mapView.mapboxMap.getStyle { style ->
                style.addSource(
                    geoJsonSource("route-source-$index") {
                        data(firstRouteFeature.toString())
                    },
                )
                style.addLayer(
                    lineLayer(
                        "route-layer-$index",
                        "route-source-$index",
                    ) {
                        lineWidth(2.0)
                        lineColor(Color.GREEN)
                    },
                )
            }
            selectedFeatureId = buildingFeatureId // Remember the clicked building ID
        }
    }



    private fun loadFourthRouteElement(buildingFeatureId: String) {
        // Access the first feature in routesGeoJson
        val firstRouteFeature = routesGeoJson4.optJSONArray("features")?.optJSONObject(0)

        if (firstRouteFeature != null) {
            // Add the route feature to the map with a specific source (e.g., "route-source")
            binding.mapView.mapboxMap.getStyle { style ->
                style.addSource(
                    geoJsonSource("route-source-$index") {
                        data(firstRouteFeature.toString())
                    },
                )
                style.addLayer(
                    lineLayer(
                        "route-layer-$index",
                        "route-source-$index",
                    ) {
                        lineWidth(2.0)
                        lineColor(Color.GREEN)
                    },
                )
            }
            selectedFeatureId = buildingFeatureId // Remember the clicked building ID
        }
    }


    private fun loadFifthRouteElement(buildingFeatureId: String) {
        // Access the first feature in routesGeoJson
        val firstRouteFeature = routesGeoJson5.optJSONArray("features")?.optJSONObject(0)

        if (firstRouteFeature != null) {
            // Add the route feature to the map with a specific source (e.g., "route-source")
            binding.mapView.mapboxMap.getStyle { style ->
                style.addSource(
                    geoJsonSource("route-source-$index") {
                        data(firstRouteFeature.toString())
                    },
                )
                style.addLayer(
                    lineLayer(
                        "route-layer-$index",
                        "route-source-$index",
                    ) {
                        lineWidth(2.0)
                        lineColor(Color.GREEN)
                    },
                )
            }
            selectedFeatureId = buildingFeatureId // Remember the clicked building ID
        }
    }


    private fun loadSixthRouteElement(buildingFeatureId: String) {
        // Access the first feature in routesGeoJson
        val firstRouteFeature = routesGeoJson6.optJSONArray("features")?.optJSONObject(0)

        if (firstRouteFeature != null) {
            // Add the route feature to the map with a specific source (e.g., "route-source")
            binding.mapView.mapboxMap.getStyle { style ->
                style.addSource(
                    geoJsonSource("route-source-$index") {
                        data(firstRouteFeature.toString())
                    },
                )
                style.addLayer(
                    lineLayer(
                        "route-layer-$index",
                        "route-source-$index",
                    ) {
                        lineWidth(2.0)
                        lineColor(Color.GREEN)
                    },
                )
            }
            selectedFeatureId = buildingFeatureId // Remember the clicked building ID
        }
    }


    private fun loadSeventhRouteElement(buildingFeatureId: String) {
        // Access the first feature in routesGeoJson
        val firstRouteFeature = routesGeoJson7.optJSONArray("features")?.optJSONObject(0)

        if (firstRouteFeature != null) {
            // Add the route feature to the map with a specific source (e.g., "route-source")
            binding.mapView.mapboxMap.getStyle { style ->
                style.addSource(
                    geoJsonSource("route-source-$index") {
                        data(firstRouteFeature.toString())
                    },
                )
                style.addLayer(
                    lineLayer(
                        "route-layer-$index",
                        "route-source-$index",
                    ) {
                        lineWidth(2.0)
                        lineColor(Color.GREEN)
                    },
                )
            }
            selectedFeatureId = buildingFeatureId // Remember the clicked building ID
        }
    }


    private fun loadEighthRouteElement(buildingFeatureId: String) {
        // Access the first feature in routesGeoJson
        val firstRouteFeature = routesGeoJson8.optJSONArray("features")?.optJSONObject(0)

        if (firstRouteFeature != null) {
            // Add the route feature to the map with a specific source (e.g., "route-source")
            binding.mapView.mapboxMap.getStyle { style ->
                style.addSource(
                    geoJsonSource("route-source-$index") {
                        data(firstRouteFeature.toString())
                    },
                )
                style.addLayer(
                    lineLayer(
                        "route-layer-$index",
                        "route-source-$index",
                    ) {
                        lineWidth(2.0)
                        lineColor(Color.GREEN)
                    },
                )
            }
            selectedFeatureId = buildingFeatureId // Remember the clicked building ID
        }
    }


    private fun loadNinethRouteElement(buildingFeatureId: String) {
        // Access the first feature in routesGeoJson
        val firstRouteFeature = routesGeoJson9.optJSONArray("features")?.optJSONObject(0)

        if (firstRouteFeature != null) {
            // Add the route feature to the map with a specific source (e.g., "route-source")
            binding.mapView.mapboxMap.getStyle { style ->
                style.addSource(
                    geoJsonSource("route-source-$index") {
                        data(firstRouteFeature.toString())
                    },
                )
                style.addLayer(
                    lineLayer(
                        "route-layer-$index",
                        "route-source-$index",
                    ) {
                        lineWidth(2.0)
                        lineColor(Color.GREEN)
                    },
                )
            }
            selectedFeatureId = buildingFeatureId // Remember the clicked building ID
        }
    }



    private fun clearRouteElement(buildingFeatureId: String) {
        binding.mapView.mapboxMap.getStyle { style ->
            // Remove the "route-source" and "route-layer"
            style.removeStyleSource("route-source-$index")
            style.removeStyleLayer("route-layer-$index")
            index++
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { manageSensorEvent(it) }
    }

    private fun manageSensorEvent(event: SensorEvent) {
        Log.d("Salida", "Sensor event")
        event.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(it.values, 0, accelerometerReading, 0, accelerometerReading.size)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(it.values, 0, magnetometerReading, 0, magnetometerReading.size)
                }
            }

            // Compute the rotation matrix if we have both accelerometer and magnetometer data
            if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                // Get azimuth angle (orientationAngles[0]) in degrees
                val azimuthInRadians = orientationAngles[0]
                val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()

                // Update Mapbox camera rotation based on the azimuth
                val cameraOptions =
                    CameraOptions
                        .Builder()
                        .bearing(azimuthInDegrees.toDouble())
                        .build()
                binding.mapView.mapboxMap.setCamera(cameraOptions)
            }
        }
    }
}
