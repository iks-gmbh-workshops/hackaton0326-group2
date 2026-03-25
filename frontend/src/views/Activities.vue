<template>
  <div class="space-y-6">
    <h1 class="app-page-title">Aktivitaeten</h1>
    <p v-if="successMessage" class="app-alert-success">
      {{ successMessage }}
    </p>

    <div class="grid grid-cols-1 gap-6">
      <div class="bg-white rounded-lg shadow p-8 text-center">
        <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
        </svg>
        <h2 class="app-card-title mb-2">Aktivitaeten-Verwaltung</h2>
        <p class="app-muted-text mb-4">Lege hier neue Aktivitaeten fuer deine Gruppen an.</p>
        <button class="app-btn-success" @click="openCreateForm">
          + Neue Aktivitaet erstellen
        </button>
      </div>
    </div>

    <section class="bg-white rounded-lg shadow p-6 space-y-4">
      <h2 class="app-card-title">Aktivitaeten mit meiner Teilnahme</h2>
      <div class="overflow-x-auto">
        <table class="app-table">
          <thead>
            <tr>
              <th>Titel</th>
              <th>Datum/Uhrzeit</th>
              <th>Ort</th>
              <th>Beschreibung</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoadingParticipantActivities">
              <td colspan="4" class="app-table-cell-muted">Lade Aktivitaeten...</td>
            </tr>
            <tr v-else-if="participantActivitiesError">
              <td colspan="4" class="app-table-cell-error">{{ participantActivitiesError }}</td>
            </tr>
            <tr v-else-if="participantActivities.length === 0">
              <td colspan="4" class="app-table-cell-muted">Keine Aktivitaeten mit Teilnahme vorhanden.</td>
            </tr>
            <tr
              v-for="activity in participantActivities"
              v-else
              :key="activity.id"
            >
              <td class="app-table-cell-main">{{ activity.title }}</td>
              <td>{{ formatDateTime(activity.startTime) }}</td>
              <td>{{ activity.location || '-' }}</td>
              <td>{{ activity.description || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <div
      v-if="showCreateForm"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      @click.self="handleCloseRequest"
    >
      <form
        class="w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-lg bg-white p-6 shadow-xl md:p-8 fade-in"
        @submit.prevent="saveActivity"
      >
        <div class="mb-6 flex items-start justify-between">
          <h2 class="app-card-title">Neue Aktivitaet erstellen</h2>
          <button
            type="button"
            class="text-xl leading-none text-gray-500 hover:text-gray-700"
            aria-label="Editor schliessen"
            :disabled="isSubmitting"
            @click="handleCloseRequest"
          >
            x
          </button>
        </div>

        <p v-if="errorMessage" class="app-alert-error mb-4">
          {{ errorMessage }}
        </p>

        <div class="space-y-5">
          <div>
            <label for="activityGroup" class="mb-1 block text-sm font-medium text-gray-700">
              Gruppe <span class="text-red-600">*</span>
            </label>
            <select
              id="activityGroup"
              v-model="form.groupId"
              :disabled="isSubmitting || isLoadingEligibleGroups"
              required
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="" disabled>
                {{ isLoadingEligibleGroups ? 'Gruppen werden geladen...' : 'Bitte Gruppe waehlen' }}
              </option>
              <option
                v-for="group in eligibleGroups"
                :key="group.id"
                :value="group.id"
              >
                {{ group.name }}
              </option>
            </select>
            <p v-if="!isLoadingEligibleGroups && eligibleGroups.length === 0" class="mt-2 text-sm text-red-600">
              Du bist in keiner Gruppe als Verwalter hinterlegt.
            </p>
          </div>

          <div>
            <label for="activityTitle" class="mb-1 block text-sm font-medium text-gray-700">
              Titel <span class="text-red-600">*</span>
            </label>
            <input
              id="activityTitle"
              v-model.trim="form.title"
              type="text"
              required
              :disabled="isSubmitting"
              placeholder="z. B. Spieleabend im Park"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label for="activityDescription" class="mb-1 block text-sm font-medium text-gray-700">
              Beschreibung
            </label>
            <textarea
              id="activityDescription"
              v-model.trim="form.description"
              rows="3"
              :disabled="isSubmitting"
              placeholder="Kurze Beschreibung der Aktivitaet"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div class="grid grid-cols-1 gap-5 md:grid-cols-2">
            <div>
              <label for="activityDate" class="mb-1 block text-sm font-medium text-gray-700">
                Datum <span class="text-red-600">*</span>
              </label>
              <input
                id="activityDate"
                v-model="form.date"
                type="date"
                required
                :disabled="isSubmitting"
                class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label for="activityTime" class="mb-1 block text-sm font-medium text-gray-700">
                Uhrzeit <span class="text-red-600">*</span>
              </label>
              <input
                id="activityTime"
                v-model="form.time"
                type="time"
                required
                :disabled="isSubmitting"
                class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div>
            <label for="activityLocation" class="mb-1 block text-sm font-medium text-gray-700">
              Ort
            </label>
            <input
              id="activityLocation"
              v-model.trim="form.location"
              type="text"
              :disabled="isSubmitting"
              placeholder="z. B. Stadtpark, Berlin"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        <div class="mt-8 border-t border-gray-200 pt-5">
          <button
            type="submit"
            class="app-btn-primary w-full md:w-auto"
            :disabled="isSubmitting || isLoadingEligibleGroups || eligibleGroups.length === 0"
            :class="{ 'cursor-not-allowed opacity-70': isSubmitting || isLoadingEligibleGroups || eligibleGroups.length === 0 }"
          >
            {{ isSubmitting ? 'Aktivitaet wird gespeichert...' : 'Aktivitaet speichern' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, reactive, ref } from 'vue'
import { activityService } from '../api/activityService'
import { authService } from '../api/authService'
import { groupService } from '../api/groupService'

interface EligibleGroup {
  id: number
  name: string
}

const showCreateForm = ref(false)
const isSubmitting = ref(false)
const isLoadingEligibleGroups = ref(false)
const eligibleGroups = ref<EligibleGroup[]>([])
const errorMessage = ref('')
const successMessage = ref('')
const currentUserId = ref<number | null>(null)
const participantActivities = ref<Awaited<ReturnType<typeof activityService.getMyActivities>>>([])
const isLoadingParticipantActivities = ref(false)
const participantActivitiesError = ref('')

const form = reactive({
  groupId: '',
  title: '',
  description: '',
  date: '',
  time: '',
  location: ''
})

const asRecord = (value: unknown): Record<string, unknown> =>
  value !== null && typeof value === 'object' ? (value as Record<string, unknown>) : {}

const asNonEmptyString = (value: unknown): string =>
  typeof value === 'string' ? value.trim() : ''

const parseJwtPayload = (token: string): Record<string, unknown> | null => {
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const normalized = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
    return JSON.parse(atob(normalized)) as Record<string, unknown>
  } catch {
    return null
  }
}

const isGlobalAdmin = computed(() => {
  const localRole = asNonEmptyString(localStorage.getItem('role'))
  if (localRole.toUpperCase() === 'ADMIN') return true

  const token = localStorage.getItem('auth_token')
  if (!token) return false

  const payload = parseJwtPayload(token)
  if (!payload) return false

  const role = asNonEmptyString(payload.role)
  if (role.toUpperCase() === 'ADMIN') return true

  const roles = Array.isArray(payload.roles) ? payload.roles : []
  if (roles.some((entry) => asNonEmptyString(entry).toUpperCase() === 'ADMIN')) return true

  const authorities = Array.isArray(payload.authorities) ? payload.authorities : []
  return authorities.some((entry) => asNonEmptyString(entry).toUpperCase() === 'ROLE_ADMIN')
})

const resolveGroupId = (group: unknown): number | null => {
  const record = asRecord(group)
  const candidate = record.id ?? record.groupId
  const parsed = Number(candidate)
  return Number.isFinite(parsed) ? parsed : null
}

const resolveGroupName = (group: unknown): string => {
  const record = asRecord(group)
  return asNonEmptyString(record.name) || asNonEmptyString(record.groupName) || 'Unbenannte Gruppe'
}

const isManagerFromMembers = async (groupId: number): Promise<boolean> => {
  if (currentUserId.value === null) return false

  try {
    const members = await groupService.getGroupMembers(groupId)
    return members.some((member) => {
      const entry = asRecord(member)
      const userId = Number(entry.userId)
      const role = asNonEmptyString(entry.role).toUpperCase()
      const status = asNonEmptyString(entry.status).toUpperCase()
      return Number.isFinite(userId) && userId === currentUserId.value && role === 'VERWALTER' && status === 'ACTIVE'
    })
  } catch {
    return false
  }
}

const loadCurrentUserId = async (): Promise<void> => {
  if (currentUserId.value !== null) return
  try {
    const profile = await authService.getMyProfile()
    const id = Number((profile as { id?: number | string }).id)
    currentUserId.value = Number.isFinite(id) ? id : null
  } catch {
    currentUserId.value = null
  }
}

const resetForm = () => {
  form.groupId = ''
  form.title = ''
  form.description = ''
  form.date = ''
  form.time = ''
  form.location = ''
  errorMessage.value = ''
}

const hasUnsavedInput = computed(() =>
  Boolean(
    form.groupId !== '' ||
      form.title.trim() ||
      form.description.trim() ||
      form.date ||
      form.time ||
      form.location.trim()
  )
)

const loadEligibleGroups = async () => {
  isLoadingEligibleGroups.value = true
  errorMessage.value = ''

  try {
    const allGroups = await groupService.getMyGroups()
    if (isGlobalAdmin.value) {
      eligibleGroups.value = allGroups
        .map((group) => ({ id: resolveGroupId(group), name: resolveGroupName(group) }))
        .filter((group): group is EligibleGroup => group.id !== null)
      return
    }

    await loadCurrentUserId()
    if (currentUserId.value === null) {
      eligibleGroups.value = []
      errorMessage.value = 'Nutzerprofil konnte nicht ermittelt werden.'
      return
    }

    const eligible = await Promise.all(
      allGroups.map(async (group) => {
        const groupId = resolveGroupId(group)
        if (groupId === null) return null

        const memberIsManager = await isManagerFromMembers(groupId)
        if (!memberIsManager) return null

        return { id: groupId, name: resolveGroupName(group) }
      })
    )

    eligibleGroups.value = eligible.filter((group): group is EligibleGroup => group !== null)
  } catch {
    eligibleGroups.value = []
    errorMessage.value = 'Gruppen konnten nicht geladen werden.'
  } finally {
    isLoadingEligibleGroups.value = false
  }
}

const openCreateForm = async () => {
  successMessage.value = ''
  resetForm()
  showCreateForm.value = true
  await loadEligibleGroups()
}

const handleCloseRequest = () => {
  if (isSubmitting.value) return

  if (hasUnsavedInput.value) {
    const shouldDiscard = window.confirm('Moechtest du den Editor wirklich schliessen? Die Aktivitaet wird verworfen.')
    if (!shouldDiscard) return
  }

  showCreateForm.value = false
  resetForm()
}

const buildLocalDateTime = (date: string, time: string): string => `${date}T${time}:00`

const formatDateTime = (value: string): string => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('de-DE', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadParticipantActivities = async () => {
  isLoadingParticipantActivities.value = true
  participantActivitiesError.value = ''

  try {
    const activities = await activityService.getMyActivities()
    participantActivities.value = [...activities].sort((a, b) => {
      const left = new Date(a.startTime).getTime()
      const right = new Date(b.startTime).getTime()
      return left - right
    })
  } catch {
    participantActivities.value = []
    participantActivitiesError.value = 'Aktivitaeten konnten nicht geladen werden.'
  } finally {
    isLoadingParticipantActivities.value = false
  }
}

const saveActivity = async () => {
  if (isSubmitting.value) return

  const groupId = Number(form.groupId)
  const title = form.title.trim()
  const date = form.date
  const time = form.time

  if (!Number.isFinite(groupId) || groupId <= 0 || !title || !date || !time) {
    errorMessage.value = 'Bitte fuelle alle Pflichtfelder aus.'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const startTime = buildLocalDateTime(date, time)
    await activityService.createActivity({
      title,
      description: form.description.trim() || undefined,
      location: form.location.trim() || undefined,
      startTime,
      endTime: startTime,
      groupId,
      groupIds: [groupId]
    })

    successMessage.value = 'Aktivitaet wurde erfolgreich gespeichert.'
    showCreateForm.value = false
    resetForm()
    await loadParticipantActivities()
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      errorMessage.value = backendMessage || 'Aktivitaet konnte nicht gespeichert werden.'
    } else {
      errorMessage.value = 'Aktivitaet konnte nicht gespeichert werden.'
    }
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  void loadEligibleGroups()
  void loadParticipantActivities()
})
</script>
