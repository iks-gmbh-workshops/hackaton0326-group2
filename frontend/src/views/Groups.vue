<template>
  <div>
    <h1 class="mb-6 text-3xl font-bold text-gray-900">Gruppen</h1>

    <div class="grid grid-cols-1 gap-6">
      <div class="rounded-lg bg-white p-8 text-center shadow">
        <svg class="mx-auto mb-4 h-16 w-16 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M17 20h5v-2a3 3 0 00-5.856-1.488M15 10a3 3 0 11-6 0 3 3 0 016 0zM6 20a9 9 0 0118 0v2H6v-2z"
          />
        </svg>
        <h2 class="mb-2 text-xl font-semibold text-gray-900">Gruppen-Verwaltung</h2>
        <p class="mb-4 text-gray-600">Diese Seite zeigt und erstellt deine Gruppen.</p>
        <button
          class="rounded-lg bg-blue-600 px-4 py-2 text-white transition hover:bg-blue-700"
          @click="openCreateModal"
        >
          + Neue Gruppe erstellen
        </button>
      </div>
    </div>

    <div class="mt-6 rounded-lg bg-white p-6 shadow">
      <div class="mb-3 flex items-center justify-between gap-2">
        <h2 class="text-xl font-semibold text-gray-900">Meine Gruppen</h2>
        <span class="rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">
          Quelle: {{ dataSourceLabel }}
        </span>
      </div>

      <p v-if="isLoadingGroups" class="text-sm text-gray-600">Gruppen werden geladen...</p>
      <p v-else-if="groupsError" class="text-sm text-red-600">{{ groupsError }}</p>
      <p v-else-if="myGroups.length === 0" class="text-sm text-gray-600">Keine Gruppen vorhanden.</p>

      <ul v-else class="space-y-3">
        <li v-for="group in myGroups" :key="group.id" class="rounded-lg border border-gray-200 p-4">
          <p class="text-base font-semibold text-gray-900">{{ group.name }}</p>
          <p class="mt-1 text-sm text-gray-700">{{ group.description || 'Keine Beschreibung' }}</p>
          <p class="mt-2 text-xs text-gray-500">
            Mitglieder: {{ group.memberCount ?? '-' }} | Erstellt von: {{ group.createdBy }}
          </p>
        </li>
      </ul>
    </div>

    <div
      v-if="isCreateModalOpen"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      @click.self="closeCreateModal"
    >
      <div class="fade-in w-full max-w-xl rounded-xl bg-white shadow-xl">
        <div class="flex items-center justify-between border-b px-6 py-4">
          <h3 class="text-lg font-semibold text-gray-900">Neue Gruppe erstellen</h3>
          <button
            type="button"
            class="rounded-full p-2 text-gray-500 hover:bg-gray-100 hover:text-gray-700"
            aria-label="Formular schliessen"
            @click="closeCreateModal"
          >
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <form class="space-y-5 px-6 py-5" @submit.prevent="createGroup">
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700" for="group-name">Gruppenname</label>
            <BaseInputField
              id="group-name"
              v-model="groupName"
              type="text"
              placeholder="z. B. Wandergruppe Berlin"
              :input-class="'w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none'"
              required
              :show-label="false"
            />
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700" for="group-description">Beschreibung</label>
            <BaseTextareaField
              id="group-description"
              v-model="description"
              rows="4"
              placeholder="Kurzbeschreibung der Gruppe"
              :textarea-class="'w-full resize-none rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none'"
              :show-label="false"
            />
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700" for="invite-email">
              Mitglieder per E-Mail einladen
            </label>
            <div class="flex gap-2">
              <EmailField
                id="invite-email"
                v-model="inviteEmail"
                placeholder="name@beispiel.de"
                :show-label="false"
                @keydown="onInviteEmailKeydown"
              />
              <button
                type="button"
                class="flex h-10 w-10 items-center justify-center rounded-full bg-white text-blue-700 hover:bg-gray-700"
                aria-label="E-Mail hinzufuegen"
                @click="addInviteEmail"
              >
                <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 5v14m-7-7h14" />
                </svg>
              </button>
            </div>
            <p v-if="emailError" class="mt-2 text-sm text-red-600">{{ emailError }}</p>

            <div v-if="inviteEmails.length > 0" class="mt-3 flex flex-wrap gap-2">
              <span
                v-for="email in inviteEmails"
                :key="email"
                class="inline-flex items-center gap-2 rounded-full bg-blue-100 px-3 py-1 text-sm text-blue-800"
              >
                {{ email }}
                <button
                  type="button"
                  class="text-blue-700 hover:text-blue-900"
                  :aria-label="`Einladung fuer ${email} entfernen`"
                  @click="removeInviteEmail(email)"
                >
                  x
                </button>
              </span>
            </div>
          </div>

          <div class="border-t pt-4">
            <p v-if="createError" class="mb-3 text-sm text-red-600">{{ createError }}</p>
            <button
              type="submit"
              :disabled="isSubmitting"
              class="w-full rounded-lg bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700"
            >
              {{ isSubmitting ? 'Wird angelegt...' : 'Anlegen' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import BaseInputField from '../components/fields/BaseInputField.vue'
import BaseTextareaField from '../components/fields/BaseTextareaField.vue'
import EmailField from '../components/fields/EmailField.vue'
import { groupService, type Group } from '../api/groupService'
import { USE_MOCKS } from '../api/httpClient'

const isCreateModalOpen = ref(false)
const groupName = ref('')
const description = ref('')
const inviteEmail = ref('')
const inviteEmails = ref<string[]>([])
const emailError = ref('')
const myGroups = ref<Group[]>([])
const isLoadingGroups = ref(false)
const groupsError = ref('')
const createError = ref('')
const isSubmitting = ref(false)

const dataSourceLabel = computed(() => (USE_MOCKS ? 'Testdaten (JSON)' : 'Backend API'))

const hasUnsavedChanges = computed(() => {
  return (
    groupName.value.length > 0 ||
    description.value.length > 0 ||
    inviteEmail.value.length > 0 ||
    inviteEmails.value.length > 0
  )
})

const openCreateModal = () => {
  createError.value = ''
  isCreateModalOpen.value = true
}

const resetForm = () => {
  groupName.value = ''
  description.value = ''
  inviteEmail.value = ''
  inviteEmails.value = []
  emailError.value = ''
}

const closeCreateModal = () => {
  if (hasUnsavedChanges.value) {
    const shouldDiscard = window.confirm('Aenderungen verwerfen und Formular schliessen?')
    if (!shouldDiscard) {
      return
    }
  }

  isCreateModalOpen.value = false
  resetForm()
}

const isValidEmail = (email: string) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

const addInviteEmail = () => {
  emailError.value = ''
  const email = inviteEmail.value.trim().toLowerCase()

  if (!email) {
    return
  }

  if (!isValidEmail(email)) {
    emailError.value = 'Bitte eine gueltige E-Mail-Adresse eingeben.'
    return
  }

  if (inviteEmails.value.includes(email)) {
    emailError.value = 'Diese E-Mail wurde bereits hinzugefuegt.'
    return
  }

  inviteEmails.value.push(email)
  inviteEmail.value = ''
}

const removeInviteEmail = (email: string) => {
  inviteEmails.value = inviteEmails.value.filter((entry) => entry !== email)
}

const onInviteEmailKeydown = (event: KeyboardEvent) => {
  if (event.key !== 'Enter') {
    return
  }

  event.preventDefault()
  addInviteEmail()
}

const loadGroups = async () => {
  isLoadingGroups.value = true
  groupsError.value = ''

  try {
    myGroups.value = await groupService.getMyGroups()
  } catch (error) {
    console.error('Fehler beim Laden der Gruppen', error)
    groupsError.value = 'Gruppen konnten nicht geladen werden.'
  } finally {
    isLoadingGroups.value = false
  }
}

const createGroup = async () => {
  const name = groupName.value.trim()
  const groupDescription = description.value.trim()

  if (!name) {
    createError.value = 'Bitte einen Gruppennamen eingeben.'
    return
  }

  isSubmitting.value = true
  createError.value = ''

  try {
    await groupService.createGroup(name, groupDescription)

    // Einladung per E-Mail folgt, sobald das Backend den Endpoint bereitstellt.
    if (inviteEmails.value.length > 0) {
      console.info('Einladungen vorgemerkt', inviteEmails.value)
    }

    isCreateModalOpen.value = false
    resetForm()
    await loadGroups()
  } catch (error) {
    console.error('Fehler beim Erstellen der Gruppe', error)
    createError.value = 'Gruppe konnte nicht erstellt werden.'
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  void loadGroups()
})
</script>
