package com.example.cheirocafe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivityPedido : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_pedido)

        // Configuração de margens
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvProdutos)
        carregarProdutos(recyclerView)
    }

    private fun carregarProdutos(recyclerView: RecyclerView) {
        lifecycleScope.launch {
            try {
                // Chama a API
                val lista = RetrofitClient.instance.getProdutos()

                // Criamos o Adapter e definimos o comportamento do clique
                val adapter = ProdutoAdapter(lista) { produtoSelecionado ->
                    // Quando o usuário clicar, este código será executado:
                    val intent = Intent(this@MainActivityPedido, DetalheActivity::class.java)

                    // Passamos o produto inteiro para a próxima tela
                    intent.putExtra("PRODUTO_SELECIONADO", produtoSelecionado)

                    startActivity(intent)
                }

                recyclerView.adapter = adapter

            } catch (e: Exception) {
                Log.e("API_ERRO", "Erro ao buscar produtos: ${e.message}")
            }
        }
    }
}