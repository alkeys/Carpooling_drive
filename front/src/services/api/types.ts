/**
 * Tipos y DTOs sincronizados con el backend Spring Boot (Rutea API v1)
 */

// ==========================================
// 1. Tipos de Autenticación y Usuario
// ==========================================
export type RolUsuario = 'ADMIN' | 'CONDUCTOR' | 'CLIENTE' | 'USUARIO';
export type EstadoUsuario = 'ACTIVO' | 'INACTIVO' | 'BLOQUEADO' | 'SUSPENDIDO';

export interface LoginRequest {
  correo: string;
  contrasena: string;
}

export interface RegisterRequest {
  nombre: string;
  apellido: string;
  correo: string;
  telefono: string;
  contrasena: string;
  fotourl?: string;
  roles?: RolUsuario[];
}

export interface AuthResponse {
  id: string;
  nombre: string;
  apellido: string;
  correo: string;
  telefono: string;
  fotourl?: string | null;
  estado: EstadoUsuario;
  roles: RolUsuario[];
  mensaje: string;
  token: string;
}

export interface UsuarioResponse {
  id: string;
  nombre: string;
  apellido: string;
  correo: string;
  telefono: string;
  fotourl?: string | null;
  verificado: boolean;
  estado: EstadoUsuario;
  roles: RolUsuario[];
  creadoEn: string;
  actualizadoEn: string;
}

export interface UpdateUsuarioRequest {
  nombre?: string;
  apellido?: string;
  telefono?: string;
  fotourl?: string;
}

// ==========================================
// 2. Tipos de Rutas Predefinidas
// ==========================================
export interface RutaResponse {
  id: string;
  nombre: string;
  descripcion: string;
  activo: boolean;
  creadoEn: string;
  actualizadoEn: string;
}

// ==========================================
// 3. Tipos de Viajes y Geolocalización
// ==========================================
export type EstadoViaje = 'PUBLICADO' | 'COMPLETO' | 'EN_CURSO' | 'FINALIZADO' | 'CANCELADO';

export interface CoordenadaDto {
  latitud: number;
  longitud: number;
}

export interface PuntoEncuentroDto {
  id?: string;
  nombre: string;
  coordenada: CoordenadaDto;
  horaEstimada: string;
  orden: number;
}

export interface ViajeResponse {
  id: string;
  conductorId: string;
  vehiculoId: string;
  rutaId?: string | null;
  origenNombre: string;
  origenLat: number;
  origenLon: number;
  destinoNombre: string;
  destinoLat: number;
  destinoLon: number;
  fechaSalida: string;
  horaSalida: string;
  cuposTotales: number;
  cuposDisponibles: number;
  tarifaAproximada: number;
  descripcion?: string | null;
  estado: EstadoViaje;
  puntosEncuentro: PuntoEncuentroDto[];
  creadoEn: string;
  actualizadoEn: string;
}

export interface ViajeBuscarParams {
  origenLat?: number;
  origenLon?: number;
  radioOrigenKm?: number;
  destinoLat?: number;
  destinoLon?: number;
  radioDestinoKm?: number;
  origenTexto?: string;
  destinoTexto?: string;
  fecha?: string;
  cuposMinimos?: number;
}

export interface PuntoEncuentroRequest {
  nombre: string;
  descripcion?: string;
  latitud: number;
  longitud: number;
  orden: number;
  permiteSubida?: boolean;
  permiteBajada?: boolean;
}

export interface ViajePublicarRequest {
  vehiculoId: string;
  rutaId?: string | null;
  origenNombre: string;
  origenLat: number;
  origenLon: number;
  destinoNombre: string;
  destinoLat: number;
  destinoLon: number;
  fechaSalida: string;
  horaSalida: string;
  cuposTotales: number;
  cuposDisponibles?: number;
  tarifaAproximada: number;
  descripcion?: string;
  puntosEncuentro?: PuntoEncuentroRequest[];
}

// ==========================================
// 4. Tipos de Solicitudes y Reservas
// ==========================================
export type EstadoSolicitud = 'PENDIENTE' | 'ACEPTADA' | 'RECHAZADA' | 'CANCELADA';

export interface SolicitudRequest {
  viajeId: string;
  puntoEncuentroId?: string;
  cantidadCupos: number;
}

export interface SolicitudResponse {
  id: string;
  viajeId: string;
  pasajeroId: string;
  puntoSubidaId?: string | null;
  puntoBajadaId?: string | null;
  puntoEncuentroId?: string | null;
  cantidadCupos: number;
  estado: EstadoSolicitud;
  mensaje?: string | null;
  creadoEn: string;
  respondidoEn?: string | null;
  actualizadoEn: string;
}

// ==========================================
// 5. Tipos de Vehículos
// ==========================================
export interface VehiculoResponse {
  id: string;
  usuarioId: string;
  placa: string;
  marca: string;
  modelo: string;
  color: string;
  anio: number;
  activo: boolean;
  creadoEn: string;
  actualizadoEn: string;
}

export interface VehiculoCrearRequest {
  placa: string;
  marca: string;
  modelo: string;
  color?: string;
  anio?: number;
  tipo?: string;
  capacidadPasajeros: number;
  fotoUrl?: string;
}

// ==========================================
// 6. Tipos de Calificaciones
// ==========================================
export interface CalificacionResponse {
  id: string;
  viajeId: string;
  calificadorId: string;
  calificadoId: string;
  puntuacion: number;
  comentario?: string;
  creadoEn: string;
}

// ==========================================
// 7. Respuesta de Error Estándar (RFC 7807)
// ==========================================
export interface ApiErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp?: string;
}

// ==========================================
// 8. Tipos de Mensajería y Chat en Tiempo Real
// ==========================================
export interface MensajeResponse {
  id: string;
  conversacionId: string;
  remitenteId: string;
  remitenteNombre?: string;
  remitenteRol?: RolUsuario | string;
  contenido: string;
  enviadoEn: string;
  leidoEn?: string | null;
}

export interface MensajeEnviarRequest {
  contenido: string;
}

export interface ConversacionResponse {
  id: string;
  viajeId: string;
  creadoEn: string;
  participantesIds: string[];
}

export interface ConversacionAdminResponse {
  id: string;
  viajeId: string;
  origen: string;
  destino: string;
  conductorId: string;
  conductorNombre: string;
  conductorCorreo: string;
  creadoEn: string;
  participantesIds: string[];
  ultimoMensaje: string;
  ultimoMensajeEn: string;
  totalMensajes: number;
}
