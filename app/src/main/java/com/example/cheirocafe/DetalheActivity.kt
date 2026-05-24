package com.example.cheirocafe

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalheActivity : AppCompatActivity() {
    // Instância do banco Room
    private lateinit var database: AppDatabase
    // Guarda o produto que está ativo na tela no momento
    private var produtoAtual: Produto? = null
    private var mesaAtual: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhe)

        // 1. Resgata os dados enviados da MainActivityPedido
        val tituloSubcategoria = intent.getStringExtra("TITULO_TELA") ?: "Detalhes"

        @Suppress("UNCHECKED_CAST")
        val listaProdutosDaSubcategoria =
            intent.getSerializableExtra("PRODUTOS_LISTA") as? ArrayList<Produto> ?: arrayListOf()

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
                Toast.makeText(
                    this,
                    "Nenhum produto cadastrado nesta categoria!",
                    Toast.LENGTH_LONG
                ).show()
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

                    produtoAtual = produtoSelecionado
                    atualizarTamanhosEAdicionais(
                        produtoSelecionado,
                        containerTamanhos,
                        containerAdicionais
                    )
                }
            }

            // Força a seleção e renderização inicial do primeiro item do banco
            if (listaProdutosDaSubcategoria.isNotEmpty()) {
                radioGroup.check(0)

                produtoAtual = listaProdutosDaSubcategoria[0]
                atualizarTamanhosEAdicionais(
                    listaProdutosDaSubcategoria[0],
                    containerTamanhos,
                    containerAdicionais
                )
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao montar layout: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Inicializa o Room
        database = AppDatabase.getDatabase(this)
        // Configura o clique do botão de adicionar (Ajuste o ID 'btnAdicionar' para o real do seu XML)
        val btnAdicionar = findViewById<Button>(R.id.btnAdicionar)
        // Como vai ficar:
        btnAdicionar.setOnClickListener {
            val produto = produtoAtual
            if (produto != null) {
                // Opções que vão aparecer no primeiro popup
                val opcoes = arrayOf("Não dividir (Conta Única)", "Dividir Conta (Atribuir a um Cliente)")

                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Organização do Pedido")
                    .setSingleChoiceItems(opcoes, 0) { dialog, clickedIndex ->
                        dialog.dismiss() // Fecha o primeiro popup

                        if (clickedIndex == 0) {
                            // Cenário 1: Não dividir -> Passamos "Mesa Total" como pagador padrão
                            salvarPedidoNoBanco(produto, pagador = "Mesa Total", mesa = mesaAtual)
                        } else {
                            // Cenário 2: Dividir -> Chama a função para escolher qual cliente vai pagar
                            perguntarNumeroMesa(produto)
                        }
                    }
                    .show()
            } else {
                Toast.makeText(
                    this,
                    "Por favor, selecione um produto primeiro!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }//Fim da inicialização do Room

        //Bloco para configurar o + e - na aba pedidos
        val btnMenos = findViewById<Button>(R.id.btnMenos) // Ajuste para o seu ID do '-'
        val btnMais = findViewById<Button>(R.id.btnMais)   // Ajuste para o seu ID do '+'
        val txtQuantidade = findViewById<TextView>(R.id.txtQuantidade)

        btnMenos.setOnClickListener {
            val qteAtual = txtQuantidade.text.toString().toIntOrNull() ?: 1
            if (qteAtual > 1) {
                txtQuantidade.text = (qteAtual - 1).toString()
                produtoAtual?.let { produto -> atualizarPrecoBotao(produto) } // Recalcula o preço
            }
        }

        btnMais.setOnClickListener {
            val qteAtual = txtQuantidade.text.toString().toIntOrNull() ?: 1
            txtQuantidade.text = (qteAtual + 1).toString()
            produtoAtual?.let { produto -> atualizarPrecoBotao(produto) } // Recalcula o preço
        }//Fim

    }/////Fim do Oncreate////////

    ////Começo da função salvarPedidoNoBanco////
    private fun salvarPedidoNoBanco(produto: Produto, pagador: String, mesa: Int) {
        val nomeDoCafe = produto.nome
        val containerTamanhos = findViewById<LinearLayout>(R.id.containerTamanhos)
        val containerAdicionais = findViewById<LinearLayout>(R.id.containerAdicionais)
        val radioGroupTipo = findViewById<RadioGroup>(R.id.radioGroupTipo)
        val txtQuantidade = findViewById<TextView>(R.id.txtQuantidade)

        val quantidadeTexto = txtQuantidade?.text?.toString() ?: "1"
        val quantidade = quantidadeTexto.toIntOrNull() ?: 1

        // 🌟 ALTERAÇÃO 1 AQUI: Pegando o preço dinâmico do botão
        val btnAdicionar = findViewById<Button>(R.id.btnAdicionar)
        val textoBotao = btnAdicionar.text.toString()
        val precoTexto = textoBotao.substringAfter("R$ ").substringBefore(")")
        val precoTotal = precoTexto.replace(",", ".").toDoubleOrNull() ?: 0.0

        // 1. CAPTURAR O TAMANHO SELECIONADO DINAMICAMENTE
        var tamanhoSelecionado = "Não selecionado"
        for (i in 0 until containerAdicionais.childCount) {
            val filho = containerTamanhos.getChildAt(i)
            if (filho is MaterialButton && (filho.isSelected || filho.isChecked)) {
                val botao = filho as MaterialButton // 👈 Dizemos ao Kotlin que ele é um botão de verdade!
                tamanhoSelecionado = botao.text.toString().substringBefore(" (")
                break
            }
        }

        // 2. CAPTURAR OS ADICIONAIS MARCADOS DINAMICAMENTE
        val listaAdicionais = mutableListOf<String>()
        for (i in 0 until containerAdicionais.childCount) {
            val filho = containerAdicionais.getChildAt(i)
            if (filho is CheckBox && filho.isChecked) {
                listaAdicionais.add(filho.text.toString().substringBefore(" (+"))
            }
        }
        val adicionaisTexto = if (listaAdicionais.isEmpty()) "Nenhum" else listaAdicionais.joinToString(", ")

        // 3. CAPTURAR O TIPO DE GRÃO SELECIONADO
        val idSelecionado = radioGroupTipo.checkedRadioButtonId
        val radioButtonSelecionado = findViewById<RadioButton>(idSelecionado)
        val tipoCafeTexto = radioButtonSelecionado?.text?.toString() ?: "Padrão"

        // 4. MONTAR O OBJETO COM OS ATRIBUTOS COMPLETOS
        val novoItem = PedidoEntity(
            produtoId = produto.id ?: 0L,
            nomeProduto = nomeDoCafe,
            tamanhoEscolhido = tamanhoSelecionado,
            tipoGraoEscolhido = tipoCafeTexto,
            adicionaisEscolhidos = adicionaisTexto,
            quantidade = quantidade,
            precoTotalItem = precoTotal,
            pagadorItem = pagador,
            numeroMesa = mesa
        )

        // 5. SALVAR NO ROOM EM SEGUNDO PLANO
        lifecycleScope.launch(Dispatchers.IO) {
            database.pedidoDao().inserirItem(novoItem)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@DetalheActivity, "$nomeDoCafe adicionado ao carrinho!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

        private fun atualizarTamanhosEAdicionais(
            produto: Produto,
            containerTamanhos: LinearLayout,
            containerAdicionais: LinearLayout
        ) {
            containerTamanhos.removeAllViews()
            containerAdicionais.removeAllViews()

            val alturaPx =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics)
                    .toInt()
            val raioBordaPx =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics)
                    .toInt()

            // 1. Carrega os tamanhos (Variantes)
            produto.variantes?.forEachIndexed { index, variante ->
                val btnTamanho = MaterialButton(this@DetalheActivity)
                val params = LinearLayout.LayoutParams(0, alturaPx, 1f)
                btnTamanho.layoutParams = params
                btnTamanho.isCheckable = true

                val nomeComMl = if (variante.nome?.contains(
                        "ml",
                        ignoreCase = true
                    ) == true
                ) variante.nome else "${variante.nome}ml"
                btnTamanho.text = "$nomeComMl (R$ ${"%.2f".format(variante.preco)})"
                btnTamanho.setTextColor(Color.WHITE)
                btnTamanho.setBackgroundColor(Color.parseColor("#432A20"))
                btnTamanho.cornerRadius = raioBordaPx

                containerTamanhos.addView(btnTamanho)

                btnTamanho.setOnClickListener {
                    for (i in 0 until containerTamanhos.childCount) {
                        val outroFilho = containerTamanhos.getChildAt(i)
                        if (outroFilho is MaterialButton) outroFilho.isChecked = false
                    }
                    btnTamanho.isChecked = true
                    atualizarPrecoBotao(produto)
                }

                if (index < (produto.variantes?.size ?: 0) - 1) {
                    val space = Space(this)
                    val spaceWidthPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16f,
                        resources.displayMetrics
                    ).toInt()
                    space.layoutParams =
                        LinearLayout.LayoutParams(spaceWidthPx, LinearLayout.LayoutParams.MATCH_PARENT)
                    containerTamanhos.addView(space)
                }
            }
            // 2. Carrega os adicionais
            produto.adicionais?.forEach { adicional ->
                val cb = CheckBox(this)
                cb.text = "${adicional.nome} (+ R$ ${"%.2f".format(adicional.preco)})"
                cb.setTextColor(Color.parseColor("#0D0D0D"))
                cb.setOnCheckedChangeListener { _, _ -> atualizarPrecoBotao(produto) }
                containerAdicionais.addView(cb)
            }
        }//Fim do atualizarTamanhosAdicionais

    //Comeco do AtualizarPrecoBotao
    private fun atualizarPrecoBotao(produto: Produto) {
        val containerTamanhos = findViewById<LinearLayout>(R.id.containerTamanhos)
        val containerAdicionais = findViewById<LinearLayout>(R.id.containerAdicionais)
        val btnAdicionar = findViewById<Button>(R.id.btnAdicionar)

        // 1. Descobre o preço do tamanho marcado usando o ÍNDICE REAL da View
        var precoBase = 0.0
        var tamanhoMarcado = false

        for (i in 0 until containerTamanhos.childCount) {
            val filho = containerTamanhos.getChildAt(i)
            // Se for o botão de tamanho e ele estiver checado/selecionado
            if (filho is com.google.android.material.button.MaterialButton && filho.isChecked) {
                tamanhoMarcado = true

                // Descobrimos o índice real deste botão dentro do container
                val indiceBotao = containerTamanhos.indexOfChild(filho)
                // Como temos Spaces (espaçadores) entre os botões, precisamos mapear o índice correto na lista.
                // O primeiro botão é índice 0, o Space é 1, o segundo botão é 2, o Space é 3...
                // Dividindo o índice por 2, temos a posição exata na lista de variantes!
                val indiceLista = indiceBotao / 2
                val listaVariantes = produto.variantes
                if (listaVariantes != null && indiceLista in listaVariantes.indices) {
                    precoBase = listaVariantes[indiceLista].preco ?: 0.0
                }
                break
            }
        }
        // Se o usuário não marcou nenhum tamanho ainda, usa o preço base do produto
        if (!tamanhoMarcado) {
            precoBase = produto.preco ?: 0.0
        }

        // 2. Soma o preço de todos os adicionais que estão marcados
        var somaAdicionais = 0.0
        for (i in 0 until containerAdicionais.childCount) {
            val filho = containerAdicionais.getChildAt(i)
            if (filho is CheckBox && filho.isChecked) {
                val textoCheckbox = filho.text.toString().trim()
                produto.adicionais?.forEach { adicional ->
                    val nomeAdicional = adicional.nome?.trim() ?: ""
                    if (textoCheckbox.contains(nomeAdicional, ignoreCase = true)) {
                        somaAdicionais += adicional.preco ?: 0.0
                    }
                }
            }
        }
        // 3. Atualiza o texto do botão verde com a soma de TUDO
        val txtQuantidade = findViewById<TextView>(R.id.txtQuantidade) // 👈 ADICIONE
        val quantidade = txtQuantidade?.text?.toString()?.toIntOrNull() ?: 1 // 👈 ADICIONE

        // 4. Multiplica o total pela quantidade 👇
        val total = (precoBase + somaAdicionais) * quantidade // 👈 ALTERE APENAS ESSA LINHA

        // 5. Atualiza o texto do botão verde com a soma de TUDO
        btnAdicionar.text = "ADICIONAR AO PEDIDO (R$ ${"%.2f".format(total)})"

    }//Fim do atualizarPrecoBotao
    private fun perguntarNumeroMesa(produto: Produto) {
        val inputMesa = android.widget.EditText(this)
        inputMesa.hint = "Ex: 5, 12, 22..."
        inputMesa.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        val container = android.widget.LinearLayout(this)
        val params = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(48, 16, 48, 16)
        inputMesa.layoutParams = params
        container.addView(inputMesa)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Número da Mesa")
            .setMessage("Digite o número da mesa para este pedido:")
            .setView(container)
            .setPositiveButton("Confirmar") { dialog, _ ->
                val mesaDigitada = inputMesa.text.toString().trim()
                val mesaFinal = if (mesaDigitada.isNotEmpty()) mesaDigitada else "Avulso"

                dialog.dismiss()

                // Agora que temos a mesa, abrimos o painel inferior (BottomSheet)
                abrirPainelClientesMesa(produto, mesaFinal)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }//Fim do perguntarNumeroMesa
    private fun abrirPainelClientesMesa(produto: Produto, numeroMesa: String) {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)

        // Criamos o layout do painel dinamicamente
        val layoutPainel = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 32, 48, 64)
            background = android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE)
        }

        // Título do Painel (Correção do texto em SP e estilo)
        val txtTitulo = android.widget.TextView(this).apply {
            text = "Gerenciar Mesa $numeroMesa"
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 20f)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#333333"))
        }
        layoutPainel.addView(txtTitulo)

        // Subtítulo informativo (Correção do texto em SP)
        val txtSubtitulo = android.widget.TextView(this).apply {
            text = "Digite o nome do cliente desta mesa que vai pagar este item:"
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(android.graphics.Color.GRAY)
            setPadding(0, 8, 0, 24)
        }
        layoutPainel.addView(txtSubtitulo)

        // Campo para digitar o nome do cliente
        val inputNomeCliente = android.widget.EditText(this).apply {
            hint = "Nome do Cliente (Ex: Bruno, Mariana...)"
        }
        layoutPainel.addView(inputNomeCliente)

        // Botão para Confirmar e Salvar no Banco (Correção das Margens e LayoutParams)
        val btnConfirmar = com.google.android.material.button.MaterialButton(this).apply {
            text = "Adicionar à Conta do Cliente"

            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = 32 // 👈 Correção do marginTop que estava dando erro
            layoutParams = params

            backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4E342E"))
            cornerRadius = 24
        }

        btnConfirmar.setOnClickListener {
            val nomeCliente = inputNomeCliente.text.toString().trim()
            val pagadorFinal = if (nomeCliente.isNotEmpty()) nomeCliente else "Mesa Única"

            // Salva no Room salvando o nome do cliente
            salvarPedidoNoBanco(produto, pagador = "$pagadorFinal (Mesa $mesaAtual)", mesa = mesaAtual)

            bottomSheetDialog.dismiss()
        }
        layoutPainel.addView(btnConfirmar)

        // Exibe o painel na tela
        bottomSheetDialog.setContentView(layoutPainel)
        bottomSheetDialog.show()

    }//Fim do abrirPainelClientesMesa
}//Fim do detalhe activity