package com.example.cheirocafe

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class ListaProdutosFiltradosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // REUTILIZAÇÃO INTELIGENTE:
        // Usamos o mesmo layout da tela de pedido para não precisar criar um XML novo do zero!
        setContentView(R.layout.activity_main_pedido)

        // Pegamos os dados enviados pela tela anterior
        val tituloTela = intent.getStringExtra("TITULO_TELA") ?: "Produtos"
        @Suppress("UNCHECKED_CAST")
        val produtosFiltrados = intent.getSerializableExtra("PRODUTOS_LISTA") as? ArrayList<Produto> ?: arrayListOf()

        // Ajustamos os textos da tela para exibir a subcategoria correspondente
        val txtTitulo = findViewById<TextView>(R.id.textViewLabel)
        txtTitulo.text = tituloTela

        // Configuramos a grade para exibir os produtos reais filtrados
        val recyclerView = findViewById<RecyclerView>(R.id.rvProdutos)

        // Aqui usamos o seu ProdutoAdapter original! O visual do card do café aparece lindão aqui.
        val adapter = ProdutoAdapter(produtosFiltrados) { produtoSelecionado ->
            val intentDet = Intent(this, DetalheActivity::class.java)
            intentDet.putExtra("PRODUTO_SELECIONADO", produtoSelecionado)
            startActivity(intentDet)
        }

        recyclerView.adapter = adapter
    }
}