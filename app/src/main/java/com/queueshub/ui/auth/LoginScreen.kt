package com.queueshub.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.queueshub.ui.navigation.Router
import com.queueshub.R
import com.queueshub.ui.MainActivity
import com.queueshub.ui.MainActivityUiState
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.main.InputField
import timber.log.Timber

private lateinit var auth: FirebaseAuth

@Composable
fun LoginScreen(
    paddingValues: PaddingValues = PaddingValues(),
    router: Router? = null
) {
    val context = LocalContext.current
    auth = Firebase.auth
    val viewModel: LoginViewModel = hiltViewModel()
    val loginState by viewModel.state.collectAsState()
    val goHome: () -> Unit = {
        router?.goHome()
    }
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var phone by rememberSaveable { mutableStateOf("") }
            var pin by rememberSaveable { mutableStateOf("") }
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "أهلاً بعودتك!",
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = "ادخل بياناتك لتسجيل الدخول على حسابك الشخصي",
                style = MaterialTheme.typography.subtitle2
            )
            Spacer(
                modifier = Modifier.height(height = 45.dp)
            )
            InputField(
                stringResource(id = R.string.phone),
                KeyboardType.Phone,
                phone,
                onValueChange = {
                    phone = it
                },
                imeAction = ImeAction.Next
            )

            InputField(
                stringResource(id = R.string.password),
                KeyboardType.NumberPassword,
                pin,
                onValueChange = {
                    pin = it
                },
                imeAction = ImeAction.Go
            ) {
                viewModel.startLogin(phone, pin)
            }
            AppButton(modifier = Modifier.padding(top = 80.dp), R.string.login) {
                viewModel.startLogin(phone, pin)
            }

        }
        loginState.user?.let {

            Log.e("topic  is", "user_${it.id}")

            FirebaseMessaging.getInstance()
                .subscribeToTopic("user_${it.id}")
            auth.signInAnonymously()
                .addOnCompleteListener(context as MainActivity) { task ->
                    if (task.isSuccessful) {
                        goHome()
                    } else {
                        // If sign in fails, display a message to the user.
                        Timber.tag("signInAnonymously").w(task.exception, ":failure")
                    }
                }
//            if (!hasAlreadyNavigated.value) {
//                onLoginSuccess()
//                hasAlreadyNavigated.value = true
//            }
        }
        if (loginState.loading) {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.matchParentSize()
            ) {
                DialogBoxLoading()
            }
        }
        loginState.failure?.let {
            val content = it.getContentIfNotHandled()
            content?.let {
                Toast.makeText(
                    context, it.localizedMessage, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}


@Preview(locale = "ar", showBackground = true)
@Composable
fun LoginPreview() {
    // Mock data for preview
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = "أهلاً بعودتك!",
            style = MaterialTheme.typography.subtitle1
        )
        Text(
            text = "ادخل بياناتك لتسجيل الدخول على حسابك الشخصي",
            style = MaterialTheme.typography.subtitle2
        )
        Spacer(
            modifier = Modifier.height(height = 45.dp)
        )
        
        // Mock input fields
        InputField(
            stringResource(id = R.string.phone),
            KeyboardType.Phone,
            "01234567890",
            onValueChange = {},
            imeAction = ImeAction.Next
        ) {}

        InputField(
            stringResource(id = R.string.password),
            KeyboardType.NumberPassword,
            "123456",
            onValueChange = {},
            imeAction = ImeAction.Go
        ) {}
        
        AppButton(
            modifier = Modifier.padding(top = 80.dp),
            text = R.string.login
        ) {}
    }
}