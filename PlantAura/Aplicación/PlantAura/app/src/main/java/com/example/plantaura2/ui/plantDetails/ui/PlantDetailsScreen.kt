package com.example.plantaura2.ui.plantdetails.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.view.ViewGroup
import androidx.compose.ui.viewinterop.AndroidView
import com.example.plantaura2.domain.usecase.GraphUseCase
import com.example.plantaura2.domain.model.HumidityData
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PlantDetailsScreen(navController: NavController, plantName: String) {
    val viewModel: PlantDetailsViewModel = viewModel(
        factory = PlantDetailsViewModelFactory(plantName, GraphUseCase(FirebaseFirestore.getInstance()))
    )
    val plantName by viewModel.plantName.collectAsState()
    val humidityData by viewModel.humidityData.collectAsState()
    val lastHumidity by viewModel.lastHumidity.collectAsState()
    val humidityDeviation by viewModel.humidityDeviation.collectAsState()

    Log.d("PlantDetailsScreen", "Plant name: $plantName")
    Log.d("PlantDetailsScreen", "Last humidity: $lastHumidity")
    Log.d("PlantDetailsScreen", "Humidity deviation: $humidityDeviation")

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "Detalles de la Planta", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Nombre: $plantName", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(16.dp))
            lastHumidity?.let {
                Text(text = "Humedad: $it%", style = MaterialTheme.typography.headlineMedium)
            }
            humidityDeviation?.let {
                val deviationText = if (it >= 0) "Now +${it.toInt()}%" else "Now ${it.toInt()}%"
                Text(text = deviationText, style = TextStyle(color = if (it >= 0) Color.Green else Color.Red))
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (humidityData.isNotEmpty()) {
                HumidityGraph(humidityData)
            } else {
                Text(text = "Cargando datos de humedad...")
            }
        }
    }
}

@Composable
fun HumidityGraph(humidityData: List<HumidityData>) {
    val entries = humidityData.mapIndexed { index, data ->
        Entry(index.toFloat(), data.humidity.toFloat())
    }

    val dataSet = LineDataSet(entries, "Humedad").apply {
        lineWidth = 3f // Aumentamos el grosor de la línea
        setDrawValues(false) // Eliminamos los valores numéricos de cada punto
        valueTextColor = android.graphics.Color.BLACK
        valueTextSize = 10f
        setCircleColor(android.graphics.Color.BLACK) // Color de los puntos
        circleRadius = 5f // Tamaño de los puntos

        // Configurar colores graduales
        for (i in entries.indices) {
            val humidity = entries[i].y
            val color = getGradientColor(humidity)
            colors = listOf(color)
        }
    }

    val lineData = LineData(dataSet)
    LineChartWrapper(data = lineData)
}

fun getGradientColor(humidity: Float): Int {
    return when {
        humidity >= 60 -> android.graphics.Color.RED
        humidity >= 40 -> android.graphics.Color.YELLOW
        else -> android.graphics.Color.GREEN
    }
}

@Composable
fun LineChartWrapper(data: LineData) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            LineChart(context).apply {
                this.data = data
                description.text = "Humedad en el tiempo"
                animateX(1500)

                // Configuración del eje X
                xAxis.valueFormatter = IndexAxisValueFormatter((0 until data.dataSets[0].entryCount).map { it.toString() }) // Ajustar el formateador
                xAxis.granularity = 1f
                xAxis.position = XAxis.XAxisPosition.BOTTOM

                // Configuración del eje Y izquierdo
                axisLeft.granularity = 10f // Intervalos de 10 unidades
                axisLeft.axisMinimum = 0f
                axisLeft.axisMaximum = 100f

                // Deshabilitar el eje derecho
                axisRight.isEnabled = false

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        update = { lineChart ->
            lineChart.data = data
            lineChart.invalidate()
        }
    )
}
