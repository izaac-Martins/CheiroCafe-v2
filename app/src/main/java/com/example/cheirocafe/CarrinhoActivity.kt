package com.example.cheirocafe

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class CarrinhoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrinho)

        // Captura o número da mesa que a MainActivityPedido enviou
        val numeroMesa = intent.getIntExtra("NUMERO_MESA", 1)

        // Vincula o campo de texto no XML (edtNumeroMesa)
        val edtNumeroMesa = findViewById<EditText>(R.id.edtNumeroMesa)

        // Coloca o número da mesa automaticamente no campo de texto
        edtNumeroMesa.setText(numeroMesa.toString())
    }
}