package com.example.cheirocafe

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response

interface ApiService {

    @GET("produtos")
    suspend fun getProdutos(): List<Produto>

    // Adicione esta linha para o cadastro:
    @POST("produtos")
    suspend fun cadastrarProduto(@Body produto: Produto): Response<Produto>
}