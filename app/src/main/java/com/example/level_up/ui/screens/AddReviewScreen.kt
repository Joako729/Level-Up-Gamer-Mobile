package com.example.level_up.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.level_up.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(navController: NavController, profileViewModel: ProfileViewModel = viewModel()) {
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    val profileState by profileViewModel.state.collectAsState()

    LaunchedEffect(profileState.reviewSubmitSuccess) {
        if (profileState.reviewSubmitSuccess) {
            navController.popBackStack()
            profileViewModel.clearReviewSubmitSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escribir Reseña") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            RatingBar(rating = rating, onRatingChanged = { rating = it })
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                label = { Text("Escribe tu reseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { profileViewModel.submitAppReview(rating.toFloat(), reviewText) },
                enabled = !profileState.isSubmittingReview,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (profileState.isSubmittingReview) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Enviar Reseña")
                }
            }
        }
    }
}

@Composable
fun RatingBar(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        (1..5).forEach { index ->
            IconButton(onClick = { onRatingChanged(index) }) {
                Icon(
                    imageVector = if (index <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Rating $index",
                    tint = if (index <= rating) Color.Yellow else Color.Gray
                )
            }
        }
    }
}
