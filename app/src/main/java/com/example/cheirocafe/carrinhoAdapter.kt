package com.example.cheirocafe // Certifique-se de que este pacote corresponde ao seu projeto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cheirocafe.PedidoEntity
import com.example.cheirocafe.R

class CarrinhoAdapter(
    private val listaItens: List<PedidoEntity>
) : RecyclerView.Adapter<CarrinhoAdapter.CarrinhoViewHolder>() {

    class CarrinhoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNomeProduto: TextView = itemView.findViewById(R.id.txtNomeProduto)
        val txtQuantidade: TextView = itemView.findViewById(R.id.txtQuantidade)
        val txtPrecoTotal: TextView = itemView.findViewById(R.id.txtTotalCarrinho)
        val txtAdicionais: TextView = itemView.findViewById(R.id.txtAdicionais)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarrinhoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_carrinho, parent, false) // Certifique-se de ter o item_carrinho.xml criado
        return CarrinhoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarrinhoViewHolder, position: Int) {
        val item = listaItens[position]

        holder.txtNomeProduto.text = item.nomeProduto
        holder.txtQuantidade.text = "Qtd: ${item.quantidade}"
        holder.txtPrecoTotal.text = String.format("R$ %.2f", item.precoTotalItem)

        // Se houver adicionais, exibe. Se não, esconde o campo para o layout ficar limpo.
        if (!item.adicionaisEscolhidos.isNullOrEmpty()) {
            holder.txtAdicionais.text = "Adicionais: ${item.adicionaisEscolhidos}"
            holder.txtAdicionais.visibility = View.VISIBLE
        } else {
            holder.txtAdicionais.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return listaItens.size
    }
}