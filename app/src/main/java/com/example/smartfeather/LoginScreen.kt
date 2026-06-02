package com.example.smartfeather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val LoginPoppins = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val LoginBackground = Color(0xFFF6F3EC)
private val LoginSurface = Color(0xFFFFFCF7)
private val LoginField = Color(0xFFF3EFE7)
private val LoginInk = Color(0xFF121A14)
private val LoginMuted = Color(0xFF677168)
private val LoginLine = Color(0xFFD8D0C3)
private val LoginGreen = Color(0xFF1F7A3A)
private val LoginDeepGreen = Color(0xFF062717)
private val LoginGreenTwo = Color(0xFF155C2D)
private val LoginDanger = Color(0xFFC62828)

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
    onLoginClick: suspend (String, String) -> Result<Int>,
    onLoginSuccess: (Int) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var introVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

    LaunchedEffect(Unit) {
        introVisible = true
    }

    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible) {
            scrollState.smoothLoginScrollTo(210)
        } else {
            scrollState.smoothLoginScrollTo(0)
        }
    }

    val topSpace by animateDpAsState(
        targetValue = if (isKeyboardVisible) 16.dp else 76.dp,
        animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
        label = "loginTopSpace"
    )

    val brandGap by animateDpAsState(
        targetValue = if (isKeyboardVisible) 10.dp else 20.dp,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "loginBrandGap"
    )

    val formPadding by animateDpAsState(
        targetValue = if (isKeyboardVisible) 18.dp else 24.dp,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "loginFormPadding"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFBF8F1), LoginBackground, Color(0xFFEDE7DA))
                )
            )
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {
        LoginAmbientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(topSpace))

            AnimatedVisibility(
                visible = introVisible,
                enter = fadeIn(animationSpec = tween(460)) + slideInVertically(
                    animationSpec = tween(520, easing = FastOutSlowInEasing),
                    initialOffsetY = { -it / 5 }
                )
            ) {
                LoginBrandBlock(compact = isKeyboardVisible)
            }

            Spacer(modifier = Modifier.height(brandGap))

            AnimatedVisibility(
                visible = introVisible,
                enter = fadeIn(animationSpec = tween(520)) +
                        slideInVertically(
                            animationSpec = tween(560, easing = FastOutSlowInEasing),
                            initialOffsetY = { it / 5 }
                        ) +
                        scaleIn(
                            animationSpec = tween(480, easing = FastOutSlowInEasing),
                            initialScale = 0.98f
                        )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(LoginSurface.copy(alpha = 0.96f))
                        .border(1.dp, LoginLine.copy(alpha = 0.82f), RoundedCornerShape(30.dp))
                        .padding(horizontal = 22.dp, vertical = formPadding)
                ) {
                    LoginLabel("Username")

                    Spacer(modifier = Modifier.height(8.dp))

                    LoginTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        placeholder = "Enter username",
                        keyboardType = KeyboardType.Text,
                        onFocused = {
                            coroutineScope.launch {
                                scrollState.smoothLoginScrollTo(240)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LoginLabel("Password")

                    Spacer(modifier = Modifier.height(8.dp))

                    LoginTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        placeholder = "Enter password",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingContent = {
                            TextButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Text(
                                    text = if (passwordVisible) "Hide" else "Show",
                                    fontFamily = LoginPoppins,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = LoginGreen
                                )
                            }
                        },
                        onFocused = {
                            coroutineScope.launch {
                                scrollState.smoothLoginScrollTo(360)
                            }
                        }
                    )

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFECEA))
                                .border(1.dp, LoginDanger.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            fontFamily = LoginPoppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = LoginDanger
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isKeyboardVisible) 20.dp else 24.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()

                            if (username.isBlank() || password.isBlank()) {
                                errorMessage = "Please provide both username and password."
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                val result = onLoginClick(username.trim(), password)
                                isLoading = false

                                result
                                    .onSuccess {
                                        errorMessage = null
                                        onLoginSuccess(it)
                                    }
                                    .onFailure {
                                        errorMessage = it.message ?: "Unable to sign in."
                                    }
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LoginGreen,
                            disabledContainerColor = Color(0xFF94A99A)
                        ),
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = if (isLoading) "Signing in..." else "Sign In",
                            fontFamily = LoginPoppins,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginAmbientBackground() {
    val transition = rememberInfiniteTransition(label = "loginAmbient")

    val pulse by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loginAmbientPulse"
    )

    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loginAmbientDrift"
    )

    val counterDrift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loginAmbientCounterDrift"
    )

    val slowDrift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loginAmbientSlowDrift"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(LoginDeepGreen, Color(0xFF0E4025), LoginGreenTwo)
                    )
                )
        )

        Box(
            modifier = Modifier
                .padding(top = 62.dp, start = (34 + drift).dp)
                .size(176.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = pulse * 0.14f))
        )

        Box(
            modifier = Modifier
                .padding(top = 118.dp, start = (238 - counterDrift).dp)
                .size(118.dp)
                .clip(CircleShape)
                .background(Color(0xFF7AF28B).copy(alpha = pulse * 0.12f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = (28 + counterDrift).dp, bottom = (34 + drift).dp)
                .size(190.dp)
                .clip(CircleShape)
                .background(LoginGreen.copy(alpha = pulse * 0.10f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = (22 + drift).dp, bottom = (86 - counterDrift).dp)
                .size(128.dp)
                .clip(CircleShape)
                .background(Color(0xFFCFE8D2).copy(alpha = pulse * 0.16f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = (118 + slowDrift).dp, bottom = (112 - counterDrift).dp)
                .size(76.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = pulse * 0.12f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = (132 - slowDrift).dp, bottom = (28 + drift).dp)
                .size(54.dp)
                .clip(CircleShape)
                .background(LoginGreenTwo.copy(alpha = pulse * 0.13f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = (44 + counterDrift).dp, bottom = 156.dp)
                .size(92.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAF3EC).copy(alpha = pulse * 0.13f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(end = (82 + drift).dp, bottom = 62.dp)
                .size(66.dp)
                .clip(CircleShape)
                .background(LoginGreen.copy(alpha = pulse * 0.11f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = (210 - counterDrift).dp, bottom = 18.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = pulse * 0.13f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 6.dp, bottom = (218 + counterDrift).dp)
                .size(132.dp)
                .clip(CircleShape)
                .background(LoginGreen.copy(alpha = pulse * 0.16f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = (18 + counterDrift).dp, top = 170.dp)
                .size(108.dp)
                .clip(CircleShape)
                .background(Color(0xFFCFE8D2).copy(alpha = pulse * 0.18f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(210.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            LoginGreen.copy(alpha = pulse * 0.05f),
                            LoginDeepGreen.copy(alpha = pulse * 0.04f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun LoginBrandBlock(compact: Boolean) {
    val logoSize = if (compact) 52.dp else 74.dp
    val titleSize = if (compact) 24.sp else 31.sp

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(logoSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SF",
                fontFamily = LoginPoppins,
                fontWeight = FontWeight.Black,
                fontSize = if (compact) 18.sp else 23.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 16.dp))

        Text(
            text = "SmartFeather",
            fontFamily = LoginPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = titleSize,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = if (compact) 28.sp else 35.sp
        )
    }
}

@Composable
private fun LoginLabel(text: String) {
    Text(
        text = text,
        fontFamily = LoginPoppins,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = LoginInk
    )
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: @Composable (() -> Unit)? = null,
    onFocused: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = trailingContent,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.isFocused) {
                    onFocused()
                }
            },
        shape = RoundedCornerShape(18.dp),
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = LoginPoppins,
                fontWeight = FontWeight.Medium,
                color = LoginMuted.copy(alpha = 0.72f),
                fontSize = 14.sp
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LoginField,
            unfocusedContainerColor = LoginField,
            focusedBorderColor = LoginGreen,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = LoginInk,
            unfocusedTextColor = LoginInk,
            cursorColor = LoginGreen
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLoginClick = { _, _ -> Result.success(2) },
        onLoginSuccess = {}
    )
}