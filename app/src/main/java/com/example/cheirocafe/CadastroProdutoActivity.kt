package com.example.cheirocafe

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CadastroProdutoActivity : AppCompatActivity() {

    private val listaVariantesTemp = mutableListOf<Variante>()
    private val listaOpcionaisTemp = mutableListOf<Adicional>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_produto)

        // Referências do Layout
        val editNome = findViewById<EditText>(R.id.editNome)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinnerCategoria)
        val editPreco = findViewById<EditText>(R.id.editPreco)

        // Variantes (Tamanhos)
        val editNomeVar = findViewById<EditText>(R.id.editNomeVariante)
        val editPrecoVar = findViewById<EditText>(R.id.editPrecoVariante)
        val btnAddVar = findViewById<Button>(R.id.btnAddVariante)
        val txtListaVar = findViewById<TextView>(R.id.txtListaVariantes)

        // Opcionais / Adicionais
        val rgVinculoOpcional = findViewById<RadioGroup>(R.id.radioGroupVinculoOpcional)
        val rbVinculoProduto = findViewById<RadioButton>(R.id.rbVinculoProduto)
        val editNomeOpc = findViewById<EditText>(R.id.editNomeOpcional)
        val editPrecoOpc = findViewById<EditText>(R.id.editPrecoOpcional)
        val btnAddOpc = findViewById<Button>(R.id.btnAddOpcional)
        val txtListaOpc = findViewById<TextView>(R.id.txtListaOpcionais)

        val btnSalvar = findViewById<Button>(R.id.btnSalvarProduto)

        // CONFIGURAÇÃO DO SPINNER DE CATEGORIAS
        val categorias = arrayOf("BEBIDAS", "DOCES", "SALGADOS")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterSpinner

        // SPINNER DE SUBCATEGORIAS
        val spinnerSubcategoria = findViewById<Spinner>(R.id.spinnerSubcategoria)
        val subcategorias = arrayOf("TRADICIONAL", "ESPECIAL", "SUCOS_IOGURTES", "REFRIGERANTES")
        val adapterSub = ArrayAdapter(this, android.R.layout.simple_spinner_item, subcategorias)
        adapterSub.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubcategoria.adapter = adapterSub

        spinnerCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val catSelecionada = categorias[position]
                spinnerSubcategoria.visibility = if (catSelecionada == "BEBIDAS") View.VISIBLE else View.GONE

                // Ajusta o texto do RadioButton dinamicamente para guiar o usuário
                findViewById<RadioButton>(R.id.rbVinculoCategoria).text = "Geral de $catSelecionada"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Adicionar Tamanho (+)
        btnAddVar.setOnClickListener {
            val n = editNomeVar.text.toString().trim()
            val p = editPrecoVar.text.toString().toDoubleOrNull()
            if (n.isNotEmpty() && p != null) {
                listaVariantesTemp.add(Variante(nome = n, preco = p))
                txtListaVar.text = listaVariantesTemp.joinToString("\n") { "${it.nome} - R$ ${it.preco}" }
                editNomeVar.text.clear()
                editPrecoVar.text.clear()
            }
        }

        // Adicionar Opcional (+) com Inteligência de Vínculo REAL
        btnAddOpc.setOnClickListener {
            val n = editNomeOpc.text.toString().trim()
            val p = editPrecoOpc.text.toString().toDoubleOrNull()

            if (n.isNotEmpty() && p != null) {
                val categoriaAtual = spinnerCategoria.selectedItem.toString()

                // Se a opção marcada for produto, mandamos "PRODUTO", senão criamos a flag da categoria correspondente
                val tipoVinculo = if (rbVinculoProduto.isChecked) {
                    "PRODUTO"
                } else {
                    "CATEGORIA_$categoriaAtual"
                }

                listaOpcionaisTemp.add(Adicional(nome = n, preco = p, vinculo = tipoVinculo))

                // Atualiza o texto na listagem indicando onde esse opcional vai parar
                txtListaOpc.text = listaOpcionaisTemp.joinToString("\n") {
                    "${it.nome} - R$ ${it.preco} [${it.vinculo}]"
                }

                editNomeOpc.text.clear()
                editPrecoOpc.text.clear()
            }
        }

        // Lógica Final para Salvar e Enviar para a API
        btnSalvar.setOnClickListener {
            val nome = editNome.text.toString().trim()
            val cat = spinnerCategoria.selectedItem.toString()
            val subcat = if (cat == "BEBIDAS") spinnerSubcategoria.selectedItem.toString() else ""
            val precoBase = editPreco.text.toString().toDoubleOrNull() ?: 0.0

            if (nome.isEmpty()) {
                Toast.makeText(this, "O nome do produto é obrigatório!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val variantesFinal = listaVariantesTemp.toList()
            val adicionaisFinal = listaOpcionaisTemp.toList()

            val produtoParaEnviar = Produto(
                id = null,
                nome = nome,
                descricao = subcat,
                preco = precoBase,
                categoria = cat,
                variantes = variantesFinal,
                adicionais = adicionaisFinal
            )

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.cadastrarProduto(produtoParaEnviar)

                    if (response.isSuccessful) {
                        Toast.makeText(this@CadastroProdutoActivity, "Produto salvo com sucesso no banco!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@CadastroProdutoActivity, "Erro no servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CadastroProdutoActivity, "Falha de conexão: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}