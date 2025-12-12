package com.example.level_up.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.level_up.R
import com.example.level_up.local.Entidades.ProductoEntidad
import com.example.level_up.ui.theme.GreenAccent
import com.example.level_up.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    producto: ProductoEntidad?,
    reviewViewModel: ReviewViewModel = viewModel() // Inyectamos el ViewModel de reseñas
) {
    var showReviewDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Obtenemos el estado del ViewModel
    val reviewState by reviewViewModel.state.collectAsState()

    // 1. Cargar la información del producto en el ViewModel al entrar
    // Esto es crucial para que el VM sepa qué ID de producto usar al enviar la reseña
    LaunchedEffect(producto) {
        if (producto != null) {
            reviewViewModel.loadProductReviews(producto.id)
        }
    }

    // 2. Escuchar evento de éxito (Reseña guardada en BD)
    LaunchedEffect(reviewState.submitSuccess) {
        if (reviewState.submitSuccess) {
            Toast.makeText(context, "¡Reseña guardada en la base de datos!", Toast.LENGTH_LONG).show()
            showReviewDialog = false
            reviewViewModel.clearSubmitSuccess()
        }
    }

    // 3. Escuchar errores (Ej: Sin conexión o usuario no logueado)
    LaunchedEffect(reviewState.error) {
        if (reviewState.error != null) {
            Toast.makeText(context, reviewState.error, Toast.LENGTH_LONG).show()
            reviewViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(producto?.nombre ?: "Detalle del Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (producto != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- Imagen del Producto ---
                val model = remember(producto.urlImagen) {
                    if (producto.urlImagen.startsWith("http")) {
                        producto.urlImagen
                    } else {
                        val id = context.resources.getIdentifier(
                            producto.urlImagen.substringBeforeLast("."),
                            "drawable",
                            context.packageName
                        )
                        if (id != 0) id else R.drawable.ic_launcher_foreground
                    }
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(model)
                        .crossfade(true)
                        .error(R.drawable.ic_launcher_foreground)
                        .build(),
                    contentDescription = "Imagen de ${producto.nombre}",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = producto.categoria,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$${producto.precio}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenAccent
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón para abrir el diálogo
                Button(
                    onClick = { showReviewDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Dejar una reseña")
                }
            }

            // --- Diálogo de Reseña Conectado ---
            if (showReviewDialog) {
                ProductReviewDialog(
                    onDismiss = { showReviewDialog = false },
                    isSubmitting = reviewState.isSubmitting, // Pasamos estado de carga
                    onSubmit = { rating, comment ->
                        // LLAMADA AL MICROSERVICIO A TRAVÉS DEL VIEWMODEL
                        reviewViewModel.submitReview(rating.toFloat(), comment)
                    }
                )
            }

        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Producto no encontrado")
            }
        }
    }
}

@Composable
fun ProductReviewDialog(
    onDismiss: () -> Unit,
    isSubmitting: Boolean,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Califica este producto") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { index ->
                        Icon(
                            imageVector = if (index <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Estrella $index",
                            tint = if (index <= rating) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Escribe tu opinión") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !isSubmitting // Deshabilitar mientras se envía
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                enabled = rating > 0 && comment.isNotEmpty() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enviar")
                }
            }
        },
        dismissButton = {
            if (!isSubmitting) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}