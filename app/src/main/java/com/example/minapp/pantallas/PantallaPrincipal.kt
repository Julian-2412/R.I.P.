package com.example.minapp.pantallas

import com.example.minapp.cajas.SaldoCaja
import com.example.minapp.cajas.HistorialCaja
import com.example.minapp.cajas.BarraSuperior
import com.example.minapp.cajas.BarraInferior
import com.example.minapp.negocio.FinanzasViewModel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api 
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)     
/*Indica que se está utilizando una API xperimental de Material3 en Jetpack Compose. Esto es 
necesario porque algunas funciones o componentes de Material3 aún están en desarrollo y pueden 
cambiar en futuras versiones.*/

@Composable                                 
/*Etiqueta que vuelve una función un componente visual*/

fun PantallaPrincipal(
    /*Recibe parámetros con los que va a trabajar/mostrar eventos o modificadores*/

    viewModel : FinanzasViewModel, /*Esto es lo que permite consumir lo que está en la base de datos por fin */

    eventoVerHistorial : () -> Unit,
    eventoAgregarRegistro : () -> Unit,
    eventoPantallaPerfil : () -> Unit,
    eventoPantallaInicio : () -> Unit
){

val transacciones by viewModel.listaTransacciones.collectAsState()
val saldoActual  by viewModel.saldoTotal.collectAsState()
val ingresos by viewModel.ingresosTotales.collectAsState()
val egresos by viewModel.egresosTotales.collectAsState()

/*El "collectAsState" funciona como el oyente de flow, que era el que estaba 
conetado directamente con la base de datos espectante a algún cambio, y avisa su hubo algún
cambios para poder redibujar la pantalla si fuere necesario */

Scaffold(
    /*Un esqueleto para una pantalla, me da espacio para la barra superior, la inferior, un botón
    flotante y el centro de la aplicación, todo listo para rellenar */

    topBar = {
        BarraSuperior()
    },

    bottomBar = {
        BarraInferior(
            onInicioClick = eventoPantallaInicio,
            onPerfilClick = eventoPantallaPerfil
        )
    },

    floatingActionButton = {
        FloatingActionButton(
            onClick = {
                eventoAgregarRegistro()
            }
        ){
            Icon(Icons.Filled.Add, contentDescription = "Añadir")
        }
    }  

){innerpadding-> 
    /*Este es el cuerpo de la pantallla */

    Column(
        modifier = Modifier
        .fillMaxSize()
        .padding(innerpadding) /*encaja el contenido en el centro */
        .padding(16.dp)
    ){
        SaldoCaja(saldo = saldoActual, ingresos = ingresos, egresos = egresos)
        /*Se le pasa el sado par que lo dibuje, lo mimso con la función de abajo */
        HistorialCaja(lista = transacciones )

        Button(
            onClick = eventoVerHistorial
        ){ 
            Text("Ver todo el historial") 
        }
    }
}
}   

