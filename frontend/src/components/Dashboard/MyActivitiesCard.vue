<template>
  <div>
    <!-- Loading State -->
    <div v-if="isLoading" class="flex items-center justify-center py-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-green-600"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <p class="text-red-800">{{ error }}</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="activities.length === 0" class="text-center py-8">
      <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
      </svg>
      <p class="text-gray-500">Keine zukünftigen Aktivitäten</p>
    </div>

    <!-- Activities List -->
    <div v-else class="space-y-3">
      <div 
        v-for="activity in activities" 
        :key="activity.id"
        class="p-4 border border-green-200 bg-green-50 rounded-lg hover:bg-green-100 transition"
      >
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1">
            <h3 class="font-semibold text-gray-900">{{ activity.title }}</h3>
            <p class="text-sm text-gray-600 mt-1 line-clamp-2">{{ activity.description }}</p>
            <div class="flex flex-col gap-2 mt-3 text-xs text-gray-600">
              <span class="flex items-center gap-2">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/></svg>
                {{ formatDateTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}
              </span>
              <span class="flex items-center gap-2">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm3.5-9c.83 0 1.5-.67 1.5-1.5S16.33 8 15.5 8 14 8.67 14 9.5s.67 1.5 1.5 1.5zm-7 0c.83 0 1.5-.67 1.5-1.5S9.33 8 8.5 8 7 8.67 7 9.5 7.67 11 8.5 11zm3.5 6.5c2.33 0 4.31-1.46 5.11-3.5H6.89c.8 2.04 2.78 3.5 5.11 3.5z"/></svg>
                {{ activity.location }}
              </span>
            </div>
            <span v-if="activity.groupName" class="inline-block mt-2 px-2 py-1 bg-blue-100 text-blue-800 text-xs font-semibold rounded">
              {{ activity.groupName }}
            </span>
          </div>
          <svg class="w-5 h-5 text-gray-400 flex-shrink-0" fill="currentColor" viewBox="0 0 24 24">
            <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Activity } from '../../api/activityService'

interface Props {
  activities: Activity[]
  isLoading: boolean
  error: string | null
}

defineProps<Props>()

const formatDateTime = (date: string) => {
  const d = new Date(date)
  return d.toLocaleDateString('de-DE', { month: 'short', day: 'numeric' }) + ' ' + d.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })
}

const formatTime = (date: string) => {
  return new Date(date).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })
}
</script>
