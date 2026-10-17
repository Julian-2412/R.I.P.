package com.example.minapp.cajas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minapp.database.EntidadRegistros

@Composable
fun HistorialCaja(
    lista: List<EntidadRegistros>, 
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título del recuadro
            Text(
                text = "Historial Reciente",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            /*LazyColumn es un contenedor inteligente, permite renrdezar solo unos cuantos
            registros y a medida que se hace sroll con el que trae integrado los va desechando
            y renderizando */
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    // Limita la altura del recuadro. Si mide más de 250dp, activa el scroll.
                    .heightIn(max = 300.dp) 
            ) {
                // Si la base de datos está vacía, muestra un mensaje amigable
                if (lista.isEmpty()) {
                    item {
                        Text(
                            text = "No hay registros guardados aún.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                // Recorremos la lista y dibujamos cada fila de forma eficiente
                items(lista) { transaccion ->
                    FilaTransaccion(item = transaccion)
                }
            }
        }
    }
}

// Componente secundario para diseñar cómo se ve cada renglón del historial
@Composable
fun FilaTransaccion(item: EntidadRegistros) {
    Column(
        modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)
    ){
        val formateador = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaLegible = java.time.Instant.ofEpochMilli(item.fecha)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .format(formateador)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Título del gasto/ingreso (Ocupa el espacio izquierdo)
            Text(
                text = item.titulo,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
            )

            // Monto (Ocupa el espacio derecho). Cambia de color si es positivo o negativo.
            val esIngreso = item.monto > 0
            Text(
                text = (if (esIngreso) "+$" else "-$") + String.format("%.2f", Math.abs(item.monto)),
                color = if (esIngreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Text(
            text = fechaLegible, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, // Un color secundario más suave
            fontSize = 12.sp, // Un tamaño un poco más pequeño para que parezca subtítulo
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
