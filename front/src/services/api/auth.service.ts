import { apiClient } from './client';
import type { 
  LoginRequest, 
  AuthResponse, 
  UsuarioResponse, 
  RegisterRequest, 
  UpdateUsuarioRequest 
} from './types';

const USER_STORAGE_KEY = 'rutea_auth_user';

export const authService = {
  /**
   * Inicia sesión con correo y contraseña.
   * Guarda el token retornado, persiste el perfil y gestiona la Cookie HttpOnly.
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>('/auth/login', credentials);
    if (response?.token) {
      apiClient.setToken(response.token);
      if (typeof window !== 'undefined') {
        localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(response));
        window.dispatchEvent(new CustomEvent('auth-state-changed', { detail: { user: response } }));
      }
    }
    return response;
  },

  /**
   * Cierra sesión e invalida la Cookie y el token local.
   */
  async logout(): Promise<{ mensaje: string }> {
    try {
      const res = await apiClient.post<{ mensaje: string }>('/auth/logout');
      return res;
    } catch (err) {
      console.warn('Advertencia al cerrar sesión en el servidor:', err);
      return { mensaje: 'Sesión finalizada localmente' };
    } finally {
      apiClient.clearToken();
      if (typeof window !== 'undefined') {
        localStorage.removeItem(USER_STORAGE_KEY);
        // Limpiar cookies de sesión accesibles desde el navegador
        try {
          const cookies = document.cookie.split(";");
          for (const cookie of cookies) {
            const eqPos = cookie.indexOf("=");
            const name = eqPos > -1 ? cookie.substring(0, eqPos).trim() : cookie.trim();
            if (name) {
              document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`;
            }
          }
        } catch (_) {}
        window.dispatchEvent(new CustomEvent('auth-state-changed', { detail: { user: null } }));
      }
    }
  },

  /**
   * Retorna el token JWT actual
   */
  getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem('rutea_auth_token') || sessionStorage.getItem('rutea_auth_token');
  },

  /**
   * Retorna si el usuario está actualmente autenticado.
   */
  isAuthenticated(): boolean {
    return Boolean(this.getToken());
  },

  /**
   * Retorna los datos del usuario almacenado en sesión local.
   */
  getStoredUser(): AuthResponse | null {
    if (typeof window === 'undefined') return null;
    const userJson = localStorage.getItem(USER_STORAGE_KEY);
    if (!userJson) return null;
    try {
      return JSON.parse(userJson) as AuthResponse;
    } catch {
      return null;
    }
  },

  /**
   * Verifica autenticación. Si no está autenticado, dispara evento para abrir el modal
   * de inicio de sesión/registro con mensaje explicativo.
   */
  requireAuth(actionDescription: string = 'realizar esta acción'): boolean {
    if (this.isAuthenticated()) {
      return true;
    }

    if (typeof window !== 'undefined') {
      const isDriverAction = actionDescription.toLowerCase().includes('publicar') || actionDescription.toLowerCase().includes('conductor');
      const suffix = isDriverAction ? 'como conductor' : '';
      const message = `🔒 Debes iniciar sesión o registrarte ${suffix ? suffix + ' ' : ''}para ${actionDescription}.`;

      window.dispatchEvent(new CustomEvent('request-auth-required', {
        detail: {
          action: actionDescription,
          message
        }
      }));
    }
    return false;
  },

  /**
   * Obtiene la información del usuario actualmente autenticado (/me).
   */
  async getMe(): Promise<UsuarioResponse> {
    return apiClient.get<UsuarioResponse>('/auth/me');
  },

  /**
   * Actualiza el perfil del usuario autenticado.
   */
  async updateMe(data: UpdateUsuarioRequest): Promise<UsuarioResponse> {
    return apiClient.post<UsuarioResponse>('/auth/me/update', data);
  },

  /**
   * Registro público de un nuevo usuario en la plataforma.
   */
  async register(data: RegisterRequest): Promise<UsuarioResponse> {
    return apiClient.post<UsuarioResponse>('/auth/register', data);
  }
};
