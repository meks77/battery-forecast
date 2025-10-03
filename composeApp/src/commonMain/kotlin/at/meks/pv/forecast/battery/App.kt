package at.meks.pv.forecast.battery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import at.meks.pv.forecast.battery.calculation.CalculatorScreen
import at.meks.pv.forecast.battery.import.ImportScreen
import battery_forecast.composeapp.generated.resources.Res
import battery_forecast.composeapp.generated.resources.app_calculate_icon_desc
import battery_forecast.composeapp.generated.resources.app_calculate_icon_text
import battery_forecast.composeapp.generated.resources.app_import_icon_desc
import battery_forecast.composeapp.generated.resources.app_import_icon_text
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class Destination(
    val icon: ImageVector
) {
    CALCULATOR(Icons.Default.Calculate),
    IMPORT(Icons.Default.ImportExport)
}


@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    val logger: Logger = createLogger("App")
    MaterialTheme {
        var showImport by remember { mutableStateOf(false) }
        var showCalculator by remember { mutableStateOf(true) }
        Scaffold (
            modifier = modifier,
            topBar = {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                    NavigationBarItem(
                        selected = showCalculator,
                        onClick = {
                            showImport = false
                            showCalculator = true
                            logger.debug("Calculator clicked")
                        },
                        icon = {
                            Icon(
                                Destination.CALCULATOR.icon,
                                contentDescription = stringResource(Res.string.app_calculate_icon_desc)
                            )
                        },
                        label = { Text(stringResource(Res.string.app_calculate_icon_text)) }
                    )
                    NavigationBarItem(
                        selected = showImport,
                        onClick = {
                            showImport = true
                            showCalculator = false
                            logger.debug("Import clicked")
                        },
                        icon = {
                            Icon(
                                Destination.IMPORT.icon,
                                contentDescription = stringResource(Res.string.app_import_icon_desc)
                            )
                        },
                        label = { Text(stringResource(Res.string.app_import_icon_text)) }  )
                }

            },
            content = {
                Column(
                    modifier = Modifier.padding(it).scrollable(rememberScrollState(), Orientation.Vertical),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    AnimatedVisibility(showCalculator) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CalculatorScreen()
                        }
                    }
                    AnimatedVisibility(showImport) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ImportScreen()
                        }
                    }
                }
            }

        )
    }
}