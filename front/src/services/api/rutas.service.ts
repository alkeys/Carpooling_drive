import { apiClient } from './client';
import type { RutaResponse } from './types';

export interface RutaCrearRequest {
  nombre: string;
  descripcion: string;
}

export const rutasService = {
  /**
   * Consulta pública del catálogo maestro de rutas interurbanas y costeras activas.
   */
  async getRutas(): Promise<RutaResponse[]> {
    return apiClient.get<RutaResponse[]>('/rutas');
  },

  /**
   * Consulta administrativa de todas las rutas, incluyendo inactivas. Requiere rol ADMIN.
   */
  async getAllRutas(): Promise<RutaResponse[]> {
    return apiClient.get<RutaResponse[]>('/rutas/todas');
  },

  /**
   * Obtiene una ruta por su UUID.
   */
  async getRutaById(id: string): Promise<RutaResponse> {
    return apiClient.get<RutaResponse>(`/rutas/${id}`);
  },

  /**
   * Agrega una nueva ruta al catálogo oficial. Requiere rol ADMIN.
   */
  async crearRuta(data: RutaCrearRequest): Promise<RutaResponse> {
    return apiClient.post<RutaResponse>('/rutas', data);
  },

  /**
   * Modifica los datos de una ruta existente. Requiere rol ADMIN.
   */
  async actualizarRuta(id: string, data: RutaCrearRequest): Promise<RutaResponse> {
    return apiClient.put<RutaResponse>(`/rutas/${id}`, data);
  },

  /**
   * Activa o desactiva una ruta en el catálogo. Requiere rol ADMIN.
   */
  async cambiarEstadoRuta(id: string, activo: boolean): Promise<RutaResponse> {
    return apiClient.patch<RutaResponse>(`/rutas/${id}/estado?activo=${activo}`);
  }
};
