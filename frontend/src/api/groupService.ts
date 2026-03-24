import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export interface Group {
  id: number
  name: string
  description: string
  createdBy: string
  createdAt: string
  memberCount?: number
}

export interface GroupInvitation {
  id: number
  groupId: number
  groupName: string
  invitedBy: string
  invitedAt: string
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED'
}

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const groupService = {
  // Get all groups of current user
  getMyGroups: async (): Promise<Group[]> => {
    const response = await api.get('/groups/my-groups')
    return response.data
  },

  // Get group invitations for current user
  getGroupInvitations: async (): Promise<GroupInvitation[]> => {
    const response = await api.get('/groups/invitations')
    return response.data
  },

  // Accept group invitation
  acceptInvitation: async (invitationId: number): Promise<void> => {
    await api.post(`/groups/invitations/${invitationId}/accept`)
  },

  // Decline group invitation
  declineInvitation: async (invitationId: number): Promise<void> => {
    await api.post(`/groups/invitations/${invitationId}/decline`)
  },

  // Get group details
  getGroup: async (groupId: number): Promise<Group> => {
    const response = await api.get(`/groups/${groupId}`)
    return response.data
  },

  // Create a new group
  createGroup: async (name: string, description: string): Promise<Group> => {
    const response = await api.post('/groups', { name, description })
    return response.data
  }
}
