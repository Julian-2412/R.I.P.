package com.example.minapp.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.minapp.database.EntidadRegistros
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private val formateadorFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun PantallaHistorial(
    lista: List<EntidadRegistros>,
    eventoEditarRegistro: (id: String) -> Unit,
    eventoEliminarRegistro: (id: String) -> Unit,
    eventoVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    /* Se usa el modifier recibido como base, en vez de crear uno nuevo */
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = eventoVolver) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Atrás"
                )
            }

            Text(
                text = "Historial transacciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        /* LazyColumn solo dibuja los elementos visibles y reutiliza el resto al hacer scroll */
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            if (lista.isEmpty()) {
                item {
                    Text(
                        text = "No hay registros guardados aún.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            /* key mantiene estable cada fila aunque la lista cambie */
            items(lista, key = { it.id }) { transaccion ->
                FilaTransaccion(
                    item = transaccion,
                    onEditarClick = { eventoEditarRegistro(transaccion.id) },
                    onEliminarClick = { eventoEliminarRegistro(transaccion.id) }
                )
            }
        }
    }
}

/* Cada renglón es una Card clicable: toda el área responde al toque */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilaTransaccion(
    item: EntidadRegistros,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    /* La fecha se guarda como medianoche UTC (así la entrega el DatePicker),
       por eso se formatea con UTC y no con la zona del teléfono. */
    val fechaLegible = Instant.ofEpochMilli(item.fecha)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(formateadorFecha)

    val esIngreso = item.monto >= 0

    Card(
        onClick = onEditarClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                /* Título a la izquierda, ocupa el espacio sobrante */
                Text(
                    text = item.titulo,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp
                )

                /* Monto a la derecha, con color según sea ingreso o egreso */
                Text(
                    text = (if (esIngreso) "+$" else "-$") + String.format("%.2f", abs(item.monto)),
                    color = if (esIngreso) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = onEliminarClick,
                    modifier = Modifier.semantics {
                        contentDescription = "Eliminar ${item.titulo}"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = fechaLegible,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            /* isNullOrBlank evita mostrar un renglón vacío si la descripción es "" */
            if (!item.descripcion.isNullOrBlank()) {
                Text(
                    text = item.descripcion,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}