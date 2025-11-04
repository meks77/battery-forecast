package at.meks.pv.forecast.battery.import

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
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
    ImportSuccessfull
}

@Composable
fun ImportScreen(modifier : Modifier = Modifier) {
    val navController = rememberNavController()
    val uriHandler = LocalUriHandler.current
    FlowColumn (
        modifier = Modifier.fillMaxSize().padding(10.dp)
    ) {
        val importHelpLink = stringResource(Res.string.import_help_link)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = { uriHandler.openUri(importHelpLink) },
                modifier = Modifier.testTag("helpButton")
            ) {
                Text(stringResource(Res.string.calculation_help_button))
            }
        }
        NavHost(
            navController = navController,
            startDestination = ImportScreen.ImportSelection.name,
            modifier = Modifier
                .fillMaxSize()
        ) {
            composable(ImportScreen.ImportSelection.name) {
                ImportSelectionScreen(onClickNext = { navController.navigate(ImportScreen.ImportSuccessfull.name) }) }
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
            modifier = Modifier.padding(10.dp)
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
    var showConfig by remember { mutableStateOf(false) }
    var fileContentState by remember { mutableStateOf<String?>(null) }

    // Read the file content when a file is selected, then show the configuration dialog
    LaunchedEffect(pendingFile) {
        pendingFile?.let { file ->
            isImporting = true
            try {
                val fileContent = file.readString()
                fileContentState = fileContent
                showConfig = true
            } finally {
                isImporting = false
                pendingFile = null
            }
        }
    }

    // File picker
    val filePickerResult = rememberFilePickerLauncher(type = FileKitType.File(listOf("csv"))) {
        if (it != null && !isImporting) {
            pendingFile = it // Store the file object instead of reading it immediately
        }
    }

    // Import configuration dialog
    if (showConfig && fileContentState != null) {
        var separator by remember { mutableStateOf(";") }
        var timestampIndex by remember { mutableStateOf(0) }
        var powerIndex by remember { mutableStateOf(1) }
        var hasHeader by remember { mutableStateOf(true) }

        val separatorVm = remember { StringViewModel(1, ";") }
        val timestampVm = remember { IntViewModel(0) }
        val powerVm = remember { IntViewModel(1) }

        AlertDialog(
            onDismissRequest = {
                showConfig = false
                fileContentState = null
            },
            confirmButton = {
                val inputsValid = !separatorVm.inputHasErrors && !timestampVm.inputHasErrors && !powerVm.inputHasErrors
                Button(enabled = inputsValid, onClick = {
                    val structure = FileContentStructure(
                        columnSeparator = separator.first(),
                        colIndexTimestamp = timestampIndex,
                        colIndexPower = powerIndex,
                        containsHeader = hasHeader
                    )
                    // Do the import now with the provided structure
                    fileContentState?.let { content ->
                        isImporting = true
                        try {
                            fileImporter.import(content, fileType, structure)
                            afterImport.invoke()
                        } finally {
                            isImporting = false
                            showConfig = false
                            fileContentState = null
                        }
                    }
                }) {
                    Text(stringResource(Res.string.import_config_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfig = false
                    fileContentState = null
                }) { Text(stringResource(Res.string.import_config_cancel)) }
            },
            title = { Text(stringResource(Res.string.import_config_title)) },
            text = {
                Column {
                    // Inputs
                    FlowRow() {
                        ValidatingInputField(
                            label = stringResource(Res.string.import_config_label_separator),
                            updateState = { value: String -> separator = value },
                            viewModel = separatorVm,
                            supportingText = stringResource(Res.string.import_config_support_separator),
                            modifier = Modifier.width(160.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        ValidatingInputField(
                            label = stringResource(Res.string.import_config_label_timestamp_index),
                            updateState = { value: Int -> timestampIndex = value },
                            viewModel = timestampVm,
                            supportingText = stringResource(Res.string.import_config_support_index_number),
                            modifier = Modifier.width(220.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        ValidatingInputField(
                            label = stringResource(Res.string.import_config_label_power_index),
                            updateState = { value: Int -> powerIndex = value },
                            viewModel = powerVm,
                            supportingText = stringResource(Res.string.import_config_support_index_number),
                            modifier = Modifier.width(200.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasHeader, onCheckedChange = { hasHeader = it })
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.import_config_header_checkbox))
                    }

                    // Live preview
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(Res.string.import_config_preview_title), style = MaterialTheme.typography.titleSmall)

                    var previewRows by remember { mutableStateOf<List<PowerLine>>(emptyList()) }
                    var previewError by remember { mutableStateOf<String?>(null) }
                    val unknownParsingError = stringResource(Res.string.import_config_preview_unknown_error)

                    LaunchedEffect(fileContentState, separator, timestampIndex, powerIndex, hasHeader) {
                        val content = fileContentState ?: return@LaunchedEffect
                        try {
                            val structurePreview = FileContentStructure(
                                columnSeparator = separator.first(),
                                colIndexTimestamp = timestampIndex,
                                colIndexPower = powerIndex,
                                containsHeader = hasHeader
                            )
                            val parser = PowerFileParser(content, structurePreview)
                            previewRows = parser.stream().take(5).toList()
                            previewError = null
                        } catch (e: Exception) {
                            previewRows = emptyList()
                            previewError = e.message ?: unknownParsingError
                        }
                    }

                    if (previewError != null) {
                        Text(previewError!!, color = Color.Red)
                    } else if (previewRows.isEmpty()) {
                        Text(stringResource(Res.string.import_config_preview_no_rows))
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(stringResource(Res.string.import_config_preview_col_timestamp), modifier = Modifier.weight(1f))
                                Text(stringResource(Res.string.import_config_preview_col_power), modifier = Modifier.weight(1f), textAlign = TextAlign.Right)
                            }
                            HorizontalDivider()
                            previewRows.forEach { line ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    Text(line.timestamp.toString(), modifier = Modifier.weight(1f))
                                    Text(line.power.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.Right)
                                }
                            }
                        }
                    }
                }
            }
        )
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