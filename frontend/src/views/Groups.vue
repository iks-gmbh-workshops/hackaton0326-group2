<template>
  <div>
    <h1 class="text-3xl font-bold text-gray-900 mb-6">Gruppen</h1>
    <p v-if="successMessage" class="mb-4 rounded-md border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">
      {{ successMessage }}
    </p>
    
    <div class="grid grid-cols-1 gap-6">
      <div class="bg-white rounded-lg shadow p-6 space-y-5">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <h2 class="text-xl font-semibold text-gray-900">Meine Gruppen</h2>
          <button
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
            @click="openCreateForm"
          >
            + Neue Gruppe erstellen
          </button>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full table-auto">
            <thead class="bg-gray-50/70">
              <tr>
                <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700">Name</th>
                <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700">Beschreibung</th>
                <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700">Erstellt durch</th>
                <th class="px-3 py-2 text-left text-sm font-semibold text-gray-700">Anzahl der Mitglieder</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="isLoadingGroups">
                <td colspan="4" class="px-3 py-3 text-sm text-gray-500">Lade Gruppen...</td>
              </tr>
              <tr v-else-if="groupsError">
                <td colspan="4" class="px-3 py-3 text-sm text-red-600">{{ groupsError }}</td>
              </tr>
              <tr v-else-if="groups.length === 0">
                <td colspan="4" class="px-3 py-3 text-sm text-gray-500">Keine Gruppen vorhanden.</td>
              </tr>
              <tr v-for="group in groups" v-else :key="group.id">
                <td class="px-3 py-2 text-sm text-gray-900">{{ group.name }}</td>
                <td class="px-3 py-2 text-sm text-gray-700">{{ group.description || '-' }}</td>
                <td class="px-3 py-2 text-sm text-gray-700">{{ group.createdBy || '-' }}</td>
                <td class="px-3 py-2 text-sm text-gray-700">{{ group.memberCount ?? '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div
      v-if="showCreateForm"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      @click.self="closeCreateForm"
    >
      <form
        class="w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-white rounded-lg shadow-xl p-6 md:p-8 fade-in"
        @submit.prevent="createGroup"
      >
        <div class="flex items-start justify-between mb-6">
          <h2 class="text-xl font-semibold text-gray-900">Neue Gruppe anlegen</h2>
          <button
            type="button"
            class="text-gray-500 hover:text-gray-700 text-xl leading-none"
            aria-label="Popup schliessen"
            :disabled="isSubmitting"
            @click="closeCreateForm"
          >
            x
          </button>
        </div>
        <p v-if="errorMessage" class="mb-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {{ errorMessage }}
        </p>

        <div class="space-y-5">
          <div>
            <label for="groupName" class="block text-sm font-medium text-gray-700 mb-1">
              Gruppenname <span class="text-red-600">*</span>
            </label>
            <input
              id="groupName"
              v-model.trim="form.groupName"
              type="text"
              required
              :disabled="isSubmitting"
              placeholder="z. B. Laufgruppe Mittwoch"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div>
            <label for="description" class="block text-sm font-medium text-gray-700 mb-1">
              Beschreibung (optional)
            </label>
            <textarea
              id="description"
              v-model.trim="form.description"
              rows="3"
              :disabled="isSubmitting"
              placeholder="Kurze Beschreibung der Gruppe"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div>
            <label for="memberInput" class="block text-sm font-medium text-gray-700 mb-1">
              Gruppenmitglieder hinzufuegen
            </label>
            <div class="flex gap-2">
              <input
                id="memberInput"
                v-model.trim="memberInput"
                type="text"
                :disabled="isSubmitting"
                placeholder="Benutzername oder E-Mail-Adresse"
                class="flex-1 rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                @keyup.enter.prevent="addMember"
              />
              <button
                type="button"
                :disabled="isSubmitting"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                :class="{ 'cursor-not-allowed opacity-70': isSubmitting }"
                @click="addMember"
              >
                +
              </button>
            </div>

            <ul v-if="form.members.length" class="mt-3 space-y-2">
              <li
                v-for="(member, index) in form.members"
                :key="`${member}-${index}`"
                class="flex items-center justify-between rounded-lg bg-gray-50 px-3 py-2 text-sm text-gray-700"
              >
                <span>{{ member }}</span>
                <button
                  type="button"
                  :disabled="isSubmitting"
                  class="text-red-600 hover:text-red-700"
                  @click="removeMember(index)"
                >
                  Entfernen
                </button>
              </li>
            </ul>
          </div>
        </div>

        <div class="mt-8 pt-5 border-t border-gray-200">
          <button
            type="submit"
            :disabled="isSubmitting"
            class="w-full md:w-auto px-5 py-2.5 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
            :class="{ 'cursor-not-allowed opacity-70': isSubmitting }"
          >
            {{ isSubmitting ? 'Gruppe wird angelegt...' : 'Gruppe anlegen' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { onMounted, reactive, ref } from 'vue'
import { groupService, type CreateGroupRequest, type Group } from '../api/groupService'

const showCreateForm = ref(false)
const memberInput = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const groups = ref<Group[]>([])
const isLoadingGroups = ref(false)
const groupsError = ref('')

const form = reactive({
  groupName: '',
  description: '',
  members: [] as string[],
})

const resetForm = () => {
  form.groupName = ''
  form.description = ''
  form.members = []
  memberInput.value = ''
}

const openCreateForm = () => {
  resetForm()
  errorMessage.value = ''
  successMessage.value = ''
  showCreateForm.value = true
}

const closeCreateForm = () => {
  if (isSubmitting.value) return
  errorMessage.value = ''
  showCreateForm.value = false
}

const addMember = () => {
  const value = memberInput.value.trim()
  if (!value) return

  form.members.push(value)
  memberInput.value = ''
}

const removeMember = (index: number) => {
  form.members.splice(index, 1)
}

const loadGroups = async () => {
  isLoadingGroups.value = true
  groupsError.value = ''

  try {
    groups.value = await groupService.getMyGroups()
  } catch {
    groupsError.value = 'Gruppen konnten nicht geladen werden.'
  } finally {
    isLoadingGroups.value = false
  }
}

const createGroup = async () => {
  const name = form.groupName.trim()
  if (!name || isSubmitting.value) return

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const payload: CreateGroupRequest = { name }
    const description = form.description.trim()
    if (description) payload.description = description
    if (form.members.length) payload.members = [...form.members]

    await groupService.createGroup(payload)
    await loadGroups()

    successMessage.value = 'Gruppe wurde erfolgreich angelegt.'
    showCreateForm.value = false
    resetForm()
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      errorMessage.value = backendMessage || 'Gruppe konnte nicht angelegt werden.'
    } else {
      errorMessage.value = 'Gruppe konnte nicht angelegt werden.'
    }
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  void loadGroups()
})
</script>
