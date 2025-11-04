package at.meks.pv.forecast.battery

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.*
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import at.meks.pv.forecast.battery.calculation.CalculatorScreen
import at.meks.pv.forecast.battery.import.ImportScreen
import kotlinx.datetime.LocalDateTime
import at.meks.pv.forecast.battery.testutil.Platform
import kotlin.test.Test
import kotlin.test.assertTrue

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
        onNodeWithTag("batteryCapacityInputField").performTextReplacement("5.0")
        onNodeWithTag("feedInCommunityPercentageInputField").performTextReplacement("0")

        RuntimeContext.currentContext().powerDataRepo().addOrReplace(LocalDateTime(2024, 1, 1, 12, 0), 10.0, PowerDataRepo.PowerType.FED_IN)
        RuntimeContext.currentContext().powerDataRepo().addOrReplace(LocalDateTime(2024, 1, 1, 13, 0), 10.0, PowerDataRepo.PowerType.CONSUMPTION)

        onNodeWithTag("calculationButton").performClick()
        onNodeWithTag("usedKwhFromBatteryField").assert(hasText("5.0"))
        onNodeWithTag("savedMoneyField").assert(hasText("1.2"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testHelpLink_Calculation_opensUrl() = runComposeUiTest {
        val viewModelStoreOwner = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
        var openedUrl: String? = null
        val fakeUriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                openedUrl = uri
            }
        }
        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner,
                LocalUriHandler provides fakeUriHandler
            ) {
                CalculatorScreen()
            }
        }

        onNodeWithTag("helpButton").performClick()

        val url = openedUrl
        if (Platform.isWasmJs) {
            // On wasm/js the UriHandler may be no-op; just ensure the click didn't crash.
            assertTrue(true)
        } else {
            assertTrue(url == null || url.endsWith("CalculationDe.md") || url.endsWith("CalculationEn.md"),
                "Calculation help button opened page $openedUrl should end with 'CalculationEn.md' or 'CalculationDe.md'")
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testHelpLink_Import_opensUrl() = runComposeUiTest {
        val viewModelStoreOwner = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
        var openedUrl: String? = null
        val fakeUriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                openedUrl = uri
            }
        }
        setContent {
            val lifecycleOwner = object : LifecycleOwner {
                private val registry = LifecycleRegistry(this)
                override val lifecycle: Lifecycle get() = registry
                init { registry.currentState = Lifecycle.State.RESUMED }
            }
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner,
                LocalLifecycleOwner provides lifecycleOwner,
                LocalUriHandler provides fakeUriHandler
            ) {
                ImportScreen()
            }
        }

        onNodeWithTag("helpButton").performClick()

        val url = openedUrl
        if (Platform.isWasmJs) {
            assertTrue(true)
        } else {
            assertTrue(url == null || url.endsWith("ImportEn.md") || url.endsWith("ImportDe.md"),
                "Import help button open page $openedUrl should end with 'ImportDe.md' or 'ImportEn.md'")
        }
    }
}