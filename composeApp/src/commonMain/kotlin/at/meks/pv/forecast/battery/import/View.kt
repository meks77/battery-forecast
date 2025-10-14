package at.meks.pv.forecast.battery.import

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.meks.pv.forecast.battery.PowerDataRepo
import at.meks.pv.forecast.battery.RuntimeContext.Companion.currentContext
import at.meks.pv.forecast.battery.calculation.ValidatingViewModel
import battery_forecast.composeapp.generated.resources.*
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readString
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
    var isImporting by remember { mutableStateOf(false) }
    var pendingFile by remember { mutableStateOf<io.github.vinceglb.filekit.PlatformFile?>(null) }
    
    LaunchedEffect(pendingFile) {
        pendingFile?.let { file ->
            isImporting = true
            try {
                val fileContent = file.readString()
                // TODO: add a preview of interpreted content
                // TODO: start import after configuring and confirming

                fileImporter.import(fileContent, fileType)
                afterImport.invoke()
            } finally {
                isImporting = false
                pendingFile = null
            }
        }
    }
    
    val filePickerResult = rememberFilePickerLauncher(type = FileKitType.File(listOf("csv"))) {
        if (it != null && !isImporting) {
            pendingFile = it // Store the file object instead of reading it immediately
        }
    }
    
    Button(
        modifier = Modifier.padding(10.dp), 
        onClick = { filePickerResult.launch() },
        enabled = !isImporting
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importing...")
            } else {
                if (fileType == PowerDataRepo.PowerType.CONSUMPTION) {
                    Text(stringResource(Res.string.import_button_consumption))
                } else {
                    Text(stringResource(Res.string.import_button_fed_in))
                }
            }
        }
    }
}

@Composable
fun ImportPreview(fileContent: String) {

    FlowRow() {
        ValidatingInputField(label = "Separator", updateState = {}, viewModel = StringViewModel(1, ";"), supportingText = "Das Trennzeichen muss ein einzelnes Zeichen sein"  )
        ValidatingInputField(label = "Zeitstempel-Spaltenindex", updateState = {}, viewModel = IntViewModel(0), supportingText = "Der Index muss eine Zahl sein"  )
        ValidatingInputField(label = "Energiemenge-Spaltenindex", updateState = {}, viewModel = IntViewModel(1), supportingText = "Der Index muss eine Zahl sein"  )
        Text("Spaltenüberschrift vorhanden")
        Checkbox(checked = true, onCheckedChange = {})

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
        supportingText = {
            if (validatingViewModel.inputHasErrors) {
                Text(supportingText)
            }
        }
    )
}

class StringViewModel(val length: Int, initialValue: String) : ValidatingViewModel<String>(initialValue) {
    override fun isInputValid(input: String): Boolean = input.length == length
    override fun convertedValue(): String = input
}

class IntViewModel(initialValue: Int?) : ValidatingViewModel<Int>(initialValue?.toString() ?: "") {
    override fun isInputValid(input: String): Boolean = input.toIntOrNull() != null
    override fun convertedValue(): Int = input.toInt()
}