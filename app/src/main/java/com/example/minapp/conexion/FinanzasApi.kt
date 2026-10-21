package com.example.minapp.conexion

import com.example.minapp.database.EntidadRegistros

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

data class LoginRequest(val nombreUsuario: String, val password: String)

data class UsuarioLoginResponse(
    val id: Long? = null,
    val nombreUsuario: String? = null
)

data class AuthResponse(
    @SerializedName("Usuario") val usuario: UsuarioLoginResponse? = null,
    val token: String? = null,
    val mensaje: String? = null,
    val statusCode: Int? = null
)

interface FinanzasApi{

    @POST("/login")
    suspend fun iniciarSesion(@Body request : LoginRequest) : AuthResponse
    
    /*Se trae la lista de registros que estén en la base de datos */
    @GET("/usuarios/{usuarioId}/transacciones")
    suspend fun importarRegistros(
        @Path("usuarioId") usuarioId : String,
        @Header("Authorization") token: String
    ) : List<EntidadRegistros>

    /*Aqí se envia la lista de los archivos que no han sido sincroziados en el servidor */
    @POST("/usuarios/{usuarioId}/transacciones")
    suspend fun exportarRegistros(
        @Path("usuarioId") usuarioId : String,
        @Header("Authorization") token: String,
        @Body registros : List<EntidadRegistros>
    ) : retrofit2.Response<Unit> /*Valida si el servidor respondio con OK */
}
