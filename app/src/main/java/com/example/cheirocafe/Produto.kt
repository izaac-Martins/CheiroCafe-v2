package com.example.cheirocafe

import java.io.Serializable

data class Variante(
    val nome: String,
    val preco: Double
) : Serializable

// Atualizado: agora ele sabe se pertence ao produto ou à categoria global
data class Adicional(
    val nome: String,
    val preco: Double,
    val vinculo: String = "PRODUTO" // Valores: "PRODUTO", "CATEGORIA_BEBIDAS", "CATEGORIA_DOCES", "CATEGORIA_SALGADOS"
) : Serializable

data class Produto(
    val id: Long? = null,
    val nome: String,
    val descricao: String,
    val preco: Double,
    val categoria: String,
    val variantes: List<Variante>,
    val adicionais: List<Adicional>
) : Serializable