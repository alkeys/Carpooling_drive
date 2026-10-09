import { apiClient } from './client';
import type { UsuarioResponse, UpdateUsuarioRequest } from './types';

export const usuariosService = {
  /**
   * Obtiene la lista completa de usuarios registrados.
   */
  async getAllUsuarios(): Promise<UsuarioResponse[]> {
    return apiClient.get<UsuarioResponse[]>('/usuarios');
  },

  /**
   * Obtiene un usuario por su UUID.
   */
  async getUsuarioById(id: string): Promise<UsuarioResponse> {
    return apiClient.get<UsuarioResponse>(`/usuarios/${id}`);
  },

  /**
   * Actualiza los datos de un usuario existente.
   */
  async updateUsuario(id: string, data: UpdateUsuarioRequest): Promise<UsuarioResponse> {
    return apiClient.put<UsuarioResponse>(`/usuarios/${id}`, data);
  },

  /**
   * Modifica los roles asignados a un usuario (ADMIN, CONDUCTOR, CLIENTE, USUARIO).
   * Requiere rol ADMIN.
   */
  async changeRoles(id: string, roles: string[]): Promise<UsuarioResponse> {
    return apiClient.put<UsuarioResponse>(`/usuarios/${id}/roles`, roles);
  },

  /**
   * Elimina un usuario por su ID.
   */
  async deleteUsuario(id: string): Promise<void> {
    return apiClient.delete<void>(`/usuarios/${id}`);
  }
};
