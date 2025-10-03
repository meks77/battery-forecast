package at.meks.pv.forecast.battery.calculation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import kotlin.math.absoluteValue
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

    abstract internal fun isInputValid(input: String): Boolean
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

        Text("Calculation Params")
        ValidatingInputField(
            updateState = { userInput.year = it },
            label = "Calculated year",
            viewModel = IntViewModel(userInput.year),
            supportingText = ERROR_TEXT_WRONG_INT
        )
        Text("Battery Parameters")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            maxItemsInEachRow = 4
        ) {
            ValidatingInputField(
                updateState = { userInput.batteryCapacity = it },
                label = "Battery capacity",
                viewModel = DoubleViewModel(userInput.batteryCapacity),
                supportingText = ERROR_TEXT_WRONG_DECIMAL_NUMBER
            )
            ValidatingInputField(
                updateState = { userInput.batteryCycles = it },
                label = "Battery Lifetime Cycles",
                viewModel = IntViewModel(userInput.batteryCycles),
                supportingText = ERROR_TEXT_WRONG_INT
            )
        }

        Text("Energy Prices Grid")
        ValidatingInputField(
            updateState = { userInput.pricePerKwh = it },
            label = "Price per kWh",
            viewModel = DoubleViewModel(userInput.pricePerKwh),
            supportingText = ERROR_TEXT_WRONG_DECIMAL_NUMBER
        )

        Text("Energy Prices Community")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ValidatingInputField(
                updateState = { userInput.feedInTariffs.feedInTariffGrid = it },
                label = "Feed-in tariff grid",
                viewModel = DoubleViewModel(userInput.feedInTariffs.feedInTariffGrid),
                supportingText = ERROR_TEXT_WRONG_DECIMAL_NUMBER
            )
            ValidatingInputField(
                updateState = { userInput.feedInTariffs.feedInTariffEnergyCommunity = it },
                label = "Feed-in tariff energy community",
                viewModel = DoubleViewModel(userInput.feedInTariffs.feedInTariffEnergyCommunity),
                supportingText = ERROR_TEXT_WRONG_DECIMAL_NUMBER
            )
            ValidatingInputField(
                updateState = { userInput.feedInTariffs.percentageAmountDeliveryToCommunity = it / 100.0 },
                label = "% of fed in energy to community",
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
            fedInKwh = forecast.fedInKwh().toString()
            usedKwhFromBattery = forecast.usedKwh().toString()
            batteryLifecycles = forecast.batteryCycles().toString()
            savedMoney = forecast.savedMoneyPerYear().absoluteValue.toString()
            savedMoneyBecauseOfBattery = forecast.savedMoneyBecauseOfSavedPower().toString()
            lostMoneyBecauseNotFedId = forecast.lostFeedInMoney().toString()
            logger.debug("Forecast saved money per year: ${forecast.savedMoneyPerYear()}")
        }, modifier = modifier.fillMaxWidth()) {
            Text("Calculate")
        }
        Text("Calculation Results")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DisplayField(fedInKwh, "Fed in kWh")
            DisplayField(usedKwhFromBattery, "Used kWh from battery")
            DisplayField(batteryLifecycles, "Battery Lifetime Cycles")
            DisplayField(savedMoney, "Saved money")
            DisplayField(savedMoneyBecauseOfBattery, "Saved money/saved to battery")
            DisplayField(lostMoneyBecauseNotFedId, "Lost money/not fed in")
        }
    }
}