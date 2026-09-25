# Gestión de Eventos Nueva EPS — prototipo MOCK

Dos proyectos independientes: `frontend/` (Angular 18 standalone) y `backend/` (Java 21 / Spring Boot 3.3). **No es una integración productiva con Portal**; los perfiles, el prestador, las notificaciones y las firmas son simulados. Los datos se almacenan en memoria y se reinician al reiniciar el backend.

## Ejecutar

1. Java 21 y Gradle 8.5 o superior: `cd backend && gradle bootRun` (puerto 8080).
2. Node 20 LTS y npm: `cd frontend && npm install && npm start` (http://localhost:4200).
3. Cambia el perfil en la esquina superior derecha para probar Prestador, Colaborador o Administrador.
4. Para inspeccionar la API: `GET http://localhost:8080/api/events`.

## Funcionalidades mock incluidas
- Catálogo y detalle de eventos, administrador crea eventos con franjas >=30 minutos.
- Inscripción por código de habilitación, hasta 5 personas por solicitud, cargos parametrizados, validación básica.
- Listado de inscritos por prestador, hasta 50 compromisos por prestador y evento, cierre simulado.
- Carga de nombre de archivo PDF de acta (no persiste bytes), estado de firma pendiente.
- Notificación de inscripción y acta por consola; recordatorios bajo demanda con `POST /api/mock/reminders`.

## Pendiente para integración real
- OIDC/SSO Portal, intercambio seguro de sesión, cookie por aplicación, refresh, logout aislado y protección CSRF.
- Autorización en backend con JWT y permisos reales; validar relación usuario-prestador, IPS/operador y asignación de colaborador (selector de perfil actual solo demo).
- Persistencia PostgreSQL, migraciones, control transaccional de cupos, concurrencia e idempotencia.
- Servicio de correo real y job diario, plantillas, reintentos, auditoría y consentimientos.
- Almacenamiento seguro del acta, antivirus, trazabilidad y firma electrónica real del prestador.
- Validar agenda: no solapamientos, disponibilidad y franjas generadas automáticamente.
- Controlar vigencia y transición de estados antes de inscribir y antes de crear compromisos.
- Registrar asistencia real y vista de seguimiento completa; roles y pruebas automatizadas.

**Seguridad:** los endpoints son públicos exclusivamente para desarrollo local. No publicar este mock en Internet ni usar datos personales reales.

## Ampliación de flujos mock (septiembre 2026)

- Se añadieron las vistas de asignación de prestadores, control de asistencia, estado del evento, consulta y simulación de firma del acta, y bitácora mock de notificaciones.
- API adicional bajo `/api/workflow` con prestadores IPS y operador farmacéutico de demostración, asignaciones, asistencia, actas y estados. Los datos se reinician al reiniciar el servidor.
- **Importante:** estas funciones siguen siendo prototipo: no existe autenticación, verificación real de permisos, persistencia, firma válida ni correo programado. Las nuevas pantallas amplían el mock anterior; aún se requieren componentes Angular separados, pruebas de integración, Gradle Wrapper y validaciones de negocio adicionales antes de considerar la historia completa.

## Rama de ampliación funcional

Rama: `feature/gestion-eventos-mock-completo`. Ejecuta `git checkout feature/gestion-eventos-mock-completo` tras clonar, o descarga el ZIP de esa rama desde GitHub.

### Pantallas y flujos
- Prestador: catálogo, detalle, inscripción con componente reutilizable validado, agenda, confirmación, consulta de inscripciones, compromisos y actas, firma simulada.
- Colaborador: prestadores asignados, asistencia, compromisos y carga de acta (solo simulación).
- Administrador: creación de eventos, franjas, responsables, estado, asignación de prestadores y consulta de notificaciones mock.
- Backend: restricciones básicas de inscripción duplicada, fechas, franjas, límite de compromisos, finalización y validación elemental de PDF; outbox de correos simulados y tarea diaria de recordatorios.

### Ejecutar
Backend: Java 21, Gradle 8.5+: `cd backend && gradle bootRun`. Frontend: Node 20: `cd frontend && npm install && npm start`. Pruebas: `cd backend && gradle test`, `cd frontend && npm run build`. CI definida en `.github/workflows/mock-ci.yml`.

### Limitaciones importantes
Esta rama es una **demo mock ampliada**, no una implementación completa de producción. La selección de perfil no autentica; las asignaciones no protegen los endpoints; los datos están en memoria; los correos van a un outbox, no se envían; la firma no es jurídica; los PDFs se guardan temporalmente y no existe un repositorio documental. Aún faltan integración SSO/Portal, seguridad backend, persistencia, colas/reintentos de correo, gestión transaccional de cupos, trazabilidad, pruebas end-to-end y Gradle Wrapper. No uses información personal real ni despliegues los endpoints mock públicamente.
