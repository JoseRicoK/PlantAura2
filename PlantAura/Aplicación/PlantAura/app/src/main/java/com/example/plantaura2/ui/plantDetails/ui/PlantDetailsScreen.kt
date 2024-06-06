package com.example.plantaura2.ui.plantDetails.ui

import android.app.Application
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.plantaura2.domain.model.MeasurementData
import com.example.plantaura2.domain.model.PlantTypeRanges
import com.example.plantaura2.domain.usecase.GetPlantTypeByNameUseCase
import com.example.plantaura2.domain.usecase.GetPlantTypeRangesUseCase
import com.example.plantaura2.domain.usecase.GraphUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.jaikeerthick.composable_graphs.composables.line.LineGraph
import com.jaikeerthick.composable_graphs.composables.line.model.LineData
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphColors
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphFillType
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphStyle
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphVisibility
import java.io.File
import kotlin.math.roundToInt
import com.example.plantaura2.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.filled.*


@Composable
fun PlantDetailsScreen(navController: NavController, plantName: String) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: PlantDetailsViewModel = viewModel(
        factory = PlantDetailsViewModelFactory(
            plantName,
            GraphUseCase(FirebaseFirestore.getInstance()),
            GetPlantTypeByNameUseCase(FirebaseFirestore.getInstance()),
            GetPlantTypeRangesUseCase(FirebaseFirestore.getInstance()),
            context
        )
    )
    val plantName by viewModel.plantName.collectAsState()
    val plantType by viewModel.plantType.collectAsState()
    val sensorType by viewModel.sensorType.collectAsState() // Añadir esta línea
    val plantTypeRanges by viewModel.plantTypeRanges.collectAsState()
    val lastHumidityAmbiente by viewModel.lastHumidityAmbiente.collectAsState()
    val lastHumiditySuelo by viewModel.lastHumiditySuelo.collectAsState()
    val lastTemperature by viewModel.lastTemperature.collectAsState()
    val lastLuminosidad by viewModel.lastLuminosidad.collectAsState()
    val lastConductividad by viewModel.lastConductividad.collectAsState()
    val lastPh by viewModel.lastPh.collectAsState()
    val lastNitrogeno by viewModel.lastNitrogeno.collectAsState()
    val lastFosforo by viewModel.lastFosforo.collectAsState()
    val lastPotasio by viewModel.lastPotasio.collectAsState()
    val lastSalinidad by viewModel.lastSalinidad.collectAsState()
    val lastTds by viewModel.lastTds.collectAsState()
    val revive by viewModel.revive.collectAsState()
    val (showDialog, setShowDialog) = remember { mutableStateOf(false) }
    val plantId by viewModel.plantId.collectAsState()
    val imageDirectory = File(LocalContext.current.filesDir, "com.example.plantaura2.data.imagesPlants")
    if (showDialog) {
        ReviveInfoDialog(onDismiss = { setShowDialog(false) })
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (plantId != null) {
                val imagePath = "${imageDirectory.path}/sensor_$plantId.jpg"
                val bitmap = BitmapFactory.decodeFile(imagePath)
                if (bitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    ){
                        Text(
                            text = plantName,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Imagen de $plantName",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xFFFEFAFE)),
                                        startY = 5f,
                                        endY = 500f
                                    )
                                )
                        )
                        Text(
                            text = plantName,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))
            if (plantType.isNotEmpty() && plantTypeRanges != null) {
                PlantTypeDetails(
                    plantType = plantType,
                    plantTypeRanges = plantTypeRanges!!,
                    lastHumidityAmbiente = lastHumidityAmbiente,
                    lastHumiditySuelo = lastHumiditySuelo,
                    lastTemperature = lastTemperature,
                    lastLuminosidad = lastLuminosidad,
                    lastConductividad = lastConductividad,
                    lastPh = lastPh,
                    lastNitrogeno = lastNitrogeno,
                    lastFosforo = lastFosforo,
                    lastPotasio = lastPotasio,
                    lastSalinidad = lastSalinidad,
                    lastTds = lastTds,
                    humidityAmbienteRange = plantTypeRanges?.humedadAmbienteMin?.let { min -> plantTypeRanges?.humedadAmbienteMax?.let { max -> min..max } } ?: 0..100,
                    humiditySueloRange = plantTypeRanges?.humedadSueloMin?.let { min -> plantTypeRanges?.humedadSueloMax?.let { max -> min..max } } ?: 0..100,
                    temperatureRange = plantTypeRanges?.temperaturaAmbienteMin?.toFloat()?.let { min -> plantTypeRanges?.temperaturaAmbienteMax?.toFloat()?.let { max -> min..max } } ?: 0f..100f,
                    luminosidadRange = plantTypeRanges?.luzMin?.toFloat()?.let { min -> plantTypeRanges?.luzMax?.toFloat()?.let { max -> min..max } } ?: 0f..100f,
                    conductividadRange = plantTypeRanges?.conductividadMin?.let { min -> plantTypeRanges?.conductividadMax?.let { max -> min..max } } ?: 0..100,
                    phRange = plantTypeRanges?.phMin?.toFloat()?.let { min -> plantTypeRanges?.phMax?.toFloat()?.let { max -> min..max } } ?: 0f..14f,
                    nitrogenoRange = plantTypeRanges?.nitrogenoMin?.let { min -> plantTypeRanges?.nitrogenoMax?.let { max -> min..max } } ?: 0..100,
                    fosforoRange = plantTypeRanges?.fosforoMin?.let { min -> plantTypeRanges?.fosforoMax?.let { max -> min..max } } ?: 0..100,
                    potasioRange = plantTypeRanges?.potasioMin?.let { min -> plantTypeRanges?.potasioMax?.let { max -> min..max } } ?: 0..100,
                    salinidadRange = plantTypeRanges?.salinidadMin?.let { min -> plantTypeRanges?.salinidadMax?.let { max -> min..max } } ?: 0..100,
                    tdsRange = plantTypeRanges?.tdsMin?.let { min -> plantTypeRanges?.tdsMax?.let { max -> min..max } } ?: 0..100,
                    viewModel = viewModel,
                    sensorType = sensorType // Añadir esta línea
                )
            } else {
                Text(
                    text = "Cargando datos de la planta...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            //Spacer(modifier = Modifier.height(8.dp))

            PredictHealthSection(viewModel = viewModel)

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.7.dp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón "Revive"
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center // Centrar horizontalmente
            ) {
                Button(
                    onClick = {
                        if (plantId != null) {
                            viewModel.toggleRevive(plantId!!, revive)
                            if (!revive) {
                                // Mostrar el diálogo solo si se está activando el modo revive
                                setShowDialog(true)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (revive) Color(0xFD4D4D4D) else Color(0xE8FC2F2F)),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(text = if (revive) "Desactivar Revive" else "Activar Revive")
                }
            }
        }
    }
}





@Composable
fun AnimatedBorderContainer(viewModel: PlantDetailsViewModel) {
    val saludPredicha by viewModel.saludPredicha.observeAsState(0)
    val isButtonClicked = remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition()
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val colors = listOf(Color(0xFFD6D3F1), Color(0xFFA86AFF), Color(0xFF1A31FF), Color(0xFF9EFAFA))
    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(0f, 0f),
        end = Offset(animatedProgress * 2000f, animatedProgress * 2000f)
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color.Transparent)
            .border(BorderStroke(5.dp, brush), RoundedCornerShape(30.dp))
            .background(Color(0xFF821ACC), shape = RoundedCornerShape(30.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    viewModel.predictSalud()
                    isButtonClicked.value = !isButtonClicked.value
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF821ACC), shape = RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF821ACC)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                border = BorderStroke(0.dp, Color.Transparent)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ia_estrella),
                    contentDescription = "Star Icon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Predecir Salud",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isButtonClicked.value) {
                CustomComponent(indicatorValue = saludPredicha, bigTextSuffix = "%")
            }
        }
    }
}

@Composable
fun CustomComponent(
    canvasSize: Dp = 200.dp,
    indicatorValue: Int = 0,
    maxIndicatorValue: Int = 100,
    backgroundIndicatorColor: Color = Color.Gray.copy(alpha = 0.3f),
    backgroundIndicatorStrokeWidth: Float = 90f,
    foregroundIndicatorColor: Color = Color.White,
    foregroundIndicatorStrokeWidth: Float = 70f,
    bigTextFontSize: TextUnit = 24.sp,
    bigTextColor: Color = Color.White,
    bigTextSuffix: String = "%",
    smallText: String = "Salud",
    smallTextFontSize: TextUnit = 16.sp,
    smallTextColor: Color = Color.White.copy(alpha = 0.7f)
) {
    var allowedIndicatorValue by remember {
        mutableStateOf(maxIndicatorValue)
    }
    allowedIndicatorValue = if (indicatorValue <= maxIndicatorValue) {
        indicatorValue
    } else {
        maxIndicatorValue
    }

    var animatedIndicatorValue by remember { mutableStateOf(0f) }
    LaunchedEffect(key1 = allowedIndicatorValue) {
        animatedIndicatorValue = allowedIndicatorValue.toFloat()
    }

    val percentage =
        (animatedIndicatorValue / maxIndicatorValue) * 100

    val sweepAngle by animateFloatAsState(
        targetValue = (2.4 * percentage).toFloat(),
        animationSpec = tween(1000)
    )

    val receivedValue by animateIntAsState(
        targetValue = allowedIndicatorValue,
        animationSpec = tween(1000)
    )

    val animatedBigTextColor by animateColorAsState(
        targetValue = if (allowedIndicatorValue == 0)
            Color.White.copy(alpha = 0.3f)
        else
            bigTextColor,
        animationSpec = tween(1000)
    )

    Column(
        modifier = Modifier
            .size(canvasSize)
            .drawBehind {
                val componentSize = size / 1.25f
                backgroundIndicator(
                    componentSize = componentSize,
                    indicatorColor = backgroundIndicatorColor,
                    indicatorStrokeWidth = backgroundIndicatorStrokeWidth
                )
                foregroundIndicator(
                    sweepAngle = sweepAngle,
                    componentSize = componentSize,
                    indicatorColor = foregroundIndicatorColor,
                    indicatorStrokeWidth = foregroundIndicatorStrokeWidth
                )
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmbeddedElements(
            bigText = receivedValue,
            bigTextFontSize = bigTextFontSize,
            bigTextColor = animatedBigTextColor,
            bigTextSuffix = bigTextSuffix,
            smallText = smallText,
            smallTextColor = smallTextColor,
            smallTextFontSize = smallTextFontSize
        )
    }
}

fun DrawScope.backgroundIndicator(
    componentSize: Size,
    indicatorColor: Color,
    indicatorStrokeWidth: Float
) {
    drawArc(
        size = componentSize,
        color = indicatorColor,
        startAngle = 150f,
        sweepAngle = 240f,
        useCenter = false,
        style = Stroke(
            width = indicatorStrokeWidth,
            cap = StrokeCap.Round
        ),
        topLeft = Offset(
            x = (size.width - componentSize.width) / 2f,
            y = (size.height - componentSize.height) / 2f
        )
    )
}

fun DrawScope.foregroundIndicator(
    sweepAngle: Float,
    componentSize: Size,
    indicatorColor: Color,
    indicatorStrokeWidth: Float
) {
    drawArc(
        size = componentSize,
        color = indicatorColor,
        startAngle = 150f,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(
            width = indicatorStrokeWidth,
            cap = StrokeCap.Round
        ),
        topLeft = Offset(
            x = (size.width - componentSize.width) / 2f,
            y = (size.height - componentSize.height) / 2f
        )
    )
}

@Composable
fun EmbeddedElements(
    bigText: Int,
    bigTextFontSize: TextUnit,
    bigTextColor: Color,
    bigTextSuffix: String = "%",
    smallText: String,
    smallTextColor: Color,
    smallTextFontSize: TextUnit
) {
    Text(
        text = smallText,
        color = smallTextColor,
        fontSize = smallTextFontSize,
        textAlign = TextAlign.Center
    )
    Text(
        text = "$bigText $bigTextSuffix",
        color = bigTextColor,
        fontSize = bigTextFontSize,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}

@Composable
@Preview(showBackground = true)
fun CustomComponentPreview() {
    CustomComponent()
}

@Composable
fun PredictHealthSection(viewModel: PlantDetailsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedBorderContainer(viewModel)
    }
}

@Composable
fun PlantTypeDetails(
    plantType: String,
    plantTypeRanges: PlantTypeRanges,
    lastHumidityAmbiente: Int?,
    lastHumiditySuelo: Int?,
    lastTemperature: Float?,
    lastLuminosidad: Float?,
    lastConductividad: Int?,
    lastPh: Float?,
    lastNitrogeno: Int?,
    lastFosforo: Int?,
    lastPotasio: Int?,
    lastSalinidad: Int?,
    lastTds: Int?,
    humidityAmbienteRange: IntRange,
    humiditySueloRange: IntRange,
    temperatureRange: ClosedFloatingPointRange<Float>,
    luminosidadRange: ClosedFloatingPointRange<Float>,
    conductividadRange: IntRange,
    phRange: ClosedFloatingPointRange<Float>,
    nitrogenoRange: IntRange,
    fosforoRange: IntRange,
    potasioRange: IntRange,
    salinidadRange: IntRange,
    tdsRange: IntRange,
    sensorType: String,
    viewModel: PlantDetailsViewModel
) {
    val showHumidityAmbienteGraph = remember { mutableStateOf(false) }
    val showHumiditySueloGraph = remember { mutableStateOf(false) }
    val showTemperatureGraph = remember { mutableStateOf(false) }
    val showLuminosidadGraph = remember { mutableStateOf(false) }
    val showConductividadGraph = remember { mutableStateOf(false) }
    val showPhGraph = remember { mutableStateOf(false) }
    val showNitrogenoGraph = remember { mutableStateOf(false) }
    val showFosforoGraph = remember { mutableStateOf(false) }
    val showPotasioGraph = remember { mutableStateOf(false) }
    val showSalinidadGraph = remember { mutableStateOf(false) }
    val showTdsGraph = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Tipo de planta: $plantType",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.7.dp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        val revive by viewModel.revive.collectAsState()
        if (revive) {
            ReviveSection(viewModel = viewModel)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (lastHumidityAmbiente != null) {
            ParameterStatusRow(
                parameterName = "Humedad Ambiente",
                parameterValue = lastHumidityAmbiente,
                range = humidityAmbienteRange,
                recommendation = viewModel.getRecommendation("Humedad Ambiente", lastHumidityAmbiente.toFloat(), humidityAmbienteRange.start.toFloat()..humidityAmbienteRange.endInclusive.toFloat()),
                unit = "%",
                onShowGraph = { showHumidityAmbienteGraph.value = !showHumidityAmbienteGraph.value },
                isGraphVisible = showHumidityAmbienteGraph.value,
                measurementData = viewModel.measurementData.collectAsState().value.map { it.timestamp to (it.humedadAmbiente ?: 0) }
            )
        }
        if (lastHumiditySuelo != null) {
            ParameterStatusRow(
                parameterName = "Humedad Suelo",
                parameterValue = lastHumiditySuelo,
                range = humiditySueloRange,
                recommendation = viewModel.getRecommendation("Humedad Suelo", lastHumiditySuelo.toFloat(), humiditySueloRange.start.toFloat()..humiditySueloRange.endInclusive.toFloat()),
                unit = "%",
                onShowGraph = { showHumiditySueloGraph.value = !showHumiditySueloGraph.value },
                isGraphVisible = showHumiditySueloGraph.value,
                measurementData = viewModel.measurementData.collectAsState().value.map { it.timestamp to (it.humedadSuelo ?: 0) }
            )
        }
        if (lastTemperature != null) {
            ParameterStatusRow(
                parameterName = "Temperatura",
                parameterValue = lastTemperature.toInt(),
                range = temperatureRange.start.toInt()..temperatureRange.endInclusive.toInt(),
                recommendation = viewModel.getRecommendation("Temperatura", lastTemperature, temperatureRange),
                unit = "ºC",
                onShowGraph = { showTemperatureGraph.value = !showTemperatureGraph.value },
                isGraphVisible = showTemperatureGraph.value,
                measurementData = viewModel.measurementData.collectAsState().value.map { it.timestamp to (it.temperatura?.toInt() ?: 0) }
            )
        }
        if (lastLuminosidad != null) {
            ParameterStatusRow(
                parameterName = "Luminosidad",
                parameterValue = lastLuminosidad.toInt(),
                range = luminosidadRange.start.toInt()..luminosidadRange.endInclusive.toInt(),
                recommendation = viewModel.getRecommendation("Luminosidad", lastLuminosidad, luminosidadRange),
                unit = "luxes",
                onShowGraph = { showLuminosidadGraph.value = !showLuminosidadGraph.value },
                isGraphVisible = showLuminosidadGraph.value,
                measurementData = viewModel.measurementData.collectAsState().value.map { it.timestamp to (it.luminosidad?.toInt() ?: 0) }
            )
        }

        // Datos adicionales para el sensor Pro
        if (sensorType == "Sensor PlantAura Pro") {
            if (lastConductividad != null) {
                ParameterStatusRow(
                    parameterName = "Conductividad",
                    parameterValue = lastConductividad,
                    range = conductividadRange,
                    recommendation = viewModel.getRecommendation("Conductividad", lastConductividad.toFloat(), conductividadRange.start.toFloat()..conductividadRange.endInclusive.toFloat()),
                    unit = "µS/cm",
                    onShowGraph = { showConductividadGraph.value = !showConductividadGraph.value },
                    isGraphVisible = showConductividadGraph.value,
                    measurementData = viewModel.measurementData.collectAsState().value.map { it.timestamp to (it.conductividad ?: 0) }
                )
            }
            if (lastPh != null) {
                ParameterStatusRow(
                    parameterName = "pH",
                    parameterValue = lastPh.toInt(),
                    range = phRange.start.toInt()..phRange.endInclusive.toInt(),
                    recommendation = viewModel.getRecommendation("pH", lastPh, phRange),
                    unit = "pH",
                    onShowGraph = { showPhGraph.value = !showPhGraph.value },
                    isGraphVisible = showPhGraph.value,
                    measurementData = viewModel.measurementData.collectAsState().value.map { it.timestamp to (it.ph?.toInt() ?: 0) }
                )
            }
            if (lastNitrogeno != null) {
                ParameterStatusRow(
                    parameterName = "Nitrógeno",
                    parameterValue = lastNitrogeno,
                    range = nitrogenoRange,
                    recommendation = viewModel.getRecommendation("Nitrógeno", lastNitrogeno.toFloat(), nitrogenoRange.start.toFloat()..nitrogenoRange.endInclusive.toFloat()),
                    unit = "mg/kg",
                    onShowGraph = { showNitrogenoGraph.value = !showNitrogenoGraph.value },
                    isGraphVisible = showNitrogenoGraph.value,
                    measurementData = viewModel.measurementData.collectAsState().value.map { it.timestamp to (it.nitrogeno ?: 0) }
                )
            }
            if (lastFosforo != null) {
                ParameterStatusRow(
                    parameterName = "Fósforo",
                    parameterValue = lastFosforo,
                    range = fosforoRange,
                    recommendation = viewModel.getRecommendation("Fósforo", lastFosforo.toFloat(), fosforoRange.start.toFloat()..fosforoRange.endInclusive.toFloat()),
                    unit = "mg/kg",
                    onShowGraph = { showFosforoGraph.value = !showFosforoGraph.value },
                    isGraphVisible = showFosforoGraph.value,
                    measurementData = viewModel.measurementData.collectAsState().value.map { it.timestamp to (it.fosforo ?: 0) }
                )
            }
            if (lastPotasio != null) {
                ParameterStatusRow(
                    parameterName = "Potasio",
                    parameterValue = lastPotasio,
                    range = potasioRange,
                    recommendation = viewModel.getRecommendation("Potasio", lastPotasio.toFloat(), potasioRange.start.toFloat()..potasioRange.endInclusive.toFloat()),
                    unit = "mg/kg",
                    onShowGraph = { showPotasioGraph.value = !showPotasioGraph.value },
                    isGraphVisible = showPotasioGraph.value,
                    measurementData = viewModel.measurementData.collectAsState().value.map { it.timestamp to (it.potasio ?: 0) }
                )
            }
            if (lastSalinidad != null) {
                ParameterStatusRow(
                    parameterName = "Salinidad",
                    parameterValue = lastSalinidad,
                    range = salinidadRange,
                    recommendation = viewModel.getRecommendation("Salinidad", lastSalinidad.toFloat(), salinidadRange.start.toFloat()..salinidadRange.endInclusive.toFloat()),
                    unit = "mg/L",
                    onShowGraph = { showSalinidadGraph.value = !showSalinidadGraph.value },
                    isGraphVisible = showSalinidadGraph.value,
                    measurementData = viewModel.measurementData.collectAsState().value.map { it.timestamp to (it.salinidad ?: 0) }
                )
            }
            if (lastTds != null) {
                ParameterStatusRow(
                    parameterName = "TDS",
                    parameterValue = lastTds,
                    range = tdsRange,
                    recommendation = viewModel.getRecommendation("TDS", lastTds.toFloat(), tdsRange.start.toFloat()..tdsRange.endInclusive.toFloat()),
                    unit = "mg/L",
                    onShowGraph = { showTdsGraph.value = !showTdsGraph.value },
                    isGraphVisible = showTdsGraph.value,
                    measurementData = viewModel.measurementData.collectAsState().value.map { it.timestamp to (it.tds ?: 0) }
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.7.dp,
            color = Color.Gray
        )
    }
}


@Composable
fun ParameterStatusRow(
    parameterName: String,
    parameterValue: Int,
    range: IntRange,
    unit: String = "",
    recommendation: String?,
    onShowGraph: () -> Unit,
    isGraphVisible: Boolean,
    measurementData: List<Pair<String, Int>>
) {
    val isWithinRange = parameterValue in range
    val statusIcon = if (isWithinRange) {
        "✅" // Emoji for "within range"
    } else {
        "❌" // Emoji for "out of range"
    }
    val parameterIcon = getIconForParameter(parameterName)

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Icon(
                imageVector = parameterIcon,
                contentDescription = "$parameterName Icon",
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 8.dp),
                tint = Color.Gray
            )

            Text(
                text = "$parameterName: ",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "$parameterValue $unit",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusIcon,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onShowGraph,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow),
                    contentDescription = "Show Graph",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        if (!isWithinRange && recommendation != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0),
                ),
                border = BorderStroke(1.dp, Color(0xFFFFA500)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFFFA500), // Orange color for the icon
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = recommendation,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        if (isGraphVisible) {
            Spacer(modifier = Modifier.height(16.dp))
            MeasurementGraph(data = measurementData, title = parameterName)
        }
    }
}




fun getIconForParameter(parameterName: String): ImageVector {
    return when (parameterName) {
        "Humedad Ambiente", "Humedad Suelo" -> Icons.Default.WaterDrop
        "Temperatura" -> Icons.Default.Thermostat
        "Luminosidad" -> Icons.Default.WbSunny
        "Conductividad" -> Icons.Default.ElectricalServices
        "pH" -> Icons.Default.Science
        "Nitrógeno", "Fósforo", "Potasio", "Salinidad", "TDS" -> Icons.Default.Science
        else -> Icons.Default.Info
    }
}


@Composable
fun MeasurementSection(
    measurementData: List<MeasurementData>,
    lastHumidityAmbiente: Int?,
    lastHumiditySuelo: Int?,
    lastTemperature: Float?,
    lastLuminosidad: Float?,
    lastConductividad: Int?,
    lastPh: Float?,
    lastNitrogeno: Int?,
    lastFosforo: Int?,
    lastPotasio: Int?,
    lastSalinidad: Int?,
    lastTds: Int?,
    sensorType: String
) {
    // Recuadro para humedad ambiente y gráfica
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Humedad Ambiente: $lastHumidityAmbiente%",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (measurementData.isNotEmpty()) {
                MeasurementGraph(measurementData.map { it.timestamp to it.humedadAmbiente }, "Humedad Ambiente")
            } else {
                Text(text = "Cargando datos de humedad ambiente...")
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    // Recuadro para humedad suelo y gráfica
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Humedad Suelo: $lastHumiditySuelo%",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (measurementData.isNotEmpty()) {
                MeasurementGraph(measurementData.map { it.timestamp to it.humedadSuelo }, "Humedad Suelo")
            } else {
                Text(text = "Cargando datos de humedad suelo...")
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    // Recuadro para temperatura y gráfica
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Temperatura: $lastTemperature°C",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (measurementData.isNotEmpty()) {
                MeasurementGraph(measurementData.map { it.timestamp to it.temperatura.toInt() }, "Temperatura")
            } else {
                Text(text = "Cargando datos de temperatura...")
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    // Recuadro para luminosidad y gráfica
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Luminosidad: $lastLuminosidad luxes",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (measurementData.isNotEmpty()) {
                MeasurementGraph(measurementData.map { it.timestamp to it.luminosidad.toInt() }, "Luminosidad")
            } else {
                Text(text = "Cargando datos de luminosidad...")
            }
        }
    }
    if (sensorType == "Sensor PlantAura Pro") {
        // Datos adicionales para el sensor Pro
        if (lastConductividad != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Conductividad: $lastConductividad µS/cm",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (measurementData.isNotEmpty()) {
                        MeasurementGraph(measurementData.map { it.timestamp to (it.conductividad ?: 0) }, "Conductividad")
                    } else {
                        Text(text = "Cargando datos de conductividad...")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (lastPh != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "pH: $lastPh",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (measurementData.isNotEmpty()) {
                        MeasurementGraph(measurementData.map { it.timestamp to (it.ph?.toInt() ?: 0) }, "pH")
                    } else {
                        Text(text = "Cargando datos de pH...")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (lastNitrogeno != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Nitrógeno: $lastNitrogeno mg/kg",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (measurementData.isNotEmpty()) {
                        MeasurementGraph(measurementData.map { it.timestamp to (it.nitrogeno ?: 0) }, "Nitrógeno")
                    } else {
                        Text(text = "Cargando datos de nitrógeno...")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (lastFosforo != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Fósforo: $lastFosforo mg/kg",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (measurementData.isNotEmpty()) {
                        MeasurementGraph(measurementData.map { it.timestamp to (it.fosforo ?: 0) }, "Fósforo")
                    } else {
                        Text(text = "Cargando datos de fósforo...")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (lastPotasio != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Potasio: $lastPotasio mg/kg",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (measurementData.isNotEmpty()) {
                        MeasurementGraph(measurementData.map { it.timestamp to (it.potasio ?: 0) }, "Potasio")
                    } else {
                        Text(text = "Cargando datos de potasio...")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (lastSalinidad != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Salinidad: $lastSalinidad mg/L",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (measurementData.isNotEmpty()) {
                        MeasurementGraph(measurementData.map { it.timestamp to (it.salinidad ?: 0) }, "Salinidad")
                    } else {
                        Text(text = "Cargando datos de salinidad...")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (lastTds != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TDS: $lastTds mg/L",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (measurementData.isNotEmpty()) {
                        MeasurementGraph(measurementData.map { it.timestamp to (it.tds ?: 0) }, "TDS")
                    } else {
                        Text(text = "Cargando datos de TDS...")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}



@Composable
fun MeasurementGraph(data: List<Pair<String, Int>>, title: String) {
    // Formatear las fechas para mostrar solo la hora
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val reversedData = data.asReversed()
    val xAxisData = reversedData.mapIndexed { index, dataPoint ->
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dataPoint.first)
        when (index) {
            0, reversedData.size - 1 -> dateFormat.format(date)
            else -> ""
        }
    }
    val yAxisData = reversedData.map { it.second.toFloat() }

    val darkGreen = Color(0xFF006400) // Definimos un color verde oscuro

    // Obtener los valores mínimo y máximo de Y
    val minY = yAxisData.minOrNull() ?: 0f
    val maxY = yAxisData.maxOrNull() ?: 100f

    // Crear etiquetas personalizadas para el eje Y
    val yAxisLabels = (0..10).map { (minY + it * (maxY - minY) / 10).roundToInt().toString() }

    val style = LineGraphStyle(
        colors = LineGraphColors(
            lineColor = darkGreen,
            pointColor = Color.Transparent, // Hacemos los puntos transparentes
            clickHighlightColor = darkGreen, // Resaltamos el punto seleccionado
            crossHairColor = Color.Gray,
            fillType = LineGraphFillType.Gradient(Brush.verticalGradient(
                colors = listOf(darkGreen.copy(alpha = 0.4f), Color.Transparent)
            ))
        ),
        visibility = LineGraphVisibility(
            isCrossHairVisible = true,
            isYAxisLabelVisible = true,
            isXAxisLabelVisible = true,
            isGridVisible = false
        )
    )

    val clickedPoint = remember { mutableStateOf<LineData?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Ajustar la altura del contenedor
    ) {
        LineGraph(
            modifier = Modifier.fillMaxSize(),
            data = xAxisData.zip(yAxisData) { x, y -> LineData(x, y) },
            style = style,
            onPointClick = { point ->
                clickedPoint.value = point

                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Valor: ${point.y}",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun ReviveSection(viewModel: PlantDetailsViewModel) {
    val filteredRecommendations by viewModel.filteredRecommendations.collectAsState()
    Column {
        filteredRecommendations.forEachIndexed { index, recommendation ->
            ReviveRecommendation(
                recommendation = recommendation,
                onClose = {
                    viewModel.hideRecommendation(index)
                }
            )
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.7.dp,
        color = Color.Gray
    )
}
@Composable
fun ReviveRecommendation(recommendation: String, onClose: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE5E5),
        ),
        border = BorderStroke(1.dp, Color.Red)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(2.dp)
        ) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = recommendation,
                color = Color.Black,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black
                )
            }
        }
    }
}
@Composable
fun ReviveInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Revive Activado", style = MaterialTheme.typography.headlineMedium)
        },
        text = {
            Column {
                Text("Las recomendaciones a seguir se mostrarán en la parte superior.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Los sensores medirán con mayor frecuencia y precisión.")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}