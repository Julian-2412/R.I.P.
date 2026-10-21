package com.example.minapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database (entities = [EntidadRegistros::class], version = 4, exportSchema = true)
/*Acá ya es dónde se crea la base de datos como tal, usa la clase de EntidadRegistro 
como plantilla para crearla, pues allí es dónde se definen las tablas, valores, ect */
abstract class AppDatabase : RoomDatabase(){
    /*Acá es dónde "se le da permiso" a la appa para usar los comandos del DAO */
    abstract fun registroDao() : RegistroDao

    companion object {
        @Volatile
        private var INSTANCE : AppDatabase? = null 

        /*Acá se crea ya la base de datos como tal, se verifica su no exite antes */
        fun obtenerBaseDatos (context : Context) : AppDatabase {
            return INSTANCE ?: synchronized(this){
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java ,
                    "R.I.P._Finanzas_Database" /*Este es el nombre del archivo de la base de datos que aparecerá en el dispositivo */
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instancia 
                instancia
            }
        }
    }
}