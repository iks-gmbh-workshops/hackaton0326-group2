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

export const activityService = {
  // Get all activities where user is a participant
  getMyActivities: async (): Promise<Activity[]> => {
    const response = await api.get('/activities/my-activities')
    return response.data
  },

  // Get pending activities (where user needs to respond)
  getPendingActivities: async (): Promise<ActivityParticipation[]> => {
    const response = await api.get('/activities/pending')
    return response.data
  },

  // Get activity details
  getActivity: async (activityId: number): Promise<Activity> => {
    const response = await api.get(`/activities/${activityId}`)
    return response.data
  },

  // Accept activity participation
  acceptActivity: async (activityId: number): Promise<void> => {
    await api.post(`/activities/${activityId}/accept`)
  },

  // Decline activity participation
  declineActivity: async (activityId: number): Promise<void> => {
    await api.post(`/activities/${activityId}/decline`)
  },

  // Set activity participation back to pending
  setActivityPending: async (activityId: number): Promise<void> => {
    await api.post(`/activities/${activityId}/pending`)
  },

  // Create a new activity
  createActivity: async (
    title: string,
    description: string,
    location: string,
    startTime: string,
    endTime: string,
    groupId?: number
  ): Promise<Activity> => {
    const response = await api.post('/activities', {
      title,
      description,
      location,
      startTime,
      endTime,
      groupId
    })
    return response.data
  }
}
