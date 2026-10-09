/**
 * Cliente HTTP base para la API de Rutea
 * Soporta autenticación por Cookie HttpOnly y encabezado Bearer Token
 */

import type { ApiErrorResponse } from './types';

// Determina el endpoint base:
// En el navegador usa el proxy de Vite '/api/v1' para evitar problemas de CORS;
// o 'http://localhost:8080/api/v1' según la variable de entorno PUBLIC_API_URL.
const DEFAULT_API_BASE = typeof window !== 'undefined'
  ? '/api/v1'
  : 'http://localhost:8080/api/v1';

export const API_BASE_URL = 
  (typeof import.meta !== 'undefined' && import.meta.env?.PUBLIC_API_URL)
    ? `${import.meta.env.PUBLIC_API_URL}/api/v1`
    : DEFAULT_API_BASE;

export class ApiError extends Error {
  status: number;
  data: ApiErrorResponse;

  constructor(status: number, data: ApiErrorResponse) {
    super(data.message || `Error HTTP ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

export interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | undefined | null>;
}

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string = API_BASE_URL) {
    this.baseUrl = baseUrl;
  }

  /**
   * Obtiene el token guardado en el cliente (si existe)
   */
  private getToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('rutea_auth_token') || sessionStorage.getItem('rutea_auth_token');
    }
    return null;
  }

  /**
   * Guarda el token en el almacenamiento local
   */
  public setToken(token: string, persist: boolean = true): void {
    if (typeof window !== 'undefined') {
      if (persist) {
        localStorage.setItem('rutea_auth_token', token);
      } else {
        sessionStorage.setItem('rutea_auth_token', token);
      }
    }
  }

  /**
   * Elimina el token almacenado
   */
  public clearToken(): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('rutea_auth_token');
      sessionStorage.removeItem('rutea_auth_token');
    }
  }

  /**
   * Construye la URL con parámetros de consulta
   */
  private buildUrl(path: string, params?: RequestOptions['params']): string {
    const cleanPath = path.startsWith('/') ? path : `/${path}`;
    const url = new URL(`${this.baseUrl}${cleanPath}`, typeof window !== 'undefined' ? window.location.origin : 'http://localhost:8080');

    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          url.searchParams.append(key, String(value));
        }
      });
    }

    return url.toString();
  }

  /**
   * Ejecuta la petición HTTP
   */
  public async request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const url = this.buildUrl(path, options.params);
    const token = this.getToken();

    const headers = new Headers(options.headers || {});
    if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
      headers.set('Content-Type', 'application/json');
    }

    // Agregar token en Bearer si está disponible
    if (token && !headers.has('Authorization')) {
      headers.set('Authorization', `Bearer ${token}`);
    }

    const config: RequestInit = {
      ...options,
      headers,
      credentials: 'include', // Clave para enviar/recibir Cookies HttpOnly
    };

    try {
      const response = await fetch(url, config);

      if (!response.ok) {
        let errorData: ApiErrorResponse;
        try {
          errorData = await response.json();
        } catch {
          errorData = {
            status: response.status,
            error: response.statusText,
            message: `Error al procesar solicitud (${response.status})`,
            path,
          };
        }
        throw new ApiError(response.status, errorData);
      }

      // Si la respuesta no tiene contenido (204 No Content)
      if (response.status === 204) {
        return {} as T;
      }

      return (await response.json()) as T;
    } catch (error) {
      if (error instanceof ApiError) {
        throw error;
      }
      throw new ApiError(500, {
        status: 500,
        error: 'NetworkError',
        message: (error as Error).message || 'Error de conexión con el servidor',
        path,
      });
    }
  }

  public get<T>(path: string, params?: RequestOptions['params'], options?: RequestOptions): Promise<T> {
    return this.request<T>(path, { ...options, method: 'GET', params });
  }

  public post<T>(path: string, body?: unknown, options?: RequestOptions): Promise<T> {
    return this.request<T>(path, {
      ...options,
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  public put<T>(path: string, body?: unknown, options?: RequestOptions): Promise<T> {
    return this.request<T>(path, {
      ...options,
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  public patch<T>(path: string, body?: unknown, options?: RequestOptions): Promise<T> {
    return this.request<T>(path, {
      ...options,
      method: 'PATCH',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  public delete<T>(path: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(path, { ...options, method: 'DELETE' });
  }
}

export const apiClient = new ApiClient();
