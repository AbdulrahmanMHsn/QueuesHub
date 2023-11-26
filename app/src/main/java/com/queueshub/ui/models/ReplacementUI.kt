package com.queueshub.ui.models

import androidx.compose.ui.text.input.KeyboardType


data class ReplacementUI(
    val title: String,
    val subtitle: String,
    val image: Int,
    val keyboardType: KeyboardType = KeyboardType.Text,
    var value: String = "",
) {

}