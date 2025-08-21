package at.meks.pv.forecast.battery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class Destination(
    val label: String,
    val icon: ImageVector
) {
    CALCULATOR("Calculate", Icons.Default.Calculate),
    IMPORT("Import Power Data", Icons.Default.ImportExport)
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
                                contentDescription = Destination.CALCULATOR.label
                            )
                        },
                        label = { Text(Destination.CALCULATOR.label) }
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
                                contentDescription = Destination.IMPORT.label
                            )
                        },
                        label = { Text(Destination.IMPORT.label) }  )
                }

            }

        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
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
    }
}