package com.example.plantaura2.ui.plantdetails.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.plantaura2.domain.usecase.GraphUseCase
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
        factory = PlantDetailsViewModelFactory(plantName, GraphUseCase(FirebaseFirestore.getInstance()))
    )
    val plantName by viewModel.plantName.collectAsState()
    val measurementData by viewModel.measurementData.collectAsState()
    val lastHumidityAmbiente by viewModel.lastHumidityAmbiente.collectAsState()
    val lastHumiditySuelo by viewModel.lastHumiditySuelo.collectAsState()
    val lastTemperature by viewModel.lastTemperature.collectAsState()

    Log.d("PlantDetailsScreen", "Plant name: $plantName")
    Log.d("PlantDetailsScreen", "Last humidity ambiente: $lastHumidityAmbiente")
    Log.d("PlantDetailsScreen", "Last humidity suelo: $lastHumiditySuelo")
    Log.d("PlantDetailsScreen", "Last temperature: $lastTemperature")

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Habilitar desplazamiento vertical
        ) {
            // Nombre de la planta arriba en el medio
            Text(
                text = plantName,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp), // Incrementar el tamaño del texto
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp)) // Más separación

            // Recuadro para humedad ambiente y gráfica
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp), // Ajustar la altura de la tarjeta
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
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp) // Ajustar el tamaño del texto
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
                    .height(300.dp), // Ajustar la altura de la tarjeta
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
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp) // Ajustar el tamaño del texto
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
                    .height(300.dp), // Ajustar la altura de la tarjeta
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
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp) // Ajustar el tamaño del texto
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (measurementData.isNotEmpty()) {
                        MeasurementGraph(measurementData.map { it.timestamp to it.temperatura.toInt() }, "Temperatura")
                    } else {
                        Text(text = "Cargando datos de temperatura...")
                    }
                }
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
