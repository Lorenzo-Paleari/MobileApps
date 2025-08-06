package com.example.filmworkappproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.filmworkappproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //oggetto che rappresenta tutto il layout activity_main, che contiene il fragment della lista
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // mostra il layout all'utente

        enableEdgeToEdge()
        //per evitare che l'app sia tagliata da altro
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets -> //quando cambiano gli spazi
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()) //prende le grandezze
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom) //li setta
            insets
        }
    }
}