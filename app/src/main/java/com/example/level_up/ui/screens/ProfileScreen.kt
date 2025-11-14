package com.example.level_up.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.level_up.local.Entidades.PedidoEntidad
import com.example.level_up.local.Entidades.UsuarioEntidad
import com.example.level_up.viewmodel.AuthViewModel
import com.example.level_up.viewmodel.ProfileViewModel
import com.example.level_up.ui.theme.GreenAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val authState by authViewModel.state.collectAsState()
    val profileState by profileViewModel.state.collectAsState()
    val currentUser = profileState.currentUser

    LaunchedEffect(authState.currentUser) {
        profileViewModel.loadUserData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                profileState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                currentUser != null -> {
                    ProfileLoggedIn(
                        user = currentUser,
                        orders = profileState.userOrders,
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
                else -> {
                    ProfileLoggedOut(onLogin = { navController.navigate(Routes.AUTH) })
                }
            }
        }
    }
}

@Composable
fun ProfileLoggedIn(
    user: UsuarioEntidad,
    orders: List<PedidoEntidad>,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                Toast.makeText(context, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                Toast.makeText(context, "Archivo seleccionado: $uri", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No se seleccionó ningún archivo", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        // Profile Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(70.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(user.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(user.correo, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(Modifier.height(24.dp))
            }
        }

        // Stats
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatCard(icon = Icons.Default.WorkspacePremium, title = "Nivel", value = "${user.nivel}")
                StatCard(icon = Icons.Default.Star, title = "Puntos", value = "${user.puntosLevelUp}")
                StatCard(icon = Icons.Default.Redeem, title = "Compras", value = "${user.totalCompras}")
            }
            Spacer(Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
        }

        // Add review button
        item {
            Button(
                onClick = { navController.navigate(Routes.ADD_REVIEW) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Dejar una reseña")
            }
            Spacer(Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
        }

        // Location and File Chooser buttons
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Ubicación", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Obtener Ubicación")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar Archivo", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Adjuntar Archivo")
                }
            }
            Spacer(Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
        }

        // Order History
        item {
            Text(
                "Historial de Compras",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        if (orders.isEmpty()) {
            item {
                Text(
                    "Aún no has realizado ninguna compra.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            items(orders) { order ->
                OrderHistoryItem(order, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
        
        // Logout Button
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Cerrar Sesión")
            }
        }
    }
}

@Composable
fun ProfileLoggedOut(onLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(90.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        Spacer(Modifier.height(24.dp))
        Text("No se ha iniciado sesión", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Inicia sesión para ver tu perfil y acceder a todos los beneficios.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onLogin,
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Text("Iniciar Sesión o Registrarse", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCard(icon: ImageVector, title: String, value: String) {
    Card(
        modifier = Modifier.width(110.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun OrderHistoryItem(order: PedidoEntidad, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Pedido #${order.id}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    "${java.text.SimpleDateFormat("dd MMM, yyyy").format(java.util.Date(order.fechaCreacion))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("$${order.montoFinal}", style = MaterialTheme.typography.titleMedium, color = GreenAccent, fontWeight = FontWeight.ExtraBold)
        }
    }
}
