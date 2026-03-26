<template>
  <div class="space-y-6">
    <h1 class="app-page-title">Administration</h1>

    <p v-if="errorMessage" class="app-alert-error">
      {{ errorMessage }}
    </p>

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
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoadingUsers">
              <td colspan="4" class="app-table-cell-muted">Lade User...</td>
            </tr>
            <tr v-else-if="users.length === 0">
              <td colspan="4" class="app-table-cell-muted">Keine User vorhanden.</td>
            </tr>
            <tr v-for="user in users" v-else :key="`user-${user.id}`">
              <td class="app-table-cell-main">{{ user.id }}</td>
              <td>{{ user.displayName }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.role }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

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
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoadingGroups">
              <td colspan="5" class="app-table-cell-muted">Lade Gruppen...</td>
            </tr>
            <tr v-else-if="groups.length === 0">
              <td colspan="5" class="app-table-cell-muted">Keine Gruppen vorhanden.</td>
            </tr>
            <tr v-for="group in groups" v-else :key="`group-${group.id}`">
              <td class="app-table-cell-main">{{ group.id }}</td>
              <td>{{ group.name }}</td>
              <td>{{ group.description }}</td>
              <td>{{ group.createdBy }}</td>
              <td>{{ group.memberCount }}</td>
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

const users = ref<AdminUserRow[]>([])
const groups = ref<AdminGroupRow[]>([])
const isLoadingUsers = ref(false)
const isLoadingGroups = ref(false)
const errorMessage = ref('')

const asRecord = (value: unknown): Record<string, unknown> =>
  value !== null && typeof value === 'object' ? (value as Record<string, unknown>) : {}

const asNonEmptyString = (value: unknown): string =>
  typeof value === 'string' ? value.trim() : ''

const toFiniteNumber = (value: unknown): number | null => {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
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

const loadUsers = async () => {
  isLoadingUsers.value = true
  try {
    const data = await adminService.getUsers()
    users.value = normalizeUsers(data)
  } catch (error) {
    users.value = []
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      errorMessage.value = backendMessage || 'User konnten nicht geladen werden.'
    } else {
      errorMessage.value = 'User konnten nicht geladen werden.'
    }
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
      if (axios.isAxiosError(error)) {
        const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
        errorMessage.value = backendMessage || 'Gruppen konnten nicht geladen werden.'
      } else {
        errorMessage.value = 'Gruppen konnten nicht geladen werden.'
      }
    }
  } finally {
    isLoadingGroups.value = false
  }
}

onMounted(() => {
  void Promise.all([loadUsers(), loadGroups()])
})
</script>
