# Gestión de Eventos Nueva EPS — prototipo MOCK

Dos proyectos independientes: `frontend/` (Angular 18 standalone) y `backend/` (Java 17 / Spring Boot 3.3). **No es una integración productiva con Portal**; los perfiles, el prestador, las notificaciones y las firmas son simulados. Los datos se almacenan en memoria y se reinician al reiniciar el backend.

## Ejecutar

1. Java 17 y Maven 3.9+: `cd backend && mvn spring-boot:run` (puerto 8080).
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
