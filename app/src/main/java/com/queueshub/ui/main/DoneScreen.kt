package com.queueshub.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.queueshub.R
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.SpanishGreen
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun DoneScreen(
    paddingValues: PaddingValues = PaddingValues(),
    router: Router? = null) {

    val goAllOrders: () -> Unit = {
        router?.goBack()
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val image: Painter = painterResource(id = R.drawable.done_img)
        LaunchedEffect(Unit) {
            while(true) {
                delay(2.seconds)
                goAllOrders()
            }
        }
        Image(
            modifier = Modifier,
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Text(
            modifier = Modifier.padding(top = 43.dp),
            text = "شكراً لك!",
            style = MaterialTheme.typography.subtitle1,
            color = SpanishGreen
        )
        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = "تمت العملية بنجاح",
            style = MaterialTheme.typography.body1
        )

    }
}

@Preview(locale = "ar", showBackground = true)
@Composable
fun DoneScreenPreview() {
    DoneScreen()
}