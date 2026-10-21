package com.example.minapp.pantallas

import com.example.minapp.cajas.BarraInferior
import com.example.minapp.cajas.BarraSuperior

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun PantallaPerfil(
    logeado : Boolean,
    mensajError : String?,
    onLoginClick : (String, String) -> Unit,
    onExportarClick : () -> Unit,
    onImportarClick : () -> Unit,
    onCerrarSesionClick : () -> Unit,
    eventoPantallaInicio : () -> Unit,
    eventoPantallaPerfil : () -> Unit,

){
    var usuarioInput by remember {mutableStateOf("")}
    var claveInput by remember {mutableStateOf("")}
    var snackbarHostState = remember {SnackbarHostState()}

    /*Hay que crear inicializar el control del teclado */
    val keyboardController = LocalSoftwareKeyboardController.current

    /*Se mantiene al tanto para que cada que mensajError cambie su valor se activa este bloque
    de código y muestra el mensaje flotante */
    LaunchedEffect(mensajError){
        mensajError?.let{ textoDeError ->
                snackbarHostState.showSnackbar(
                    message = textoDeError,
                    actionLabel = "Entendido"
                )
        }
    }

    Scaffold(
        topBar = {BarraSuperior()},

        /*Hueco que trae Scaffold para ostrar informació rapída en la parte inferior */
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },

        bottomBar ={
            BarraInferior(
                onInicioClick = eventoPantallaInicio,
                onPerfilClick = eventoPantallaPerfil,
                mostrarInicio = false,
                mostrarPerfil = true  /*Esas instrucciones hacen que se ilumine el ícono de la pantalla */
            )
        }
    ){innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ){
            /*Acá es dónde se define que pantalla mostrando, si la variable de logeado es fale
            o true */
            if(!logeado){
                Text(
                    "Iniciar Sesión en la nube",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = usuarioInput,
                    onValueChange = {usuarioInput = it},
                    label = {Text("Usuario")},
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = claveInput,
                    onValueChange = {claveInput = it},
                    label = {Text("Contraseña")},
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        keyboardController?.hide() 
                        onLoginClick(usuarioInput, claveInput)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = usuarioInput.isNotBlank() && claveInput.isNotBlank()
                ){
                    Text("Acceder")
                }

            }else{
                Text(
                    "Sinronización en la nube",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onExportarClick,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("Exportar datos")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onImportarClick,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("Importar datos")
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onCerrarSesionClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ){
                    Text("Cerrar sesión")
                }
            }
        }
    }
}