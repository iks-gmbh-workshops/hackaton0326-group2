import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

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

//TODO: Add API to set Activity to Pending