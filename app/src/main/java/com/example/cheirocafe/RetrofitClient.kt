package com.example.cheirocafe

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
    private const val BASE_URL = "http://192.168.100.37:8080/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Mostra tudo o que chega e sai
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client) // Adiciona o log aqui
            .build()
            .create(ApiService::class.java)
    }
}