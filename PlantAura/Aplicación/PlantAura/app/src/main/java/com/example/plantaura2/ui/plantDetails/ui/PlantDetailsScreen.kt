package com.example.plantaura2.ui.plantDetails.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.plantaura2.ui.plantdetails.ui.PlantDetailsViewModel
import com.example.plantaura2.ui.plantdetails.ui.PlantDetailsViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.jaikeerthick.composable_graphs.composables.line.LineGraph
import com.jaikeerthick.composable_graphs.composables.line.model.LineData
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphColors
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphFillType
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphStyle
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphVisibility
import kotlin.math.roundToInt

@Composable
fun PlantDetailsScreen(navController: NavController, plantName: String) {
    val viewModel: PlantDetailsViewModel = viewModel(
        factory = PlantDetailsViewModelFactory(
            plantName,
            GraphUseCase(FirebaseFirestore.getInstance()),
            GetPlantTypeByNameUseCase(FirebaseFirestore.getInstance()),
            GetPlantTypeRangesUseCase(FirebaseFirestore.getInstance())
        )
    )
    val plantName by viewModel.plantName.collectAsState()
    val plantType by viewModel.plantType.collectAsState()
    val plantTypeRanges by viewModel.plantTypeRanges.collectAsState()
    val measurementData by viewModel.measurementData.collectAsState()
    val lastHumidityAmbiente by viewModel.lastHumidityAmbiente.collectAsState()
    val lastHumiditySuelo by viewModel.lastHumiditySuelo.collectAsState()
    val lastTemperature by viewModel.lastTemperature.collectAsState()
    val lastLuminosidad by viewModel.lastLuminosidad.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = plantName,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (plantType.isNotEmpty()) {
                PlantTypeDetails(
                    plantType = plantType,
                    plantTypeRanges = plantTypeRanges,
                    lastHumidityAmbiente = lastHumidityAmbiente,
                    lastHumiditySuelo = lastHumiditySuelo,
                    lastTemperature = lastTemperature,
                    lastLuminosidad = lastLuminosidad
                )
            } else {
                Text(
                    text = "Cargando datos de la planta...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            MeasurementSection(
                measurementData = measurementData,
                lastHumidityAmbiente = lastHumidityAmbiente,
                lastHumiditySuelo = lastHumiditySuelo,
                lastTemperature = lastTemperature,
                lastLuminosidad = lastLuminosidad
            )
        }
    }
}

@Composable
fun PlantTypeDetails(
    plantType: String,
    plantTypeRanges: PlantTypeRanges?,
    lastHumidityAmbiente: Int?,
    lastHumiditySuelo: Int?,
    lastTemperature: Float?,
    lastLuminosidad: Float?
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

        Spacer(modifier = Modifier.height(16.dp))

        plantTypeRanges?.let { ranges ->
            if (lastHumidityAmbiente != null) {
                ParameterStatusRow(
                    parameterName = "Humedad Ambiente",
                    parameterValue = lastHumidityAmbiente,
                    range = ranges.humedadAmbienteMin..ranges.humedadAmbienteMax,
                    isPercentage = true
                )
            }

            if (lastHumiditySuelo != null) {
                ParameterStatusRow(
                    parameterName = "Humedad Suelo",
                    parameterValue = lastHumiditySuelo,
                    range = ranges.humedadSueloMin..ranges.humedadSueloMax,
                    isPercentage = true
                )
            }

            if (lastTemperature != null) {
                ParameterStatusRow(
                    parameterName = "Temperatura",
                    parameterValue = lastTemperature.toInt(),
                    range = ranges.temperaturaAmbienteMin..ranges.temperaturaAmbienteMax,
                    unit = "°C"
                )
            }

            if (lastLuminosidad != null) {
                ParameterStatusRow(
                    parameterName = "Luminosidad",
                    parameterValue = lastLuminosidad.toInt(),
                    range = ranges.luzMin..ranges.luzMax,
                    unit = " luxes"
                )
            }
        }

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
    isPercentage: Boolean = false,
    unit: String = ""
) {
    val isWithinRange = parameterValue in range
    val statusIcon = if (isWithinRange) {
        "✅"
    } else {
        "❌"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Text(
            text = "$parameterName: ",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = if (isPercentage) "$parameterValue%" else "$parameterValue$unit",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = statusIcon,
            style = MaterialTheme.typography.bodyLarge
        )
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
                text = "Luminosidad: $lastLuminosidad lux",
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
fun MeasurementGraph(data: List<Pair<String, Int>>, title: String) {
    val reversedData = data.asReversed()
    val xAxisData = reversedData.mapIndexed { index, dataPoint ->
        when (index) {
            0 -> dataPoint.first
            reversedData.size - 1 -> dataPoint.first
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
                Log.d("MeasurementGraph", "Punto clicado: ${point.x}, ${point.y}")
            }
        )
    }
}

