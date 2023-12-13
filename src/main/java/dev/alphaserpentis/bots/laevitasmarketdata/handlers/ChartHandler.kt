package dev.alphaserpentis.bots.laevitasmarketdata.handlers

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.Plot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.category.StackedBarRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.chart.title.TextTitle
import org.jfree.chart.ui.HorizontalAlignment
import org.jfree.chart.ui.RectangleEdge
import org.jfree.chart.ui.RectangleInsets
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.time.Day
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import javax.imageio.ImageIO

class ChartHandler {
    companion object {
        private val monthMap = mapOf(
            "JAN" to 1,
            "FEB" to 2,
            "MAR" to 3,
            "APR" to 4,
            "MAY" to 5,
            "JUN" to 6,
            "JUL" to 7,
            "AUG" to 8,
            "SEP" to 9,
            "OCT" to 10,
            "NOV" to 11,
            "DEC" to 12
        )
        private val defaultFirstColor = Color("4fe46e".toInt(16))

        fun generateStackedBarGraph(
            title: String,
            xAxisLabel: String,
            yAxisLabel: String,
            data: List<Pair<String, List<Pair<String, Double>>>>
        ): ByteArray {
            val dataset = DefaultCategoryDataset()

            data.forEach { (key, value) ->
                value.forEach {
                    dataset.addValue(it.second, key, it.first)
                }
            }

            val chart = ChartFactory.createStackedBarChart(
                title,
                capitalize(xAxisLabel),
                capitalize(yAxisLabel),
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            )
            val plot = chart.plot as CategoryPlot
            val yAxis = plot.rangeAxis as NumberAxis
            yAxis.numberFormatOverride = NumberFormat.getIntegerInstance()
            val renderer = plot.renderer as StackedBarRenderer

            renderer.setShadowVisible(false)
            renderer.barPainter = StandardBarPainter()
            chart.categoryPlot.renderer = renderer

            applyDefaultChartConfig(chart, title, data.flatMap { it.second })

            return getImageInBytes(ChartPanel(chart), chart)
        }

        fun generateXYTimedSeriesChart(
            title: String,
            xAxisLabel: String,
            yAxisLabel: String,
            data: List<Pair<String, Double>>
        ): ByteArray {
            val dataset = TimeSeriesCollection()
            val series = TimeSeries("Data")

            data.forEach {
                series.add(Day(parseMaturityDate(it.first)), it.second)
            }

            dataset.addSeries(series)

            val chart = ChartFactory.createXYLineChart(
                title,
                capitalize(xAxisLabel),
                capitalize(yAxisLabel),
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            )
            chart.removeLegend()

            val dateFormat = SimpleDateFormat("d MMM ''yy")
            val dateAxis = DateAxis(xAxisLabel)
            dateAxis.dateFormatOverride = dateFormat
            chart.xyPlot.domainAxis = dateAxis

            applyDefaultChartConfig(chart, title, data)

            return getImageInBytes(ChartPanel(chart), chart)
        }

        fun generateXYTimedSeriesChart(
            title: String,
            xAxisLabel: String,
            yAxisLabel: String,
            data: HashMap<String, List<Pair<String, Double>>>
        ): ByteArray {
            val dataset = TimeSeriesCollection()

            data.forEach { (key, value) ->
                val timeSeries = TimeSeries(key)

                value.forEach {
                    timeSeries.add(Day(parseMaturityDate(it.first)), it.second)
                }

                dataset.addSeries(timeSeries)
            }

            val chart = ChartFactory.createXYLineChart(
                title,
                capitalize(xAxisLabel),
                capitalize(yAxisLabel),
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            )

            val dateFormat = SimpleDateFormat("d MMM ''yy")
            val dateAxis = DateAxis(xAxisLabel)
            dateAxis.dateFormatOverride = dateFormat
            chart.xyPlot.domainAxis = dateAxis

            applyDefaultChartConfig(chart, title, data.values.flatten())

            return getImageInBytes(ChartPanel(chart), chart)
        }

        fun generateXYSeriesChart(
            title: String,
            xAxisLabel: String,
            yAxisLabel: String,
            data: List<Pair<String, Double>>
        ): ByteArray {
            val dataset = XYSeriesCollection()
            val series = XYSeries("Data")

            data.forEach {
                series.add(it.first.toDouble(), it.second)
            }

            dataset.addSeries(series)

            val chart = ChartFactory.createXYLineChart(
                title,
                capitalize(xAxisLabel),
                capitalize(yAxisLabel),
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            )
            chart.removeLegend()

            applyDefaultChartConfig(chart, title, data)

            return getImageInBytes(ChartPanel(chart), chart)
        }

        fun generateXYSeriesChart(
            title: String,
            xAxisLabel: String,
            yAxisLabel: String,
            data: HashMap<String, List<Pair<String, Double>>>
        ): ByteArray {
            val dataset = XYSeriesCollection()

            data.forEach { (key, value) ->
                val series = XYSeries(key)

                value.forEach {
                    series.add(it.first.toDouble(), it.second)
                }

                dataset.addSeries(series)
            }

            val chart = ChartFactory.createXYLineChart(
                title,
                capitalize(xAxisLabel),
                capitalize(yAxisLabel),
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            )
            applyDefaultChartConfig(chart, title, data.values.flatten())

            return getImageInBytes(ChartPanel(chart), chart)
        }

        private fun applyDefaultChartConfig(chart: JFreeChart, title: String, data: List<Pair<String, Double>>) {
            // Sets the whole background
            chart.backgroundPaint = Color("0E151A".toInt(16))

            // Sets the plotting area
            val plot = chart.plot

            if (plot is XYPlot) {
                plot.renderer.setSeriesPaint(0, defaultFirstColor)
                plot.domainGridlinePaint = Color.DARK_GRAY
                plot.rangeGridlinePaint = Color.DARK_GRAY
                configurePlot(plot, data)
            } else if (plot is CategoryPlot) {
                plot.renderer.setSeriesPaint(0, defaultFirstColor)
                plot.domainGridlinePaint = Color.DARK_GRAY
                plot.rangeGridlinePaint = Color.DARK_GRAY
            }

            configureAxisLabels(plot)

            plot.backgroundPaint = Color("0E151A".toInt(16))
            plot.isOutlineVisible = false

            // Sets the title and source
            chart.setTitle(generateTitle(title))
            chart.addSubtitle(generateSourceSubtitle())

            // Configures the legend
            val legend = chart.legend

            if (legend != null) {
                legend.itemFont = Font("SansSerif", Font.PLAIN, 22)
                legend.backgroundPaint = Color("0E151A".toInt(16))
                legend.itemPaint = Color.WHITE
                legend.padding = RectangleInsets(0.0, 0.0, 20.0, 0.0)
            }
        }

        /**
         * Configure the chart's axis labels with a specific font
         */
        private fun configureAxisLabels(plot: Plot) {
            val font = Font("SansSerif", Font.PLAIN, 32)
            val tickFont = Font("SansSerif", Font.PLAIN, 22)

            if (plot is XYPlot) {
                val domainAxis = plot.domainAxis
                val rangeAxis = plot.rangeAxis

                domainAxis.labelFont = font
                rangeAxis.labelFont = font
                domainAxis.tickLabelFont = tickFont
                rangeAxis.tickLabelFont = tickFont
                rangeAxis.labelPaint = Color.WHITE
                rangeAxis.tickLabelPaint = Color.WHITE
                rangeAxis.axisLinePaint = Color.WHITE
                domainAxis.labelPaint = Color.WHITE
                domainAxis.tickLabelPaint = Color.WHITE
                domainAxis.axisLinePaint = Color.WHITE
            } else if (plot is CategoryPlot) {
                val domainAxis = plot.domainAxis
                val rangeAxis = plot.rangeAxis

                domainAxis.labelFont = font
                rangeAxis.labelFont = font
                domainAxis.tickLabelFont = tickFont
                rangeAxis.tickLabelFont = tickFont
                rangeAxis.labelPaint = Color.WHITE
                rangeAxis.tickLabelPaint = Color.WHITE
                rangeAxis.axisLinePaint = Color.WHITE
                domainAxis.labelPaint = Color.WHITE
                domainAxis.tickLabelPaint = Color.WHITE
                domainAxis.axisLinePaint = Color.WHITE
            }
        }

        private fun generateTitle(name: String): TextTitle {
            val title = TextTitle(name)

            title.font = Font("SansSerif", Font.PLAIN, 52)
            title.paint = Color.WHITE
            title.position = RectangleEdge.TOP
            title.setPadding(20.0, 0.0, 10.0, 0.0)
            return title
        }

        /**
         * Generates the source subtitle for the chart.
         * @return The source subtitle
         */
        private fun generateSourceSubtitle(): TextTitle {
            val sourceSubtitle = TextTitle("Source: laevitas.ch, ${generatedTimeNowUTC()}")

            sourceSubtitle.font = Font("SansSerif", Font.PLAIN, 22)
            sourceSubtitle.paint = Color.WHITE
            sourceSubtitle.position = RectangleEdge.BOTTOM
            sourceSubtitle.horizontalAlignment = HorizontalAlignment.LEFT
            sourceSubtitle.setPadding(0.0, 20.0, 0.0, 0.0)

            return sourceSubtitle
        }

        /**
         * Configures the plot's bounds
         * @param plot The plot to configure
         * @param data The data to use to configure the plot
         */
        private fun configurePlot(plot: XYPlot, data: List<Pair<String, Double>>) {
            val lowerBound = data.asSequence()
                .map { it.second }
                .minOrNull() ?: 0.0
            val upperBound = data.maxOf { it.second }
            val upperBoundMultiplier = if (upperBound < 0) 0.99 else 1.01
            val lowerBoundMultiplier = if (lowerBound < 0) 1.01 else 0.99

            plot.rangeAxis.setRange(lowerBoundMultiplier * lowerBound, upperBoundMultiplier * upperBound)
        }

        /**
         * Generates a byte array of the chart image.
         * @param panel The chart panel
         * @param chart The chart
         * @return The byte array of the chart image
         */
        private fun getImageInBytes(panel: ChartPanel, chart: JFreeChart): ByteArray {
            panel.preferredSize = Dimension(2560, 1440)
            val chartImage = chart.createBufferedImage(2560, 1440)

            ByteArrayOutputStream(3686400).use {
                ImageIO.write(chartImage, "png", it)
                return it.toByteArray()
            }
        }

        private fun parseMaturityDate(dateStr: String): Date {
            val dayEndIndex = if (Character.isDigit(dateStr[1])) 2 else 1
            val day = dateStr.substring(0, dayEndIndex).toInt()
            val monthStr = dateStr.substring(dayEndIndex, dayEndIndex + 3)
            val year = dateStr.substring(dayEndIndex + 3).toInt() + 2000
            val month = monthMap[monthStr.uppercase(Locale.ROOT)] ?: error("Invalid month")
            val dateInMilliseconds = LocalDate.of(year, month, day).atStartOfDay(ZoneOffset.UTC).toInstant()
                .toEpochMilli()

            return Date(dateInMilliseconds)
        }

        private fun capitalize(str: String) = str.substring(0, 1).uppercase(Locale.ROOT) + str.substring(1)

        private fun generatedTimeNowUTC(): String {
            val utcTime = LaevitasDataHandler.getUTCTimeFromMilli(System.currentTimeMillis())

            return "$utcTime UTC"
        }
    }
}
