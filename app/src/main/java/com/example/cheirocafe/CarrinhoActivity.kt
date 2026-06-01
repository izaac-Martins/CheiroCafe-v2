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

class CarrinhoActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var pedidoDao: PedidoDao
    private lateinit var recyclerView: RecyclerView

    // Mudamos para tipos comuns e permitimos nulo temporariamente por segurança de ID
    private var txtTotalCarrinho: TextView? = null
    private var txtNumeroMesaCarrinho: TextView? = null
    private var btnEnviarCozinha: View? = null // View genérica aceita tanto Button quanto AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carrinho)

        // Ajuste seguro dos Insets para evitar crash caso o container mude de ID
        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Vínculo dos componentes do layout
        recyclerView = findViewById(R.id.rvItensCarrinho)
        recyclerView.layoutManager = LinearLayoutManager(this)

        txtTotalCarrinho = findViewById(R.id.txtTotalCarrinho)
        txtNumeroMesaCarrinho = findViewById(R.id.txtNumeroMesaCarrinho)
        btnEnviarCozinha = findViewById(R.id.btnEnviarPedidoCozinha)

        database = AppDatabase.getDatabase(this)
        pedidoDao = database.pedidoDao()

        // Resgata o número da mesa enviado pela Intent (Padrão 1 se falhar)
        val numeroMesaRecebido = intent.getIntExtra("NUMERO_MESA", 1)

        // Define o texto usando safe call (?)
        txtNumeroMesaCarrinho?.text = "Mesa %02d".format(numeroMesaRecebido)

        // Inicia a escuta dos itens
        ouvirItensDoCarrinho(numeroMesaRecebido)

        btnEnviarCozinha?.setOnClickListener {
            enviarPedidoParaCozinha()
        }
    } // Fim do onCreate

    private fun ouvirItensDoCarrinho(numeroMesa: Int) {
        // Iniciamos uma coroutine no escopo da tela
        lifecycleScope.launch {
            // Escutamos o Flow do Room diretamente
            pedidoDao.obterCarrinhoDaMesa(numeroMesa).collect { listaPedidos ->
                if (listaPedidos.isEmpty()) {
                    txtTotalCarrinho?.text = "R$ 0,00"
                    recyclerView.adapter = CarrinhoAdapter(emptyList())
                } else {
                    // Alimenta o adapter com os itens reais do banco
                    recyclerView.adapter = CarrinhoAdapter(listaPedidos)

                    var valorTotalGeral = 0.0
                    for (pedido in listaPedidos) {
                        valorTotalGeral += pedido.precoTotalItem
                    }
                    txtTotalCarrinho?.text = String.format("R$ %.2f", valorTotalGeral)
                }
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
} // Fim da classe