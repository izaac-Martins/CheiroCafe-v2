package com.example.cheirocafe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubcategoriaAdapter(
    private val lista: List<SubcategoriaItem>,
    private val cliqueItem: (SubcategoriaItem) -> Unit
) : RecyclerView.Adapter<SubcategoriaAdapter.SubcategoriaViewHolder>() {

    class SubcategoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProduto: ImageView = view.findViewById(R.id.ivProduto)
        val tvNomeProduto: TextView = view.findViewById(R.id.tvNomeProduto)
        val tvPrecoProduto: TextView = view.findViewById(R.id.tvPrecoProduto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoriaViewHolder {
        // Usamos o seu layout 'item_produto.xml' para manter o mesmo design arredondado e elegante
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_produto, parent, false)
        return SubcategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubcategoriaViewHolder, position: Int) {
        val item = lista[position]

        // Define o título da subcategoria (Ex: "Cafés Tradicionais") e a foto correspondente
        holder.tvNomeProduto.text = item.nome
        holder.ivProduto.setImageResource(item.imagemResId)

        // COMO VOCÊ SUGERIU: Removemos totalmente o preço da tela para estes cards de categorias!
        holder.tvPrecoProduto.visibility = View.GONE

        holder.itemView.setOnClickListener {
            cliqueItem(item)
        }
    }

    override fun getItemCount(): Int = lista.size
}