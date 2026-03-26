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

export interface AdminActivity {
  id?: number
  title?: string
  description?: string
  location?: string
  startTime?: string
  endTime?: string
  createdByName?: string
  participantCount?: number
}

export interface ChangeUserRoleRequest {
  role: 'USER' | 'ADMIN'
}

export const adminService = {
  getUsers: async (): Promise<AdminUser[]> => {
    const response = await api.get('/admin/users')
    return Array.isArray(response.data) ? response.data : []
  },

  changeUserRole: async (userId: number, payload: ChangeUserRoleRequest): Promise<AdminUser> => {
    const response = await api.put(`/admin/users/${userId}/role`, payload)
    return response.data
  },

  deleteUser: async (userId: number): Promise<void> => {
    await api.delete(`/admin/users/${userId}`)
  },

  getGroups: async (): Promise<AdminGroup[]> => {
    const response = await api.get('/admin/groups')
    return Array.isArray(response.data) ? response.data : []
  },

  deleteGroup: async (groupId: number): Promise<void> => {
    await api.delete(`/admin/groups/${groupId}`)
  },

  getActivities: async (): Promise<AdminActivity[]> => {
    const response = await api.get('/admin/activities')
    return Array.isArray(response.data) ? response.data : []
  },

  deleteActivity: async (activityId: number): Promise<void> => {
    await api.delete(`/admin/activities/${activityId}`)
  }
}
