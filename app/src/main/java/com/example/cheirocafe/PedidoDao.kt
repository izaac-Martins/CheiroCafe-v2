package com.example.cheirocafe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    @Insert
    suspend fun inserirItem(item: PedidoEntity)

    @Query("SELECT * FROM itens_pedido")
    suspend fun obterTodosOsItens(): List<PedidoEntity>

    @Query("DELETE FROM itens_pedido")
    suspend fun limparComanda()

    //Busca todos os pedidos ATIVOS de uma mesa específica para listar no carrinho
    @Query("SELECT * FROM itens_pedido WHERE numeroMesa = :mesaId AND statusPedido = 'RASCUNHO'")
    fun obterCarrinhoDaMesa(mesaId: Int): kotlinx.coroutines.flow.Flow<List<PedidoEntity>>

    // A QUERY CHAVE: Soma o valor total e conta os itens da mesa para o painel iFood
    @Query("SELECT SUM(precoTotalItem) as totalPreco, COUNT(*) as totalItens FROM itens_pedido WHERE numeroMesa = :mesaId AND statusPedido = 'RASCUNHO'")
    fun obterResumoPainel(mesaId: Int): kotlinx.coroutines.flow.Flow<ResumoPainel?>
}