package com.example.minapp.logica

import com.example.minapp.negocio.FinanzasViewModel
import com.example.minapp.pantallas.PantallaPrincipal
import com.example.minapp.pantallas.PantallaAgregarRegistro

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable 
/*Para poder navegar entre pantallas hay que definir una navegación que indica a los "lugares"
y como llegar a ellos, siendo los lugares, las pantallas */
fun Navegacion (viewModel : FinanzasViewModel){
    /*Acá se crea el controlador principal */
    val navController = rememberNavController() /*Gracias a esto funciona como una pila y 
                                                    retorna a la pantalla anterior */

    /*El NavHost es dónde se registran las pantallas a las que se puede ir */
    NavHost(
        navController = navController,
        startDestination = "inicio" /*Acá se define la pantalla principal, dónde va a brir la app */
    ){
        /*Acá se define la ruta 1, la de inicio */
        composable("inicio"){
            PantallaPrincipal(
                viewModel = viewModel,
                eventoAgregarRegistro = {navController.navigate("agregar_registro")},
                eventoVerHistorial = {navController.navigate("historial_completo")},

                /*Estos son los eventos asociados a los botones de la barra inferior */
                eventoPantallaPerfil = {navController.navigate("perfil")},
                eventoPantallaInicio ={} /*Creo que a pesar de que está acá parada, hay que definirla */
            )
        }  
        
        /*Definción de la 2d ruta */
        composable("perfil"){
            /*PantallaPerfil(
                eventoPantallaInicio ={navController.navigate("inicio")}
                eventoPantallaPerfil = {},
            )*/
        }

        composable("agregar_registro"){
            PantallaAgregarRegistro(
                eventoGuardar = {titulo, monto, fecha, descripcion -> 
                    viewModel.insertarTransaccion(titulo, monto, fecha, descripcion)
                    navController.navigate("inicio"){ popUpTo("inicio") { inclusive = true }}
                },
                eventoCancelar = {navController.navigate("inicio")},
                eventoVolver = {navController.navigate("inicio")}
            )
        }

        composable("historial_completo"){
        }
    }
}