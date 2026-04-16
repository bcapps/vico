package com.example.vicobackfillrepro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private const val autoReplayDelayMillis = 8_000L
private const val backfillDelayMillis = 3_000L
private const val dataPointsPerPage = 28
private const val historyPageCount = 4
private const val visibleDomainDays = (dataPointsPerPage - 1).toDouble()
private const val xAxisLabelSpacing = 7
private const val xAxisLabelOffset = 3

private val axisDateFormatter = DateTimeFormatter.ofPattern("M/d", Locale.US)
private val baseDate = LocalDate.of(2025, 12, 29)

private val historyX = (0 until dataPointsPerPage * historyPageCount).toList()
private val historyY =
    historyX.map { index ->
        10_350 + ((index % 9) * 6) - (index / 5)
    }
private val cachedX = historyX.takeLast(dataPointsPerPage)
private val cachedY = historyY.takeLast(dataPointsPerPage)

private val startAxisValueFormatter =
    CartesianValueFormatter.decimal(
        prefix = "$",
        thousandsSeparator = ",",
        decimalCount = 0,
    )

private val bottomAxisValueFormatter =
    object : CartesianValueFormatter {
        override fun format(
            context: com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext,
            value: Double,
            verticalAxisPosition: Axis.Position.Vertical?,
        ): String {
            return axisDateFormatter.format(baseDate.plusDays(value.toLong()))
        }
    }

private val rangeProvider =
    CartesianLayerRangeProvider.fixed(
        minY = 0.0,
        maxY = 12_500.0,
    )

private val xAxisItemPlacer =
    HorizontalAxis.ItemPlacer.aligned(
        spacing = { xAxisLabelSpacing },
        offset = { xAxisLabelOffset },
        shiftExtremeLines = false,
        addExtremeLabelPadding = false,
    )

private val yAxisItemPlacer = VerticalAxis.ItemPlacer.count(count = { 5 })

private enum class ReplayPhase(@StringRes val labelResId: Int) {
    Cached(R.string.loading_phase_cached),
    FullHistory(R.string.loading_phase_full),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BackfillReproScreen()
                }
            }
        }
    }
}

@Composable
private fun BackfillReproScreen(modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    var replayGeneration by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(ReplayPhase.Cached) }

    val pageZoom = remember {
        Zoom.max(
            Zoom.x(visibleDomainDays),
            Zoom.Content,
        )
    }
    val scrollState = rememberVicoScrollState(
        scrollEnabled = phase == ReplayPhase.FullHistory,
        initialScroll = Scroll.Absolute.End,
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth,
        autoScrollAnimationSpec = snap(),
    )
    val zoomState = rememberVicoZoomState(
        zoomEnabled = false,
        initialZoom = pageZoom,
        minZoom = pageZoom,
        maxZoom = pageZoom,
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(autoReplayDelayMillis)
            replayGeneration++
        }
    }

    LaunchedEffect(replayGeneration) {
        phase = ReplayPhase.Cached
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = cachedX,
                    y = cachedY,
                )
            }
        }
        delay(backfillDelayMillis)
        phase = ReplayPhase.FullHistory
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = historyX,
                    y = historyY,
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PaddingValues(20.dp)),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.repro_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.repro_subtitle),
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = { replayGeneration++ }) {
            Text(text = stringResource(R.string.replay_transition))
        }
        Text(
            text = stringResource(phase.labelResId),
            style = MaterialTheme.typography.labelLarge,
        )
        Card {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                CartesianChartHost(
                    chart = rememberReproChart(),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    scrollState = scrollState,
                    zoomState = zoomState,
                )
            }
        }
        Text(
            text = stringResource(R.string.auto_replay_notice),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun rememberReproChart() =
    rememberCartesianChart(
        rememberLineCartesianLayer(
            pointSpacing = 20.dp,
            rangeProvider = rangeProvider,
        ),
        endAxis = VerticalAxis.rememberEnd(
            valueFormatter = startAxisValueFormatter,
            itemPlacer = yAxisItemPlacer,
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            valueFormatter = bottomAxisValueFormatter,
            itemPlacer = xAxisItemPlacer,
        ),
        layerPadding = { CartesianLayerPadding() },
    )

@Composable
@Preview
private fun BackfillReproScreenPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BackfillReproScreen()
        }
    }
}
