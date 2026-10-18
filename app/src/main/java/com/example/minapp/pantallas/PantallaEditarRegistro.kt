package com.example.minapp.pantallas

import com.example.minapp.negocio.FinanzasViewModel

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private val formateadorFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/* El DatePicker de Material3 trabaja con milisegundos de medianoche UTC.
   Por eso se formatea siempre con ZoneOffset.UTC; si se usara la zona del
   teléfono (ej. Colombia, UTC-5), la fecha se mostraría un día antes. */
private fun formatearFecha(millisUtc: Long): String =
    Instant.ofEpochMilli(millisUtc)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(formateadorFecha)

/* Convierte "hoy" (según la zona del teléfono) a medianoche UTC de ese día. */
private fun hoyEnMillisUtc(): Long =
    LocalDate.now(ZoneId.systemDefault())
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarRegistro(
    idRegistro: String,    
    viewModel: FinanzasViewModel,
    eventoGuardar: (id: String, titulo: String, monto: Double, fecha: Long, descripcion: String) -> Unit,
    eventoCancelar: () -> Unit,
    eventoVolver: () -> Unit
) {
    val registros by viewModel.listaTransacciones.collectAsState()
    val registro = registros.find { it.id == idRegistro }
    val esEdicion = registro != null

    /* remember(registro) vuelve a inicializar los campos cuando el registro carga o cambia */
    var tituloInput by remember(registro) { mutableStateOf(registro?.titulo ?: "") }
    var montoInput by remember(registro) {
        mutableStateOf(registro?.monto?.let { abs(it).toString() } ?: "")
    }
    var descripcionInput by remember(registro) { mutableStateOf(registro?.descripcion ?: "") }
    var esIngreso by remember(registro) { mutableStateOf((registro?.monto ?: 1.0) >= 0) }
    var fecha by remember(registro) { mutableStateOf(registro?.fecha ?: hoyEnMillisUtc()) }

    var mostrarCalendario by remember { mutableStateOf(false) }

    val montoNumerico = montoInput.toDoubleOrNull() ?: 0.0
    /* El botón solo se habilita si hay título y el monto es un número mayor a cero */
    val datosValidos = tituloInput.isNotBlank() && montoNumerico > 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (esEdicion) "Editar registro" else "Nuevo registro",
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = eventoVolver) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize() /* fillMaxSize para que el weight empuje los botones abajo */
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            /* Campo 1: título */
            OutlinedTextField(
                value = tituloInput,
                onValueChange = { tituloInput = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            /* Selector ingreso / egreso */
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = esIngreso,
                    onClick = { esIngreso = true },
                    label = { Text("Ingreso") }
                )
                FilterChip(
                    selected = !esIngreso,
                    onClick = { esIngreso = false },
                    label = { Text("Egreso") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            /* Campo 2: monto (siempre positivo en pantalla; el signo lo da el selector) */
            OutlinedTextField(
                value = montoInput,
                onValueChange = { texto ->
                    /* Solo permite dígitos y un punto decimal */
                    if (texto.isEmpty() || texto.matches(Regex("^\\d*\\.?\\d*$"))) {
                        montoInput = texto
                    }
                },
                label = { Text("Monto ($)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            /* Calendario */
            if (mostrarCalendario) {
                val estadoCalendario = rememberDatePickerState(initialSelectedDateMillis = fecha)

                DatePickerDialog(
                    onDismissRequest = { mostrarCalendario = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                fecha = estadoCalendario.selectedDateMillis ?: fecha
                                mostrarCalendario = false
                            }
                        ) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarCalendario = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = estadoCalendario)
                }
            }

            /* Campo 3: fecha (solo lectura, se cambia con el calendario).
               El texto se deriva de `fecha`, así no hay que mantener dos estados. */
            OutlinedTextField(
                value = formatearFecha(fecha),
                onValueChange = {},
                label = { Text("Fecha de transacción") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { mostrarCalendario = true }) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Seleccionar fecha"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            /* Campo 4: descripción (opcional) */
            OutlinedTextField(
                value = descripcionInput,
                onValueChange = { descripcionInput = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4 
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = eventoCancelar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        /* Si es egreso, el monto se almacena como negativo */
                        val montoFinal = if (esIngreso) montoNumerico else -montoNumerico
                        val estado = if (esEdicion) "EDITADO" else "NUEVO"
                        eventoGuardar(
                            idRegistro,
                            tituloInput.trim(),
                            montoFinal,
                            fecha,
                            descripcionInput.trim()
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = datosValidos
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}