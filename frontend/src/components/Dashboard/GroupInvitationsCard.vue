<template>
  <div>
    <!-- Loading State -->
    <div v-if="isLoading" class="flex items-center justify-center py-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-yellow-600"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <p class="text-red-800">{{ error }}</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="invitations.length === 0" class="text-center py-8">
      <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/>
      </svg>
      <p class="text-gray-500">Keine Einladungen vorhanden</p>
    </div>

    <!-- Invitations List -->
    <div v-else class="space-y-3">
      <div 
        v-for="invitation in invitations" 
        :key="invitation.id"
        class="p-4 border border-yellow-200 bg-yellow-50 rounded-lg hover:bg-yellow-100 transition"
      >
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1">
            <h3 class="font-semibold text-gray-900">{{ invitation.groupName }}</h3>
            <p class="text-sm text-gray-600 mt-1">Eingeladen von: <span class="font-medium">{{ invitation.invitedBy }}</span></p>
            <p class="text-xs text-gray-500 mt-2">{{ formatDate(invitation.invitedAt) }}</p>
            <span v-if="invitation.status === 'PENDING'" class="inline-block mt-2 px-2 py-1 bg-yellow-200 text-yellow-800 text-xs font-semibold rounded">
              Ausstehend
            </span>
          </div>
          <div class="flex gap-2 flex-shrink-0">
            <button
              @click="$emit('accept', invitation.id)"
              class="px-3 py-2 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700 transition whitespace-nowrap"
            >
              Akzeptieren
            </button>
            <button
              @click="$emit('decline', invitation.id)"
              class="px-3 py-2 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 transition whitespace-nowrap"
            >
              Ablehnen
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { GroupInvitation } from '../../api/groupService'

interface Props {
  invitations: GroupInvitation[]
  isLoading: boolean
  error: string | null
}

defineProps<Props>()

defineEmits<{
  accept: [invitationId: number]
  decline: [invitationId: number]
}>()

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('de-DE', { year: 'numeric', month: 'short', day: 'numeric' })
}
</script>
