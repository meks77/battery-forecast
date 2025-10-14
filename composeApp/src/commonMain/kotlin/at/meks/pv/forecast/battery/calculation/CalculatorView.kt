package at.meks.pv.forecast.battery.calculation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType.Companion.Number
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import at.meks.pv.forecast.battery.Logger
import at.meks.pv.forecast.battery.RuntimeContext
import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.calculation.model.FeedInTariffs
import at.meks.pv.forecast.battery.createLogger
import battery_forecast.composeapp.generated.resources.*
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.LabelProperties
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.round
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

abstract class ValidatingViewModel<T>(initialValue: String) : ViewModel() {
    var input by mutableStateOf(initialValue)
        private set

    fun updateInput(value: String) {
        input = value
    }

    val inputHasErrors by derivedStateOf {
        !isInputValid(input)
    }

    internal abstract fun isInputValid(input: String): Boolean
    abstract fun convertedValue(): T

}

class DoubleViewModel(initialValue: Double?) : ValidatingViewModel<Double>(initialValue?.toString() ?: "") {
    override fun isInputValid(input: String): Boolean = input.toDoubleOrNull() != null
    override fun convertedValue(): Double = input.toDouble()
}

class IntViewModel(initialValue: Int?) : ValidatingViewModel<Int>(initialValue?.toString() ?: "") {
    override fun isInputValid(input: String): Boolean = input.toIntOrNull() != null
    override fun convertedValue(): Int = input.toInt()
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun <T> ValidatingInputField(
    label: String,
    updateState: (T) -> Unit,
    viewModel: ValidatingViewModel<T>,
    supportingText: String,
    modifier: Modifier = Modifier
) {
    val validatingViewModel = viewModel<ValidatingViewModel<T>>(key = Uuid.random().toString()) { viewModel }
    var inputText by remember { mutableStateOf(validatingViewModel.input) }
    OutlinedTextField(
        modifier = modifier
            .padding(1.dp),
        value = validatingViewModel.input,
        onValueChange = {
            validatingViewModel.updateInput(it)
            inputText = it
            if (!validatingViewModel.inputHasErrors) {
                updateState(validatingViewModel.convertedValue())
            }
        },
        label = { Text(label) },
        isError = validatingViewModel.inputHasErrors,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = Number),
        supportingText = {
            if (validatingViewModel.inputHasErrors) {
                Text(supportingText)
            }
        }
    )
}

@Composable
fun EnergyDiagram(bars: List<Bars>, minValue: Double, maxValue: Double, modifier: Modifier = Modifier, showYLabels: Boolean = true) {
    RowChart(
        modifier = modifier.height(320.dp).padding(horizontal = 0.dp),
        data = bars,
        minValue = minValue,
        maxValue = maxValue,
        barProperties = BarProperties(
            spacing = 3.dp,
            thickness = 7.dp
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        labelProperties = LabelProperties(enabled = showYLabels)
    )
}

@Composable
fun DisplayField(value: String, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        modifier = modifier.padding(1.dp),
        value = value,
        label = { Text(label) },
        singleLine = true,
        readOnly = true,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
        onValueChange = {}
    )
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    val logger: Logger = createLogger("CalculatorScreen")
    var userInput by remember { mutableStateOf(UserInput(0.27, 9.0, 6000, 2024, FeedInTariffs(0.06, 0.1, 0.1))) }
    Column(modifier = modifier.padding(4.dp)) {

        Text(stringResource(Res.string.calculation_params))
        ValidatingInputField(
            updateState = { userInput.year = it },
            label = stringResource(Res.string.calculation_params_year),
            viewModel = IntViewModel(userInput.year),
            supportingText = stringResource(Res.string.calculation_params_error_not_digits),
            modifier = Modifier.testTag("yearInputField")
        )
        Text(stringResource(Res.string.calculation_params_battery))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            maxItemsInEachRow = 4
        ) {
            ValidatingInputField(
                updateState = { userInput.batteryCapacity = it },
                label = stringResource(Res.string.calculation_params_battery_capacity),
                viewModel = DoubleViewModel(userInput.batteryCapacity),
                supportingText = stringResource(Res.string.calculation_params_error_wrong_decimal_number),
                modifier = Modifier.testTag("batteryCapacityInputField")
            )
        }

        Text(stringResource(Res.string.calculation_params_prices_grid))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ValidatingInputField(
                updateState = { userInput.pricePerKwh = it },
                label = stringResource(Res.string.calculation_params_prices_grid_consumption),
                viewModel = DoubleViewModel(userInput.pricePerKwh),
                supportingText = stringResource(Res.string.calculation_params_error_wrong_decimal_number),
                modifier = Modifier.testTag("pricePerKwhInputField")
            )
            ValidatingInputField(
                updateState = { userInput.feedInTariffs.feedInTariffGrid = it },
                label = stringResource(Res.string.calculation_params_prices_grid_feed_in),
                viewModel = DoubleViewModel(userInput.feedInTariffs.feedInTariffGrid),
                supportingText = stringResource(Res.string.calculation_params_error_wrong_decimal_number),
                modifier = Modifier.testTag("feedInGridInputField")
            )
        }

        Text(stringResource(Res.string.calculation_params_prices_community))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {

            ValidatingInputField(
                updateState = { userInput.feedInTariffs.feedInTariffEnergyCommunity = it },
                label = stringResource(Res.string.calculation_params_prices_community_feed_in),
                viewModel = DoubleViewModel(userInput.feedInTariffs.feedInTariffEnergyCommunity),
                supportingText = stringResource(Res.string.calculation_params_error_wrong_decimal_number)
            )
            ValidatingInputField(
                updateState = { userInput.feedInTariffs.percentageAmountDeliveryToCommunity = it / 100.0 },
                label = stringResource(Res.string.calculation_params_prices_community_percent_feed_in),
                viewModel = DoubleViewModel(userInput.feedInTariffs.feedInTariffEnergyCommunity * 100.0),
                supportingText = stringResource(Res.string.calculation_params_error_wrong_decimal_number),
                modifier = Modifier.testTag("feedInCommunityPercentageInputField")
            )
        }
        var fedInKwh by remember { mutableStateOf("") }
        var usedKwhFromBattery by remember { mutableStateOf("") }
        var batteryLifecycles by remember { mutableStateOf("") }
        var savedMoney by remember { mutableStateOf("") }
        var savedMoneyBecauseOfBattery by remember { mutableStateOf("") }
        var lostMoneyBecauseNotFedId by remember { mutableStateOf("") }
        val monthNames = mapOf(
            Pair(Month.JANUARY, stringResource(Res.string.calculation_result_chart_row_label_january)),
            Pair(Month.FEBRUARY, stringResource(Res.string.calculation_result_chart_row_label_february)),
            Pair(Month.MARCH, stringResource(Res.string.calculation_result_chart_row_label_march)),
            Pair(Month.APRIL, stringResource(Res.string.calculation_result_chart_row_label_april)),
            Pair(Month.MAY, stringResource(Res.string.calculation_result_chart_row_label_may)),
            Pair(Month.JUNE, stringResource(Res.string.calculation_result_chart_row_label_june)),
            Pair(Month.JULY, stringResource(Res.string.calculation_result_chart_row_label_july)),
            Pair(Month.AUGUST, stringResource(Res.string.calculation_result_chart_row_label_august)),
            Pair(Month.SEPTEMBER, stringResource(Res.string.calculation_result_chart_row_label_september)),
            Pair(Month.OCTOBER, stringResource(Res.string.calculation_result_chart_row_label_october)),
            Pair(Month.NOVEMBER, stringResource(Res.string.calculation_result_chart_row_label_november)),
            Pair(Month.DECEMBER, stringResource(Res.string.calculation_result_chart_row_label_december)),
        )
        val barNameConsumptionWithBattery = stringResource(Res.string.calculation_result_chart_bar_name_with_battery, stringResource(Res.string.calculation_result_chart_bar_consumption))
        val barNameConsumptionWithoutBattery = stringResource(Res.string.calculation_result_chart_bar_name_without_battery, stringResource(Res.string.calculation_result_chart_bar_consumption))
        val barNameFeedInWithBattery = stringResource(Res.string.calculation_result_chart_bar_name_with_battery, stringResource(Res.string.calculation_result_chart_bar_feed_in))
        val barNameFeedInWithoutBattery = stringResource(Res.string.calculation_result_chart_bar_name_without_battery, stringResource(Res.string.calculation_result_chart_bar_feed_in))
        var consumptionBars by remember { mutableStateOf(listOf(
            monthBar(monthNames[Month.JANUARY]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery),
            monthBar(monthNames[Month.FEBRUARY]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery),
            monthBar(monthNames[Month.MARCH]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery),
            monthBar(monthNames[Month.APRIL]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery),
            monthBar(monthNames[Month.MAY]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery),
            monthBar(monthNames[Month.JUNE]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery),
            monthBar(monthNames[Month.JULY]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery),
            monthBar(monthNames[Month.AUGUST]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery),
            monthBar(monthNames[Month.SEPTEMBER]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery),
            monthBar(monthNames[Month.OCTOBER]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery),
            monthBar(monthNames[Month.NOVEMBER]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery),
            monthBar(monthNames[Month.DECEMBER]!!, 0.0, 0.0, ConsumptionOrFeedIn.CONSUMPTION,
                barNameConsumptionWithoutBattery,
                barNameConsumptionWithBattery)),
        ) }

        var feedInBars by remember { mutableStateOf(listOf(
            monthBar(monthNames[Month.JANUARY]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery),
            monthBar(monthNames[Month.FEBRUARY]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery),
            monthBar(monthNames[Month.MARCH]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery),
            monthBar(monthNames[Month.APRIL]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery),
            monthBar(monthNames[Month.MAY]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery),
            monthBar(monthNames[Month.JUNE]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery),
            monthBar(monthNames[Month.JULY]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery),
            monthBar(monthNames[Month.AUGUST]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery),
            monthBar(monthNames[Month.SEPTEMBER]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery),
            monthBar(monthNames[Month.OCTOBER]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery),
            monthBar(monthNames[Month.NOVEMBER]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery),
            monthBar(monthNames[Month.DECEMBER]!!, 0.0, 0.0, ConsumptionOrFeedIn.FEED_IN,
                barNameFeedInWithoutBattery, barNameFeedInWithBattery)),
        ) }

        var minValueFeedIn by remember { mutableStateOf(-10.0) }
        var maxValueConsumption by remember { mutableStateOf(10.0) }

        Button(onClick = {
            logger.debug("UserInput: $userInput")
            val calculatonResult =
                RuntimeContext.currentContext().forecastCalculator().calculateForecast(userInput.pricePerKwh, userInput.batteryCapacity,
                    userInput.batteryCycles, Year(userInput.year), userInput.feedInTariffs.feedInTariffGrid,
                    userInput.feedInTariffs.feedInTariffEnergyCommunity,
                    userInput.feedInTariffs.percentageAmountDeliveryToCommunity)
            val forecast = calculatonResult.forecast

            fedInKwh = forecast.fedInKwh().round(2).toString()
            usedKwhFromBattery = forecast.consumptionFromBatteryKwh().round(2).toString()
            batteryLifecycles = forecast.batteryCycles().round(2).toString()
            savedMoney = forecast.savedMoneyPerYear().round(2).toString()
            savedMoneyBecauseOfBattery = forecast.savedMoneyBecauseOfSavedPower().round(2).toString()
            lostMoneyBecauseNotFedId = forecast.lostFeedInMoney().round(2).toString()
            val consumptionFromGrid = calculatonResult.originalPowerDataAggregation.consumptionPerMonth()
            val fedInToGrid = calculatonResult.originalPowerDataAggregation.fedInPerMonth()
            consumptionBars = forecast.consumptionFromGrid()
                .entries.sortedBy { entry -> entry.key }
                .map { entry -> monthBar(monthNames[entry.key.month]!!,
                    consumptionFromGrid[entry.key.month]?:0.0, entry.value, ConsumptionOrFeedIn.CONSUMPTION,
                    barNameConsumptionWithoutBattery,
                    barNameConsumptionWithBattery) }
            feedInBars = forecast.feedInPerMonth()
                .entries.sortedBy { entry -> entry.key }
                .map { entry -> monthBar(monthNames[entry.key.month]!!,
                    fedInToGrid[entry.key.month]?:0.0, entry.value.times(-1.0), ConsumptionOrFeedIn.FEED_IN,
                    barNameFeedInWithoutBattery,
                    barNameFeedInWithBattery) }
            val maxConsumption = forecast.consumptionFromGrid().maxOf { it.value }
            val maxFeedIn = forecast.feedInPerMonth().maxOf { it.value }
            val maxValueOfForecast = max(maxConsumption, maxFeedIn)

            val maxOrigConsumption = consumptionFromGrid.maxOf { it.value }
            val maxOrigFeedIn = fedInToGrid.maxOf { it.value.absoluteValue }
            val maxValueOfOriginalData = max(maxOrigConsumption, maxOrigFeedIn)
            max(maxValueOfForecast, maxValueOfOriginalData).let {
                maxValueConsumption = kotlin.math.ceil(it.times(1.1).div(10.0)).times(10.0)
                minValueFeedIn = -maxValueConsumption
            }

            },
            modifier = modifier.fillMaxWidth().testTag("calculationButton")) {
            Text(stringResource(Res.string.calculation_button_start))
        }
        Text(stringResource(Res.string.calculation_result))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DisplayField(fedInKwh, stringResource(Res.string.calculation_result_feed_in_kwh))
            DisplayField(usedKwhFromBattery, stringResource(Res.string.calculation_result_consumption_from_battery), Modifier.testTag("usedKwhFromBatteryField"))
            DisplayField(batteryLifecycles, stringResource(Res.string.calculation_result_battery_charging_cycles))
            DisplayField(value = savedMoney, label = stringResource(Res.string.calculation_result_saved_money), modifier = Modifier.testTag("savedMoneyField"))
            DisplayField(savedMoneyBecauseOfBattery,
                stringResource(Res.string.calculation_result_saved_money_reason_battery))
            DisplayField(lostMoneyBecauseNotFedId,
                stringResource(Res.string.calculation_result_lost_money_reason_not_feed_in))
        }
        Row {
            EnergyDiagram(feedInBars, minValue = minValueFeedIn, maxValue = 0.0, modifier = Modifier.weight(1f))
            EnergyDiagram(consumptionBars, minValue = 0.0, maxValue = maxValueConsumption, modifier = Modifier.weight(1f),
                showYLabels = false)
        }
    }

}

private enum class ConsumptionOrFeedIn(val label: String, val colorWithBattery: Color, val colorWithoutBattery: Color) {
    CONSUMPTION("Bezug", Color.Blue, Color.Green), FEED_IN("Einspeisung", Color.Red, Color.DarkGray);

}

private fun monthBar(monthName: String, withoutBattery: Double, withBattery: Double, type: ConsumptionOrFeedIn,
                     barNameWithoutBattery: String,
                     barNameWithBattery: String
): Bars = Bars(
    label = monthName,
    values = listOf(
        Bars.Data(label = barNameWithoutBattery, value = withoutBattery, color = SolidColor(type.colorWithoutBattery)),
        Bars.Data(label = barNameWithBattery, value = withBattery, color = SolidColor(type.colorWithBattery)),
    ),
)

fun Double.round(decimals: Int): Double {
    var factor = 1
    for (x in 1..decimals) {
        factor *= 10
    }
    return round(this.times(factor)).div(factor)
}
