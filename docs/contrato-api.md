# Contrato HTTP para la app Android

La app Android usa JSON sobre HTTPS y espera que el backend implemente las rutas descritas aquí. Para desarrollo en el emulador, la URL predeterminada es `http://10.0.2.2:8080/`; se puede cambiar con `-PapiBaseUrl=https://<host>/`.

## Inicio de sesión

`POST /api/auth/login`

Solicitud:

```json
{
  "usuario": "julian",
  "contrasena": "..."
}
```

Respuesta `200`:

```json
{
  "token": "<token-de-acceso>",
  "usuario": "julian",
  "nombre": "Julian",
  "correo": "julian@example.com"
}
```

`usuario` debe ser un identificador estable de la cuenta. Las respuestas `400` o `401` indican credenciales inválidas. La app conserva el token solo en memoria y no almacena la contraseña.

## Descargar transacciones

`GET /api/transacciones`

Requiere `Authorization: Bearer <token>`. Respuesta `200`: una lista JSON con todas las transacciones de esa cuenta.

```json
[
  {
    "clientId": "b5d6a32a-272d-4af2-aa42-1648ce06bb6e",
    "tipo": "GASTO",
    "titulo": "Almuerzo",
    "descripcion": null,
    "montoCentavos": 1800000,
    "fecha": 1771000000000,
    "updatedAt": 1771000000000
  }
]
```

`tipo` solo puede ser `INGRESO` o `GASTO`; `montoCentavos` debe ser un entero positivo. `fecha` y `updatedAt` son milisegundos desde Unix epoch.

## Subir transacciones pendientes

`POST /api/transacciones/sincronizar`

Requiere `Authorization: Bearer <token>`.

Solicitud:

```json
{
  "transacciones": [
    {
      "clientId": "b5d6a32a-272d-4af2-aa42-1648ce06bb6e",
      "tipo": "GASTO",
      "titulo": "Almuerzo",
      "descripcion": null,
      "montoCentavos": 1800000,
      "fecha": 1771000000000,
      "updatedAt": 1771000000000
    }
  ]
}
```

Respuesta `200`:

```json
{
  "aceptados": ["b5d6a32a-272d-4af2-aa42-1648ce06bb6e"]
}
```

El backend debe realizar un *upsert* idempotente con clave única `(usuario autenticado, clientId)`. Si recibe otra vez un `clientId` existente, debe actualizar ese registro con la versión enviada, no crear un duplicado. Debe confirmar en `aceptados` únicamente los identificadores efectivamente guardados. La app solo marca como sincronizada la versión local que envió; si se editó mientras la petición estaba en curso, permanece pendiente.

## Reglas de combinación

- Cada registro nuevo recibe un UUID `clientId` local permanente; el ID autoincremental de Room no se envía al servidor.
- Al descargar, un registro remoto que no existe localmente se inserta como sincronizado.
- Si ya existe el mismo `clientId`, se conserva la versión local. Si no coincide con la remota, se marca local como pendiente para que una subida posterior aplique la política de conservar lo local.
- Los registros locales sin cuenta asignada se asocian a la primera cuenta que inicia sesión. Los registros asociados a otra cuenta no se envían a la cuenta actual.
- La app no elimina registros durante la importación ni implementa sincronización de borrados.

Las llamadas no deben registrar contraseñas ni tokens en logs. El permiso HTTP sin cifrar está habilitado solo para el manifiesto de compilación `debug`; una compilación `release` debe configurarse con una URL HTTPS.
