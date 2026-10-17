package com.example.minapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModelProvider
import com.example.minapp.database.AppDatabase 
import com.example.minapp.negocio.FinanzasViewModel 
import com.example.minapp.logica.Navegacion 

class MainActivity : ComponentActivity() {

    // 🧠 1. Inicializamos el cerebro de tus finanzas (Room + ViewModel)
    private val finanzasViewModel: FinanzasViewModel by lazy {
        // Obtenemos el DAO de tu base de datos real
        val dao = AppDatabase.obtenerBaseDatos(applicationContext).registroDao()
        
        // Creamos el ViewModel pasándole el DAO
        FinanzasViewModel(dao)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // 🗺️ 2. Solo encendemos el mapa de navegación de la app
                // Le pasamos el finanzasViewModel para que la PantallaPrincipal pueda consumirlo
                Navegacion(viewModel = finanzasViewModel)
            }
        }
    }
}
