<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <h1 class="app-page-title">Meine Aktivitaeten</h1>
      <button class="app-btn-success" @click="openCreateForm">
        Neue Aktivitaet erstellen
      </button>
    </div>

    <p v-if="successMessage" class="app-alert-success">
      {{ successMessage }}
    </p>
    <p v-if="errorMessage" class="app-alert-error">
      {{ errorMessage }}
    </p>

    <section class="bg-white rounded-lg shadow p-6 space-y-4">
      <h2 class="app-card-title">Meine Aktivitaeten</h2>
      <div class="overflow-x-auto">
        <table class="app-table">
          <thead>
            <tr>
              <th>Titel</th>
              <th>Datum/Uhrzeit</th>
              <th>Ort</th>
              <th>Beschreibung</th>
              <th class="app-table-col-right">Aktionen</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoadingActivities">
              <td colspan="5" class="app-table-cell-muted">Lade Aktivitaeten...</td>
            </tr>
            <tr v-else-if="activitiesError">
              <td colspan="5" class="app-table-cell-error">{{ activitiesError }}</td>
            </tr>
            <tr v-else-if="acceptedActivities.length === 0">
              <td colspan="5" class="app-table-cell-muted">Keine zugesagten Aktivitaeten vorhanden.</td>
            </tr>
            <tr v-for="activity in acceptedActivities" :key="`accepted-${activity.id}`">
              <td class="app-table-cell-main">{{ activity.title }}</td>
              <td>{{ formatDateTime(activity.startTime) }}</td>
              <td>{{ displayOrDash(activity.location) }}</td>
              <td>{{ shortDescription(activity.description) }}</td>
              <td class="app-table-cell-right">
                <div class="inline-flex items-center gap-2">
                  <button
                    type="button"
                    class="inline-flex h-8 w-8 items-center justify-center rounded-md text-gray-600 transition hover:bg-gray-100 hover:text-blue-700"
                    aria-label="Details ansehen"
                    :disabled="isActionLoading(activity.id)"
                    @click="openDetails(activity)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                      <path d="M10 3c4.204 0 7.663 2.687 8.819 6.433a1.5 1.5 0 0 1 0 .934C17.663 14.113 14.204 16.8 10 16.8S2.337 14.113 1.18 10.367a1.5 1.5 0 0 1 0-.934C2.337 5.687 5.796 3 10 3Zm0 2C6.768 5 4.053 6.97 3.06 9.9c.993 2.93 3.708 4.9 6.94 4.9s5.947-1.97 6.94-4.9C15.947 6.97 13.232 5 10 5Zm0 1.7a3.2 3.2 0 1 1 0 6.4 3.2 3.2 0 0 1 0-6.4Z" />
                    </svg>
                  </button>
                  <button
                    type="button"
                    class="app-btn-danger-sm"
                    :disabled="isActionLoading(activity.id)"
                    @click="respondToActivity(activity.id, 'DECLINED')"
                  >
                    Absagen
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="bg-white rounded-lg shadow p-6 space-y-4">
      <h2 class="app-card-title">Einladungen</h2>
      <div class="overflow-x-auto">
        <table class="app-table">
          <thead>
            <tr>
              <th>Titel</th>
              <th>Datum/Uhrzeit</th>
              <th>Ort</th>
              <th>Beschreibung</th>
              <th class="app-table-col-right">Aktionen</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoadingActivities">
              <td colspan="5" class="app-table-cell-muted">Lade Einladungen...</td>
            </tr>
            <tr v-else-if="activitiesError">
              <td colspan="5" class="app-table-cell-error">{{ activitiesError }}</td>
            </tr>
            <tr v-else-if="pendingActivities.length === 0">
              <td colspan="5" class="app-table-cell-muted">Keine offenen Einladungen vorhanden.</td>
            </tr>
            <tr v-for="activity in pendingActivities" :key="`pending-${activity.id}`">
              <td class="app-table-cell-main">{{ activity.title }}</td>
              <td>{{ formatDateTime(activity.startTime) }}</td>
              <td>{{ displayOrDash(activity.location) }}</td>
              <td>{{ shortDescription(activity.description) }}</td>
              <td class="app-table-cell-right">
                <div class="inline-flex items-center gap-2">
                  <button
                    type="button"
                    class="inline-flex h-8 w-8 items-center justify-center rounded-md text-gray-600 transition hover:bg-gray-100 hover:text-blue-700"
                    aria-label="Details ansehen"
                    :disabled="isActionLoading(activity.id)"
                    @click="openDetails(activity)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                      <path d="M10 3c4.204 0 7.663 2.687 8.819 6.433a1.5 1.5 0 0 1 0 .934C17.663 14.113 14.204 16.8 10 16.8S2.337 14.113 1.18 10.367a1.5 1.5 0 0 1 0-.934C2.337 5.687 5.796 3 10 3Zm0 2C6.768 5 4.053 6.97 3.06 9.9c.993 2.93 3.708 4.9 6.94 4.9s5.947-1.97 6.94-4.9C15.947 6.97 13.232 5 10 5Zm0 1.7a3.2 3.2 0 1 1 0 6.4 3.2 3.2 0 0 1 0-6.4Z" />
                    </svg>
                  </button>
                  <button
                    type="button"
                    class="app-btn-success-sm"
                    :disabled="isActionLoading(activity.id)"
                    @click="respondToActivity(activity.id, 'ACCEPTED')"
                  >
                    Zusagen
                  </button>
                  <button
                    type="button"
                    class="app-btn-danger-sm"
                    :disabled="isActionLoading(activity.id)"
                    @click="respondToActivity(activity.id, 'DECLINED')"
                  >
                    Absagen
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="bg-white rounded-lg shadow p-6 space-y-4">
      <h2 class="app-card-title">Abgelehnte Einladungen</h2>
      <div class="overflow-x-auto">
        <table class="app-table">
          <thead>
            <tr>
              <th>Titel</th>
              <th>Datum/Uhrzeit</th>
              <th>Ort</th>
              <th>Beschreibung</th>
              <th class="app-table-col-right">Aktionen</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoadingActivities">
              <td colspan="5" class="app-table-cell-muted">Lade Einladungen...</td>
            </tr>
            <tr v-else-if="activitiesError">
              <td colspan="5" class="app-table-cell-error">{{ activitiesError }}</td>
            </tr>
            <tr v-else-if="declinedActivities.length === 0">
              <td colspan="5" class="app-table-cell-muted">Keine abgelehnten Einladungen vorhanden.</td>
            </tr>
            <tr v-for="activity in declinedActivities" :key="`declined-${activity.id}`">
              <td class="app-table-cell-main">{{ activity.title }}</td>
              <td>{{ formatDateTime(activity.startTime) }}</td>
              <td>{{ displayOrDash(activity.location) }}</td>
              <td>{{ shortDescription(activity.description) }}</td>
              <td class="app-table-cell-right">
                <div class="inline-flex items-center gap-2">
                  <button
                    type="button"
                    class="inline-flex h-8 w-8 items-center justify-center rounded-md text-gray-600 transition hover:bg-gray-100 hover:text-blue-700"
                    aria-label="Details ansehen"
                    :disabled="isActionLoading(activity.id)"
                    @click="openDetails(activity)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                      <path d="M10 3c4.204 0 7.663 2.687 8.819 6.433a1.5 1.5 0 0 1 0 .934C17.663 14.113 14.204 16.8 10 16.8S2.337 14.113 1.18 10.367a1.5 1.5 0 0 1 0-.934C2.337 5.687 5.796 3 10 3Zm0 2C6.768 5 4.053 6.97 3.06 9.9c.993 2.93 3.708 4.9 6.94 4.9s5.947-1.97 6.94-4.9C15.947 6.97 13.232 5 10 5Zm0 1.7a3.2 3.2 0 1 1 0 6.4 3.2 3.2 0 0 1 0-6.4Z" />
                    </svg>
                  </button>
                  <button
                    type="button"
                    class="app-btn-success-sm"
                    :disabled="isActionLoading(activity.id)"
                    @click="respondToActivity(activity.id, 'ACCEPTED')"
                  >
                    Zusagen
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <div
      v-if="showDetailsModal && selectedActivity"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      @click.self="closeDetails"
    >
      <div class="w-full max-w-xl rounded-lg bg-white p-6 shadow-xl fade-in">
        <div class="mb-5 flex items-start justify-between">
          <h2 class="app-card-title">Aktivitaetsdetails</h2>
          <button
            type="button"
            class="text-xl leading-none text-gray-500 hover:text-gray-700"
            aria-label="Details schliessen"
            @click="closeDetails"
          >
            x
          </button>
        </div>

        <div class="space-y-3 text-sm text-gray-700">
          <p><span class="font-semibold text-gray-900">Titel:</span> {{ selectedActivity.title }}</p>
          <p><span class="font-semibold text-gray-900">Datum/Uhrzeit:</span> {{ formatDateTime(selectedActivity.startTime) }}</p>
          <p><span class="font-semibold text-gray-900">Ort:</span> {{ displayOrDash(selectedActivity.location) }}</p>
          <p><span class="font-semibold text-gray-900">Beschreibung:</span> {{ displayOrDash(selectedActivity.description) }}</p>
        </div>
      </div>
    </div>

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

        <p v-if="createErrorMessage" class="app-alert-error mb-4">
          {{ createErrorMessage }}
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
import { activityService, type Activity } from '../api/activityService'
import { authService } from '../api/authService'
import { groupService } from '../api/groupService'

interface EligibleGroup {
  id: number
  name: string
}

type ParticipationStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED'

const showCreateForm = ref(false)
const isSubmitting = ref(false)
const isLoadingEligibleGroups = ref(false)
const eligibleGroups = ref<EligibleGroup[]>([])
const createErrorMessage = ref('')
const successMessage = ref('')
const errorMessage = ref('')
const currentUserId = ref<number | null>(null)
const allActivities = ref<Activity[]>([])
const activityStatuses = ref<Record<number, ParticipationStatus>>({})
const isLoadingActivities = ref(false)
const activitiesError = ref('')
const actionLoadingById = ref<Record<number, boolean>>({})
const showDetailsModal = ref(false)
const selectedActivity = ref<Activity | null>(null)

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

const normalizeParticipationStatus = (value: unknown): ParticipationStatus => {
  const status = asNonEmptyString(value).toUpperCase()
  if (status === 'ACCEPTED') return 'ACCEPTED'
  if (status === 'DECLINED') return 'DECLINED'
  return 'PENDING'
}

const getOwnStatusForActivity = async (activityId: number): Promise<ParticipationStatus> => {
  if (currentUserId.value === null) return 'PENDING'

  try {
    const participants = await activityService.getParticipants(activityId)
    const ownParticipant = participants.find((participant) => Number(participant.userId) === currentUserId.value)
    return normalizeParticipationStatus(ownParticipant?.status)
  } catch {
    return 'PENDING'
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
  createErrorMessage.value = ''
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

const acceptedActivities = computed(() =>
  allActivities.value.filter((activity) => activityStatuses.value[activity.id] === 'ACCEPTED')
)

const pendingActivities = computed(() =>
  allActivities.value.filter((activity) => activityStatuses.value[activity.id] === 'PENDING')
)

const declinedActivities = computed(() =>
  allActivities.value.filter((activity) => activityStatuses.value[activity.id] === 'DECLINED')
)

const displayOrDash = (value: unknown): string => {
  const normalized = asNonEmptyString(value)
  return normalized || '-'
}

const shortDescription = (value: unknown): string => {
  const description = asNonEmptyString(value)
  if (!description) return '-'
  return description.length > 20 ? `${description.slice(0, 20)}...` : description
}

const loadEligibleGroups = async () => {
  isLoadingEligibleGroups.value = true
  createErrorMessage.value = ''

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
      createErrorMessage.value = 'Nutzerprofil konnte nicht ermittelt werden.'
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
    createErrorMessage.value = 'Gruppen konnten nicht geladen werden.'
  } finally {
    isLoadingEligibleGroups.value = false
  }
}

const openCreateForm = async () => {
  successMessage.value = ''
  errorMessage.value = ''
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

const loadActivities = async () => {
  isLoadingActivities.value = true
  activitiesError.value = ''

  try {
    await loadCurrentUserId()
    const activities = await activityService.getMyActivities()
    const sorted = [...activities].sort((a, b) => {
      const left = new Date(a.startTime).getTime()
      const right = new Date(b.startTime).getTime()
      return left - right
    })

    const statuses = await Promise.all(
      sorted.map(async (activity) => {
        const status = await getOwnStatusForActivity(activity.id)
        return [activity.id, status] as const
      })
    )

    allActivities.value = sorted
    activityStatuses.value = Object.fromEntries(statuses)
  } catch {
    allActivities.value = []
    activityStatuses.value = {}
    activitiesError.value = 'Aktivitaeten konnten nicht geladen werden.'
  } finally {
    isLoadingActivities.value = false
  }
}

const isActionLoading = (activityId: number): boolean => Boolean(actionLoadingById.value[activityId])

const respondToActivity = async (activityId: number, status: ParticipationStatus) => {
  if (!Number.isFinite(activityId)) return
  if (isActionLoading(activityId)) return

  actionLoadingById.value = {
    ...actionLoadingById.value,
    [activityId]: true
  }

  errorMessage.value = ''
  successMessage.value = ''

  try {
    await activityService.respondToActivity(activityId, status)
    activityStatuses.value = {
      ...activityStatuses.value,
      [activityId]: status
    }
    successMessage.value = status === 'ACCEPTED' ? 'Du hast erfolgreich zugesagt.' : 'Du hast erfolgreich abgesagt.'
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      errorMessage.value = backendMessage || 'Antwort zur Aktivitaet konnte nicht gespeichert werden.'
    } else {
      errorMessage.value = 'Antwort zur Aktivitaet konnte nicht gespeichert werden.'
    }
  } finally {
    const nextMap = { ...actionLoadingById.value }
    delete nextMap[activityId]
    actionLoadingById.value = nextMap
  }
}

const openDetails = (activity: Activity) => {
  selectedActivity.value = activity
  showDetailsModal.value = true
}

const closeDetails = () => {
  showDetailsModal.value = false
  selectedActivity.value = null
}

const saveActivity = async () => {
  if (isSubmitting.value) return

  const groupId = Number(form.groupId)
  const title = form.title.trim()
  const date = form.date
  const time = form.time

  if (!Number.isFinite(groupId) || groupId <= 0 || !title || !date || !time) {
    createErrorMessage.value = 'Bitte fuelle alle Pflichtfelder aus.'
    return
  }

  isSubmitting.value = true
  createErrorMessage.value = ''

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
    errorMessage.value = ''
    showCreateForm.value = false
    resetForm()
    await loadActivities()
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      createErrorMessage.value = backendMessage || 'Aktivitaet konnte nicht gespeichert werden.'
    } else {
      createErrorMessage.value = 'Aktivitaet konnte nicht gespeichert werden.'
    }
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  void loadEligibleGroups()
  void loadActivities()
})
</script>
