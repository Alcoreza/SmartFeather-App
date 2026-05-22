package com.example.smartfeather

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val LoginPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private suspend fun ScrollState.smoothLoginScrollTo(value: Int) {
    animateScrollTo(
        value = value,
        animationSpec = tween(
            durationMillis = 520,
            easing = FastOutSlowInEasing
        )
    )
}

@Composable
fun LoginScreen(
    onLoginClick: suspend (String, String) -> Result<Unit>,
    onLoginSuccess: (String) -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible) {
            scrollState.smoothLoginScrollTo(320)
        } else {
            scrollState.smoothLoginScrollTo(0)
        }
    }

    val topSpacer by animateDpAsState(
        targetValue = if (isKeyboardVisible) 0.dp else 18.dp,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "loginTopSpacer"
    )

    val logoSize by animateDpAsState(
        targetValue = if (isKeyboardVisible) 0.dp else 74.dp,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "loginLogoSize"
    )

    val logoBottomSpacer by animateDpAsState(
        targetValue = if (isKeyboardVisible) 0.dp else 18.dp,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "loginLogoBottomSpacer"
    )

    val titleBottomSpacer by animateDpAsState(
        targetValue = if (isKeyboardVisible) 4.dp else 34.dp,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "loginTitleBottomSpacer"
    )

    val cardVerticalPadding by animateDpAsState(
        targetValue = if (isKeyboardVisible) 16.dp else 26.dp,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "loginCardPadding"
    )

    val forest = Color(0xFF06351F)
    val deepForest = Color(0xFF021D12)
    val leaf = Color(0xFF2F8F45)
    val brightLeaf = Color(0xFF48B85F)
    val surface = Color(0xFFFFFCF7)
    val fieldFill = Color(0xFFF7F4EE)
    val ink = Color(0xFF17231B)
    val muted = Color(0xFF747B72)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surface)
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isKeyboardVisible) 210.dp else 280.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(deepForest, forest)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isKeyboardVisible) {
                Spacer(modifier = Modifier.height(topSpacer))

                Box(
                    modifier = Modifier
                        .size(logoSize)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.28f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SF",
                        fontFamily = LoginPoppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(logoBottomSpacer))

                Text(
                    text = "SmartFeather",
                    fontFamily = LoginPoppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Poultry Management System",
                    fontFamily = LoginPoppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFFDDEEE0),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(titleBottomSpacer))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 22.dp,
                        vertical = cardVerticalPadding
                    )
                ) {
                    Text(
                        text = "Welcome back",
                        fontFamily = LoginPoppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = ink
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sign in to continue your farm duties.",
                        fontFamily = LoginPoppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = muted
                    )

                    Spacer(modifier = Modifier.height(if (isKeyboardVisible) 18.dp else 24.dp))

                    Text(
                        text = "Employee ID",
                        fontFamily = LoginPoppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ink
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userId,
                        onValueChange = {
                            userId = it
                            errorMessage = null
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (it.isFocused) {
                                    coroutineScope.launch {
                                        scrollState.smoothLoginScrollTo(360)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(18.dp),
                        placeholder = {
                            Text(
                                text = "Enter your employee ID",
                                fontFamily = LoginPoppins,
                                color = Color(0xFF9A9A9A),
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = fieldFill,
                            unfocusedContainerColor = fieldFill,
                            focusedBorderColor = brightLeaf,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = ink,
                            unfocusedTextColor = ink,
                            cursorColor = leaf
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Password",
                        fontFamily = LoginPoppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ink
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (it.isFocused) {
                                    coroutineScope.launch {
                                        scrollState.smoothLoginScrollTo(480)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(18.dp),
                        placeholder = {
                            Text(
                                text = "Enter your password",
                                fontFamily = LoginPoppins,
                                color = Color(0xFF9A9A9A),
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = fieldFill,
                            unfocusedContainerColor = fieldFill,
                            focusedBorderColor = brightLeaf,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = ink,
                            unfocusedTextColor = ink,
                            cursorColor = leaf
                        )
                    )

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = it,
                            fontFamily = LoginPoppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFFC92222)
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isKeyboardVisible) 20.dp else 26.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()

                            if (userId.isBlank() || password.isBlank()) {
                                errorMessage = "Please provide both employee ID and password."
                                return@Button
                            }

                            if (userId.any { !it.isDigit() }) {
                                errorMessage = "Employee ID should contain numbers only."
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                val result = onLoginClick(userId.trim(), password)
                                isLoading = false

                                result
                                    .onSuccess {
                                        errorMessage = null
                                        onLoginSuccess(userId.trim())
                                    }
                                    .onFailure {
                                        errorMessage = it.message ?: "Unable to sign in."
                                    }
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = forest,
                            disabledContainerColor = Color(0xFF8EA394)
                        ),
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text(
                            text = if (isLoading) "Signing in..." else "Sign In",
                            fontFamily = LoginPoppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }

            if (!isKeyboardVisible) {
                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Secure access for authorized farm personnel",
                    fontFamily = LoginPoppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF68736A),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLoginClick = { _, _ -> Result.success(Unit) },
        onLoginSuccess = {}
    )
}