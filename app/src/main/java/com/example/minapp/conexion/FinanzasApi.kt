package com.example.minapp.conexion

import com.example.minapp.database.EntidadRegistros

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header

data class LoginRequest(val usuario : String, val contraseña : String)

data class AuthResponse(val token : String, val exito : Boolean)

interface FinanzasApi{

    @POST("api/auth/login")
    suspend fun iniciarSesion(@Body request : LoginRequest) : AuthResponse
    
    /*Se trae la lista de registros que estén en la base de datos */
    @GET("api/transacciones/respaldo")
    suspend fun importarRegistros(
        @Header("Authorization") token : String 
    ) : List<EntidadRegistros>

    /*Aqí se envia la lista de los archivos que no han sido sincroziados en el servidor */
    @POST("api/transacciones/sincronizar")
    suspend fun exportarRegistros(
        @Header("Authorization") token : String,
        @Body registros : List<EntidadRegistros>
    ) : retrofit2.Response<Unit> /*Valida si el servidor respondio con OK */
}