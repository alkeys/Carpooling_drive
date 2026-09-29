-- ============================================================
-- RUTEA - V1__init_schema.sql
-- PostgreSQL + PostGIS Initial Schema (Idempotent)
-- ============================================================

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- ENUMS (Idempotent)
-- ============================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_usuario') THEN
        CREATE TYPE estado_usuario AS ENUM ('ACTIVO', 'SUSPENDIDO', 'INACTIVO');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_viaje') THEN
        CREATE TYPE estado_viaje AS ENUM ('PUBLICADO', 'COMPLETO', 'EN_CURSO', 'FINALIZADO', 'CANCELADO');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_solicitud') THEN
        CREATE TYPE estado_solicitud AS ENUM ('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'CANCELADA');
    END IF;
END$$;

-- ============================================================
-- USUARIOS
-- ============================================================

CREATE TABLE IF NOT EXISTS usuarios (
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

CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id UUID NOT NULL,
    rol VARCHAR(50) NOT NULL,
    PRIMARY KEY (usuario_id, rol),
    CONSTRAINT fk_usuario_roles_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_usuario_roles_usuario ON usuario_roles(usuario_id);

-- ============================================================
-- VEHICULOS
-- ============================================================

CREATE TABLE IF NOT EXISTS vehiculos (
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

-- Asegurar que la columna activo exista si la tabla ya existía previamente
ALTER TABLE vehiculos ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE;

-- ============================================================
-- RUTAS
-- ============================================================

CREATE TABLE IF NOT EXISTS rutas (
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

CREATE TABLE IF NOT EXISTS viajes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    conductor_id UUID NOT NULL,

    vehiculo_id UUID NOT NULL,

    ruta_id UUID,

    origen_nombre VARCHAR(150) NOT NULL,

    origen_geom GEOGRAPHY(POINT, 4326) NOT NULL,

    destino_nombre VARCHAR(150) NOT NULL,

    destino_geom GEOGRAPHY(POINT, 4326) NOT NULL,

    fecha_salida DATE NOT NULL,

    hora_salida TIME NOT NULL,

    cupos_totales SMALLINT NOT NULL,

    cupos_disponibles SMALLINT NOT NULL,

    tarifa_aproximada NUMERIC(8,2),

    descripcion TEXT,

    estado estado_viaje NOT NULL DEFAULT 'PUBLICADO',

    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),

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

CREATE TABLE IF NOT EXISTS puntos_encuentro (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    viaje_id UUID NOT NULL,

    nombre VARCHAR(150) NOT NULL,

    descripcion TEXT,

    ubicacion GEOGRAPHY(POINT, 4326) NOT NULL,

    orden SMALLINT NOT NULL,

    permite_subida BOOLEAN NOT NULL DEFAULT TRUE,

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

CREATE TABLE IF NOT EXISTS solicitudes_viaje (
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

CREATE TABLE IF NOT EXISTS conversaciones (
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

CREATE TABLE IF NOT EXISTS participantes_conversacion (
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

CREATE TABLE IF NOT EXISTS mensajes (
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

CREATE TABLE IF NOT EXISTS calificaciones (
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

CREATE INDEX IF NOT EXISTS idx_usuarios_estado ON usuarios(estado);
CREATE INDEX IF NOT EXISTS idx_vehiculos_propietario ON vehiculos(propietario_id);
CREATE INDEX IF NOT EXISTS idx_vehiculos_activo ON vehiculos(activo);
CREATE INDEX IF NOT EXISTS idx_viajes_conductor ON viajes(conductor_id);
CREATE INDEX IF NOT EXISTS idx_viajes_vehiculo ON viajes(vehiculo_id);
CREATE INDEX IF NOT EXISTS idx_viajes_ruta ON viajes(ruta_id);
CREATE INDEX IF NOT EXISTS idx_viajes_fecha_hora ON viajes(fecha_salida, hora_salida);
CREATE INDEX IF NOT EXISTS idx_viajes_estado ON viajes(estado);
CREATE INDEX IF NOT EXISTS idx_viajes_fecha_estado ON viajes(fecha_salida, estado);

CREATE INDEX IF NOT EXISTS idx_viajes_origen_geom ON viajes USING GIST (origen_geom);
CREATE INDEX IF NOT EXISTS idx_viajes_destino_geom ON viajes USING GIST (destino_geom);

CREATE INDEX IF NOT EXISTS idx_puntos_encuentro_viaje ON puntos_encuentro(viaje_id);
CREATE INDEX IF NOT EXISTS idx_puntos_encuentro_ubicacion ON puntos_encuentro USING GIST (ubicacion);

CREATE INDEX IF NOT EXISTS idx_solicitudes_viaje ON solicitudes_viaje(viaje_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_pasajero ON solicitudes_viaje(pasajero_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_estado ON solicitudes_viaje(estado);

CREATE INDEX IF NOT EXISTS idx_participantes_usuario ON participantes_conversacion(usuario_id);
CREATE INDEX IF NOT EXISTS idx_mensajes_conversacion_fecha ON mensajes(conversacion_id, enviado_en);

CREATE INDEX IF NOT EXISTS idx_calificaciones_evaluado ON calificaciones(evaluado_id);
CREATE INDEX IF NOT EXISTS idx_calificaciones_viaje ON calificaciones(viaje_id);
