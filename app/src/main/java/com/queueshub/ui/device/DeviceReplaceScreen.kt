package com.queueshub.ui.device

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.data.api.model.ApiLog
import com.queueshub.data.api.model.ApiLogItem
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.MainActivity
import com.queueshub.ui.car.LogsViewModel
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.main.InputField
import com.queueshub.ui.models.ReplacementUI
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.LightGrey
import com.queueshub.ui.theme.Teal400
import com.queueshub.ui.theme.Tundora

@Composable
fun DeviceReplaceScreen(
    paddingValues: PaddingValues = PaddingValues(),
    router: Router? = null,
) {
    val goNote: () -> Unit = {
        router?.goOrderNote()
    }
    val context = LocalContext.current

    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)
    val vmLog: LogsViewModel = hiltViewModel()

    val uiState by sharedViewModel.state.collectAsState()

    if (uiState.loading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            DialogBoxLoading()
        }
    }
    uiState.failure?.let {
        val content = it.getContentIfNotHandled()
        content?.let {
            Toast.makeText(
                context,
                it.localizedMessage,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        var nextAvailable: Boolean by rememberSaveable { mutableStateOf(false) }
        var selected = remember { mutableStateListOf<Int>(-1) }
        val (title, repairList, subtitle, pic, next) = createRefs()
        Text(
            text = "استبدال",
            modifier = Modifier
                .padding(top = 56.dp)
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            style = MaterialTheme.typography.subtitle1,
        )

        Text(
            modifier = Modifier
                .constrainAs(subtitle) {
                    top.linkTo(title.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(vertical = 19.dp, horizontal = 16.dp),
            text = "اختر امر التشغيل",
            style = MaterialTheme.typography.subtitle1,
        )

        Column(
            modifier = Modifier
                .constrainAs(repairList) {
                    top.linkTo(subtitle.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(vertical = 19.dp, horizontal = 16.dp),
        ) {
            listOf(
                ReplacementUI(
                    "استبدال جهاز",
                    "(IMEI) رقم هوية الجهاز القديم",
                    R.drawable.replace_device,
                    KeyboardType.Number,
                ),
                ReplacementUI(
                    "استبدال شريحة",
                    "رقم المسلسل (SN)",
                    R.drawable.replace_sim,
                    KeyboardType.Number,
                ),
                ReplacementUI(
                    "نقل الجهاز",
                    "رقم اللوحة القديمة",
                    R.drawable.transfer_device,
                ),
            ).forEachIndexed { index, it ->
                SelectableWithInputWidget(
                    modifier = Modifier.padding(vertical = 8.dp),
                    isSelected = selected.find {
                        it == index
                    } != null,
                    id = index.toLong(),
                    replacementUI = it,
                    onToggleChange = { checked, it ->
                        if (checked) {
                            selected.add(it.toInt())
                        } else {
                            selected.remove(it.toInt())
                        }
                        nextAvailable = true
                    },
                    onTextChange = { id, text ->
                        when (id) {
                            0L -> {
                                sharedViewModel.oldImei = text
                            }
                            1L -> {
                                sharedViewModel.oldSim = text
                            }
                            2L -> {
                                sharedViewModel.oldPlate = text
                            }
                        }
                    },
                )
            }
        }
        AppButton(
            modifier = Modifier
                .constrainAs(next) {
                    top.linkTo(repairList.bottom, margin = 24.dp, goneMargin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(bottom = 34.dp),
            text = R.string.done,
            isEnabled = nextAvailable,
        ) {
            sharedViewModel.saveOrderType(
                selected.filter { it != -1 }.map {
                    when (it) {
                        0 -> {
                            "استبدال جهاز"
                        }
                        1 -> {
                            "استبدال شريحة"
                        }
                        else -> {
                            "استبدال لوحة"
                        }
                    }
                } as ArrayList<String>,
            )

            var description = "(الاختيار داخل الغرض  استبدال ونقل الجهاز) :"

            if (sharedViewModel.oldImei.isNotEmpty()) {
                description = description + "استبدال الجهاز رقم هويه قديم(IMEI):  " + sharedViewModel.oldImei + " : "
            }
            if (sharedViewModel.oldSim.isNotEmpty()) {
                description = description + "استبدال الشريحه رقم مسلسل قديم(SN):  " + sharedViewModel.oldSim + " : "
            }

            if (sharedViewModel.oldPlate.isNotEmpty()) {
                description = description + "نقل الجهاز  رقم اللوحه القديمه:  " + sharedViewModel.oldPlate + " : "
            }

            val orderType = ApiLogItem(
                sharedViewModel.plateNum,
                description = description,
                type = "inside_type",
                sharedViewModel.selectedOrder?.id?.toInt(),
                generatedId =sharedViewModel.generatedId,
            )

            val logArray = ArrayList<ApiLogItem>()
            logArray.add(orderType)
            val logModel = ApiLog(logArray)
            vmLog.addLogs(logModel)

            goNote()
        }
    }
}

@Composable
fun SelectableWithInputWidget(
    modifier: Modifier,
    isSelected: Boolean,
    id: Long,
    replacementUI: ReplacementUI,
    onToggleChange: (Boolean, Long) -> Unit,
    onTextChange: (Long, String) -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(replacementUI.value) }
    val selectedTint = Teal400
    val unselectedBorderTint = LightGrey
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = isSelected, enabled = true, onValueChange = { selected ->

                onToggleChange(selected, id)
            })
            .border(
                BorderStroke(
                    1.dp,
                    if (isSelected) selectedTint else unselectedBorderTint,
                ),
                RoundedCornerShape(4.dp),
            )
            .background(Color.White),
    ) {
        val (title, checkbox, pic, input, next) = createRefs()
        Checkbox(
            modifier = Modifier.constrainAs(checkbox) {
                top.linkTo(parent.top, margin = 25.dp)
                start.linkTo(parent.start, margin = 13.dp)
            },
            checked = isSelected,
            onCheckedChange = { onToggleChange(!isSelected, id) },
        )

        Text(
            text = replacementUI.title,
            style = MaterialTheme.typography.subtitle2,
            color = if (isSelected) Teal400 else Tundora,
            modifier = Modifier.constrainAs(title) {
                bottom.linkTo(checkbox.bottom)
                top.linkTo(checkbox.top)
                start.linkTo(checkbox.end, margin = 11.dp)
            },
            maxLines = 1,
        )
        InputField(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(input) {
                    top.linkTo(checkbox.bottom, margin = 26.dp)
                    start.linkTo(parent.start, margin = 13.dp)
                    end.linkTo(parent.end, margin = 13.dp)
                    bottom.linkTo(parent.bottom, margin = 13.dp)
                }
                .padding(horizontal = 13.dp),
            text = replacementUI.subtitle,
            keyboardType = replacementUI.keyboardType,
            imeAction = ImeAction.Done,
            content = value,
            onValueChange = {
                value = it
                onTextChange(id, it)
            },
            isEnabled = isSelected,
        ) {
            value = it
            onTextChange(id, it)
        }

        val image: Painter = painterResource(id = replacementUI.image)

        Image(
            modifier = Modifier.constrainAs(pic) {
                top.linkTo(parent.top, margin = 13.dp)
                end.linkTo(parent.end, margin = 22.dp)
            },
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

@Preview(locale = "ar")
@Composable
fun SelectableWithInputWidgetPreview() {
    SelectableWithInputWidget(
        modifier = Modifier,
        true,
        1,
        ReplacementUI(
            "نقل الجهاز",
            "رقم اللوحة القديمة",
            R.drawable.transfer_device,
        ),
        { b, it -> },
        { id, text -> },
    )
}

@Preview(locale = "ar")
@Composable
fun DeviceReplaceScreenPreview() {
    DeviceReplaceScreen()
}
