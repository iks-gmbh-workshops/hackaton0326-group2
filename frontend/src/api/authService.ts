import { apiClient as api } from './httpClient'

export interface RegisterRequest {
  email: string
  password: string
  displayName: string
  agbAccepted: boolean
}

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  email: string
  displayName: string
  role: string
}

export interface ProfileResponse {
  id?: number
  email?: string
  displayName?: string
  username?: string
  firstName?: string
  lastName?: string
}

export interface UpdateProfileRequest {
  email: string
  displayName: string
  firstName?: string
  lastName?: string
  password?: string
}

export const authService = {
  register: async (payload: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', payload)
    return response.data
  },

  login: async (payload: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', payload)
    return response.data
  },

  getMyProfile: async (): Promise<ProfileResponse> => {
    const response = await api.get('/users/me')
    return response.data
  },

  updateMyProfile: async (payload: UpdateProfileRequest): Promise<ProfileResponse> => {
    const response = await api.put('/users/me', payload)
    return response.data
  },

  deleteMyProfile: async (): Promise<void> => {
    await api.delete('/users/me')
  }
}
