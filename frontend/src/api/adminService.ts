import { apiClient as api } from './httpClient'

export interface AdminUser {
  id?: number
  email?: string
  displayName?: string
  role?: string
  createdAt?: string
}

export interface AdminGroup {
  id?: number
  name?: string
  description?: string
  createdBy?: string
  memberCount?: number
  createdAt?: string
}

export const adminService = {
  getUsers: async (): Promise<AdminUser[]> => {
    const response = await api.get('/admin/users')
    return Array.isArray(response.data) ? response.data : []
  },

  getGroups: async (): Promise<AdminGroup[]> => {
    const response = await api.get('/admin/groups')
    return Array.isArray(response.data) ? response.data : []
  }
}
