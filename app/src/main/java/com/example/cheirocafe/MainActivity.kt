package com.example.cheirocafe

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Força o app a usar sempre o modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({

            val telaPesquisa = Intent(this, MainActivityPedido::class.java)

            // 1. Diz ao Android para NÃO animar a abertura da próxima tela
            telaPesquisa.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

            startActivity(telaPesquisa)

            // 2. Fecha a Splash Screen
            finish()

            // 3. Garante que nenhuma transição seja executada no fechamento
            overridePendingTransition(0, 0)

        }, 2000)
    }
}