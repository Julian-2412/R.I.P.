package com.example.minapp.cajas

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable 
fun BarraInferior(
    onInicioClick : () -> Unit,
    onPerfilClick: () -> Unit,

    mostrarInicio : Boolean = true,
    mostrarPerfil : Boolean = false
    /*Estas variables son las que se usan para "desactivar" o "activar" una ventana u otra */
){
    NavigationBar {
        /*Acá se definen las pestañas, esta es la primera */
        NavigationBarItem(
            selected = mostrarInicio, /*Funciona como la pantalla principal y dice si está activa o no */
            onClick = onInicioClick,
            icon = {
                Icon(imageVector = Icons.Filled.Home, contentDescription = "Inicio")
            },
            label = { /*Define el texto que sale debajo del icóno */
                Text("Inicio") 
            }
        )
        /*Acá se define la segunda */
        NavigationBarItem(
            selected  = mostrarPerfil,
            onClick = onPerfilClick,
            icon = {
                Icon(imageVector = Icons.Filled.Person, contentDescription = "Perfil")
            },
            label = {
                Text("Perfil")
            }
        )
    }
}