package com.example.cheirocafe

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class DetalheActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhe)

        // 1. Resgata os dados enviados da MainActivityPedido
        val tituloSubcategoria = intent.getStringExtra("TITULO_TELA") ?: "Detalhes"

        @Suppress("UNCHECKED_CAST")
        val listaProdutosDaSubcategoria = intent.getSerializableExtra("PRODUTOS_LISTA") as? ArrayList<Produto> ?: arrayListOf()

        // 2. Vincula as Views locais
        val imgProduto = findViewById<ImageView>(R.id.imgProduto)
        val txtTitulo = findViewById<TextView>(R.id.txtTitulo)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupTipo)
        val containerAdicionais = findViewById<LinearLayout>(R.id.containerAdicionais)
        val containerTamanhos = findViewById<LinearLayout>(R.id.containerTamanhos)

        txtTitulo.text = tituloSubcategoria

        if (tituloSubcategoria.contains("Especiais", ignoreCase = true)) {
            imgProduto.setImageResource(R.drawable.cafecremoso)
        } else {
            imgProduto.setImageResource(R.drawable.cafeprensa)
        }

        try {
            radioGroup.removeAllViews()
            containerAdicionais.removeAllViews()
            containerTamanhos.removeAllViews()

            if (listaProdutosDaSubcategoria.isEmpty()) {
                Toast.makeText(this, "Nenhum produto cadastrado nesta categoria!", Toast.LENGTH_LONG).show()
                return
            }

            // 3. Monta dinamicamente os RadioButtons do banco de dados
            listaProdutosDaSubcategoria.forEachIndexed { index, produtoDoBanco ->
                val rb = RadioButton(this)
                rb.id = index
                rb.text = produtoDoBanco.nome
                rb.setTextColor(Color.parseColor("#040404"))
                radioGroup.addView(rb)
            }

            // 4. Configura o escutador de troca de opções
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId in 0 until listaProdutosDaSubcategoria.size) {
                    val produtoSelecionado = listaProdutosDaSubcategoria[checkedId]
                    atualizarTamanhosEAdicionais(produtoSelecionado, containerTamanhos, containerAdicionais)
                }
            }

            // Força a seleção e renderização inicial do primeiro item do banco
            if (listaProdutosDaSubcategoria.isNotEmpty()) {
                radioGroup.check(0)
                atualizarTamanhosEAdicionais(listaProdutosDaSubcategoria[0], containerTamanhos, containerAdicionais)
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao montar layout: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun atualizarTamanhosEAdicionais(
        produto: Produto,
        containerTamanhos: LinearLayout,
        containerAdicionais: LinearLayout
    ) {
        containerTamanhos.removeAllViews()
        containerAdicionais.removeAllViews()

        val alturaPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics).toInt()
        val raioBordaPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics).toInt()

        // 1. Carrega os tamanhos (Variantes)
        produto.variantes?.forEachIndexed { index, variante ->
            val btnTamanho = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle)
            val params = LinearLayout.LayoutParams(0, alturaPx, 1f)
            btnTamanho.layoutParams = params

            btnTamanho.text = "${variante.nome} (R$ ${"%.2f".format(variante.preco)})"
            btnTamanho.setTextColor(Color.WHITE)
            btnTamanho.setBackgroundColor(Color.parseColor("#432A20"))
            btnTamanho.cornerRadius = raioBordaPx

            containerTamanhos.addView(btnTamanho)

            if (index < (produto.variantes?.size ?: 0) - 1) {
                val space = Space(this)
                val spaceWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
                space.layoutParams = LinearLayout.LayoutParams(spaceWidthPx, LinearLayout.LayoutParams.MATCH_PARENT)
                containerTamanhos.addView(space)
            }
        }

        // 2. Carrega os adicionais
        produto.adicionais?.forEach { adicional ->
            val cb = CheckBox(this)
            cb.text = "${adicional.nome} (+ R$ ${"%.2f".format(adicional.preco)})"
            cb.setTextColor(Color.parseColor("#0D0D0D"))
            containerAdicionais.addView(cb)
        }
    }
}