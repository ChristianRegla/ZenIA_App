package com.zenia.app.ui.screens.recursos

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.model.Recurso // Tu modelo real
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaTeal
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.zenia.app.util.DevicePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecursoDetailScreen(
    uiState: RecursoDetailUiState,
    onNavigateBack: () -> Unit,
    onMarkAsCompleted: () -> Unit
) {
    val dimensions = ZenIATheme.dimensions

    Scaffold(
        topBar = {
            val title = if (uiState is RecursoDetailUiState.Success) uiState.recurso.tipo else "Recurso"
            ZeniaTopBar(
                title = title,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when (uiState) {
                is RecursoDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ZeniaTeal
                    )
                }
                is RecursoDetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 500.dp)
                            .padding(dimensions.paddingExtraLarge),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is RecursoDetailUiState.Success -> {
                    val recurso = uiState.recurso
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 750.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.placeholder_resource_1),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = dimensions.paddingExtraLarge,
                                    vertical = dimensions.paddingLarge
                                )
                        ) {
                            Text(
                                text = recurso.titulo,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (recurso.duracionEstimada != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tiempo estimado: ${recurso.duracionEstimada}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))

                            val customTypography = markdownTypography(
                                text = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                h1 = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                                h2 = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                h3 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                quote = MaterialTheme.typography.bodyLarge.copy(
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            val customColors = markdownColor(
                                text = MaterialTheme.colorScheme.onBackground,
                                dividerColor = ZeniaTeal.copy(alpha = 0.2f)
                            )

                            val contenidoLimpio = recurso.contenido.replace("\\n", "\n")

                            Markdown(
                                content = contenidoLimpio,
                                modifier = Modifier.fillMaxWidth(),
                                typography = customTypography,
                                colors = customColors
                            )

                            Spacer(modifier = Modifier.height(48.dp))

                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Button(
                                    onClick = onMarkAsCompleted,
                                    modifier = Modifier
                                        .widthIn(max = dimensions.buttonMaxWidth)
                                        .fillMaxWidth()
                                        .heightIn(min = dimensions.buttonHeight),
                                    shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
                                    colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.resource_detail_mark_completed),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun RecursoDetailScreenPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        RecursoDetailScreen(
            uiState = RecursoDetailUiState.Success(
                recurso = Recurso(
                    id = "1",
                    tipo = "Artículo de Psicología",
                    titulo = "Cómo superar la ansiedad nocturna en 5 pasos",
                    duracionEstimada = "5 min de lectura",
                    contenido = """
                        # La ansiedad no tiene que ganar
                        
                        A veces, cuando cae la noche, nuestra mente decide encenderse. Este es un problema muy común.
                        
                        ## Paso 1: Reconoce el sentimiento
                        No intentes pelear con tu mente. Acepta que estás sintiendo ansiedad.
                        
                        > "La paz viene de la aceptación, no de la resistencia."
                        
                        ## Paso 2: Respiración 4-7-8
                        Aplica la técnica de respirar profundo. Inhala en 4 segundos, sostén por 7 y exhala en 8.
                    """.trimIndent()
                )
            ),
            onNavigateBack = {},
            onMarkAsCompleted = {}
        )
    }
}