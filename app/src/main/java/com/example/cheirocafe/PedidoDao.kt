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

    // A QUERY CHAVE: Soma o valor total e conta os itens de todas as mesas em rascunho para o painel iFood
    @Query(value = "SELECT SUM(precoTotalItem) as totalPreco, COUNT(*) as totalItens FROM itens_pedido WHERE statusPedido = 'RASCUNHO'")
    fun obterResumoPainel(): kotlinx.coroutines.flow.Flow<ResumoPainel?>
    @Query(value = "UPDATE itens_pedido SET statusPedido = 'ENVIADO' WHERE statusPedido = 'RASCUNHO'")
    suspend fun atualizarStatusParaEnviado()
}