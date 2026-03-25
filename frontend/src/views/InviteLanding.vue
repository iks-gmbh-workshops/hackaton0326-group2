<template>
  <div class="mx-auto max-w-2xl rounded-xl border border-gray-200 bg-white p-8 shadow-sm">
    <h1 class="app-section-title mb-6">Gruppeneinladung</h1>

    <p class="text-base text-gray-800">
      Moechten Sie der Gruppe <span class="font-semibold">{{ groupName }}</span> beitreten?
    </p>

    <p v-if="infoMessage" class="app-alert-success mt-4">
      {{ infoMessage }}
    </p>
    <p v-if="errorMessage" class="app-alert-error mt-4">
      {{ errorMessage }}
    </p>

    <div class="mt-8 flex flex-col gap-3 sm:flex-row">
      <button
        type="button"
        :disabled="isSubmitting"
        class="app-btn-primary w-full sm:w-auto"
        :class="{ 'cursor-not-allowed opacity-70': isSubmitting }"
        @click="acceptInvitation"
      >
        Einladung annehmen
      </button>
      <button
        type="button"
        :disabled="isSubmitting"
        class="w-full rounded-lg border border-gray-300 bg-white px-4 py-2 font-medium text-gray-700 transition hover:bg-gray-50 sm:w-auto"
        :class="{ 'cursor-not-allowed opacity-70': isSubmitting }"
        @click="declineInvitation"
      >
        Einladung ablehnen
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { groupService } from '../api/groupService'

const route = useRoute()
const router = useRouter()
const isSubmitting = ref(false)
const infoMessage = ref('')
const errorMessage = ref('')
const defaultGroupName = 'dieser Gruppe'

const inviteToken = computed<string | null>(() => {
  const token = String(route.params.token ?? '').trim()
  return token ? token : null
})

const groupName = computed(() => {
  const raw = route.query.groupName
  if (typeof raw !== 'string') return defaultGroupName
  const value = raw.trim()
  return value ? value : defaultGroupName
})

const acceptInvitation = async () => {
  const token = inviteToken.value
  if (!token || isSubmitting.value) {
    errorMessage.value = 'Ungueltiger Einladungslink.'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''
  infoMessage.value = ''

  try {
    await groupService.joinByInviteToken(token)
    infoMessage.value = 'Einladung wurde angenommen.'
    await router.push('/')
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      errorMessage.value = backendMessage || 'Einladung konnte nicht angenommen werden.'
    } else {
      errorMessage.value = 'Einladung konnte nicht angenommen werden.'
    }
  } finally {
    isSubmitting.value = false
  }
}

const declineInvitation = async () => {
  if (isSubmitting.value) {
    errorMessage.value = 'Ungueltiger Einladungslink.'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''
  infoMessage.value = ''

  try {
    // No backend decline-by-token endpoint exists; declining simply ignores the invite.
    infoMessage.value = 'Einladung wurde abgelehnt.'
    await router.push('/')
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      errorMessage.value = backendMessage || 'Einladung konnte nicht abgelehnt werden.'
    } else {
      errorMessage.value = 'Einladung konnte nicht abgelehnt werden.'
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>
