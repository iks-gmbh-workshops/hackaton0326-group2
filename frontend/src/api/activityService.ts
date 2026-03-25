import { apiClient as api } from './httpClient'

export interface Activity {
  id: number
  title: string
  description: string
  location: string
  startTime: string
  endTime: string
  createdBy: string
  createdAt: string
  groupId?: number
  groupName?: string
}

export interface ActivityParticipation {
  id: number
  activityId: number
  activityTitle: string
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED'
  startTime: string
  endTime: string
  respondedAt?: string
}

export interface ParticipantResponse {
  userId?: number
  displayName?: string
  email?: string
  status?: 'PENDING' | 'ACCEPTED' | 'DECLINED' | string
  respondedAt?: string
}

export interface CreateActivityRequest {
  title: string
  description?: string
  location?: string
  startTime: string
  endTime: string
  groupId?: number
  groupIds?: number[]
}

export const activityService = {
  // Get all upcoming activities where user is a participant
  getMyActivities: async (): Promise<Activity[]> => {
    const response = await api.get('/activities/upcoming')
    return response.data
  },

  // Explicit alias for readability in newer code
  getUpcomingActivities: async (): Promise<Activity[]> => {
    const response = await api.get('/activities/upcoming')
    return response.data
  },

  // Legacy helper. Backend has no dedicated pending endpoint.
  getPendingActivities: async (): Promise<ActivityParticipation[]> => {
    const upcoming = await api.get('/activities/upcoming')
    return (upcoming.data as Activity[]).map((activity) => ({
      id: activity.id,
      activityId: activity.id,
      activityTitle: activity.title,
      status: 'PENDING' as const,
      startTime: activity.startTime,
      endTime: activity.endTime
    }))
  },

  // Get activity details
  getActivity: async (activityId: number): Promise<Activity> => {
    const response = await api.get(`/activities/${activityId}`)
    return response.data
  },

  // Accept activity participation
  acceptActivity: async (activityId: number): Promise<void> => {
    await api.post(`/activities/${activityId}/respond`, { status: 'ACCEPTED' })
  },

  // Decline activity participation
  declineActivity: async (activityId: number): Promise<void> => {
    await api.post(`/activities/${activityId}/respond`, { status: 'DECLINED' })
  },

  // Set activity participation back to pending
  setActivityPending: async (activityId: number): Promise<void> => {
    await api.post(`/activities/${activityId}/respond`, { status: 'PENDING' })
  },

  // Create a new activity
  createActivity: async (payload: CreateActivityRequest): Promise<Activity> => {
    const response = await api.post('/activities', payload)
    return response.data
  },

  // Get participants of an activity
  getParticipants: async (activityId: number): Promise<ParticipantResponse[]> => {
    const response = await api.get(`/activities/${activityId}/participants`)
    return response.data
  },

  // Set own participation status for an activity
  respondToActivity: async (activityId: number, status: 'PENDING' | 'ACCEPTED' | 'DECLINED'): Promise<ParticipantResponse> => {
    const response = await api.post(`/activities/${activityId}/respond`, { status })
    return response.data
  }
}
