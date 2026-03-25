import { apiClient as api, USE_MOCKS } from './httpClient'
import mockData from '../mocks/testdata.json'

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

interface MockUser {
  id: number
  email: string
  passwordHash: string
  displayName: string
  role: 'USER' | 'ADMIN'
  agbAccepted: boolean
  createdAt: string
  updatedAt: string
}

interface MockGroup {
  id: number
  name: string
  description: string
  createdBy: number
  createdAt: string
  inviteToken: string
}

interface MockGroupMember {
  id: number
  userId: number
  groupId: number
  role: 'VERWALTER' | 'MITGLIED'
  status: 'PENDING' | 'ACTIVE' | 'LEFT'
  joinedAt: string
}

interface MockActivity {
  id: number
  title: string
  description: string
  location: string
  startTime: string
  endTime: string
  createdBy: number
  createdAt: string
}

interface MockGroupActivity {
  groupId: number
  activityId: number
}

interface MockActivityParticipant {
  activityId: number
  userId: number
  status: 'ACCEPTED' | 'DECLINED' | 'PENDING'
  respondedAt: string | null
}

interface MockData {
  users: MockUser[]
  groups: MockGroup[]
  groupMembers: MockGroupMember[]
  activities: MockActivity[]
  groupActivities: MockGroupActivity[]
  activityParticipants: MockActivityParticipant[]
}

const mockStore: MockData = {
  users: mockData.users.map((user) => ({ ...user })),
  groups: mockData.groups.map((group) => ({ ...group })),
  groupMembers: mockData.groupMembers.map((member) => ({ ...member })),
  activities: mockData.activities.map((activity) => ({ ...activity })),
  groupActivities: mockData.groupActivities.map((entry) => ({ ...entry })),
  activityParticipants: mockData.activityParticipants.map((participant) => ({ ...participant }))
}

const findUserById = (userId: number): MockUser | undefined =>
  mockStore.users.find((entry) => entry.id === userId)

const getCurrentUser = (): MockUser | null => {
  const authEmail = localStorage.getItem('auth_email')
  if (authEmail) {
    const userByEmail = mockStore.users.find((entry) => entry.email.toLowerCase() === authEmail.toLowerCase())
    if (userByEmail) {
      return userByEmail
    }
  }

  return mockStore.users[0] ?? null
}

const toGroupDto = (group: MockGroup): Group => {
  const creator = findUserById(group.createdBy)
  const activeMemberCount = mockStore.groupMembers.filter(
    (member) => member.groupId === group.id && member.status === 'ACTIVE'
  ).length

  return {
    id: group.id,
    name: group.name,
    description: group.description,
    createdBy: creator?.displayName ?? creator?.email ?? 'Unbekannt',
    createdAt: group.createdAt,
    memberCount: activeMemberCount
  }
}

const toInvitationDto = (membership: MockGroupMember): GroupInvitation | null => {
  const group = mockStore.groups.find((entry) => entry.id === membership.groupId)
  if (!group) {
    return null
  }

  const inviter = findUserById(group.createdBy)

  return {
    id: membership.id,
    groupId: group.id,
    groupName: group.name,
    invitedBy: inviter?.displayName ?? inviter?.email ?? 'Unbekannt',
    invitedAt: membership.joinedAt,
    status: 'PENDING'
  }
}

export const groupService = {
  // Get all groups of current user
  getMyGroups: async (): Promise<Group[]> => {
    if (USE_MOCKS) {
      const currentUser = getCurrentUser()
      if (!currentUser) {
        return []
      }

      const activeMemberships = mockStore.groupMembers
        .filter((member) => member.userId === currentUser.id && member.status === 'ACTIVE')
        .map((member) => member.groupId)

      return mockStore.groups.filter((group) => activeMemberships.includes(group.id)).map(toGroupDto)
    }

    const response = await api.get('/groups/my-groups')
    return response.data
  },

  // Get group invitations for current user
  getGroupInvitations: async (): Promise<GroupInvitation[]> => {
    if (USE_MOCKS) {
      const currentUser = getCurrentUser()
      if (!currentUser) {
        return []
      }

      return mockStore.groupMembers
        .filter((member) => member.userId === currentUser.id && member.status === 'PENDING')
        .map(toInvitationDto)
        .filter((entry): entry is GroupInvitation => entry !== null)
    }

    const response = await api.get('/groups/invitations')
    return response.data
  },

  // Accept group invitation
  acceptInvitation: async (invitationId: number): Promise<void> => {
    if (USE_MOCKS) {
      const invitation = mockStore.groupMembers.find((entry) => entry.id === invitationId)
      if (!invitation || invitation.status !== 'PENDING') {
        throw new Error('Invitation not found')
      }

      invitation.status = 'ACTIVE'
      invitation.joinedAt = new Date().toISOString()
      return
    }

    await api.post(`/groups/invitations/${invitationId}/accept`)
  },

  // Decline group invitation
  declineInvitation: async (invitationId: number): Promise<void> => {
    if (USE_MOCKS) {
      const invitation = mockStore.groupMembers.find((entry) => entry.id === invitationId)
      if (!invitation || invitation.status !== 'PENDING') {
        throw new Error('Invitation not found')
      }

      invitation.status = 'LEFT'
      return
    }

    await api.post(`/groups/invitations/${invitationId}/decline`)
  },

  // Get group details
  getGroup: async (groupId: number): Promise<Group> => {
    if (USE_MOCKS) {
      const group = mockStore.groups.find((entry) => entry.id === groupId)
      if (!group) {
        throw new Error('Group not found')
      }

      return toGroupDto(group)
    }

    const response = await api.get(`/groups/${groupId}`)
    return response.data
  },

  // Create a new group
  createGroup: async (name: string, description: string): Promise<Group> => {
    if (USE_MOCKS) {
      const currentUser = getCurrentUser()
      if (!currentUser) {
        throw new Error('No current user available')
      }

      const nextGroupId =
        mockStore.groups.length === 0 ? 1 : Math.max(...mockStore.groups.map((group) => group.id)) + 1

      const nextMemberId =
        mockStore.groupMembers.length === 0
          ? 1
          : Math.max(...mockStore.groupMembers.map((member) => member.id)) + 1

      const now = new Date().toISOString()

      const newGroup: MockGroup = {
        id: nextGroupId,
        name,
        description,
        createdBy: currentUser.id,
        createdAt: now,
        inviteToken: `invite-${nextGroupId}-${Math.random().toString(36).slice(2, 10)}`
      }

      mockStore.groups.unshift(newGroup)
      mockStore.groupMembers.push({
        id: nextMemberId,
        userId: currentUser.id,
        groupId: nextGroupId,
        role: 'VERWALTER',
        status: 'ACTIVE',
        joinedAt: now
      })

      return toGroupDto(newGroup)
    }

    const response = await api.post('/groups', { name, description })
    return response.data
  }
}
