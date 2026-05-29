package com.example.cheirocafe

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import android.view.View
import androidx.recyclerview.widget.RecyclerView.LayoutManager // Força o import caso suma

class CarrinhoActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var pedidoDao: PedidoDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtTotalCarrinho: TextView
    private lateinit var txtNumeroMesaCarrinho: TextView
    private lateinit var btnEnviarCozinha: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carrinho)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<android.view.View>(R.id.containerHeaderCarrinho).parent as android.view.View) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.rvItensCarrinho)
        txtTotalCarrinho = findViewById(R.id.txtTotalCarrinho)
        txtNumeroMesaCarrinho = findViewById(R.id.txtNumeroMesaCarrinho)
        btnEnviarCozinha = findViewById(R.id.btnEnviarPedidoCozinha)

        recyclerView.layoutManager = LinearLayoutManager(this)

        database = AppDatabase.getDatabase(this)
        pedidoDao = database.pedidoDao()


        // --- AJUSTE DEFINITIVO: Buscar a mesa direto do último rascunho salvo no banco ---
        lifecycleScope.launch {
            // Buscamos todos os rascunhos ativos
            pedidoDao.obterCarrinhoDaMesa(3).collect { todosOsItens ->
                // Se houver algum item no banco, pegamos a mesa dele dinamicamente!
                if (todosOsItens.isNotEmpty()) {
                    val mesaDoBanco = todosOsItens.first().numeroMesa
                    txtNumeroMesaCarrinho.text = "Mesa %02d".format(mesaDoBanco)

                    // Passamos essa mesa para listar os produtos na tela
                    ouvirItensDoCarrinho(mesaDoBanco.toString())
                } else {
                    // Se o banco estiver totalmente limpo, mostra sem mesa
                    txtNumeroMesaCarrinho.text = "Carrinho Vazio"
                    txtTotalCarrinho.text = "R$ 0,00"
                    recyclerView.adapter = CarrinhoAdapter(emptyList())
                }
            }
        }

        btnEnviarCozinha.setOnClickListener {
            enviarPedidoParaCozinha()
        }
    }//fim do Oncreate



    // --- AJUSTE 2: A função agora recebe a mesa como String ---
    private fun ouvirItensDoCarrinho(mesaIdAtual: String) {
        lifecycleScope.launch {
            // Usando a sua função real do DAO passando o número correto como texto
            pedidoDao.obterCarrinhoDaMesa(mesaIdAtual.toInt()).collect { listaPedidos ->

                recyclerView.adapter = CarrinhoAdapter(listaPedidos)

                var valorTotalGeral = 0.0
                for (pedido in listaPedidos) {
                    valorTotalGeral += pedido.precoTotalItem
                }

                // Atualiza o valor total na tela bonitinho
                txtTotalCarrinho.text = String.format("R$ %.2f", valorTotalGeral)
            }
        }
    }

    private fun enviarPedidoParaCozinha() {
        lifecycleScope.launch {
            pedidoDao.atualizarStatusParaEnviado()
            Toast.makeText(this@CarrinhoActivity, "Pedido enviado para a cozinha!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}//fim da classe