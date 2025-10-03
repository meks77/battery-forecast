package at.meks.pv.forecast.battery.import

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import at.meks.pv.forecast.battery.PowerDataRepo
import at.meks.pv.forecast.battery.RuntimeContext.Companion.currentContext
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Composable
fun FileImportButton(fileType: PowerDataRepo.PowerType, afterImport: () -> Unit) {
    val fileImporter = PowerfileImporter()
    val filePickerResult = rememberFilePickerLauncher(type = FileKitType.File(listOf("csv"))) {
        if (it != null) {
            GlobalScope.launch {
                fileImporter.import(it.readString(), fileType)
                afterImport.invoke()
            }
        }
    }
    Button(modifier = Modifier.padding(10.dp), onClick = {
        filePickerResult.launch()
    }) {
        Text("Import ${fileType.description}")
    }
}

@Composable
fun ImportScreen(modifier : Modifier = Modifier) {

    val powerDataRepo = currentContext().powerDataRepo()

    val powerDataValuesCount = remember { mutableIntStateOf(powerDataRepo.size()) }

    Box(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        contentAlignment = Alignment.TopCenter,
    ) {

        FlowRow(modifier = Modifier.padding(10.dp)) {
            BadgedBox(badge =  {
                    Badge(contentColor = Color.White) {
                        Text("${powerDataValuesCount.value} Power Data Entries")
                    }
                }
            ) {

            }
        }
        FlowRow(modifier = Modifier.padding(10.dp)) {
            FileImportButton(PowerDataRepo.PowerType.CONSUMPTION,
                afterImport = { powerDataValuesCount.intValue = powerDataRepo.size() })
            FileImportButton(PowerDataRepo.PowerType.FED_IN,
                afterImport = { powerDataValuesCount.intValue = powerDataRepo.size() })
            Button(modifier = Modifier.padding(10.dp), onClick = {
                powerDataRepo.deleteAll()
                powerDataValuesCount.intValue = powerDataRepo.size()
            }) { Text("Delete All")}
        }

    }
}