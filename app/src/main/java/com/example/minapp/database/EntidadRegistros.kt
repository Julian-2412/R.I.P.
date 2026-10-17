package com.example.minapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transacciones")
data class EntidadRegistros(
    @PrimaryKey
    val id : String = UUID.randomUUID().toString(), /*Con UUID se gemera un id global único 
                                                    en formato de texto para poder sinronizar
                                                    sin problema con el servidor de SpingBoot*/
    val titulo : String,

    val monto : Double,

    val descripcion : String? = null, 

    val fecha : Long,

    val estado : String = "NUEVO", /*Acá va a revibir valores como "NUEVO" para indicar que
                                    que no se ha enviado al servidor; "MODIFICADO" para indicar
                                    que fue editado en la aplicación pero no se ha enviado;
                                    "SINCRONIZADO" para indicar que ya está en el servidor y
                                    "ELIMINADO" para decir que fue eliminado en la App y hay 
                                    que hacer lo mismo en el servidor*/
    val ultimaModificacion : Long = System.currentTimeMillis()                                    
)