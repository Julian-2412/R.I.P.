package com.example.minapp.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.DateRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarRegistro(
   eventoGuardar : (titulo : String, monto : Double, fecha : Long, descripcion : String) -> Unit,
   /*Este evento manda esa información, la que el usuario introduzca en el formulario */
   eventoCancelar : () -> Unit,
   eventoVolver : () -> Unit 
){
    var descripcionInput by remember { mutableStateOf("") }
    var tituloInput by remember {mutableStateOf("")}
    var montoInput by remember {mutableStateOf("")}
    var fecha by remember {mutableStateOf(System.currentTimeMillis())}
    var esIngreso by remember {mutableStateOf(true)}
    /*remember es una función de Jetpack Compose que más o menos alamcena el dato temporalmente,
    mutableStateOf crea un estado mutable que inicializa con una cadena vacia */

    val montoNumerico = montoInput.toDoubleOrNull() ?: 0.0
    val datosValidos = tituloInput.isNotBlank() && montoNumerico > 0.0
    /*El botón se encenderá SOLO si hay texto en la descripción y si el monto es un número 
    válido mayor a cero */

    /*Se formatea la fecha para presentar de una mejor manera */
    val formateador = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyy")
    var fechaTextoInput by remember{
        mutableStateOf(
            java.time.Instant.ofEpochMilli(fecha)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .format(formateador)
        )
    }

    /*Variable para saber si el calendario está abierto o no */
    var mostrarCalendario by remember {mutableStateOf(false)}

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Registro Nuevo", fontSize = 20.sp)},
                navigationIcon = {
                    IconButton(onClick = eventoVolver){
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Átras")
                    }
                }
            )
        }
    ){innerPadding ->
        Column(
            modifier = Modifier
            .fillMaxWidth()
            .padding(innerPadding)
            .padding(20.dp)
            
        ){
            /*Acá se define la tarjeta para escoger si es un ingreso o un gasto, dos botones
            uno junto al otro y se colorea cundo está seleccionado */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ){
                Column(modifier = Modifier.padding(16.dp)){
                    Text(
                        text = "Tipo de movimiento",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ){
                        /*Acá está el botón del ingreso */
                        Button(
                            onClick = {esIngreso = true},
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (esIngreso) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surface
                            )
                        ){
                            Text("Ingreso (+)", color = if (esIngreso) MaterialTheme.colorScheme.onPrimary
                                                            else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        /*Botón de egreso */
                        Button(
                            onClick = {esIngreso = false},
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!esIngreso) MaterialTheme.colorScheme.error
                                                    else MaterialTheme.colorScheme.surface
                            )
                        ){
                            Text("Egreso (-)", color = if (!esIngreso) MaterialTheme.colorScheme.onError
                                                        else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            /*Acá se define el primer campo rellenable, titulo */
            OutlinedTextField(
                value = tituloInput,
                onValueChange = {tituloInput = it},
                label = {Text("Titulo")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            /*Acá se define el segundo campo, el monto */
            OutlinedTextField(
                value = montoInput,
                onValueChange = {texto -> 
                    /*Acá se define un filtro para poder recibir solo números */
                    if (texto.isEmpty()  ||  texto.matches(Regex("^\\d*\\.?\\d*\$"))){
                        montoInput = texto
                    }
                },
                label = {Text("Monto ($)")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                /*Esta instrucción hace que se abra el teclado número automaticamente */
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)

            )

            Spacer(modifier = Modifier.height(16.dp))

            if(mostrarCalendario){
                /*Se inicializa el calendario con la variable generica de arriba */
                val estadoCalendario = rememberDatePickerState(initialSelectedDateMillis = fecha)
                
                DatePickerDialog(
                    onDismissRequest = {mostrarCalendario = false}, /*Hace que se ciera si se toca fuera de el */
                    confirmButton = {
                        Button(
                            onClick = {
                                /*Acá es dónde se decide si se deja la fecha prederminada o la que el usuario pone */
                                val seleccionMilis = estadoCalendario.selectedDateMillis ?: fecha
                                fecha = seleccionMilis

                                /*Actualiza el campo del texto, con formato DD/MM/AAAA */
                                fechaTextoInput =  java.time.Instant.ofEpochMilli(seleccionMilis)
                                                    .atZone(java.time.ZoneId.systemDefault())
                                                    .toLocalDate()
                                                    .format(formateador)
                                
                                mostrarCalendario = false
                            }
                        ){
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = {mostrarCalendario = false}){
                            Text("Cancelar")
                        }
                    }
                ){
                    DatePicker(state = estadoCalendario)
                }
            }

            /*Acá se define el tercer campo, la fecha, la cual siempre comienza con una sugerencia 
            de la fecha actual, y si no se modifica, esa es la que se manda*/
            OutlinedTextField(
                value = fechaTextoInput,
                onValueChange = {},
                label = {Text("Fecha de transacción")},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,  /*Evita que se abra cualquier teclado */
                trailingIcon = {
                    IconButton(onClick = {mostrarCalendario = true}){
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Seleccionar feccha"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            /*Acá se define el cuarto campo, la descripción, la cual es completamente opcional */
             OutlinedTextField(
                value = descripcionInput,
                onValueChange = {descripcionInput = it},
                label = {Text("Descripción")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ){
                OutlinedButton(
                    onClick = eventoCancelar,
                    modifier = Modifier.weight(1f)
                ){
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        /*Acá se tiene que modificar el monto pues si es egreso, hay que
                        almacenar el monto como negativo */
                        val montoFinal = if(esIngreso) montoNumerico else (montoNumerico * -1)
                        eventoGuardar(tituloInput, montoFinal, fecha, descripcionInput)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = datosValidos
                ){
                    Text("Guardar")
                }
            }
        }

    }
}
