package com.example.cheirocafe

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Dizemos ao Room quais tabelas existem no app e a versão do banco
@Database(entities = [PedidoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // 2. Expomos o DAO para conseguirmos usar as funções de inserir, deletar, etc.
    abstract fun pedidoDao(): PedidoDao

    // 3. Criamos um Singleton (garante que só exista uma instância do banco aberta no app)
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cheiro_cafe_database" // Nome do arquivo do banco no celular
                )
                    // Dica: Se você mudar a estrutura do banco depois, ele recria sem travar o app
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}