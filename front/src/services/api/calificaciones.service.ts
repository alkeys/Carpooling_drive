import { apiClient } from './client';
import type { CalificacionResponse } from './types';

export interface ReputacionResponse {
  promedioPuntuacion: number;
  totalCalificaciones: number;
  distribucionEstrellas?: Record<string, number>;
}

export interface CalificarRequest {
  viajeId: string;
  evaluadoId: string;
  puntuacion: number;
  comentario?: string;
}

export const calificacionesService = {
  /**
   * Califica a un usuario tras un viaje finalizado.
   */
  async calificar(data: CalificarRequest): Promise<CalificacionResponse> {
    return apiClient.post<CalificacionResponse>('/calificaciones', data);
  },

  /**
   * Consulta la reputación pública de un usuario (estrellas y total).
   */
  async getReputacionUsuario(usuarioId: string): Promise<ReputacionResponse> {
    return apiClient.get<ReputacionResponse>(`/calificaciones/usuario/${usuarioId}`);
  },

  /**
   * Obtiene todas las reseñas recibidas por un usuario.
   */
  async getResenasUsuario(usuarioId: string): Promise<CalificacionResponse[]> {
    return apiClient.get<CalificacionResponse[]>(`/calificaciones/usuario/${usuarioId}/todas`);
  }
};
