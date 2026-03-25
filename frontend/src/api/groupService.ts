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

export interface GroupMember {
  userId?: number
  displayName?: string
  email?: string
  role?: string
  status?: string
  joinedAt?: string
}

export interface CreateGroupRequest {
  name: string
  description?: string
}

export interface UpdateGroupRequest {
  name: string
  description?: string
}

export interface InviteGroupMemberRequest {
  email: string
}

export const groupService = {
  // Get all groups of current user
  getMyGroups: async (): Promise<Group[]> => {
    const response = await api.get('/groups')
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

  // Get members of a group
  getGroupMembers: async (groupId: number): Promise<GroupMember[]> => {
    const response = await api.get(`/groups/${groupId}/members`)
    return response.data
  },

  // Remove a member from a group
  removeGroupMember: async (groupId: number, userId: number): Promise<void> => {
    await api.delete(`/groups/${groupId}/members/${userId}`)
  },

  // Create a new group
  createGroup: async (payloadOrName: CreateGroupRequest | string, description = ''): Promise<Group> => {
    const payload: CreateGroupRequest =
      typeof payloadOrName === 'string'
        ? { name: payloadOrName, ...(description ? { description } : {}) }
        : payloadOrName

    const response = await api.post('/groups', payload)
    return response.data
  },

  // Update an existing group
  updateGroup: async (groupId: number, payload: UpdateGroupRequest): Promise<Group> => {
    const response = await api.put(`/groups/${groupId}`, payload)
    return response.data
  },

  // Invite a member to an existing group
  inviteMember: async (groupId: number, payload: InviteGroupMemberRequest): Promise<void> => {
    await api.post(`/groups/${groupId}/invite`, payload)
  },

  // Join a group via invitation token (backend currently expects a path id, but resolves by token)
  joinByInviteToken: async (inviteToken: string): Promise<Group> => {
    const response = await api.post(`/groups/0/join?token=${encodeURIComponent(inviteToken)}`)
    return response.data
  },

  // Delete a group
  deleteGroup: async (groupId: number): Promise<void> => {
    await api.delete(`/groups/${groupId}`)
  }
}
