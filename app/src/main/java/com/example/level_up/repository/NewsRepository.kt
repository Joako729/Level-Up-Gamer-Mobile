// Archivo: app/src/main/java/com/example/level_up/repository/NewsRepository.kt

package com.example.level_up.repository

import com.example.level_up.remote.model.Article
import com.example.level_up.remote.service.NewsApiService
import java.io.IOException

class NewsRepository(private val apiService: NewsApiService) {

    suspend fun fetchGamingNews(): List<Article> {
        return try {
            val response = apiService.getTopGamingHeadlines()
            if (response.isSuccessful) {
                // Filtra artículos que no tienen imagen, título o URL
                response.body()?.articles
                    ?.filter { !it.urlToImage.isNullOrBlank() && !it.title.isNullOrBlank() && !it.url.isNullOrBlank() }
                    ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            // Error de conexión (ej. no hay internet)
            emptyList()
        } catch (e: Exception) {
            // Otros errores
            emptyList()
        }
    }
}