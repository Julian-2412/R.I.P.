package com.example.minapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.OnConflictStrategy

@Dao
interface RegistroDao{
    /*Insertar un registro nuevo, si ya existe, es decir si tiene el mismo id, se reescribe */
    @Insert(onConflict  = OnConflictStrategy.REPLACE) /*La anotación de insert recibe un parámetro
                                                       para que haga un remplazo de toda la fila*/
    suspend fun guardarActualizar(registro : EntidadRegistros) 

    /*Acá se guarda la lista que viene desde el servidor */
    @Insert(onConflict  = OnConflictStrategy.REPLACE)
    suspend fun guardarListaImportada(registros : List<EntidadRegistros>)

    /*Acá es dónde busca los registros que el usuario ha modificado y no han sido enviado al servidor */
    @Query("SELECT * FROM transacciones WHERE estado = 'NUEVO' OR  estado = 'MODIFICADO' OR estado = 'ELIMINADO'")
    suspend fun obtenerRegistrosPendientes() : List<EntidadRegistros>

    /*Acá se actualiza el estado de los registros que se enviaron de cualquier otro valor a 
    "SINCRONIZADO" con el fín de que en una futura exportación al servidor no se vuelvan a enviar */
    @Query("UPDATE transacciones SET estado = 'SINCRONIZADO' WHERE id = :id")
    suspend fun marcarComoSincronizado(id : String)

    @Query("UPDATE transacciones SET estado = 'ELIMINADO' WHERE id = :id")
    suspend fun marcarComoEliminado(id: String)

    /*Trae todos los registros, glow es una conexión directa con la base de datos, ayuda a no
    estar preguntando todo el rato si ha sido actualizada sino que queda allí quieta
    esperando que un movimiento en la base, que es innmediatamente empujado */   
    @Query("SELECT * FROM transacciones WHERE estado != 'ELIMINADO' ORDER BY fecha DESC")
    fun obtenerTodosRegistros() : Flow<List<EntidadRegistros>>
}