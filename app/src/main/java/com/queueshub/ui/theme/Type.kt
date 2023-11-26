package com.queueshub.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.queueshub.R

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily(Font(R.font.vazirmatn)),
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = Black,
        textDirection = TextDirection.Content
    ),
    button = TextStyle(
        fontFamily = FontFamily(Font(R.font.vazirmatn)),
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        color = Color.White,
        textDirection = TextDirection.Content
    ),
    subtitle1 = TextStyle(
        fontFamily = FontFamily(Font(R.font.vazirmatn)),
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = ChathamsBlue
    ),
    subtitle2 = TextStyle(
        fontFamily = FontFamily(Font(R.font.vazirmatn)),
        fontSize = 14.sp,
        color = Tundora
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)