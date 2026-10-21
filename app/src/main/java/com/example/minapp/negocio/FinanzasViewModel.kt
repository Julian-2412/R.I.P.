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
import retrofit2.HttpException

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
    
    private var usuarioIdActual: String? = null
    
    var mensajError by mutableStateOf<String?>(null)

    var cargandoDatos  by mutableStateOf(false)

    private var jwtToken: String? = null

    fun inicioSesionExitoso(){
        usuarioLogeado = true
    }

    fun cerrarSesion(){
        usuarioIdActual = null
        jwtToken = null
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

    fun eliminarTransaccion(id: String) {
        viewModelScope.launch {
            dao.marcarComoEliminado(id)
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
                usuarioIdActual = null
                jwtToken = null
                usuarioLogeado = false
                /*Se envia el JSON al servidor */

                val respuesta = ProveedorNetwork.api.iniciarSesion(
                    LoginRequest(nombreUsuario = usuario, password = contrasena)
                )

                val token = respuesta.token?.trim()?.takeIf { it.isNotEmpty() }
                val usuarioRespuesta = respuesta.usuario

                if(usuarioRespuesta?.id != null && token != null){
                    usuarioIdActual = usuarioRespuesta.id.toString()
                    jwtToken = token
                    usuarioLogeado = true
                }else{
                    mensajError = respuesta.mensaje
                        ?: "El backend no devolvió el usuario y el JWT esperados."
                }
            }catch(e : HttpException){
                mensajError = when(e.code()){
                    401 -> "Usuario o contraseña incorrectos (HTTP 401)."
                    403 -> "El servidor prohibió el inicio de sesión (HTTP 403). Revisa su configuración de seguridad."
                    404 -> "Usuario o contraseña incorrectos."
                    else -> "El servidor respondió con el error HTTP ${e.code()}."
                }
            }catch(e : IOException){
                mensajError = "No se pudo conectar al servidor"
            }catch(e : Exception){
                mensajError = "Error al procesar la respuesta: ${e.localizedMessage ?: e.javaClass.simpleName}"
            }
        }
    }

    fun importarDatos(){
        val usuarioId = usuarioIdActual
        if(usuarioId == null){
            mensajError = "No se encontró el ID del usuario. Inicie sesión de nuevo."
            return
        }

        val tokenAuth = jwtToken
            ?.takeIf { it.isNotBlank() }
            ?.let { "Bearer $it" }
         
        if(tokenAuth == null){
            mensajError = "Inicie sesión de nuevo para obtener un token."
            return
        }

        viewModelScope.launch{
            try{
                /*Se encvia el token, con el formato estandar "Bearer" anteponiendolo */
                val datosNube = ProveedorNetwork.api.importarRegistros(
                    usuarioId = usuarioId,
                    token = tokenAuth
                )

                /*Luego se inyectan en la base de datos local */
                dao.guardarListaImportada(datosNube)
            }catch(e : HttpException){
                mensajError = "El servidor rechazó la importación (HTTP ${e.code()})."
            }catch(e : IOException){
                mensajError = "No se pudo conectar al servidor para importar los registros."
            }catch(e : Exception){
                mensajError = "Error al importar los registros: ${e.localizedMessage ?: e.javaClass.simpleName}"
            }
        }
    }

    fun exportarDatos(){
        val usuarioId = usuarioIdActual
        if(usuarioId == null){
            mensajError = "No se encontró el ID del usuario. Inicie sesión de nuevo."
            return
        }
        val tokenAuth = jwtToken
            ?.takeIf { it.isNotBlank() }
            ?.let { "Bearer $it" }
         
        if(tokenAuth == null){
            mensajError = "Inicie sesión de nuevo para obtener un token."
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
                    usuarioId = usuarioId,
                    token = tokenAuth,
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
            }catch(e : HttpException){
                mensajError = "El servidor rechazó la exportación (HTTP ${e.code()})."
            }catch(e : IOException){
                mensajError = "No se pudo conectar al servidor para exportar los registros."
            }catch(e : Exception){
                mensajError = "Error al exportar los registros: ${e.localizedMessage ?: e.javaClass.simpleName}"
            }finally{
                cargandoDatos = false
            }
        }
    }
}
