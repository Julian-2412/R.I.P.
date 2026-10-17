package com.example.minapp.negocio 

import com.example.minapp.database.RegistroDao
import com.example.minapp.database.EntidadRegistros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



class FinanzasViewModel(private val dao : RegistroDao) : ViewModel(){
    /*Acá se trae todos los registros y se mantiene expectante de los cambios que se presenten
    en la base de datos */
    val listaTransacciones : StateFlow<List<EntidadRegistros>> = dao.obtenerTodosRegistros()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /*Acá mapea la lista se calcula el saldo total con ayuda de los montos */
    val saldoTotal : StateFlow<Double> = listaTransacciones.map{
        lista -> lista.sumOf{it.monto}
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    /*Acá obtengo solo los registros positivos */
    val ingresosTotales : StateFlow<Double> = listaTransacciones.map{
        lista -> lista
                    .filter{it.monto > 0}
                    .sumOf{it.monto}
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    /*Acá obtengo solo los registros negtivos */
    val egresosTotales : StateFlow<Double> = listaTransacciones.map{
        lista -> lista
                    .filter{it.monto < 0}
                    .sumOf{it.monto}
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

        /* Función para insertar una nueva transacción en la base de datos */
    fun insertarTransaccion(titulo: String, monto: Double, fecha : Long, descripcion: String) {
        viewModelScope.launch {
            // 1. Creamos el objeto de tipo EntidadRegistros con los datos que vienen de la pantalla
            val nuevoRegistro = EntidadRegistros(
                titulo = titulo,
                monto = monto,
                // Si la descripción viene vacía en el formulario, la guardamos como null
                descripcion = if (descripcion.isBlank()) null else descripcion,
                fecha = fecha, // Le asignamos la fecha y hora actuales en milisegundos
                ultimaModificacion = System.currentTimeMillis()
            )
            
            // 2. Le ordenamos al DAO que lo guarde de forma segura en segundo plano
            dao.guardarActualizar(nuevoRegistro)
        }
    }
}