package com.example.cheirocafe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.example.cheirocafe.AppDatabase
import com.example.cheirocafe.PedidoDao
import android.view.View

class MainActivityPedido : AppCompatActivity() {

    private var listaProdutosCompleta: List<Produto> = emptyList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var textViewLabel: TextView
    private lateinit var database: AppDatabase
    private lateinit var pedidoDao: PedidoDao
    private lateinit var layoutPainelIfood: View
    private lateinit var txtQuantidadeItens: TextView
    private lateinit var txtValorTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_pedido)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.rvProdutos)
        textViewLabel = findViewById(R.id.textViewLabel)

        val btnDoces = findViewById<AppCompatButton>(R.id.buttonDoces)
        val btnSalgados = findViewById<AppCompatButton>(R.id.buttonSalgados)
        val btnBebidas = findViewById<AppCompatButton>(R.id.buttonBebidas)

        buscarDadosDoServidor()

        btnBebidas.setOnClickListener {
            textViewLabel.text = "Bebidas"
            mostrarMenuDeSubcategorias()
        }

        btnDoces.setOnClickListener {
            textViewLabel.text = "Doces"
            val doces = listaProdutosCompleta.filter { it.categoria == "DOCES" }
            recyclerView.adapter = ProdutoAdapter(doces) { produto -> abrirDetalhes(produto) }
        }

        btnSalgados.setOnClickListener {
            textViewLabel.text = "Salgados"
            val salgados = listaProdutosCompleta.filter { it.categoria == "SALGADOS" }
            recyclerView.adapter = ProdutoAdapter(salgados) { produto -> abrirDetalhes(produto) }
        }

        val btnMenuOpcoes = findViewById<ImageButton>(R.id.btnMenuOpcoes)
        btnMenuOpcoes.setOnClickListener {
            val intent = Intent(this, CadastroProdutoActivity::class.java)
            startActivity(intent)
        }

        // Inicializa o Room Database e o DAO
        database = AppDatabase.getDatabase(this) // Ajuste para a sua classe de Banco
        pedidoDao = database.pedidoDao() // Ajuste se seu método tiver outro nome

        // Vincula os componentes do painel inferior
        layoutPainelIfood = findViewById(R.id.painelCarrinhoIFood)
        txtQuantidadeItens = findViewById(R.id.txtTotalItensPainel)
        txtValorTotal = findViewById(R.id.txtPrecoTotalPainel)

        //evento de click na sacola, no pop-up inferior
        val btnVerSacola = findViewById<View>(R.id.btnVerSacola)

        btnVerSacola.setOnClickListener {
            val intent = Intent(this, CarrinhoActivity::class.java)
            startActivity(intent)
        }

        // Ativa o monitoramento em tempo real do banco
        configurarObservadorCarrinho()
    }//Fim do oncriate

    //função de monitoramento
    private fun configurarObservadorCarrinho() {
        // Como estamos na MainActivity, precisamos passar um ID de mesa para a Query.
        // Se você ainda não tem a mesa salva na MainActivity, vou colocar uma fixa (ex: 1)
        // apenas para testar, mas depois você pode pegar a mesa dinâmica.
        val mesaIdAtual = 1

        // Iniciamos uma Coroutine no escopo de ciclo de vida da Activity
        lifecycleScope.launch {
            // Coletamos o Flow em tempo real do Banco de Dados
            pedidoDao.obterResumoPainel().collect { resumo ->

                // Se o resumo for nulo ou se não houver itens na mesa
                if (resumo == null || resumo.totalItens == 0) {
                    layoutPainelIfood.visibility = android.view.View.GONE
                } else {
                    // Tem itens! Mostra a barra rosa do iFood
                    layoutPainelIfood.visibility = android.view.View.VISIBLE

                    // Pega os dados direto do objeto ResumoPainel que sua Query calculou
                    val totalItens = resumo.totalItens
                    val valorTotal = resumo.totalPreco ?: 0.0

                    // Atualiza os textos do layout
                    txtQuantidadeItens.text = "$totalItens ${if (totalItens == 1) "item" else "itens"}"
                    txtValorTotal.text = String.format("R$ %.2f", valorTotal)
                }
            }
        }
    }


    private fun buscarDadosDoServidor() {
        lifecycleScope.launch {
            try {
                listaProdutosCompleta = RetrofitClient.instance.getProdutos()
                mostrarMenuDeSubcategorias()
            } catch (e: Exception) {
                Log.e("API_ERRO", "Erro ao buscar produtos: ${e.message}")
            }
        }
    }

    private fun mostrarMenuDeSubcategorias() {
        val subcategoriasMenu = listOf(
            SubcategoriaItem("Cafés Tradicionais", "TRADICIONAL", R.drawable.cafeprensa),
            SubcategoriaItem("Cafés Especiais", "ESPECIAL", R.drawable.cafecremoso),
            SubcategoriaItem("Sucos e Iogurtes", "SUCOS_IOGURTES", R.drawable.sucomanga),
            SubcategoriaItem("Refrigerantes", "REFRIGERANTES", R.drawable.refrilata)
        )

        val adapter = SubcategoriaAdapter(subcategoriasMenu) { subcategoriaSelecionada ->
            // Filtra os produtos da API pela categoria BEBIDAS e mapeia pela subcategoria/descrição correta
            val produtosFiltrados = listaProdutosCompleta.filter {
                it.categoria == "BEBIDAS" && it.descricao == subcategoriaSelecionada.codigo
            }

            // Abre direto a DetalheActivity
            val intent = Intent(this@MainActivityPedido, DetalheActivity::class.java)
            intent.putExtra("TITULO_TELA", subcategoriaSelecionada.nome)
            intent.putExtra("PRODUTOS_LISTA", ArrayList(produtosFiltrados))
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }

    private fun abrirDetalhes(produto: Produto) {
        val intent = Intent(this, DetalheActivity::class.java)
        intent.putExtra("PRODUTO_SELECIONADO", produto)
        startActivity(intent)
    }
}

data class SubcategoriaItem(
    val nome: String,
    val codigo: String,
    val imagemResId: Int
)