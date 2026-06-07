package com.example.seller.ui.shops

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopsScreen(viewModel: ShopsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("dbs") }

    LaunchedEffect(Unit) { viewModel.handleIntent(ShopsIntent.LoadShops) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои магазины") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Text("Добавить новый магазин", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text("API Токен") }, modifier = Modifier.fillMaxWidth())

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = type == "dbs", onClick = { type = "dbs" })
                Text("DBS")
                Spacer(Modifier.width(16.dp))
                RadioButton(selected = type == "dbw", onClick = { type = "dbw" })
                Text("DBW")
            }

            Button(
                onClick = {
                    if (name.isNotBlank() && token.isNotBlank()) {
                        viewModel.handleIntent(ShopsIntent.AddShop(name, token, type))
                        name = ""; token = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving
            ) {
                Text(if (state.isSaving) "Сохранение..." else "Добавить магазин")
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Список магазинов", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(state.shops) { shop ->
                    ListItem(
                        headlineContent = { Text(shop.name) },
                        supportingContent = { Text("Тип: ${shop.type}") },
                        trailingContent = {
                            IconButton(onClick = { viewModel.handleIntent(ShopsIntent.DeleteShop(shop.id)) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            }
        }
    }
}