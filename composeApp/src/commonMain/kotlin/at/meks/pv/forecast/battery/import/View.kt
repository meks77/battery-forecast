package at.meks.pv.forecast.battery.import

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.meks.pv.forecast.battery.PowerDataRepo
import at.meks.pv.forecast.battery.RuntimeContext.Companion.currentContext
import battery_forecast.composeapp.generated.resources.*
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

enum class ImportScreen {
    ImportSelection,
    ImportConfiguration,
    ImportSuccessfull
}

@Composable
fun ImportScreen(modifier : Modifier = Modifier) {
    val navController = rememberNavController()
    FlowColumn (
        modifier = Modifier.fillMaxSize().padding(10.dp)
    ) {
        NavHost(
            navController = navController,
            startDestination = ImportScreen.ImportSelection.name,
            modifier = Modifier
                .fillMaxSize()
        ) {
            composable(ImportScreen.ImportSelection.name) {
                ImportSelectionScreen(onClickNext = { navController.navigate(ImportScreen.ImportSuccessfull.name)}) }
            composable(ImportScreen.ImportSuccessfull.name) {
                ImportSuccessfullScreen(onContinue = { navController.navigate(ImportScreen.ImportSelection.name) })
            }
        }
    }

}

@Composable
fun ImportSelectionScreen(modifier : Modifier = Modifier, onClickNext: () -> Unit = {}) {
    val powerDataRepo = currentContext().powerDataRepo()

    val powerDataValuesCount = remember { mutableIntStateOf(powerDataRepo.size()) }

    Column(modifier = modifier.padding(10.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {


        BadgedBox(badge =  {
            Badge(contentColor = Color.White) {
                Text(stringResource(Res.string.imported_power_entries, powerDataValuesCount.value))
            }
        },
            modifier = Modifier.padding(10.dp).fillMaxSize().align(Alignment.CenterHorizontally)
        ) {
            FlowRow(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.Center) {
                FileImportButton(PowerDataRepo.PowerType.CONSUMPTION,
                    afterImport = {
                        powerDataValuesCount.intValue = powerDataRepo.size()
                        onClickNext.invoke()
                    })
                FileImportButton(PowerDataRepo.PowerType.FED_IN,
                    afterImport = {
                        powerDataValuesCount.intValue = powerDataRepo.size()
                        onClickNext.invoke()
                    })
                Button(modifier = Modifier.padding(10.dp), onClick = {
                    powerDataRepo.deleteAll()
                    powerDataValuesCount.intValue = powerDataRepo.size()
                }) { Text(stringResource(Res.string.import_delete))}
            }
        }
    }

}

@Composable
fun FileImportButton(fileType: PowerDataRepo.PowerType, afterImport: () -> Unit) {
    val fileImporter = PowerfileImporter()
    val filePickerResult = rememberFilePickerLauncher(type = FileKitType.File(listOf("csv"))) {
        if (it != null) {
            val job = GlobalScope.launch {
                fileImporter.import(it.readString(), fileType)
            }
            
            // TODO: wait with spin wheel or progress bar until import is finished
            afterImport.invoke()
        }
    }
    Button(modifier = Modifier.padding(10.dp), onClick = {
        filePickerResult.launch()
    }) {
        if (fileType == PowerDataRepo.PowerType.CONSUMPTION) {
            Text(stringResource(Res.string.import_button_consumption))
        } else {
            Text(stringResource(Res.string.import_button_fed_in))
        }
    }
}

@Composable
fun ImportSuccessfullScreen(modifier : Modifier = Modifier, onContinue: () -> Unit) {
    Column (horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Import successful")
        Button(onClick = onContinue) {
            Text(text = "Continue")
        }
    }
}