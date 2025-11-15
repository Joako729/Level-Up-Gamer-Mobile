// Archivo: app/src/main/java/com/example/level_up/remote/model/NewsApiModel.kt

package com.example.level_up.remote.model

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

data class Article(
    val source: ArticleSource?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

data class ArticleSource(
    val id: String?,
    val name: String?
)