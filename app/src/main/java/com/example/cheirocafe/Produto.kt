package com.example.cheirocafe

import java.io.Serializable

// Representa uma variante (tamanho/tipo), contendo o nome e o preço específico dela
data class Variante(
    val nome: String,
    val preco: Double
) : Serializable

// Representa um adicional, contendo o nome e o preço dele
data class Adicional(
    val nome: String,
    val preco: Double
) : Serializable

// A sua classe principal de Produto
data class Produto(
    val id: Long,
    val nome: String,
    val descricao: String,
    val preco: Double, // Preço base (se houver)
    val categoria: String,
    // Agora o produto recebe a lista completa de objetos, não apenas textos soltos
    val variantes: List<Variante>,
    val adicionais: List<Adicional>
): Serializable