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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.containerHeaderCarrinho).parent as android.view.View) { v, insets ->
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

        ouvirItensDoCarrinho()

        btnEnviarCozinha.setOnClickListener {
            enviarPedidoParaCozinha()
        }
    }

    private fun ouvirItensDoCarrinho() {
        lifecycleScope.launch {
            // Como sua query pede o ID da mesa, vamos colocar a Mesa 1 fixa por enquanto
            val mesaIdAtual = 1

            // Usando a sua função real do DAO: obterCarrinhoDaMesa
            pedidoDao.obterCarrinhoDaMesa(mesaIdAtual).collect { listaPedidos ->

                // Se o seu CarrinhoAdapter reclamar de receber PedidoEntity, mude o tipo na lista dele para List<PedidoEntity>
                recyclerView.adapter = CarrinhoAdapter(listaPedidos)

                var valorTotalGeral = 0.0
                for (pedido in listaPedidos) {
                    // Usando o campo real da sua tabela que aparece na linha 25 do seu DAO
                    valorTotalGeral += pedido.precoTotalItem
                }
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
}