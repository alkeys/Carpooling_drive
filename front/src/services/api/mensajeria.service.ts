import { apiClient } from './client';
import type { 
  ConversacionAdminResponse, 
  ConversacionResponse, 
  MensajeResponse, 
  MensajeEnviarRequest 
} from './types';

/**
 * Cliente WebSocket STOMP ligero y resiliente sin dependencias externas
 */
export class StompChatClient {
  private ws: WebSocket | null = null;
  private token: string | null = null;
  private subscriptions: Map<string, { id: string; callback: (msg: MensajeResponse) => void }> = new Map();
  private subCounter = 0;
  private isConnected = false;
  private reconnectTimer: any = null;
  private onStatusChange?: (status: 'CONNECTING' | 'CONNECTED' | 'DISCONNECTED' | 'ERROR') => void;

  constructor(token?: string | null, onStatusChange?: (status: 'CONNECTING' | 'CONNECTED' | 'DISCONNECTED' | 'ERROR') => void) {
    this.token = token || null;
    this.onStatusChange = onStatusChange;
  }

  public setToken(token: string | null) {
    this.token = token;
  }

  public connect(): void {
    if (typeof window === 'undefined') return;
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return;
    }

    if (this.onStatusChange) this.onStatusChange('CONNECTING');

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    // Se usa el proxy local de Vite/Astro o puerto 8080 directo
    const wsUrl = `${protocol}//${window.location.host}/ws`;

    try {
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = () => {
        // Enviar frame CONNECT de STOMP 1.2
        const connectHeaders: string[] = [
          'CONNECT',
          'accept-version:1.2,1.1,1.0',
          'heart-beat:10000,10000'
        ];
        if (this.token) {
          connectHeaders.push(`Authorization:Bearer ${this.token}`);
          connectHeaders.push(`token:${this.token}`);
        }
        connectHeaders.push('', ''); // Termina con dos saltos de línea y byte nulo
        this.ws?.send(connectHeaders.join('\n') + '\x00');
      };

      this.ws.onmessage = (event) => {
        const data = typeof event.data === 'string' ? event.data : '';
        this.handleStompFrame(data);
      };

      this.ws.onclose = () => {
        this.isConnected = false;
        if (this.onStatusChange) this.onStatusChange('DISCONNECTED');
        this.scheduleReconnect();
      };

      this.ws.onerror = () => {
        if (this.onStatusChange) this.onStatusChange('ERROR');
      };
    } catch {
      if (this.onStatusChange) this.onStatusChange('ERROR');
      this.scheduleReconnect();
    }
  }

  private handleStompFrame(raw: string): void {
    // Parser simple de frames STOMP delimitados por \x00
    const frames = raw.split('\x00').filter(f => f.trim().length > 0);

    for (const frame of frames) {
      const lines = frame.split('\n');
      const command = lines[0]?.trim();

      if (command === 'CONNECTED') {
        this.isConnected = true;
        if (this.onStatusChange) this.onStatusChange('CONNECTED');

        // Re-suscribir a todos los tópicos activos tras reconexión
        this.subscriptions.forEach((sub, destination) => {
          this.sendSubscribe(sub.id, destination);
        });
      } else if (command === 'MESSAGE') {
        // Encontrar fin de encabezados (línea vacía)
        let bodyStartIndex = -1;
        let destination = '';

        for (let i = 1; i < lines.length; i++) {
          const line = lines[i].trim();
          if (line.startsWith('destination:')) {
            destination = line.substring(12).trim();
          } else if (line === '') {
            bodyStartIndex = i + 1;
            break;
          }
        }

        if (bodyStartIndex !== -1) {
          const body = lines.slice(bodyStartIndex).join('\n').trim();
          try {
            const mensaje: MensajeResponse = JSON.parse(body);
            // Notificar a los suscriptores correspondientes
            this.subscriptions.forEach((sub, subDest) => {
              if (subDest === destination || destination.startsWith(subDest)) {
                sub.callback(mensaje);
              }
            });
          } catch (e) {
            console.warn('[STOMP] Error al parsear mensaje JSON:', e);
          }
        }
      }
    }
  }

  private sendSubscribe(subId: string, destination: string) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;
    const frame = [
      'SUBSCRIBE',
      `id:${subId}`,
      `destination:${destination}`,
      '',
      ''
    ].join('\n') + '\x00';
    this.ws.send(frame);
  }

  public subscribe(destination: string, callback: (msg: MensajeResponse) => void): () => void {
    const subId = `sub-${this.subCounter++}`;
    this.subscriptions.set(destination, { id: subId, callback });

    if (this.isConnected && this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.sendSubscribe(subId, destination);
    }

    return () => {
      this.subscriptions.delete(destination);
      if (this.isConnected && this.ws && this.ws.readyState === WebSocket.OPEN) {
        const frame = ['UNSUBSCRIBE', `id:${subId}`, '', ''].join('\n') + '\x00';
        this.ws.send(frame);
      }
    };
  }

  public send(destination: string, payload: any): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      return;
    }
    const body = JSON.stringify(payload);
    const frame = [
      'SEND',
      `destination:${destination}`,
      'content-type:application/json',
      `content-length:${body.length}`,
      '',
      body
    ].join('\n') + '\x00';
    this.ws.send(frame);
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    this.reconnectTimer = setTimeout(() => {
      this.connect();
    }, 4000);
  }

  public disconnect(): void {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.isConnected = false;
    this.subscriptions.clear();
  }

  public getIsConnected(): boolean {
    return this.isConnected;
  }
}

/**
 * Servicio de mensajería REST y WebSocket para Rutea
 */
export const mensajeriaService = {
  /**
   * Obtiene todas las conversaciones del sistema para auditoría y soporte de Administrador
   */
  async listarTodasAdmin(): Promise<ConversacionAdminResponse[]> {
    return apiClient.get<ConversacionAdminResponse[]>('/conversaciones');
  },

  /**
   * Obtiene o crea la conversación vinculada a un viaje específico
   */
  async obtenerOCrearPorViaje(viajeId: string): Promise<ConversacionResponse> {
    return apiClient.get<ConversacionResponse>(`/conversaciones/viaje/${viajeId}`);
  },

  /**
   * Obtiene las conversaciones del usuario autenticado
   */
  async obtenerMisConversaciones(): Promise<ConversacionResponse[]> {
    return apiClient.get<ConversacionResponse[]>('/conversaciones/mis-conversaciones');
  },

  /**
   * Obtiene el historial completo de mensajes en orden cronológico
   */
  async obtenerMensajes(conversacionId: string): Promise<MensajeResponse[]> {
    return apiClient.get<MensajeResponse[]>(`/conversaciones/${conversacionId}/mensajes`);
  },

  /**
   * Envía un mensaje a la conversación a través del endpoint REST
   */
  async enviarMensaje(conversacionId: string, contenido: string): Promise<MensajeResponse> {
    const payload: MensajeEnviarRequest = { contenido };
    return apiClient.post<MensajeResponse>(`/conversaciones/${conversacionId}/mensajes`, payload);
  },

  /**
   * Crea una instancia de cliente WebSocket STOMP
   */
  crearWebSocketClient(
    token?: string | null, 
    onStatusChange?: (status: 'CONNECTING' | 'CONNECTED' | 'DISCONNECTED' | 'ERROR') => void
  ): StompChatClient {
    return new StompChatClient(token, onStatusChange);
  }
};
