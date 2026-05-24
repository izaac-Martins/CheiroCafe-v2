package com.example.cheirocafe

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itens_pedido")
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val produtoId: Long,
    val nomeProduto: String,
    val tamanhoEscolhido: String?,
    val tipoGraoEscolhido: String?,
    val adicionaisEscolhidos: String, // Salvaremos como texto (Ex: "Nutella, Chantilly")
    val quantidade: Int,
    val precoTotalItem: Double,
    val pagadorItem: String = "Mesa Total",
    val numeroMesa: Int,             //ADICIONEi ESTA LINHA
    val statusPedido: String = "RASCUNHO", // ADICIONEi ESTA LINHA (com o valor padrão "RASCUNHO")
)
