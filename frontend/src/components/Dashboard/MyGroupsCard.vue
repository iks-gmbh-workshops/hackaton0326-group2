<template>
  <div>
    <!-- Loading State -->
    <div v-if="isLoading" class="flex items-center justify-center py-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <p class="text-red-800">{{ error }}</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="groups.length === 0" class="text-center py-8">
      <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.856-1.488M15 10a3 3 0 11-6 0 3 3 0 016 0zM6 20a9 9 0 0118 0v2H6v-2z"/>
      </svg>
      <p class="text-gray-500">Noch keine Gruppen vorhanden</p>
      <button class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
        + Neue Gruppe erstellen
      </button>
    </div>

    <!-- Groups List -->
    <div v-else class="space-y-3">
      <div 
        v-for="group in groups" 
        :key="group.id"
        class="p-4 border border-gray-200 rounded-lg hover:shadow-md transition cursor-pointer group"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <h3 class="font-semibold text-gray-900 group-hover:text-blue-600 transition">{{ group.name }}</h3>
            <p class="text-sm text-gray-600 mt-1 line-clamp-2">{{ group.description }}</p>
            <div class="flex items-center gap-4 mt-3 text-xs text-gray-500">
              <span v-if="group.memberCount" class="flex items-center gap-1">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/></svg>
                {{ group.memberCount }} Mitglieder
              </span>
              <span>Erstellt: {{ formatDate(group.createdAt) }}</span>
            </div>
          </div>
          <svg class="w-5 h-5 text-gray-400 group-hover:text-gray-600 transition" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Group } from '../../api/groupService'

interface Props {
  groups: Group[]
  isLoading: boolean
  error: string | null
}

defineProps<Props>()

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('de-DE', { year: 'numeric', month: 'short', day: 'numeric' })
}
</script>
