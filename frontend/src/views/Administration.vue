<template>
  <div class="space-y-6">
    <h1 class="app-page-title">Administration</h1>

    <p v-if="successMessage" class="app-alert-success">
      {{ successMessage }}
    </p>
    <p v-if="errorMessage" class="app-alert-error">
      {{ errorMessage }}
    </p>

    <!-- User Section -->
    <section class="rounded-lg bg-white p-6 shadow space-y-4">
      <h2 class="app-card-title">Liste an User</h2>
      <div class="overflow-x-auto">
        <table class="app-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>E-Mail</th>
              <th>Rolle</th>
              <th class="app-table-col-right">Aktion</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoadingUsers">
              <td colspan="5" class="app-table-cell-muted">Lade User...</td>
            </tr>
            <tr v-else-if="users.length === 0">
              <td colspan="5" class="app-table-cell-muted">Keine User vorhanden.</td>
            </tr>
            <tr v-for="user in users" v-else :key="`user-${user.id}`">
              <td class="app-table-cell-main">{{ user.id }}</td>
              <td>{{ user.displayName }}</td>
              <td>{{ user.email }}</td>
              <td>
                <select
                  :value="user.role"
                  :disabled="isChangingRole[user.id] || isDeletingUser[user.id]"
                  class="rounded-md border border-gray-300 px-2 py-1 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  @change="changeUserRole(user.id, String(($event.target as HTMLSelectElement).value))"
                >
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </td>
              <td class="app-table-cell-right">
                <div class="inline-flex items-center gap-1">
                  <button
                    type="button"
                    :title="'User loeschen'"
                    aria-label="User loeschen"
                    :disabled="isDeletingUser[user.id] || isChangingRole[user.id]"
                    class="inline-flex h-8 w-8 items-center justify-center rounded-md text-red-600 transition hover:bg-red-50 hover:text-red-700 disabled:cursor-not-allowed disabled:opacity-40"
                    @click="deleteUser(user)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                      <path fill-rule="evenodd" d="M8.75 2.5a1.5 1.5 0 0 0-1.5 1.5V5H5.5a.75.75 0 0 0 0 1.5h.286l.603 8.243A2 2 0 0 0 8.384 16.6h3.232a2 2 0 0 0 1.995-1.857l.603-8.243h.286a.75.75 0 0 0 0-1.5h-1.75V4a1.5 1.5 0 0 0-1.5-1.5h-2.5Zm2.5 2.5V4h-2.5v1h2.5Zm-2 3a.75.75 0 0 0-1.5 0v5a.75.75 0 0 0 1.5 0V8Zm3.5-.75a.75.75 0 0 1 .75.75v5a.75.75 0 0 1-1.5 0V8a.75.75 0 0 1 .75-.75Z" clip-rule="evenodd" />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Groups Section -->
    <section class="rounded-lg bg-white p-6 shadow space-y-4">
      <h2 class="app-card-title">Liste an Gruppen</h2>
      <div class="overflow-x-auto">
        <table class="app-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Beschreibung</th>
              <th>Erstellt von</th>
              <th>Mitglieder</th>
              <th class="app-table-col-right">Aktion</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoadingGroups">
              <td colspan="6" class="app-table-cell-muted">Lade Gruppen...</td>
            </tr>
            <tr v-else-if="groups.length === 0">
              <td colspan="6" class="app-table-cell-muted">Keine Gruppen vorhanden.</td>
            </tr>
            <tr v-for="group in groups" v-else :key="`group-${group.id}`">
              <td class="app-table-cell-main">{{ group.id }}</td>
              <td>{{ group.name }}</td>
              <td>{{ group.description }}</td>
              <td>{{ group.createdBy }}</td>
              <td>{{ group.memberCount }}</td>
              <td class="app-table-cell-right">
                <div class="inline-flex items-center gap-1">
                  <button
                    type="button"
                    :title="'Gruppe loeschen'"
                    aria-label="Gruppe loeschen"
                    :disabled="isDeletingGroup[group.id]"
                    class="inline-flex h-8 w-8 items-center justify-center rounded-md text-red-600 transition hover:bg-red-50 hover:text-red-700 disabled:cursor-not-allowed disabled:opacity-40"
                    @click="deleteGroup(group)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                      <path fill-rule="evenodd" d="M8.75 2.5a1.5 1.5 0 0 0-1.5 1.5V5H5.5a.75.75 0 0 0 0 1.5h.286l.603 8.243A2 2 0 0 0 8.384 16.6h3.232a2 2 0 0 0 1.995-1.857l.603-8.243h.286a.75.75 0 0 0 0-1.5h-1.75V4a1.5 1.5 0 0 0-1.5-1.5h-2.5Zm2.5 2.5V4h-2.5v1h2.5Zm-2 3a.75.75 0 0 0-1.5 0v5a.75.75 0 0 0 1.5 0V8Zm3.5-.75a.75.75 0 0 1 .75.75v5a.75.75 0 0 1-1.5 0V8a.75.75 0 0 1 .75-.75Z" clip-rule="evenodd" />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Activities Section -->
    <section class="rounded-lg bg-white p-6 shadow space-y-4">
      <h2 class="app-card-title">Liste an Aktivitaeten</h2>
      <div class="overflow-x-auto">
        <table class="app-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Titel</th>
              <th>Beschreibung</th>
              <th>Ort</th>
              <th>Datum/Uhrzeit</th>
              <th>Erstellt von</th>
              <th>Teilnehmer</th>
              <th class="app-table-col-right">Aktion</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoadingActivities">
              <td colspan="8" class="app-table-cell-muted">Lade Aktivitaeten...</td>
            </tr>
            <tr v-else-if="activities.length === 0">
              <td colspan="8" class="app-table-cell-muted">Keine Aktivitaeten vorhanden.</td>
            </tr>
            <tr v-for="activity in activities" v-else :key="`activity-${activity.id}`">
              <td class="app-table-cell-main">{{ activity.id }}</td>
              <td>{{ activity.title }}</td>
              <td>{{ activity.description }}</td>
              <td>{{ activity.location }}</td>
              <td>{{ activity.startTimeLabel }}</td>
              <td>{{ activity.createdByName }}</td>
              <td>{{ activity.participantCount }}</td>
              <td class="app-table-cell-right">
                <div class="inline-flex items-center gap-1">
                  <button
                    type="button"
                    :title="'Aktivitaet loeschen'"
                    aria-label="Aktivitaet loeschen"
                    :disabled="isDeletingActivity[activity.id]"
                    class="inline-flex h-8 w-8 items-center justify-center rounded-md text-red-600 transition hover:bg-red-50 hover:text-red-700 disabled:cursor-not-allowed disabled:opacity-40"
                    @click="deleteActivity(activity)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                      <path fill-rule="evenodd" d="M8.75 2.5a1.5 1.5 0 0 0-1.5 1.5V5H5.5a.75.75 0 0 0 0 1.5h.286l.603 8.243A2 2 0 0 0 8.384 16.6h3.232a2 2 0 0 0 1.995-1.857l.603-8.243h.286a.75.75 0 0 0 0-1.5h-1.75V4a1.5 1.5 0 0 0-1.5-1.5h-2.5Zm2.5 2.5V4h-2.5v1h2.5Zm-2 3a.75.75 0 0 0-1.5 0v5a.75.75 0 0 0 1.5 0V8Zm3.5-.75a.75.75 0 0 1 .75.75v5a.75.75 0 0 1-1.5 0V8a.75.75 0 0 1 .75-.75Z" clip-rule="evenodd" />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { adminService } from '../api/adminService'

interface AdminUserRow {
  id: number
  displayName: string
  email: string
  role: string
}

interface AdminGroupRow {
  id: number
  name: string
  description: string
  createdBy: string
  memberCount: number | string
}

interface AdminActivityRow {
  id: number
  title: string
  description: string
  location: string
  startTimeLabel: string
  createdByName: string
  participantCount: number | string
}

const users = ref<AdminUserRow[]>([])
const groups = ref<AdminGroupRow[]>([])
const activities = ref<AdminActivityRow[]>([])
const isLoadingUsers = ref(false)
const isLoadingGroups = ref(false)
const isLoadingActivities = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const isDeletingUser = ref<Record<number, boolean>>({})
const isChangingRole = ref<Record<number, boolean>>({})
const isDeletingGroup = ref<Record<number, boolean>>({})
const isDeletingActivity = ref<Record<number, boolean>>({})

const asRecord = (value: unknown): Record<string, unknown> =>
  value !== null && typeof value === 'object' ? (value as Record<string, unknown>) : {}

const asNonEmptyString = (value: unknown): string =>
  typeof value === 'string' ? value.trim() : ''

const toFiniteNumber = (value: unknown): number | null => {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

const formatDateTime = (value: unknown): string => {
  if (typeof value !== 'string' || !value.trim()) return '-'
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

const extractBackendMessage = (error: unknown, fallback: string): string => {
  if (axios.isAxiosError(error)) {
    const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
    return backendMessage || fallback
  }
  return fallback
}

const normalizeUsers = (payload: unknown[]): AdminUserRow[] =>
  payload
    .map((entry) => {
      const row = asRecord(entry)
      const id = toFiniteNumber(row.id ?? row.userId)
      if (id === null) return null

      return {
        id,
        displayName:
          asNonEmptyString(row.displayName) ||
          asNonEmptyString(row.username) ||
          asNonEmptyString(row.name) ||
          '-',
        email: asNonEmptyString(row.email) || '-',
        role: asNonEmptyString(row.role).toUpperCase() || '-'
      } as AdminUserRow
    })
    .filter((entry): entry is AdminUserRow => entry !== null)

const normalizeGroups = (payload: unknown[]): AdminGroupRow[] =>
  payload
    .map((entry) => {
      const row = asRecord(entry)
      const id = toFiniteNumber(row.id ?? row.groupId)
      if (id === null) return null

      const memberCount = toFiniteNumber(row.memberCount ?? row.membersCount)
      return {
        id,
        name: asNonEmptyString(row.name) || '-',
        description: asNonEmptyString(row.description) || '-',
        createdBy:
          asNonEmptyString(row.createdBy) ||
          asNonEmptyString(row.ownerName) ||
          asNonEmptyString(row.createdByName) ||
          '-',
        memberCount: memberCount ?? '-'
      } as AdminGroupRow
    })
    .filter((entry): entry is AdminGroupRow => entry !== null)

const normalizeActivities = (payload: unknown[]): AdminActivityRow[] =>
  payload
    .map((entry) => {
      const row = asRecord(entry)
      const id = toFiniteNumber(row.id ?? row.activityId)
      if (id === null) return null

      const participantCount = toFiniteNumber(row.participantCount)
      return {
        id,
        title: asNonEmptyString(row.title) || '-',
        description: asNonEmptyString(row.description) || '-',
        location: asNonEmptyString(row.location) || '-',
        startTimeLabel: formatDateTime(row.startTime),
        createdByName: asNonEmptyString(row.createdByName) || '-',
        participantCount: participantCount ?? '-'
      } as AdminActivityRow
    })
    .filter((entry): entry is AdminActivityRow => entry !== null)

const loadUsers = async () => {
  isLoadingUsers.value = true
  try {
    const data = await adminService.getUsers()
    users.value = normalizeUsers(data)
  } catch (error) {
    users.value = []
    errorMessage.value = extractBackendMessage(error, 'User konnten nicht geladen werden.')
  } finally {
    isLoadingUsers.value = false
  }
}

const loadGroups = async () => {
  isLoadingGroups.value = true
  try {
    const data = await adminService.getGroups()
    groups.value = normalizeGroups(data)
  } catch (error) {
    groups.value = []
    if (!errorMessage.value) {
      errorMessage.value = extractBackendMessage(error, 'Gruppen konnten nicht geladen werden.')
    }
  } finally {
    isLoadingGroups.value = false
  }
}

const loadActivities = async () => {
  isLoadingActivities.value = true
  try {
    const data = await adminService.getActivities()
    activities.value = normalizeActivities(data)
  } catch (error) {
    activities.value = []
    if (!errorMessage.value) {
      errorMessage.value = extractBackendMessage(error, 'Aktivitaeten konnten nicht geladen werden.')
    }
  } finally {
    isLoadingActivities.value = false
  }
}

const changeUserRole = async (userId: number, newRole: string) => {
  if (isChangingRole.value[userId]) return

  const normalized = newRole.toUpperCase()
  if (normalized !== 'USER' && normalized !== 'ADMIN') return

  isChangingRole.value = { ...isChangingRole.value, [userId]: true }
  successMessage.value = ''
  errorMessage.value = ''

  try {
    await adminService.changeUserRole(userId, { role: normalized })
    const user = users.value.find((u) => u.id === userId)
    if (user) user.role = normalized
    successMessage.value = `Rolle von User ${userId} wurde zu ${normalized} geaendert.`
  } catch (error) {
    errorMessage.value = extractBackendMessage(error, 'Rolle konnte nicht geaendert werden.')
    await loadUsers()
  } finally {
    const next = { ...isChangingRole.value }
    delete next[userId]
    isChangingRole.value = next
  }
}

const deleteUser = async (user: AdminUserRow) => {
  if (isDeletingUser.value[user.id]) return

  const confirmed = window.confirm(
    `Moechten Sie den User "${user.displayName}" (ID: ${user.id}) wirklich loeschen? Diese Aktion kann nicht rueckgaengig gemacht werden.`
  )
  if (!confirmed) return

  isDeletingUser.value = { ...isDeletingUser.value, [user.id]: true }
  successMessage.value = ''
  errorMessage.value = ''

  try {
    await adminService.deleteUser(user.id)
    users.value = users.value.filter((u) => u.id !== user.id)
    successMessage.value = `User "${user.displayName}" wurde erfolgreich geloescht.`
  } catch (error) {
    errorMessage.value = extractBackendMessage(error, 'User konnte nicht geloescht werden.')
  } finally {
    const next = { ...isDeletingUser.value }
    delete next[user.id]
    isDeletingUser.value = next
  }
}

const deleteGroup = async (group: AdminGroupRow) => {
  if (isDeletingGroup.value[group.id]) return

  const confirmed = window.confirm(
    `Moechten Sie die Gruppe "${group.name}" (ID: ${group.id}) wirklich loeschen? Diese Aktion kann nicht rueckgaengig gemacht werden.`
  )
  if (!confirmed) return

  isDeletingGroup.value = { ...isDeletingGroup.value, [group.id]: true }
  successMessage.value = ''
  errorMessage.value = ''

  try {
    await adminService.deleteGroup(group.id)
    groups.value = groups.value.filter((g) => g.id !== group.id)
    successMessage.value = `Gruppe "${group.name}" wurde erfolgreich geloescht.`
  } catch (error) {
    errorMessage.value = extractBackendMessage(error, 'Gruppe konnte nicht geloescht werden.')
  } finally {
    const next = { ...isDeletingGroup.value }
    delete next[group.id]
    isDeletingGroup.value = next
  }
}

const deleteActivity = async (activity: AdminActivityRow) => {
  if (isDeletingActivity.value[activity.id]) return

  const confirmed = window.confirm(
    `Moechten Sie die Aktivitaet "${activity.title}" (ID: ${activity.id}) wirklich loeschen? Diese Aktion kann nicht rueckgaengig gemacht werden.`
  )
  if (!confirmed) return

  isDeletingActivity.value = { ...isDeletingActivity.value, [activity.id]: true }
  successMessage.value = ''
  errorMessage.value = ''

  try {
    await adminService.deleteActivity(activity.id)
    activities.value = activities.value.filter((a) => a.id !== activity.id)
    successMessage.value = `Aktivitaet "${activity.title}" wurde erfolgreich geloescht.`
  } catch (error) {
    errorMessage.value = extractBackendMessage(error, 'Aktivitaet konnte nicht geloescht werden.')
  } finally {
    const next = { ...isDeletingActivity.value }
    delete next[activity.id]
    isDeletingActivity.value = next
  }
}

onMounted(() => {
  void Promise.all([loadUsers(), loadGroups(), loadActivities()])
})
</script>
