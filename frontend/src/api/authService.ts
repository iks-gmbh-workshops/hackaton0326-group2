import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

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

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json'
  }
})

export const authService = {
  register: async (payload: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', payload)
    return response.data
  },

  login: async (payload: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', payload)
    return response.data
  }
}
