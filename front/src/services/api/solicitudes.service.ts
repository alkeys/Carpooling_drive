import { apiClient } from './client';
import type { SolicitudRequest, SolicitudResponse } from './types';

export const solicitudesService = {
  /**
   * Solicita un cupo en un viaje publicado.
   */
  async solicitarCupo(data: SolicitudRequest): Promise<SolicitudResponse> {
    return apiClient.post<SolicitudResponse>('/solicitudes-viaje', data);
  },

  /**
   * Obtiene las solicitudes enviadas por el pasajero autenticado.
   */
  async getMisSolicitudes(): Promise<SolicitudResponse[]> {
    return apiClient.get<SolicitudResponse[]>('/solicitudes-viaje/mis-solicitudes');
  },

  /**
   * Obtiene las solicitudes recibidas para un viaje específico (solo conductor).
   */
  async getSolicitudesPorViaje(viajeId: string): Promise<SolicitudResponse[]> {
    return apiClient.get<SolicitudResponse[]>(`/solicitudes-viaje/viaje/${viajeId}`);
  },

  /**
   * Responde (acepta o rechaza) una solicitud de cupo.
   * Si se acepta, el backend descuenta el cupo atómicamente.
   */
  async responderSolicitud(id: string, aceptar: boolean): Promise<SolicitudResponse> {
    return apiClient.request<SolicitudResponse>(`/solicitudes-viaje/${id}/responder`, {
      method: 'PATCH',
      body: JSON.stringify({ aceptar }),
    });
  },

  /**
   * Cancela una solicitud de cupo.
   * Si ya estaba aceptada, los cupos se restauran automáticamente.
   */
  async cancelarSolicitud(id: string): Promise<SolicitudResponse> {
    return apiClient.put<SolicitudResponse>(`/solicitudes-viaje/${id}/cancelar`);
  }
};
