-- ============================================================
-- RUTEA
-- PostgreSQL + PostGIS
-- ============================================================

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ============================================================
-- ENUMS
-- ============================================================

CREATE TYPE estado_usuario AS ENUM (
    'ACTIVO',
    'SUSPENDIDO',
    'INACTIVO'
);

CREATE TYPE estado_viaje AS ENUM (
    'PUBLICADO',
    'COMPLETO',
    'EN_CURSO',
    'FINALIZADO',
    'CANCELADO'
);

CREATE TYPE estado_solicitud AS ENUM (
    'PENDIENTE',
    'ACEPTADA',
    'RECHAZADA',
    'CANCELADA'
);


-- ============================================================
-- USUARIOS
-- ============================================================

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),

    correo VARCHAR(150) NOT NULL,
    telefono VARCHAR(20),

    password_hash TEXT NOT NULL,

    foto_url TEXT,

    verificado BOOLEAN NOT NULL DEFAULT FALSE,

    estado estado_usuario NOT NULL DEFAULT 'ACTIVO',

    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_usuario_correo
        UNIQUE (correo),

    CONSTRAINT uk_usuario_telefono
        UNIQUE (telefono)
);

CREATE TABLE usuario_roles (
    usuario_id UUID NOT NULL,
    rol VARCHAR(50) NOT NULL,
    PRIMARY KEY (usuario_id, rol),
    CONSTRAINT fk_usuario_roles_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_usuario_roles_usuario ON usuario_roles(usuario_id);


-- ============================================================
-- VEHICULOS
-- ============================================================

CREATE TABLE vehiculos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    propietario_id UUID NOT NULL,

    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,

    anio SMALLINT,

    color VARCHAR(30),

    tipo VARCHAR(30),

    placa VARCHAR(20),

    capacidad_pasajeros SMALLINT NOT NULL DEFAULT 4,

    foto_url TEXT,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_vehiculo_propietario
        FOREIGN KEY (propietario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_vehiculo_placa
        UNIQUE (placa),

    CONSTRAINT chk_vehiculo_anio
        CHECK (
            anio IS NULL
            OR anio BETWEEN 1900 AND 2100
        ),

    CONSTRAINT chk_vehiculo_capacidad
        CHECK (capacidad_pasajeros BETWEEN 1 AND 50)
);


-- ============================================================
-- RUTAS
-- ============================================================
-- Representa una ruta general.
--
-- Ejemplo:
-- "Carretera Panamericana"
--
-- No representa un viaje concreto.
-- Una misma ruta puede tener muchos viajes.
-- ============================================================

CREATE TABLE rutas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    nombre VARCHAR(150) NOT NULL,

    descripcion TEXT,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- ============================================================
-- VIAJES
-- ============================================================
-- Representa el viaje concreto publicado por un conductor.
--
-- Ejemplo:
--
-- Santa Ana -> San Salvador
-- 25/09/2026
-- 06:30 AM
-- 3 cupos
-- $3.00
--
-- origen_geom y destino_geom son coordenadas geográficas.
-- ============================================================

CREATE TABLE viajes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    conductor_id UUID NOT NULL,

    vehiculo_id UUID NOT NULL,

    ruta_id UUID,

    -- -----------------------------------------
    -- ORIGEN
    -- -----------------------------------------

    origen_nombre VARCHAR(150) NOT NULL,

    origen_geom GEOGRAPHY(POINT, 4326) NOT NULL,

    -- -----------------------------------------
    -- DESTINO
    -- -----------------------------------------

    destino_nombre VARCHAR(150) NOT NULL,

    destino_geom GEOGRAPHY(POINT, 4326) NOT NULL,

    -- -----------------------------------------
    -- HORARIO
    -- -----------------------------------------

    fecha_salida DATE NOT NULL,

    hora_salida TIME NOT NULL,

    -- -----------------------------------------
    -- CUPOS
    -- -----------------------------------------

    cupos_totales SMALLINT NOT NULL,

    cupos_disponibles SMALLINT NOT NULL,

    -- -----------------------------------------
    -- COSTO
    -- -----------------------------------------

    tarifa_aproximada NUMERIC(8,2),

    -- -----------------------------------------
    -- INFORMACION
    -- -----------------------------------------

    descripcion TEXT,

    estado estado_viaje NOT NULL DEFAULT 'PUBLICADO',

    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- -----------------------------------------
    -- FOREIGN KEYS
    -- -----------------------------------------

    CONSTRAINT fk_viaje_conductor
        FOREIGN KEY (conductor_id)
        REFERENCES usuarios(id),

    CONSTRAINT fk_viaje_vehiculo
        FOREIGN KEY (vehiculo_id)
        REFERENCES vehiculos(id),

    CONSTRAINT fk_viaje_ruta
        FOREIGN KEY (ruta_id)
        REFERENCES rutas(id)
        ON DELETE SET NULL,

    -- -----------------------------------------
    -- CHECKS
    -- -----------------------------------------

    CONSTRAINT chk_viaje_cupos_totales
        CHECK (cupos_totales > 0),

    CONSTRAINT chk_viaje_cupos_disponibles
        CHECK (
            cupos_disponibles >= 0
            AND cupos_disponibles <= cupos_totales
        ),

    CONSTRAINT chk_viaje_tarifa
        CHECK (
            tarifa_aproximada IS NULL
            OR tarifa_aproximada >= 0
        )
);


-- ============================================================
-- PUNTOS DE ENCUENTRO
-- ============================================================
-- Lugares donde el conductor puede recoger/dejar pasajeros.
--
-- Ejemplo:
--
-- 1. Metrocentro Santa Ana
-- 2. El Congo
-- 3. Ciudad Arce
-- 4. Santa Tecla
--
-- No son necesariamente ciudades.
-- Pueden ser lugares concretos.
-- ============================================================

CREATE TABLE puntos_encuentro (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    viaje_id UUID NOT NULL,

    nombre VARCHAR(150) NOT NULL,

    descripcion TEXT,

    ubicacion GEOGRAPHY(POINT, 4326) NOT NULL,

    orden SMALLINT NOT NULL,

    -- Indica si el conductor permite recoger pasajeros ahí.
    permite_subida BOOLEAN NOT NULL DEFAULT TRUE,

    -- Indica si el conductor permite dejar pasajeros ahí.
    permite_bajada BOOLEAN NOT NULL DEFAULT TRUE,

    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_punto_viaje
        FOREIGN KEY (viaje_id)
        REFERENCES viajes(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_punto_orden
        UNIQUE (viaje_id, orden),

    CONSTRAINT chk_punto_orden
        CHECK (orden > 0)
);


-- ============================================================
-- SOLICITUDES DE VIAJE
-- ============================================================
-- Un pasajero solicita uno o más cupos.
-- ============================================================

CREATE TABLE solicitudes_viaje (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    viaje_id UUID NOT NULL,

    pasajero_id UUID NOT NULL,

    punto_subida_id UUID,

    punto_bajada_id UUID,

    cantidad_cupos SMALLINT NOT NULL DEFAULT 1,

    estado estado_solicitud NOT NULL DEFAULT 'PENDIENTE',

    mensaje TEXT,

    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    respondido_en TIMESTAMPTZ,

    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_solicitud_viaje
        FOREIGN KEY (viaje_id)
        REFERENCES viajes(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_solicitud_pasajero
        FOREIGN KEY (pasajero_id)
        REFERENCES usuarios(id),

    CONSTRAINT fk_solicitud_punto_subida
        FOREIGN KEY (punto_subida_id)
        REFERENCES puntos_encuentro(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_solicitud_punto_bajada
        FOREIGN KEY (punto_bajada_id)
        REFERENCES puntos_encuentro(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_solicitud_cupos
        CHECK (cantidad_cupos > 0),

    CONSTRAINT uk_solicitud_viaje_pasajero
        UNIQUE (viaje_id, pasajero_id)
);


-- ============================================================
-- CONVERSACIONES
-- ============================================================
-- Conversación asociada a un viaje.
-- ============================================================

CREATE TABLE conversaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    viaje_id UUID NOT NULL,

    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_conversacion_viaje
        FOREIGN KEY (viaje_id)
        REFERENCES viajes(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_conversacion_viaje
        UNIQUE (viaje_id)
);


-- ============================================================
-- PARTICIPANTES DE CONVERSACION
-- ============================================================
-- Permite que una conversación tenga conductor
-- y pasajeros aceptados.
-- ============================================================

CREATE TABLE participantes_conversacion (
    conversacion_id UUID NOT NULL,

    usuario_id UUID NOT NULL,

    unido_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (conversacion_id, usuario_id),

    CONSTRAINT fk_participante_conversacion
        FOREIGN KEY (conversacion_id)
        REFERENCES conversaciones(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_participante_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);


-- ============================================================
-- MENSAJES
-- ============================================================

CREATE TABLE mensajes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    conversacion_id UUID NOT NULL,

    remitente_id UUID NOT NULL,

    contenido TEXT NOT NULL,

    enviado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    leido_en TIMESTAMPTZ,

    CONSTRAINT fk_mensaje_conversacion
        FOREIGN KEY (conversacion_id)
        REFERENCES conversaciones(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_mensaje_remitente
        FOREIGN KEY (remitente_id)
        REFERENCES usuarios(id)
);


-- ============================================================
-- CALIFICACIONES
-- ============================================================
-- Después de finalizar un viaje:
--
-- pasajero -> conductor
-- conductor -> pasajero
-- ============================================================

CREATE TABLE calificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    viaje_id UUID NOT NULL,

    evaluador_id UUID NOT NULL,

    evaluado_id UUID NOT NULL,

    puntuacion SMALLINT NOT NULL,

    comentario TEXT,

    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_calificacion_viaje
        FOREIGN KEY (viaje_id)
        REFERENCES viajes(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_calificacion_evaluador
        FOREIGN KEY (evaluador_id)
        REFERENCES usuarios(id),

    CONSTRAINT fk_calificacion_evaluado
        FOREIGN KEY (evaluado_id)
        REFERENCES usuarios(id),

    CONSTRAINT chk_calificacion_puntuacion
        CHECK (puntuacion BETWEEN 1 AND 5),

    CONSTRAINT chk_calificacion_diferentes
        CHECK (evaluador_id <> evaluado_id),

    CONSTRAINT uk_calificacion
        UNIQUE (viaje_id, evaluador_id, evaluado_id)
);


-- ============================================================
-- INDICES
-- ============================================================

-- Usuarios
CREATE INDEX idx_usuarios_estado
ON usuarios(estado);


-- Vehículos
CREATE INDEX idx_vehiculos_propietario
ON vehiculos(propietario_id);

CREATE INDEX idx_vehiculos_activo
ON vehiculos(activo);


-- Viajes
CREATE INDEX idx_viajes_conductor
ON viajes(conductor_id);

CREATE INDEX idx_viajes_vehiculo
ON viajes(vehiculo_id);

CREATE INDEX idx_viajes_ruta
ON viajes(ruta_id);

CREATE INDEX idx_viajes_fecha_hora
ON viajes(fecha_salida, hora_salida);

CREATE INDEX idx_viajes_estado
ON viajes(estado);

CREATE INDEX idx_viajes_fecha_estado
ON viajes(fecha_salida, estado);

-- PostGIS
CREATE INDEX idx_viajes_origen_geom
ON viajes
USING GIST (origen_geom);

CREATE INDEX idx_viajes_destino_geom
ON viajes
USING GIST (destino_geom);


-- Puntos de encuentro
CREATE INDEX idx_puntos_encuentro_viaje
ON puntos_encuentro(viaje_id);

CREATE INDEX idx_puntos_encuentro_ubicacion
ON puntos_encuentro
USING GIST (ubicacion);


-- Solicitudes
CREATE INDEX idx_solicitudes_viaje
ON solicitudes_viaje(viaje_id);

CREATE INDEX idx_solicitudes_pasajero
ON solicitudes_viaje(pasajero_id);

CREATE INDEX idx_solicitudes_estado
ON solicitudes_viaje(estado);


-- Conversaciones
CREATE INDEX idx_participantes_usuario
ON participantes_conversacion(usuario_id);

CREATE INDEX idx_mensajes_conversacion_fecha
ON mensajes(conversacion_id, enviado_en);


-- Calificaciones
CREATE INDEX idx_calificaciones_evaluado
ON calificaciones(evaluado_id);

CREATE INDEX idx_calificaciones_viaje
ON calificaciones(viaje_id);