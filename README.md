# 🚗 RUTEA - Plataforma de Carpooling El Salvador (API v1)

> **Plataforma inteligente de viajes compartidos (Carpooling) diseñada para conectar conductores y pasajeros, optimizando la movilidad urbana, reduciendo costos de transporte y disminuyendo el tráfico vehicular en El Salvador y Centroamérica.**

---

## 🌟 1. Visión del Proyecto

### El Problema
En El Salvador y la región centroamericana, miles de personas se desplazan diariamente por rutas neurálgicas (como la Carretera Panamericana, Santa Ana ⇄ San Salvador, San Miguel, Sonsonate, Santa Tecla, etc.). Este flujo diario genera:
- Congestionamiento vehicular excesivo y tiempos muertos en carretera.
- Altos costos individuales de combustible y mantenimiento.
- Deficiencias en el transporte público tradicional (incomodidad, horarios rígidos, problemas de cobertura).
- Huella de carbono innecesaria provocada por miles de autos circulando con un solo ocupante.

### La Solución y Visión: Rutea
**Rutea** transforma la movilidad diaria mediante un modelo colaborativo, eficiente, seguro y accesible:
- **Optimización de cupos**: Permitir que conductores que ya realizan una ruta diaria moneticen sus asientos vacíos compartiendo gastos.
- **Viajes económicos y cómodos**: Brindar a estudiantes y trabajadores una alternativa directa, económica y segura.
- **Geolocalización precisa**: Aprovechar capacidades geoespaciales avanzadas (**PostGIS**) para definir orígenes, destinos y puntos de encuentro intermedios reales en la ruta con radio de proximidad geodésico (`ST_DWithin`).
- **Seguridad y confianza comunitaria**: Construir una red confiable basada en perfiles verificados, calificaciones bidireccionales post-viaje y comunicación directa en tiempo real mediante WebSockets.

---
[![imagen.png](https://i.postimg.cc/SKjdnySc/imagen.png)](https://postimg.cc/PLsYcn8q)
[![imagen.png](https://i.postimg.cc/SNqGWLSv/imagen.png)](https://postimg.cc/XXHFWCyf)
[![imagen.png](https://i.postimg.cc/W4Dm2X8N/imagen.png)](https://postimg.cc/4KgHPvjq)
[![imagen.png](https://i.postimg.cc/7Z9tCsSW/imagen.png)](https://postimg.cc/nXsGg1R4)
[![imagen.png](https://i.postimg.cc/VLcKKQR7/imagen.png)](https://postimg.cc/YjbNSPTg)
## 🏗️ 2. Arquitectura de Software

El backend está construido bajo el paradigma de **Monolito Modular** combinando **Arquitectura Hexagonal (Puertos y Adaptadores)** y **Vertical Slice Architecture**:

```
src/main/java/com/carpooling/sv/v1/
├── auth/                                # Slice: Autenticación, JWT y Sesión
├── usuario/                             # Slice: Gestión de Usuarios, Perfil /me y Roles
├── vehiculo/                            # Slice: Vehículos, Placas únicas y Baja Lógica
├── ruta/                                # Slice: Catálogo maestro de rutas interurbanas
├── viaje/                               # Slice: Publicación, Ciclo de vida y Búsqueda PostGIS
├── solicitud/                           # Slice: Reserva atómica de cupos y cancelaciones
├── mensajeria/                          # Slice: Chat grupal por viaje y WebSockets STOMP
├── calificacion/                        # Slice: Reseñas mutuas y reputación pública
└── shared/                              # Componentes transversales
    ├── config/                          # Swagger OpenAPI, Jackson JSR-310
    ├── exception/                       # Manejador Global (RFC 7807) y Excepciones
    ├── security/                        # Filtro JWT, Cookies HttpOnly, SecurityContextHelper
    └── websocket/                       # WebSocketConfig, STOMP y Auth Interceptor
```

---

## 💻 3. Stack Tecnológico

| Capa / Herramienta | Tecnología | Detalle |
| :--- | :--- | :--- |
| **Lenguaje** | **Java 25** | Uso intensivo de records inmutables, pattern matching y Virtual Threads |
| **Framework** | **Spring Boot 4.1.1** | Núcleo del API REST, inyección de dependencias y configuración reactiva |
| **Seguridad** | **Spring Security + JWT** | Autenticación híbrida: Cookies `HttpOnly` (Web) y Bearer Header (Móvil) |
| **Base de Datos** | **PostgreSQL 18 + PostGIS 3.6** | Persistencia relacional con índices espaciales GIST y funciones geoespaciales |
| **ORM / Migraciones** | **Spring Data JPA + Flyway** | Hibernate 7, repositorios desacoplados y control de versiones del esquema |
| **Tiempo Real** | **Spring WebSocket + STOMP** | Broker de mensajería en tiempo real con autenticación JWT en frames `CONNECT` |
| **Documentación** | **SpringDoc OpenAPI 3 (Swagger)** | Documentación interactiva en `/swagger-ui.html` |
| **Infraestructura Local** | **Docker Compose** | Contenedor de PostgreSQL 18 con PostGIS preconfigurado |

---

## 📊 4. Módulos y Funcionalidades del Sistema

### 1. Autenticación y Usuarios (`auth` & `usuario`)
- Autenticación híbrida: Cookie `HttpOnly` (`access_token`) y Header `Authorization: Bearer`.
- Endpoints de conveniencia: `GET /api/v1/usuarios/me` y `PUT /api/v1/usuarios/me` para evitar IDOR.
- Gestión de roles (`ADMIN`, `CONDUCTOR`, `CLIENTE`, `USUARIO`) y estados de cuenta.

### 2. Vehículos (`vehiculo`)
- Registro de vehículos con validación de placa única en el sistema.
- **Baja lógica (`activo = false`):** Los autos no se eliminan físicamente para mantener la integridad de viajes y calificaciones pasadas.
- **Protección Anti-IDOR:** Solo el propietario del vehículo o administradores pueden modificarlo o darlo de baja.

### 3. Rutas Predefinidas (`ruta`)
- Catálogo de trayectos interurbanos frecuentes en El Salvador.
- Consulta pública para visitantes (`GET /api/v1/rutas`).
- Mantenimiento y activación restringido a administradores (`ROLE_ADMIN`).

### 4. Viajes y Geolocalización PostGIS (`viaje`)
- **Publicación:** Validación cruzada mediante `VehiculoVerificationPort` para asegurar que el auto pertenece al conductor y está activo.
- **Búsqueda Geoespacial en Radio:** Filtra viajes activos evaluando coordenadas de origen y destino en metros mediante `ST_DWithin` geodésico sobre `geometry(Point, 4326)`.
- **Paradas Intermedias:** Registro de puntos de encuentro intermedios en la ruta.
- **Ciclo de Vida:** Control de estados `PUBLICADO` ➔ `COMPLETO` ➔ `EN_CURSO` ➔ `FINALIZADO` (o `CANCELADO`).

### 5. Solicitudes y Reservas de Cupos (`solicitud`)
- Solicitud de cupos (rechaza solicitudes a su propio viaje o duplicadas).
- **Descuento Atómico de Cupos:** Al aceptar la solicitud, los cupos del viaje se decrementan de manera atómica dentro de una transacción.
- **Restauración Automática de Cupos:** Al cancelar una solicitud aceptada, los cupos se reintegran al viaje automáticamente.

### 6. Chat Grupal en Tiempo Real (`mensajeria` & `websocket`)
- Sala de chat exclusiva por viaje. Solo pueden ingresar el conductor y pasajeros aceptados.
- **WebSockets + STOMP:**
  - Endpoint de conexión: `ws://localhost:8080/ws`
  - Canal de suscripción en tiempo real: `/topic/conversaciones/{conversacionId}`
  - Envío por socket: `/app/conversaciones/{conversacionId}/enviar`
  - Envío por REST: `POST /api/v1/conversaciones/{id}/mensajes` (también transmite al socket en vivo).
- Autenticación JWT en el frame `CONNECT` de STOMP mediante `WebSocketAuthInterceptor`.

### 7. Calificaciones y Reputación (`calificacion`)
- Reseñas mutuas obligatoriamente vinculadas a un viaje `FINALIZADO` o `EN_CURSO`.
- Validación de participación mutua: solo usuarios que compartieron el viaje pueden calificarse.
- Protección contra autocalificación y calificaciones duplicadas.
- **Consulta Pública de Reputación (`GET /api/v1/calificaciones/usuario/{id}`):** Promedio de estrellas (1 a 5), total de opiniones y reseñas para consulta abierta de confiabilidad.

---

## 🛡️ 5. Matriz de Endpoints y Seguridad

| Método | Endpoint | Rol Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Público | Inicio de sesión (retorna Cookie y JSON) |
| `POST` | `/api/v1/auth/register` | Público | Registro de usuarios |
| `POST` | `/api/v1/auth/logout` | Público | Cierre de sesión e invalidación de Cookie |
| `GET` | `/api/v1/usuarios/me` | Autenticado | Perfil propio |
| `PUT` | `/api/v1/usuarios/me` | Autenticado | Actualización de perfil propio |
| `GET` | `/api/v1/rutas` | Público | Catálogo de rutas frecuentes |
| `POST` | `/api/v1/rutas` | `ROLE_ADMIN` | Crear nueva ruta maestra |
| `POST` | `/api/v1/vehiculos` | `CONDUCTOR` | Registrar vehículo |
| `GET` | `/api/v1/vehiculos/mis-vehiculos` | Autenticado | Listar mis vehículos |
| `DELETE`| `/api/v1/vehiculos/{id}` | Conductor Propietario | Baja lógica del vehículo |
| `GET` | `/api/v1/viajes/buscar` | Público | Búsqueda geoespacial PostGIS |
| `GET` | `/api/v1/viajes/{id}` | Público | Detalle público del viaje |
| `POST` | `/api/v1/viajes` | `CONDUCTOR` | Publicar nuevo viaje |
| `PATCH` | `/api/v1/viajes/{id}/estado` | Conductor del Viaje | Cambiar estado (EN_CURSO, FINALIZADO) |
| `POST` | `/api/v1/viajes/{id}/puntos` | Conductor del Viaje | Añadir punto de encuentro |
| `POST` | `/api/v1/solicitudes-viaje` | Autenticado | Solicitar reserva de cupo |
| `GET` | `/api/v1/solicitudes-viaje/mis-solicitudes` | Autenticado | Ver mis solicitudes enviadas |
| `GET` | `/api/v1/solicitudes-viaje/viaje/{id}` | Conductor del Viaje | Ver solicitudes recibidas |
| `PATCH` | `/api/v1/solicitudes-viaje/{id}/responder` | Conductor del Viaje | Aceptar o rechazar reserva |
| `PUT` | `/api/v1/solicitudes-viaje/{id}/cancelar` | Pasajero / ADMIN | Cancelar reserva (restaura cupos) |
| `GET` | `/api/v1/conversaciones/viaje/{id}` | Conductor/Pasajero | Abrir chat del viaje |
| `GET` | `/api/v1/conversaciones/{id}/mensajes` | Participante | Historial de mensajes |
| `POST` | `/api/v1/conversaciones/{id}/mensajes` | Participante | Enviar mensaje (difunde en vivo) |
| `POST` | `/api/v1/calificaciones` | Participante del Viaje| Calificar tras viaje finalizado |
| `GET` | `/api/v1/calificaciones/usuario/{id}` | Público | Reputación pública y promedio de estrellas |
| `WS` | `/ws` | Handshake Público | Conexión WebSocket / STOMP (JWT en CONNECT) |

---

## 🔌 6. Ejemplo de Integración WebSocket (JavaScript)

```javascript
import { Client } from '@stomp/stompjs';

// 1. Inicializar cliente STOMP
const stompClient = new Client({
  brokerURL: 'ws://localhost:8080/ws',
  connectHeaders: {
    Authorization: 'Bearer ' + jwtAccessToken
  },
  reconnectDelay: 5000
});

// 2. Al conectar, suscribirse al canal del viaje
stompClient.onConnect = frame => {
  console.log('Conectado a STOMP:', frame);

  stompClient.subscribe('/topic/conversaciones/' + conversacionId, message => {
    const payload = JSON.parse(message.body);
    console.log('Mensaje recibido en vivo:', payload);
  });
};

stompClient.activate();

// 3. Enviar mensaje por WebSocket
function enviarMensaje(texto) {
  stompClient.publish({
    destination: '/app/conversaciones/' + conversacionId + '/enviar',
    body: JSON.stringify({ contenido: texto })
  });
}
```

---

## 🚀 7. Guía de Inicio Rápido

### Requisitos Previos
- **JDK 25** instalado.
- **Docker** y **Docker Compose** en ejecución.

### 1. Iniciar la Base de Datos PostGIS
```bash
docker compose up -d
```

### 2. Ejecutar la Aplicación
Flyway aplicará automáticamente las migraciones del esquema al iniciar:
```bash
./gradlew bootRun
```

### 3. Ejecutar la Suite de Pruebas Automatizadas (62 Tests)
```bash
./gradlew test
```

### 4. Swagger UI Interactiva
```
http://localhost:8080/swagger-ui.html
```

### Credenciales de Administrador por Defecto:
- **Correo**: `admin@admin.com`
- **Contraseña**: `admin`

---

## 🔒 8. Licencia
Este proyecto y su código fuente son de carácter **privado, propietario y confidencial**. Todos los derechos reservados.
Consulta el archivo [LICENSE](file:///home/alex/apis/v1/LICENSE) para más detalles.
