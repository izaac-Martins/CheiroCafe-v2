package com.example.cheirocafe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PedidoDao {

    @Insert
    suspend fun inserirItem(item: PedidoEntity)

    @Query("SELECT * FROM itens_pedido")
    suspend fun obterTodosOsItens(): List<PedidoEntity>

    @Query("DELETE FROM itens_pedido")
    suspend fun limparComanda()
}