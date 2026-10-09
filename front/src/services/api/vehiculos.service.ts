import { apiClient } from './client';
import type { VehiculoResponse, VehiculoCrearRequest } from './types';

export const vehiculosService = {
  /**
   * Obtiene la lista de vehículos activos del conductor autenticado.
   */
  async getMisVehiculos(): Promise<VehiculoResponse[]> {
    return apiClient.get<VehiculoResponse[]>('/vehiculos/mis-vehiculos');
  },

  /**
   * Registra un nuevo vehículo con placa salvadoreña única.
   */
  async registrarVehiculo(data: VehiculoCrearRequest): Promise<VehiculoResponse> {
    return apiClient.post<VehiculoResponse>('/vehiculos', data);
  },

  /**
   * Obtiene un vehículo por su UUID.
   */
  async getVehiculoById(id: string): Promise<VehiculoResponse> {
    return apiClient.get<VehiculoResponse>(`/vehiculos/${id}`);
  },

  /**
   * Da de baja lógica a un vehículo (activo = false).
   */
  async darDeBaja(id: string): Promise<void> {
    return apiClient.delete<void>(`/vehiculos/${id}`);
  }
};
