package com.example.minapp.cajas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight


@Composable 
fun SaldoCaja(saldo : Double, ingresos : Double, egresos : Double,  modifier : Modifier = Modifier) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Saldo disponible", 
                style = MaterialTheme.typography.titleMedium)

            Text(
                text = "${String.format("%.2f", saldo)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold 
            )
            
            Column (modifier = Modifier.padding(top = 8.dp)){
                Text(
                    text = "Ingresos: $${String.format("%.2f", ingresos)}",
                    color = MaterialTheme.colorScheme.primary // Puedes usar los colores del tema
                )
                Text(
                    text = "Gastos: $${String.format("%.2f", egresos)}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
