<template>
  <div>
    <!-- Loading State -->
    <div v-if="isLoading" class="flex items-center justify-center py-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-orange-600"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <p class="text-red-800">{{ error }}</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="activities.length === 0" class="text-center py-8">
      <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
      </svg>
      <p class="text-gray-500">Alle Aktivitäten sind beantwortet</p>
    </div>

    <!-- Activities List -->
    <div v-else class="space-y-3">
      <div 
        v-for="activity in activities" 
        :key="activity.id"
        class="p-4 border border-orange-200 bg-orange-50 rounded-lg hover:bg-orange-100 transition"
      >
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1">
            <h3 class="font-semibold text-gray-900">{{ activity.activityTitle }}</h3>
            <div class="flex flex-col gap-2 mt-3 text-xs text-gray-600">
              <span class="flex items-center gap-2">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/></svg>
                {{ formatDateTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}
              </span>
            </div>
            <span class="inline-block mt-2 px-2 py-1 bg-orange-200 text-orange-800 text-xs font-semibold rounded">
              PENDING
            </span>
          </div>
          <div class="flex gap-2 flex-shrink-0">
            <button
              @click="$emit('accept', activity.activityId)"
              class="px-3 py-2 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700 transition whitespace-nowrap flex items-center gap-1"
            >
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/></svg>
              Zusagen
            </button>
            <button
              @click="$emit('decline', activity.activityId)"
              class="px-3 py-2 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 transition whitespace-nowrap flex items-center gap-1"
            >
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/></svg>
              Absagen
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ActivityParticipation } from '../../api/activityService'

interface Props {
  activities: ActivityParticipation[]
  isLoading: boolean
  error: string | null
}

defineProps<Props>()

defineEmits<{
  accept: [activityId: number]
  decline: [activityId: number]
}>()

const formatDateTime = (date: string) => {
  const d = new Date(date)
  return d.toLocaleDateString('de-DE', { month: 'short', day: 'numeric' }) + ' ' + d.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })
}

const formatTime = (date: string) => {
  return new Date(date).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })
}
</script>
