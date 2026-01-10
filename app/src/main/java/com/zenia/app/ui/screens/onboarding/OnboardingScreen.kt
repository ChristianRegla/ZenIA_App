package com.zenia.app.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.theme.*

data class OnboardingPage(
    val title: String,
    val description: String,
    val iconRes: Int,
    val color: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Bienvenido a ZenIA",
            description = "Tu espacio seguro para el bienestar emocional. Un compañero que te escucha y te entiende.",
            iconRes = R.drawable.ic_nube_feli,
            color = ZeniaTeal
        ),
        OnboardingPage(
            title = "Diario Emocional",
            description = "Registra cómo te sientes cada día. Identifica patrones y comprende mejor tus emociones.",
            iconRes = R.drawable.ic_journal,
            color = ZeniaDeepTeal
        ),
        OnboardingPage(
            title = "Chat con IA",
            description = "Habla con ZenIA en cualquier momento. Un asistente inteligente diseñado para apoyarte.",
            iconRes = R.drawable.ic_chat,
            color = ZeniaDream
        ),
        OnboardingPage(
            title = "Calma y Recursos",
            description = "Accede a ejercicios de respiración y recursos profesionales cuando más lo necesites.",
            iconRes = R.drawable.ic_relax,
            color = ZeniaExercise
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Scaffold(
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(onClick = onFinish) {
                        Text(
                            text = "Saltar",
                            color = ZeniaSlateGrey,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { position ->
                OnboardingPageContent(page = pages[position])
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.height(50.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) ZeniaTeal else ZeniaLightGrey
                        val width = if (pagerState.currentPage == iteration) 24.dp else 10.dp
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(color)
                                .height(10.dp)
                                .width(width)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    if (pagerState.currentPage == pages.size - 1) {
                        Button(
                            onClick = onFinish,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Comenzar Ahora", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "Desliza para continuar",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            page.color.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = page.iconRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = page.color
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = ZeniaDark,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = ZeniaSlateGrey,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    OnboardingScreen(onFinish = {})
}