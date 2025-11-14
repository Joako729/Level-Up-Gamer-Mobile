package com.example.level_up.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.level_up.local.model.CarritoItemConImagen
import com.example.level_up.viewmodel.CartViewModel
import com.example.level_up.ui.theme.GreenAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController, vm: CartViewModel = viewModel()) {
    val items by vm.items.collectAsState()
    val subtotal by vm.subtotal.collectAsState()
    val discountAmount by vm.discountAmount.collectAsState()
    val finalTotal by vm.finalTotal.collectAsState()
    val state by vm.state.collectAsState()
    var showPaymentOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                CartSummary(finalTotal, state.isProcessingOrder) { showPaymentOptions = true }
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (showPaymentOptions) {
                PaymentOptionsDialog(
                    onDismiss = { showPaymentOptions = false },
                    onPaymentSelected = {
                        vm.processOrder() // For now, just process the order
                        showPaymentOptions = false
                    }
                )
            }

            if (state.orderSuccess) {
                OrderSuccessMessage { navController.popBackStack() }
            }
            if (state.error != null) {
                ErrorAlert(message = state.error!!)
            }

            if (items.isEmpty()) {
                EmptyCartMessage()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items, key = { it.id }) { item ->
                        CartItem(item, vm)
                    }

                    item { CartTotals(subtotal, discountAmount) }
                }
            }
        }
    }
}

@Composable
fun PaymentOptionsDialog(onDismiss: () -> Unit, onPaymentSelected: (String) -> Unit) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val paymentOptions = listOf("Tarjeta de crédito", "PayPal", "Google Pay")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar método de pago") },
        text = {
            Column {
                paymentOptions.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { selectedOption = option }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedOption == option),
                            onClick = { selectedOption = option }
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(option)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (selectedOption != null) onPaymentSelected(selectedOption!!) },
                enabled = selectedOption != null
            ) {
                Text("Confirmar Pago")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


@Composable
fun CartItem(item: CarritoItemConImagen, vm: CartViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val context = LocalContext.current
            val imageResId = remember(item.urlImagen) {
                if (item.urlImagen.isNotBlank()) {
                    context.resources.getIdentifier(item.urlImagen, "drawable", context.packageName)
                } else {
                    0
                }
            }

            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = item.nombre,
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Product Image Placeholder", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }

            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("$${item.precio}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = GreenAccent)
                Spacer(Modifier.height(8.dp))
                QuantityControl(item, vm)
            }
            IconButton(onClick = { vm.removeById(item.id) }, modifier = Modifier.align(Alignment.Top)) {
                Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun QuantityControl(item: CarritoItemConImagen, vm: CartViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(
            onClick = { vm.updateQuantity(item, item.cantidad - 1) },
            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.background, CircleShape)
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Disminuir")
        }
        Text("${item.cantidad}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(
            onClick = { vm.updateQuantity(item, item.cantidad + 1) },
            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.background, CircleShape)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Aumentar")
        }
    }
}

@Composable
fun CartTotals(subtotal: Int, discountAmount: Int) {
    Column(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal", style = MaterialTheme.typography.bodyLarge)
            Text("$${subtotal}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Descuento", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            Text("-$${discountAmount}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun CartSummary(finalTotal: Int, isProcessing: Boolean, onCheckout: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("$${finalTotal}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = GreenAccent)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onCheckout,
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(text = "Pagar ahora", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyCartMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        Spacer(Modifier.height(24.dp))
        Text("Tu carrito está vacío", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("¡Añade productos para empezar a comprar!", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun OrderSuccessMessage(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¡Compra realizada!") },
        text = { Text("Tu pedido se ha procesado con éxito.") },
        confirmButton = { Button(onClick = onDismiss) { Text("Genial") } }
    )
}

@Composable
fun ErrorAlert(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp).background(MaterialTheme.colorScheme.errorContainer)) {
        Text(message, color = MaterialTheme.colorScheme.onError, modifier = Modifier.padding(16.dp))
    }
}
