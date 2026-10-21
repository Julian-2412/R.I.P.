package com.example.minapp.conexion

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ProveedorNetwork{
    /*Se define la ip para ir a buscar */
    private const val BASE_URL = "http://10.14.17.199:4000/"

    /*Con la instrucción "by lazy" espera hasta que reciba el evento que se dispara al preisonar
    el botón para fabricar a Retrofit  */
    val api : FinanzasApi by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) /*Aça se usa un traductor automático
                                                                para converttir objetos de kotlin a JSON  */
            .build()
            .create(FinanzasApi::class.java)
    }
}