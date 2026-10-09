import { apiClient } from './client';
import type { 
  ViajeResponse, 
  ViajeBuscarParams, 
  ViajePublicarRequest 
} from './types';

export interface FormattedRideCard {
  id: string;
  driverName: string;
  driverRole: string;
  driverRating: number;
  driverTrips: number;
  origin: string;
  destination: string;
  departureTime: string;
  arrivalTime: string;
  duration: string;
  vehicle: string;
  availableSeats: number;
  totalSeats: number;
  price: number;
  tags: string[];
  category: 'costa' | 'urbano' | 'interurbano';
  description?: string;
}

/**
 * Transforma un ViajeResponse del backend a las propiedades visuales de la tarjeta
 */
export function mapViajeToRideProps(viaje: ViajeResponse): FormattedRideCard {
  const origin = viaje.origenNombre || 'Origen';
  const destination = viaje.destinoNombre || 'Destino';
  const textCombined = `${origin} ${destination} ${viaje.descripcion || ''}`.toLowerCase();

  let category: 'costa' | 'urbano' | 'interurbano' = 'urbano';
  if (/tunco|libertad|sol|playa|zonte|sunzal|costa|costero|bálsamo/i.test(textCombined)) {
    category = 'costa';
  } else if (/santa ana|san miguel|sonsonate|ahuachapán|usulután|panamericana/i.test(textCombined)) {
    category = 'interurbano';
  }

  // Formato de hora de salida (12h am/pm)
  let depTimeFormatted = viaje.horaSalida || '07:00';
  if (depTimeFormatted.length >= 5) {
    const parts = depTimeFormatted.split(':');
    const h = parseInt(parts[0], 10);
    const m = parts[1];
    const ampm = h >= 12 ? 'PM' : 'AM';
    const h12 = h % 12 || 12;
    depTimeFormatted = `${String(h12).padStart(2, '0')}:${m} ${ampm}`;
  }

  const duration = category === 'costa' ? '35 min' : category === 'interurbano' ? '50 min' : '25 min';
  const arrivalTime = 'Llegada directa';

  // Etiquetas curadas de amenidades (sin mezclar la descripción)
  const tags: string[] = [];
  if (category === 'costa') {
    tags.push('🌊 Ruta Costera', '❄️ Con A/C', '🏄 Surf Friendly');
  } else if (category === 'interurbano') {
    tags.push('🛣️ Interurbano', '⏱️ Salida Puntual', '🛡️ Asientos Cómodos');
  } else {
    tags.push('⚡ Ruta Diaria', '❄️ Con A/C', '⏱️ Puntual');
  }

  return {
    id: viaje.id,
    driverName: 'Conductor Verificado',
    driverRole: 'Conductor Verificado',
    driverRating: 4.9,
    driverTrips: 64,
    origin,
    destination,
    departureTime: depTimeFormatted,
    arrivalTime,
    duration,
    vehicle: 'Vehículo Verificado',
    availableSeats: viaje.cuposDisponibles ?? 1,
    totalSeats: viaje.cuposTotales ?? 3,
    price: Number(viaje.tarifaAproximada) || 2.50,
    tags,
    category,
    description: viaje.descripcion?.trim() || undefined
  };
}

/**
 * Genera el HTML de una tarjeta de viaje para el cliente
 */
export function generateRideCardHtml(card: FormattedRideCard): string {
  const categoryBadgeClass = card.category === 'costa' ? 'badge-fresh' : 'badge-secure';
  const categoryBadgeText = card.category === 'costa' ? '🌊 Ruta Costera' : card.category === 'interurbano' ? '🛣️ Interurbano' : '⚡ Ruta Diaria';

  const seatDots = Array.from({ length: card.totalSeats })
    .map((_, i) => `<span class="seat-dot ${i < card.availableSeats ? 'seat-free' : 'seat-taken'}" title="${i < card.availableSeats ? 'Disponible' : 'Ocupado'}"></span>`)
    .join('');

  const tagsHtml = card.tags
    .map(t => `<span class="amenity-tag">${t}</span>`)
    .join(' ');

  const descHtml = card.description
    ? `<div class="ride-card-note">
         <span class="note-quote-mark" aria-hidden="true">“</span>
         <p class="note-text">${card.description}</p>
       </div>`
    : '';

  return `
    <article 
      class="ride-card glass-card" 
      data-ride-id="${card.id}"
      data-origin="${card.origin}"
      data-destination="${card.destination}"
      data-category="${card.category}"
      data-seats="${card.availableSeats}"
      data-price="${card.price}"
    >
      <div class="card-header">
        <div class="driver-info">
          <div class="driver-avatar-circle" aria-hidden="true">
            <span>C</span>
          </div>
          <div class="driver-meta">
            <div class="driver-name-row">
              <span class="driver-name">${card.driverName}</span>
              <span class="verified-pill" title="Identidad y vehículo 100% verificados">
                <svg viewBox="0 0 24 24" width="13" height="13" fill="#0F8A8B">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                </svg>
                <span>Verificado</span>
              </span>
            </div>
            <div class="driver-subline">
              <span class="rating-badge">★ ${card.driverRating.toFixed(1)}</span>
              <span class="dot-sep">·</span>
              <span class="trips-count">${card.driverTrips} viajes</span>
            </div>
          </div>
        </div>

        <span class="badge ${categoryBadgeClass}">${categoryBadgeText}</span>
      </div>

      <div class="route-timeline">
        <div class="timeline-step origin-step">
          <div class="time-col">${card.departureTime}</div>
          <div class="marker-col">
            <span class="node-ring origin-ring"></span>
            <span class="track-line"></span>
          </div>
          <div class="location-col">
            <strong class="location-name">${card.origin}</strong>
            <span class="location-sub">Punto de encuentro seguro</span>
          </div>
        </div>

        <div class="timeline-duration-row">
          <span class="duration-badge">
            <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            <span>${card.duration} directo</span>
          </span>
        </div>

        <div class="timeline-step dest-step">
          <div class="time-col">${card.arrivalTime}</div>
          <div class="marker-col">
            <span class="node-ring dest-ring"></span>
          </div>
          <div class="location-col">
            <strong class="location-name">${card.destination}</strong>
            <span class="location-sub">Destino principal</span>
          </div>
        </div>
      </div>

      ${descHtml}

      <div class="card-vehicle-row">
        <div class="vehicle-info">
          <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
          </svg>
          <span>${card.vehicle}</span>
        </div>

        <div class="amenities-tags">
          ${tagsHtml}
        </div>
      </div>

      <div class="card-footer">
        <div class="seats-counter">
          <div class="seats-icons">
            ${seatDots}
          </div>
          <span class="seats-label">
            <strong>${card.availableSeats}</strong> ${card.availableSeats === 1 ? 'asiento libre' : 'asientos libres'}
          </span>
        </div>

        <div class="price-action-box">
          <div class="price-box">
            <span class="price-amount">$${card.price.toFixed(2)}</span>
            <span class="price-unit">/ persona</span>
          </div>

          <div class="card-buttons-group">
            <button 
              type="button" 
              class="btn btn-outline btn-sm chat-ride-btn" 
              data-ride-id="${card.id}"
              data-driver="${card.driverName}"
              data-origin="${card.origin}"
              data-dest="${card.destination}"
              title="Consultar al conductor o soporte"
            >
              <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
              </svg>
              <span>Chat</span>
            </button>

            <button 
              type="button" 
              class="btn btn-primary btn-sm book-ride-btn" 
              data-ride-id="${card.id}"
              data-driver="${card.driverName}"
              data-origin="${card.origin}"
              data-dest="${card.destination}"
              data-time="${card.departureTime}"
              data-price="${card.price.toFixed(2)}"
              data-vehicle="${card.vehicle}"
              data-seats="${card.availableSeats}"
            >
              <span>Reservar</span>
            </button>
          </div>
        </div>
      </div>
    </article>
  `;
}

export const viajesService = {
  /**
   * Búsqueda geoespacial y por texto de viajes disponibles.
   * Filtra por coordenadas de origen/destino con radio (km), texto, fecha y cupos.
   */
  async buscarViajes(params?: ViajeBuscarParams): Promise<ViajeResponse[]> {
    return apiClient.get<ViajeResponse[]>('/viajes/buscar', params as Record<string, string | number | undefined>);
  },

  /**
   * Obtiene el detalle completo de un viaje por su UUID.
   */
  async getViajeById(id: string): Promise<ViajeResponse> {
    return apiClient.get<ViajeResponse>(`/viajes/${id}`);
  },

  /**
   * Publica un nuevo viaje (requiere rol CONDUCTOR o ADMIN).
   */
  async publicarViaje(data: ViajePublicarRequest): Promise<ViajeResponse> {
    return apiClient.post<ViajeResponse>('/viajes', data);
  },

  /**
   * Obtiene la lista de viajes publicados por el conductor autenticado.
   */
  async getMisViajes(): Promise<ViajeResponse[]> {
    return apiClient.get<ViajeResponse[]>('/viajes/mis-viajes');
  },

  /**
   * Actualiza el estado del ciclo de vida de un viaje (ej. CANCELADO, EN_CURSO, FINALIZADO).
   */
  async cambiarEstado(id: string, nuevoEstado: string): Promise<ViajeResponse> {
    return apiClient.patch<ViajeResponse>(`/viajes/${id}/estado?estado=${nuevoEstado}`);
  }
};
