package com.example.cheirocafe

import retrofit2.http.GET

interface ApiService {
    // O caminho "produtos" é exatamente o que está no seu @RequestMapping("/produtos")
    @GET("produtos")
    suspend fun getProdutos(): List<Produto>
}