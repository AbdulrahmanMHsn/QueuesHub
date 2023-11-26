package com.queueshub.ui.orders

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.MainActivity
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.viewStates.CreateOrderViewState


@Composable
fun OrderNoteScreen(paddingValues: PaddingValues = PaddingValues(), router: Router? = null) {

    val context = LocalContext.current

    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)
    sharedViewModel.onUpdate.value

    val goDone: () -> Unit = {
        router?.goDone()
    }

    val uiState by sharedViewModel.state.collectAsState()
    if (uiState.success) {
        goDone()
        sharedViewModel.setStateToIdle()
    }
    if (uiState.loading) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
        ) {
            DialogBoxLoading()
        }
    }
    uiState.failure?.let {
        val content = it.getContentIfNotHandled()
        content?.let {
            Toast.makeText(
                context, it.localizedMessage, Toast.LENGTH_SHORT
            ).show()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        NoteBox(sharedViewModel,uiState)

    }
}

@Composable
fun NoteBox(sharedViewModel: AppViewModel, uiState: CreateOrderViewState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        var notes by remember { mutableStateOf("") }
        Text(
            text = "ملاحظات",
            modifier = Modifier.padding(top = 48.dp),
            style = MaterialTheme.typography.subtitle1
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp)
                .height(300.dp),
            singleLine = false,
            textStyle = MaterialTheme.typography.body1,
            label = { Text("الملاحظات", style = MaterialTheme.typography.body1) },
            placeholder = { Text("اكتب الملاحظات", style = MaterialTheme.typography.body1) },
            shape = RoundedCornerShape(8.dp),
            colors =TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
        )
        Spacer(Modifier.weight(1f))

        AppButton(
            isEnabled = !uiState.loading,
            modifier = Modifier.padding(vertical = 34.dp),
            text = R.string.done,
        ) {
            sharedViewModel.createOrder(notes)
        }
    }
}
