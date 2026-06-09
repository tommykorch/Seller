package com.example.seller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.*
import com.example.seller.data.local.AppDatabase
import com.example.seller.data.remote.RetrofitClient
import com.example.seller.data.repository.OrderRepository
import com.example.seller.domain.usecase.*
import com.example.seller.ui.orders.OrdersScreen
import com.example.seller.ui.orders.OrdersViewModel
import com.example.seller.ui.shops.ShopsScreen
import com.example.seller.ui.shops.ShopsViewModel
import com.example.seller.ui.theme.SellerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)
        val api = RetrofitClient.wbApi
        val repository = OrderRepository(api, db.appDao())

        // UseCases
        val getShopsUseCase = GetShopsUseCase(repository)
        val getOrdersUseCase = GetOrdersByStatusUseCase(repository)
        val moveOrderUseCase = MoveOrderUseCase(repository)
        val fetchAllUseCase = FetchAllOrdersUseCase(repository, getShopsUseCase)
        val deleteShopUseCase = DeleteShopUseCase(repository)

        val syncArchiveUseCase = SyncArchiveUseCase(repository)

        // ViewModels
        val ordersViewModel = OrdersViewModel(
            fetchAllUseCase,
            getOrdersUseCase,
            moveOrderUseCase,
            syncArchiveUseCase,
        )
        val shopsViewModel = ShopsViewModel(repository,deleteShopUseCase )

        setContent {
            SellerTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "orders") {
                    // Экран новых заказов
                    composable("orders") {
                        OrdersScreen(
                            viewModel = ordersViewModel,
                            isArchive = false,
                            onNavigateToShops = { navController.navigate("shops") },
                            onSwitchTab = { isArchive ->
                                if (isArchive) navController.navigate("archive") else navController.navigate("orders")
                            }
                        )
                    }
                    // Экран архива
                    composable("archive") {
                        OrdersScreen(
                            viewModel = ordersViewModel,
                            isArchive = true,
                            onNavigateToShops = { navController.navigate("shops") },
                            onSwitchTab = { isArchive ->
                                if (isArchive) navController.navigate("archive") else navController.navigate("orders")
                            }
                        )
                    }
                    // Экран магазинов
                    composable("shops") {
                        ShopsScreen(
                            viewModel = shopsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}