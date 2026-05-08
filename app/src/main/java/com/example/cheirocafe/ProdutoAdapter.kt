package com.example.cheirocafe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adicionamos o parâmetro onItemClick no construtor
class ProdutoAdapter(
    private val listaProdutos: List<Produto>,
    private val onItemClick: (Produto) -> Unit
) : RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val produto = listaProdutos[position]

        holder.nome.text = produto.nome
        holder.preco.text = "R$ %.2f".format(produto.preco)

        // Configura o clique no card inteiro
        holder.itemView.setOnClickListener {
            onItemClick(produto) // Chama a função que passamos lá atrás
        }
    }

    override fun getItemCount(): Int = listaProdutos.size

    class ProdutoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.tvNomeProduto)
        val preco: TextView = view.findViewById(R.id.tvPrecoProduto)
    }
}