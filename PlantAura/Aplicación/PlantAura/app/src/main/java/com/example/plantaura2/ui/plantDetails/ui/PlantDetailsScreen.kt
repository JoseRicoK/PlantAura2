package com.example.plantaura2.ui.plantDetails.ui

import android.app.Application
import android.graphics.BitmapFactory
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


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
    val plantTypeRanges by viewModel.plantTypeRanges.collectAsState()
    val lastHumidityAmbiente by viewModel.lastHumidityAmbiente.collectAsState()
    val lastHumiditySuelo by viewModel.lastHumiditySuelo.collectAsState()
    val lastTemperature by viewModel.lastTemperature.collectAsState()
    val lastLuminosidad by viewModel.lastLuminosidad.collectAsState()
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (plantId != null) {
                    val imagePath = "${imageDirectory.path}/sensor_$plantId.jpg"
                    val bitmap = BitmapFactory.decodeFile(imagePath)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Imagen de $plantName",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
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
                        //.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))
            if (plantType.isNotEmpty() && plantTypeRanges != null) {
                PlantTypeDetails(
                    plantType = plantType,
                    plantTypeRanges = plantTypeRanges!!, // Not null assert
                    lastHumidityAmbiente = lastHumidityAmbiente,
                    lastHumiditySuelo = lastHumiditySuelo,
                    lastTemperature = lastTemperature,
                    lastLuminosidad = lastLuminosidad,
                    humidityAmbienteRange = plantTypeRanges?.humedadAmbienteMin?.let { min -> plantTypeRanges?.humedadAmbienteMax?.let { max -> min..max } } ?: 0..100,
                    humiditySueloRange = plantTypeRanges?.humedadSueloMin?.let { min -> plantTypeRanges?.humedadSueloMax?.let { max -> min..max } } ?: 0..100,
                    temperatureRange = plantTypeRanges?.temperaturaAmbienteMin?.toFloat()?.let { min -> plantTypeRanges?.temperaturaAmbienteMax?.toFloat()?.let { max -> min..max } } ?: 0f..100f,
                    luminosidadRange = plantTypeRanges?.luzMin?.toFloat()?.let { min -> plantTypeRanges?.luzMax?.toFloat()?.let { max -> min..max } } ?: 0f..100f,
                    viewModel = viewModel
                )
            } else {
                Text(
                    text = "Cargando datos de la planta...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            //Spacer(modifier = Modifier.height(16.dp))

            PredictHealthSection(viewModel = viewModel)

            Spacer(modifier = Modifier.height(10.dp))

            MeasurementSection(
                measurementData = viewModel.measurementData.collectAsState().value,
                lastHumidityAmbiente = lastHumidityAmbiente,
                lastHumiditySuelo = lastHumiditySuelo,
                lastTemperature = lastTemperature,
                lastLuminosidad = lastLuminosidad
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    colors = ButtonDefaults.buttonColors(containerColor = if (revive) Color.Red else Color(
                        0xFF59B924
                    )
                    )
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

    val colors = listOf(Color(0xFFAE48F7), Color(0xFFA86AFF), Color(0xFFCCADF0), Color(0xFFAB63DF))
    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(0f, 0f),
        end = Offset(animatedProgress * 2000f, animatedProgress * 2000f)
    )

    Box(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.Transparent)
            .border(BorderStroke(4.dp, brush), RoundedCornerShape(16.dp))
            .background(Color(0xFF821ACC), shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    viewModel.predictSalud()
                    isButtonClicked.value = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF821ACC), shape = RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF821ACC)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                border = BorderStroke(0.dp, Color.Transparent)
            ) {
                Text(text = "Predecir Salud", color = Color.White)
            }

            if (isButtonClicked.value) {
                Text(
                    text = "Salud predicha: $saludPredicha",
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun PredictHealthSection(viewModel: PlantDetailsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
    humidityAmbienteRange: IntRange,
    humiditySueloRange: IntRange,
    temperatureRange: ClosedFloatingPointRange<Float>,
    luminosidadRange: ClosedFloatingPointRange<Float>,
    viewModel: PlantDetailsViewModel
) {
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
                unit = "%"
            )
        }
        if (lastHumiditySuelo != null) {
            ParameterStatusRow(
                parameterName = "Humedad Suelo",
                parameterValue = lastHumiditySuelo,
                range = humiditySueloRange,
                recommendation = viewModel.getRecommendation("Humedad Suelo", lastHumiditySuelo.toFloat(), humiditySueloRange.start.toFloat()..humiditySueloRange.endInclusive.toFloat()),
                unit = "%"
            )
        }
        if (lastTemperature != null) {
            ParameterStatusRow(
                parameterName = "Temperatura",
                parameterValue = lastTemperature.toInt(),
                range = temperatureRange.start.toInt()..temperatureRange.endInclusive.toInt(),
                recommendation = viewModel.getRecommendation("Temperatura", lastTemperature, temperatureRange),
                unit = "ºC"
            )
        }
        if (lastLuminosidad != null) {
            ParameterStatusRow(
                parameterName = "Luminosidad",
                parameterValue = lastLuminosidad.toInt(),
                range = luminosidadRange.start.toInt()..luminosidadRange.endInclusive.toInt(),
                recommendation = viewModel.getRecommendation("Luminosidad", lastLuminosidad, luminosidadRange),
                unit = "luxes"
            )
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
    recommendation: String?
) {
    val isWithinRange = parameterValue in range
    val statusIcon = if (isWithinRange) {
        "✅" // Emoji for "within range"
    } else {
        "❌" // Emoji for "out of range"
    }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
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
    }
}

@Composable
fun MeasurementSection(
    measurementData: List<MeasurementData>,
    lastHumidityAmbiente: Int?,
    lastHumiditySuelo: Int?,
    lastTemperature: Float?,
    lastLuminosidad: Float?
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
}

@Composable
fun MeasurementGraph(data: List<Pair<String, Int>>, title: String ) {
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
                    val result = snackbarHostState.showSnackbar(
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