package at.meks.pv.forecast.battery.calculation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType.Companion.Number
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import at.meks.pv.forecast.battery.Logger
import at.meks.pv.forecast.battery.RuntimeContext
import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.calculation.model.Forecast
import at.meks.pv.forecast.battery.createLogger
import battery_forecast.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
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
        Button(onClick = {
            logger.debug("UserInput: $userInput")
            val forecast = Forecast(inputPrice = userInput.pricePerKwh,
                                    year = Year(userInput.year),
                                    maxBatteryCapacityKwh = userInput.batteryCapacity,
                                    batteryLifetimeCycles = userInput.batteryCycles,
                                    powerData = RuntimeContext.currentContext().powerDataRepo().powerData(Year(userInput.year)),
                                    feedInTariffs = userInput.feedInTariffs)
            fedInKwh = forecast.fedInKwh().round(2).toString()
            usedKwhFromBattery = forecast.usedKwh().round(2).toString()
            batteryLifecycles = forecast.batteryCycles().round(2).toString()
            savedMoney = forecast.savedMoneyPerYear().round(2).toString()
            savedMoneyBecauseOfBattery = forecast.savedMoneyBecauseOfSavedPower().round(2).toString()
            lostMoneyBecauseNotFedId = forecast.lostFeedInMoney().round(2).toString()
        }, modifier = modifier.fillMaxWidth()) {
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
            DisplayField(savedMoneyBecauseOfBattery, stringResource(Res.string.calculation_result_saved_money_reason_battery))
            DisplayField(lostMoneyBecauseNotFedId, stringResource(Res.string.calculation_result_lost_money_reason_not_feed_in))
        }
    }

}

fun Double.round(decimals:Int): Double {
    var factor = 1
    for (x in 1..decimals) {
        factor*=10
    }
    return round(this.times(factor)).div(factor)
}
