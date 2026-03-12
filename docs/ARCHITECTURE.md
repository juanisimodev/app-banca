# Arquitectura de App Banca

## Vision general

App Banca es una aplicacion web MVC construida sobre Spring Boot. Usa Thymeleaf para renderizado del lado servidor y Spring Security para autenticacion/autorizacion por rol.

## Capas del sistema

- `config`: configuracion de seguridad y carga de datos iniciales.
- `controller`: entrada web (rutas HTTP, renderizado de vistas, validaciones de formulario).
- `service`: logica de negocio y transacciones.
- `repository`: acceso a datos con Spring Data JPA.
- `domain`: entidades y enums del dominio bancario.
- `dto`: contratos de entrada/salida para formularios y vistas.
- `exception`: excepciones de negocio para transferencias.

## Flujo de autenticacion

1. Usuario abre `/login`.
2. Spring Security procesa credenciales con `usernameParameter=rut`.
3. `CustomUserDetailsService` carga usuario por RUT.
4. En login exitoso se redirige a `/dashboard`.

## Flujo de dashboard por rol

- `USER`:
  - Ve cuentas propias.
  - Ve ultimos movimientos.
  - Puede iniciar transferencia.
- `GERENTE`:
  - Ve KPIs operativos.
  - Ve lista de clientes.
  - Ve monitoreo de movimientos con bandera de riesgo.
- `SYS_ADMIN`:
  - Crea usuarios.
  - Ve resumen consolidado de usuarios y saldos.

## Flujo de transferencia

1. Cliente autenticado envia formulario en `/transfer`.
2. `TransferController` valida DTO.
3. `TransactionService` ejecuta `@Transactional`:
   - valida cuentas,
   - valida fondos,
   - debita origen,
   - acredita destino,
   - crea dos movimientos.
4. Si falla cualquier paso, se hace rollback.

## Consideraciones de seguridad

- `/admin/**` restringido a `SYS_ADMIN`.
- Recursos protegidos por sesion autenticada.
- Passwords almacenadas con BCrypt.
- Formularios POST protegidos por CSRF.

## Persistencia

- Produccion/desarrollo: PostgreSQL (Supabase).
- Tests: H2 en memoria (`application-test.properties`).
