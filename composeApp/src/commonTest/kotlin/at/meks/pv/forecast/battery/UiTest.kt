package at.meks.pv.forecast.battery

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import at.meks.pv.forecast.battery.calculation.CalculatorScreen
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test

class UiTest {

    /**
     * Just a smoke test to ensure that the UI is working.
     * Currently, I don't know how to assert the chart.
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testCalculationScreen() = runComposeUiTest {
        val viewModelStoreOwner = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }

        setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                CalculatorScreen()
            }
        }

        onNodeWithTag("yearInputField").performTextReplacement("2024")
        onNodeWithTag("pricePerKwhInputField").performTextReplacement("0.3")
        onNodeWithTag("feedInGridInputField").performTextReplacement("0.06")
        onNodeWithTag("batteryCapacityInputField").performTextReplacement("10")
        onNodeWithTag("feedInCommunityPercentageInputField").performTextReplacement("0")

        RuntimeContext.currentContext().powerDataRepo().addOrReplace(LocalDateTime(2024, 1, 1, 12, 0), 10.0, PowerDataRepo.PowerType.FED_IN)
        RuntimeContext.currentContext().powerDataRepo().addOrReplace(LocalDateTime(2024, 1, 1, 13, 0), 10.0, PowerDataRepo.PowerType.CONSUMPTION)


        onNodeWithTag("calculationButton").performClick()
        onNodeWithTag("usedKwhFromBatteryField").assert(hasText("10.0"))
        onNodeWithTag("savedMoneyField").assert(hasText("2.4"))
    }
}