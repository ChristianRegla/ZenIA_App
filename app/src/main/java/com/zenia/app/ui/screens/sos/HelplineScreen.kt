package com.zenia.app.ui.screens.sos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.model.EmergencyContact
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaTeal
import kotlinx.coroutines.delay

private val ColorLifeline = Color(0xFFE91E63)
private val ColorFriend = Color(0xFF009688)
private val ColorChat = Color(0xFF9C27B0)
private val ColorCalm = Color(0xFFFF9800)

@Composable
fun HelplineScreen(
    onCallLifeline: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateBack: () -> Unit
) {

    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible.value = true
    }

    ZenIATheme {
        Scaffold(
            topBar = {
                ZeniaTopBar(
                    title = stringResource(R.string.helpline),
                    onNavigateBack = onNavigateBack
                    )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.sos_header),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.sos_body_html),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        // fontFamily = RobotoFlex,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    AnimatedVisibility(
                        visible = visible.value,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SosButton(
                                icon = Icons.Default.Favorite,
                                text = stringResource(R.string.sos_btn_lifeline),
                                subText = "Gratis, anónimo y disponible 24/7",
                                accentColor = ColorLifeline,
                                onClick = onCallLifeline
                            )

                            SosButton(
                                icon = Icons.Default.Person,
                                text = stringResource(R.string.sos_btn_friend),
                                subText = "Añade hasta 3 contactos de confianza",
                                accentColor = ColorFriend,
                                onClick = onNavigateToContacts
                            )

                            SosButton(
                                icon = Icons.Default.ChatBubble,
                                text = stringResource(R.string.sos_btn_support_chat),
                                accentColor = ColorChat,
                                onClick = onNavigateToChat
                            )

                            SosButton(
                                icon = Icons.Default.SelfImprovement,
                                text = stringResource(R.string.sos_btn_calm_exercises),
                                accentColor = ColorCalm,
                                onClick = onNavigateToExercises
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "ZenIA es una herramienta de apoyo emocional y no reemplaza la ayuda profesional.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SosButton(
    icon: ImageVector,
    text: String,
    accentColor: Color,
    subText: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (subText != null) {

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactsSheet(
    contacts: List<EmergencyContact>,
    onDismiss: () -> Unit,
    onCallContact: (String) -> Unit,
    onAddContact: (String, String) -> Unit,
    onDeleteContact: (EmergencyContact) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Contactos de Emergencia",
                fontFamily = RobotoFlex,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Añade hasta 3 contactos de confianza para llamarlos rápidamente en caso de crisis.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (contacts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text("No tienes contactos guardados.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(contacts) { contact ->
                        ContactCard(
                            contact = contact,
                            onCall = { onCallContact(contact.phone) },
                            onDelete = { onDeleteContact(contact) }
                        )
                    }
                }
            }

            if (contacts.size < 3) {
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = ZeniaTeal),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZeniaTeal)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Añadir Contacto (${contacts.size}/3)", fontFamily = RobotoFlex, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone ->
                onAddContact(name, phone)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ContactCard(
    contact: EmergencyContact,
    onCall: () -> Unit,
    onDelete: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ColorFriend.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = ColorFriend)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(contact.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
            IconButton(
                onClick = onCall,
                modifier = Modifier.background(ColorFriend, CircleShape)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Llamar", tint = Color.White)
            }
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val context = LocalContext.current

    val contactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            val datos = obtenerDatosDeContacto(context, uri)
            if (datos != null) {
                name = datos.first
                phone = datos.second
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactLauncher.launch(null)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Contacto", fontFamily = RobotoFlex, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Contacts, contentDescription = null, tint = ZeniaTeal)
                    Spacer(Modifier.width(8.dp))
                    Text("Elegir de mis contactos", color = ZeniaTeal, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(" O ", modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, phone) },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@SuppressLint("Range")
fun obtenerDatosDeContacto(context: Context, contactUri: Uri): Pair<String, String>? {
    var nombre = ""
    var telefono = ""
    val contentResolver = context.contentResolver

    val cursor = contentResolver.query(contactUri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            nombre = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) ?: ""
            val id = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
            val hasPhone = it.getInt(it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0

            if (hasPhone) {
                val phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(id),
                    null
                )
                phoneCursor?.use { pc ->
                    if (pc.moveToFirst()) {
                        telefono = pc.getString(pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                        telefono = telefono.replace(" ", "").replace("-", "")
                    }
                }
            }
        }
    }
    return if (nombre.isNotBlank() || telefono.isNotBlank()) Pair(nombre, telefono) else null
}

@Preview(showBackground = true, name = "SOS Screen Light", locale = "es")
@Composable
fun SosScreenPreview() {
    ZenIATheme {
        HelplineScreen(
            onCallLifeline = {},
            onNavigateToChat = {},
            onNavigateToContacts = {},
            onNavigateToExercises = {},
            onNavigateBack = {}
        )
    }
}