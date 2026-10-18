package com.example.minapp.logica

import com.example.minapp.negocio.FinanzasViewModel
import com.example.minapp.pantallas.PantallaPrincipal
import com.example.minapp.pantallas.PantallaAgregarRegistro
import com.example.minapp.pantallas.PantallaHistorial
import com.example.minapp.pantallas.PantallaEditarRegistro
import com.example.minapp.pantallas.PantallaPerfil

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState


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
            PantallaPerfil(
                logeado = viewModel.usuarioLogeado, 
                mensajError  = viewModel.mensajError,

                onLoginClick = {
                    usuario, clave -> viewModel.ejecutaLogin(usuario, clave)
                },
                onExportarClick = {
                    
                },
                onImportarClick = {
                    viewModel.importarDatos()
                },
                onCerrarSesionClick = {
                    viewModel.cerrarSesion()
                },
                eventoPantallaInicio = {navController.navigate("inicio")},
                eventoPantallaPerfil = {}
            )
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
            PantallaHistorial(
                lista = viewModel.listaTransacciones.collectAsState().value,
                eventoEditarRegistro = {idRecibido -> navController.navigate("editar_registro/$idRecibido")},
                eventoVolver = {navController.popBackStack()}
            )
        }

        composable(
            route = "editar_registro/{id}", /*Indica que es una ruta variable */
            arguments = listOf(
                navArgument("id") {type = NavType.StringType} /*Define el argumento como un texto */
            )
        ){backStackEntry ->
            /*Atrapa el id que viene en la ruta */
            val idRecibido = backStackEntry.arguments?.getString("id") ?: ""
            
            PantallaEditarRegistro(
                idRegistro = idRecibido,
                viewModel = viewModel,
                eventoGuardar = {
                    id, titulo, monto, fecha, descripcion ->
                    viewModel.editarTransaccion(id, titulo, monto, fecha, descripcion)
                    navController.popBackStack()
                },
                eventoCancelar = {navController.popBackStack()},
                eventoVolver = {navController.popBackStack()}
            )
        }
    }
}