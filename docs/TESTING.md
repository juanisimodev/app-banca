# Estrategia de Testing

## Perfiles

- `default`: usa configuracion principal.
- `test`: usa `src/test/resources/application-test.properties` con H2 en memoria.

## Suites actuales

- `AppApplicationTests`
  - Verifica carga del contexto Spring.
- `AppIntegrationTests`
  - Login y proteccion de rutas.
  - Dashboard por rol (`USER`, `GERENTE`, `SYS_ADMIN`).
  - Transferencia exitosa.
  - Casos negativos de transferencia:
    - monto invalido,
    - fondos insuficientes,
    - cuenta destino inexistente.
  - Creacion de usuarios por admin.
  - Bloqueo de creacion para no admin.
  - Flash messages y contenido HTML clave.

## Ejecucion

En Git Bash:

```bash
./mvnw test
```

En CMD de Windows:

```cmd
mvnw.cmd test
```

## Cobertura funcional lograda

- Seguridad y autorizacion por rol.
- Integridad transaccional de transferencias.
- Renderizado MVC de vistas criticas.
- Persistencia de saldos y movimientos.

## Proximas pruebas recomendadas

- Integracion de logout y expiracion de sesion.
- Pruebas de concurrencia para transferencias simultaneas.
- Pruebas de regresion visual de templates.
