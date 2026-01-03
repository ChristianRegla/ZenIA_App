package com.zenia.app.ui.screens.recursos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.ui.theme.ZeniaWhite

data class RecursoUiModel(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val imageRes: Int,
    val isPremium: Boolean
)

val mockRecursos = listOf(
    RecursoUiModel(1, "Guía Básica de Ansiedad", "Entiende los síntomas y aprende técnicas rápidas.", "Guía", R.drawable.placeholder_resource_1, false),
    RecursoUiModel(2, "Masterclass: Dormir Mejor", "Curso completo de higiene del sueño por expertos.", "Curso", R.drawable.placeholder_resource_1, true),
    RecursoUiModel(3, "Técnicas de Grounding", "Ejercicios para volver al presente en crisis.", "Artículo", R.drawable.placeholder_resource_1, false),
    RecursoUiModel(4, "Nutrición y Salud Mental", "Cómo lo que comes afecta tu estado de ánimo.", "Artículo", R.drawable.placeholder_resource_1, true),
    RecursoUiModel(5, "Diario de Gratitud", "Plantillas y beneficios de agradecer diariamente.", "Herramienta", R.drawable.placeholder_resource_1, false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecursosScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToPremium: () -> Unit,
    isUserPremium: Boolean = false
) {
    Scaffold(
        topBar = {
            ZeniaTopBar(title = "Recursos")
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 340.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            items(mockRecursos) { recurso ->
                RecursoCard(
                    recurso = recurso,
                    isLocked = recurso.isPremium && !isUserPremium,
                    onClick = {
                        if (recurso.isPremium && !isUserPremium) {
                            onNavigateToPremium()
                        } else {
                            onNavigateToDetail(recurso.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RecursoCard(
    recurso: RecursoUiModel,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isLocked) Color.Gray.copy(alpha = 0.1f) else ZeniaWhite
    val contentAlpha = if (isLocked) 0.6f else 1f
    val textColor = if (isLocked) Color.Gray else Color.Black

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
            ) {
                Image(
                    painter = painterResource(id = recurso.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = contentAlpha,
                    colorFilter = if (isLocked) ColorFilter.tint(Color.Gray, androidx.compose.ui.graphics.BlendMode.Saturation) else null
                )
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = ZeniaWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                // Categoría y Premium Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = recurso.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = ZeniaTeal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (recurso.isPremium) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = "Premium",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = recurso.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontFamily = RobotoFlex,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = recurso.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    fontFamily = RobotoFlex,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
fun RecursosScreenPhonePreview() {
    ZenIATheme {
        RecursosScreen(onNavigateToDetail = {}, onNavigateToPremium = {}, isUserPremium = false)
    }
}