package com.example.minapp.negocio 

import com.example.minapp.database.RegistroDao
import com.example.minapp.database.EntidadRegistros
import com.example.minapp.conexion.ProveedorNetwork
import com.example.minapp.conexion.LoginRequest 

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.io.IOException

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

    /*Variable para saber si el usuario se logeo o no */
    var usuarioLogeado by mutableStateOf(false)
        private set /*Esta instrucción sirve para evitar que la variable sea modificada fuera de una función */
    
    private var jwToken : String? = null /*Acá se almacena el token proviniente del servidor */
    
    var mensajError by mutableStateOf<String?>(null)

    var cargandoDatos  by mutableStateOf(false)

    fun inicioSesionExitoso(){
        usuarioLogeado = true
    }

    fun cerrarSesion(){
        jwToken = null
        usuarioLogeado = false
    }

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

    fun editarTransaccion(
        id: String,
        titulo: String,
        monto: Double,
        fecha: Long,
        descripcion: String
    ) {
        val registroActual = listaTransacciones.value
            .firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("No se encontró el registro con id $id")

        viewModelScope.launch {
            dao.guardarActualizar(
                registroActual.copy(
                    titulo = titulo,
                    monto = monto,
                    fecha = fecha,
                    descripcion = descripcion.ifBlank { null },
                    estado = if (registroActual.estado == "SINCRONIZADO") {
                        "MODIFICADO"
                    } else {
                        registroActual.estado
                    },
                    ultimaModificacion = System.currentTimeMillis()
                )
            )
        }
    }

    fun ejecutaLogin(usuario : String, contrasena : String){
        viewModelScope.launch{
            try{
                mensajError = null /*Para limpiar mensajes viejos */

                /*Se envia el JSON al servidor */
                val respuesta = ProveedorNetwork.api.iniciarSesion(LoginRequest(usuario, contrasena))

                if(respuesta.exito && respuesta.token.isNotBlank()){
                    /*Guardamos el token */
                    jwToken = respuesta.token

                    usuarioLogeado = true
                }else{
                    mensajError = "Credenciales erroneas"
                }
            }catch(e : IOException){
                mensajError = "No se pudo conectar al servidor"
            }catch(e : Exception){
                mensajError = "Ocurrió un error"
            }
        }
    }

    fun importarDatos(){
        val tokenActual = jwToken
        if(tokenActual == null){
            mensajError = "Sesión finalizada, inicie sesión de nuevo"
            return
        }
         
        viewModelScope.launch{
            try{
                /*Se encvia el token, con el formato estandar "Bearer" anteponiendolo */
                val datosNube = ProveedorNetwork.api.importarRegistros(token = "Bearer ${tokenActual}")

                /*Luego se inyectan en la base de datos local */
                dao.guardarListaImportada(datosNube)
            }catch(e : Exception){
                mensajError = "Error al importar lo registros${e.localizedMessage}"
            }
        }
    }

    fun exportarDatos(){
        val tokenActual = jwToken
        if(tokenActual == null){
            mensajError = "Sesión finalizada, inicie sesión de nuevo"
            return
        }
         viewModelScope.launch{
            try{
                cargandoDatos = true
                mensajError = null

                val registroPendientesLocal = dao.obtenerRegistrosPendientes()

                if(registroPendientesLocal.isEmpty()){
                    mensajError = "No hay registros nuevos o modificados para sincronizar."
                    return@launch
                }

                val listaPendientes = registroPendientesLocal.map{ entidad ->
                    EntidadRegistros(
                        id = entidad.id,
                        titulo = entidad.titulo,
                        monto = entidad.monto,
                        descripcion = entidad.descripcion,
                        fecha = entidad.fecha,
                        estado = entidad.estado,
                        ultimaModificacion = entidad.ultimaModificacion
                    )
                }

                val respuestaServidor = ProveedorNetwork.api.exportarRegistros(
                    token = "Bearer $tokenActual",
                    registros = listaPendientes 
                )

                if(respuestaServidor.isSuccessful){
                    registroPendientesLocal.forEach {registroEnviado ->
                        if(registroEnviado.estado == "ELIMINADO"){
                        }else{
                            dao.marcarComoSincronizado(registroEnviado.id)
                        }
                    }
                mensajError = "¡Sincronización exitosa! Datos respaldados en la nube."
                }else{
                    mensajError = "El servidor rechazó los datos: Código ${respuestaServidor.code()}"
                }
            }catch(e : Exception){
                mensajError = "Error al importar lo registros${e.localizedMessage}"
            }
        }
    }
}