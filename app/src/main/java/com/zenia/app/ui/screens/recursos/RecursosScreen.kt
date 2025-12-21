package com.zenia.app.ui.screens.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.model.Recurso
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.screens.recursos.RecursosUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecursosScreen(
    uiState: RecursosUiState,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.nav_resources)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(padding)
        ) {
            when (uiState) {
                is RecursosUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RecursosUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = onRetry) {
                            Text("Reintentar")
                        }
                    }
                }
                is RecursosUiState.Success -> {
                    if (uiState.recursos.isEmpty()) {
                        Text(
                            text = "No hay recursos disponibles por ahora.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        RecursosList(recursos = uiState.recursos)
                    }
                }
            }
        }
    }
}

@Composable
fun RecursosList(recursos: List<Recurso>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recursos) { recurso ->
            RecursoCard(recurso)
        }
    }
}

@Composable
fun RecursoCard(recurso: Recurso) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = recurso.titulo,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = recurso.contenido,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}