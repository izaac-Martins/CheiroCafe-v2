package com.example.cheirocafe

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetalheActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhe)

        // Verificação visual do que chegou no Intent
        val extras = intent.extras
        if (extras == null) {
            Log.e("DEBUG_INTENT", "Intent veio VAZIO (nulo)")
        } else {
            val keys = extras.keySet()
            keys.forEach { key ->
                Log.d("DEBUG_INTENT", "O Intent contém a chave: $key")
            }
        }

        // Tenta recuperar o produto
        val produto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("PRODUTO_SELECIONADO", Produto::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("PRODUTO_SELECIONADO") as? Produto
        }

        val imgProduto = findViewById<ImageView>(R.id.imgProduto)
        val txtTitulo = findViewById<TextView>(R.id.txtTitulo)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupTipo)
        val containerAdicionais = findViewById<LinearLayout>(R.id.containerAdicionais)

        if (produto == null) {
            // Se cair aqui, a chave do intent pode estar errada ou o objeto não foi serializado
            Toast.makeText(this, "Erro: O produto chegou nulo no Intent!", Toast.LENGTH_LONG).show()
            Log.e("DEBUG_INTENT", "Falha crítica: Objeto Produto nulo.")
            finish()
            return
        }

        // Se chegou aqui, o produto NÃO é nulo.
        txtTitulo.text = produto.nome

        // Lógica de imagem
        if (produto.categoria == "ESPECIAL") {
            imgProduto.setImageResource(R.drawable.cafecremoso)
        } else {
            imgProduto.setImageResource(R.drawable.cafeprensa)
        }

        // Lógica de carregamento de listas
        // --- DENTRO DA DetalheActivity ---
        try {
            radioGroup.removeAllViews()
            containerAdicionais.removeAllViews()

            // --- TOAST DE DIAGNÓSTICO ---
            val qtdVariantes = produto.variantes?.size ?: 0
            val qtdAdicionais = produto.adicionais?.size ?: 0

            Toast.makeText(this, "Variantes: $qtdVariantes | Adicionais: $qtdAdicionais", Toast.LENGTH_LONG).show()
            // ----------------------------

            // Carrega Variantes
            produto.variantes?.forEach { variante ->
                val rb = RadioButton(this)
                rb.text = "${variante.nome} (R$ ${"%.2f".format(variante.preco)})"
                radioGroup.addView(rb)
            }

            // Carrega Adicionais
            produto.adicionais?.forEach { adicional ->
                val cb = CheckBox(this)
                cb.text = "${adicional.nome} (+ R$ ${"%.2f".format(adicional.preco)})"
                containerAdicionais.addView(cb)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}