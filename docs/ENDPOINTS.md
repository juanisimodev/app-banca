# Endpoints y Autorizacion

## Publicos

- `GET /login`
  - Vista de autenticacion.
- `POST /login`
  - Procesamiento de login por Spring Security.

## Autenticados

- `GET /dashboard`
  - Dashboard dinamico por rol.
- `POST /logout`
  - Cierre de sesion.

## USER

- `GET /transfer`
  - Formulario de transferencia.
- `POST /transfer`
  - Ejecuta transferencia entre cuentas.

## SYS_ADMIN

- `POST /admin/users`
  - Crea un usuario nuevo con cuentas iniciales.
  - Crea cuenta AHORRO con saldo base.
  - Crea cuenta CORRIENTE con saldo base.

## Reglas de respuesta destacadas

- Solicitud no autenticada a ruta protegida: redireccion a `/login`.
- Usuario sin permisos a `/admin/users`: `403 Forbidden`.
- Transferencia valida: redireccion a `/dashboard` con flash `success`.
- Transferencia invalida por negocio: redireccion a `/transfer` con flash `error`.
