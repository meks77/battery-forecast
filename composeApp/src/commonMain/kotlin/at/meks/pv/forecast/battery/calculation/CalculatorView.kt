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
import androidx.compose.ui.text.input.KeyboardType.Companion.Number
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import at.meks.pv.forecast.battery.Logger
import at.meks.pv.forecast.battery.RuntimeContext
import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.calculation.model.Battery
import at.meks.pv.forecast.battery.calculation.model.Forecast
import at.meks.pv.forecast.battery.calculation.model.PhotovoltaikSystem
import at.meks.pv.forecast.battery.createLogger
import battery_forecast.composeapp.generated.resources.*
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.LabelProperties
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource
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
    supportingText: String
) {
    val validatingViewModel = viewModel<ValidatingViewModel<T>>(key = Uuid.random().toString()) { viewModel }
    var inputText by remember { mutableStateOf(validatingViewModel.input) }
    OutlinedTextField(
        modifier = Modifier
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
fun DisplayField(value: String, label: String) {
    OutlinedTextField(
        modifier = Modifier.padding(1.dp),
        value = value,
        label = { Text(label) },
        singleLine = true,
        readOnly = true,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
        onValueChange = {}
    )
}

private const val ERROR_TEXT_WRONG_DECIMAL_NUMBER = "Please enter a valid number with one decimal point."

private const val ERROR_TEXT_WRONG_INT = "Please enter only digits."

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    val logger: Logger = createLogger("CalculatorScreen")
    val userInput = UserInput(0.27, 9.0, 6000, 2024, FeedInTariffs(0.06, 0.1, 0.1))
    Column(modifier = modifier.padding(4.dp)) {

        Text(stringResource(Res.string.calculation_params))
        ValidatingInputField(
            updateState = { userInput.year = it },
            label = stringResource(Res.string.calculation_params_year),
            viewModel = IntViewModel(userInput.year),
            supportingText = ERROR_TEXT_WRONG_INT
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
                supportingText = ERROR_TEXT_WRONG_DECIMAL_NUMBER
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
                supportingText = ERROR_TEXT_WRONG_DECIMAL_NUMBER
            )
            ValidatingInputField(
                updateState = { userInput.feedInTariffs.feedInTariffGrid = it },
                label = stringResource(Res.string.calculation_params_prices_grid_feed_in),
                viewModel = DoubleViewModel(userInput.feedInTariffs.feedInTariffGrid),
                supportingText = ERROR_TEXT_WRONG_DECIMAL_NUMBER
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
                supportingText = ERROR_TEXT_WRONG_DECIMAL_NUMBER
            )
            ValidatingInputField(
                updateState = { userInput.feedInTariffs.percentageAmountDeliveryToCommunity = it / 100.0 },
                label = stringResource(Res.string.calculation_params_prices_community_percent_feed_in),
                viewModel = DoubleViewModel(userInput.feedInTariffs.feedInTariffEnergyCommunity * 100.0),
                supportingText = ERROR_TEXT_WRONG_DECIMAL_NUMBER
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
            val powerData = RuntimeContext.currentContext().powerDataRepo().powerData(Year(userInput.year))
            val forecast = Forecast(inputPrice = userInput.pricePerKwh,
                year = Year(userInput.year),
                maxBatteryCapacityKwh = userInput.batteryCapacity,
                batteryLifetimeCycles = userInput.batteryCycles,
                powerData = powerData,
                feedInTariffs = userInput.feedInTariffs)
            val photovoltaikSystem = PhotovoltaikSystem(Battery(0.0, 6000))
            powerData.powerdataForYear(Year(userInput.year)).forEach(photovoltaikSystem::add)


            fedInKwh = forecast.fedInKwh().round(2).toString()
            usedKwhFromBattery = forecast.consumptionFromBatteryKwh().round(2).toString()
            batteryLifecycles = forecast.batteryCycles().round(2).toString()
            savedMoney = forecast.savedMoneyPerYear().round(2).toString()
            savedMoneyBecauseOfBattery = forecast.savedMoneyBecauseOfSavedPower().round(2).toString()
            lostMoneyBecauseNotFedId = forecast.lostFeedInMoney().round(2).toString()
            val consumptionFromGrid = photovoltaikSystem.consumptionFromGrid()
            val fedInToGrid = photovoltaikSystem.feedInToGrid()
            consumptionBars = forecast.consumptionFromGrid()
                .entries.sortedBy { entry -> entry.key }
                .map { entry -> monthBar(monthNames[entry.key.month]!!,
                    consumptionFromGrid[entry.key]?:0.0, entry.value, ConsumptionOrFeedIn.CONSUMPTION,
                    barNameConsumptionWithoutBattery,
                    barNameConsumptionWithBattery) }
            feedInBars = forecast.feedInPerMonth()
                .entries.sortedBy { entry -> entry.key }
                .map { entry -> monthBar(monthNames[entry.key.month]!!,
                    fedInToGrid[entry.key]?.times(-1.0)?:0.0, entry.value.times(-1.0), ConsumptionOrFeedIn.FEED_IN,
                    barNameFeedInWithoutBattery,
                    barNameFeedInWithBattery) }
            val maxConsumption = forecast.consumptionFromGrid().maxOf { it.value }
            val maxFeedIn = forecast.feedInPerMonth().maxOf { it.value }
            max(maxConsumption, maxFeedIn).let {
                maxValueConsumption = kotlin.math.ceil(it.times(1.1).div(10.0)).times(10.0)
                minValueFeedIn = -maxValueConsumption
            }},
            modifier = modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.calculation_button_start))
        }
        Text(stringResource(Res.string.calculation_result))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DisplayField(fedInKwh, stringResource(Res.string.calculation_result_feed_in_kwh))
            DisplayField(usedKwhFromBattery, stringResource(Res.string.calculation_result_consumption_from_battery))
            DisplayField(batteryLifecycles, stringResource(Res.string.calculation_result_battery_charging_cycles))
            DisplayField(savedMoney, stringResource(Res.string.calculation_result_saved_money))
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
