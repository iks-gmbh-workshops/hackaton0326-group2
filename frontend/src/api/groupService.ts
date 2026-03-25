import { apiClient as api } from './httpClient'

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

export interface CreateGroupRequest {
  name: string
  description?: string
  members?: string[]
}

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
  createGroup: async (payloadOrName: CreateGroupRequest | string, description = ''): Promise<Group> => {
    const payload: CreateGroupRequest =
      typeof payloadOrName === 'string'
        ? { name: payloadOrName, ...(description ? { description } : {}) }
        : payloadOrName

    const response = await api.post('/groups', payload)
    return response.data
  }
}
