// Archivo: app/src/main/java/com/example/level_up/ui/screens/HomeScreen.kt

package com.example.level_up.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage // NUEVO: Para cargar imágenes por URL
import com.example.level_up.R
import com.example.level_up.local.Entidades.ProductoEntidad
import com.example.level_up.remote.model.Article // NUEVO: Para los datos de la noticia
import com.example.level_up.ui.theme.GreenAccent
import com.example.level_up.viewmodel.AuthViewModel
import com.example.level_up.viewmodel.CatalogViewModel

data class AccionRapida(
    val titulo: String,
    val icono: ImageVector,
    val ruta: String
)

data class CategoryNavigationItem(
    val title: String,
    @DrawableRes val imageRes: Int,
    val categoryFilter: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, catalogViewModel: CatalogViewModel) {
    val authViewModel: AuthViewModel = viewModel()

    val estadoAuth by authViewModel.state.collectAsState()
    val productosDestacados by catalogViewModel.featuredProducts.collectAsState()
    val newsArticles by catalogViewModel.gamingNews.collectAsState() // NUEVO: Obtenemos las noticias

    val acciones = listOf(
        AccionRapida("Catálogo", Icons.Default.Store, Routes.CATALOG),
        AccionRapida("Carrito", Icons.Default.ShoppingCart, Routes.CART),
        AccionRapida("Mi Perfil", Icons.Default.Person, Routes.PROFILE)
    )

    val categoryNavItems = listOf(
        CategoryNavigationItem("Accesorios", R.drawable.accesorios, "Accesorios"),
        CategoryNavigationItem("Consolas", R.drawable.consolas, "Consolas"),
        CategoryNavigationItem("PCs Gamers", R.drawable.pcs, "Computadores Gamers")
    )

    Scaffold(
        topBar = { QuickActionsBar(acciones, navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    if (estadoAuth.currentUser == null) {
                        AssistChip(
                            onClick = { navController.navigate(Routes.AUTH) },
                            label = { Text("Iniciar Sesión") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = if (estadoAuth.currentUser != null) "¡Hola, ${estadoAuth.currentUser?.nombre}!" else "Bienvenido a",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Level-Up Gamer",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tu tienda gamer de confianza",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Gaming News - AHORA DINÁMICO
            item {
                SectionTitle("Noticias del Mundo Gamer")
                if (newsArticles.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(newsArticles) { article ->
                            GamingNewsCard(article = article)
                        }
                    }
                } else {
                    // Muestra la tarjeta estática como fallback
                    GamingNewsPlaceholderCard(modifier = Modifier.padding(horizontal = 16.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Productos Destacados
            if (productosDestacados.isNotEmpty()) {
                item {
                    SectionTitle("Productos Destacados")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(productosDestacados.take(4)) { producto ->
                            TarjetaProductoDestacado(
                                producto = producto.copy(descripcion = "Descripción de relleno para el producto destacado."),
                                onClick = { navController.navigate(Routes.productDetail(producto.id)) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Category Navigation
            item {
                SectionTitle("Explorar Categorías")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categoryNavItems) { item ->
                        CategoryNavigationCard(
                            item = item,
                            onClick = {
                                catalogViewModel.updateSelectedCategory(item.categoryFilter)
                                navController.navigate(Routes.CATALOG)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsBar(acciones: List<AccionRapida>, navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            acciones.forEach { accion ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { navController.navigate(accion.ruta) }
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = accion.icono,
                        contentDescription = accion.titulo,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = accion.titulo,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaProductoDestacado(
    producto: ProductoEntidad,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val imageResId = remember(producto.urlImagen) {
                if (producto.urlImagen.isNotBlank()) {
                    context.resources.getIdentifier(producto.urlImagen, "drawable", context.packageName)
                } else {
                    0
                }
            }

            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Product Image Placeholder",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = producto.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${producto.precio}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenAccent
                )
            }
        }
    }
}


@Composable
fun GamingNewsCard(article: Article, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val url = article.url ?: "about:blank"

    Card(
        modifier = modifier
            .width(300.dp) // Ancho fijo para las tarjetas en LazyRow
            .clickable { uriHandler.openUri(url) },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Uso de Coil para cargar la imagen desde la URL (AsyncImage)
            AsyncImage(
                model = article.urlToImage,
                contentDescription = article.title,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.noticia1) // Fallback estático
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = article.title ?: "Noticia sin título",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.description ?: "Toca para leer más...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { uriHandler.openUri(url) }) {
                    Text("Leer más")
                }
            }
        }
    }
}

// Antigua GamingNewsCard, renombrada para ser el placeholder/fallback estático
@Composable
fun GamingNewsPlaceholderCard(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val newsUrl = "https://www.xataka.com/tag/realidad-virtual"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.noticia1),
                contentDescription = "Noticia sobre Realidad Virtual",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "El Futuro es Ahora: La Nueva Era de la Realidad Virtual (Estático)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sumérgete en mundos asombrosos con la última generación de visores VR. La tecnología háptica y el seguimiento ocular llevan la inmersión a un nivel nunca antes visto.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { uriHandler.openUri(newsUrl) }) {
                    Text("Leer más")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryNavigationCard(
    item: CategoryNavigationItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}