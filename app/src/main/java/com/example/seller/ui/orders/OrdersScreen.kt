package com.example.seller.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.seller.StatusMapper
import com.example.seller.data.local.OrderEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    isArchive: Boolean = false,
    onNavigateToShops: () -> Unit,
    onSwitchTab: (Boolean) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Автоматическое переключение данных при смене вкладки
    LaunchedEffect(isArchive) {
        if (isArchive) {
            viewModel.handleIntent(OrdersIntent.LoadArchive)
        } else {
            viewModel.handleIntent(OrdersIntent.LoadNew)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(if (isArchive) "Архив заказов" else "Новые заказы")
                    },
                    actions = {
                        TextButton(onClick = onNavigateToShops) {
                            Text("Магазины", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
                TabRow(selectedTabIndex = if (isArchive) 1 else 0) {
                    Tab(
                        selected = !isArchive,
                        onClick = { onSwitchTab(false) },
                        text = { Text("Новые") }
                    )
                    Tab(
                        selected = isArchive,
                        onClick = { onSwitchTab(true) },
                        text = { Text("Архив") }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(state.orders) { order ->
                    OrderCard(
                        order = order,
                        onAssemble = {
                            viewModel.handleIntent(OrdersIntent.SendToAssembly(order.id))
                        },
                        showButton = !isArchive,
                        showArchiveInfo = isArchive
                    )
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (state.orders.isEmpty() && !state.isLoading) {
                Text(
                    text = if (isArchive) "В архиве пока пусто" else "Новых заказов нет",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
@Composable
fun OrderCard(order: OrderEntity, onAssemble: () -> Unit, showButton: Boolean,showArchiveInfo: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                /*if (showArchiveInfo) { // если isArchive == true

                    Text(
                        text = "Статус: ${StatusMapper.mapWb(order.wbStatus)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }*/

                Text(
                    text = "${order.shopName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Создан: ${order.createdAt}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Артикул: ${order.article}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (!showArchiveInfo){
                    Text(
                        text = "Цена: ${order.salePrice} ₽",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Дата доставки: ${order.deliveryDate ?: "—"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Цена доставки: ${order.deliveryCost} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (!order.fullAddress.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.fullAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!order.comment.isNullOrBlank()) {
                    Text(
                        text = "${order.comment}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (showButton) {
                Button(onClick = onAssemble) {
                    Text("В сборку")
                }
            }
        }
    }
}