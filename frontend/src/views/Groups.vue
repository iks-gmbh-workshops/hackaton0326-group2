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
                <th class="px-3 py-2 text-right text-sm font-semibold text-gray-700">Aktion</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="isLoadingGroups">
                <td colspan="5" class="px-3 py-3 text-sm text-gray-500">Lade Gruppen...</td>
              </tr>
              <tr v-else-if="groupsError">
                <td colspan="5" class="px-3 py-3 text-sm text-red-600">{{ groupsError }}</td>
              </tr>
              <tr v-else-if="groups.length === 0">
                <td colspan="5" class="px-3 py-3 text-sm text-gray-500">Keine Gruppen vorhanden.</td>
              </tr>
              <tr v-for="group in groups" v-else :key="group.id">
                <td class="px-3 py-2 text-sm text-gray-900">{{ group.name }}</td>
                <td class="px-3 py-2 text-sm text-gray-700">{{ group.description || '-' }}</td>
                <td class="px-3 py-2 text-sm text-gray-700">{{ group.createdBy || '-' }}</td>
                <td class="px-3 py-2 text-sm text-gray-700">{{ group.memberCount ?? '-' }}</td>
                <td class="px-3 py-2 text-right">
                  <button
                    type="button"
                    class="inline-flex h-8 w-8 items-center justify-center rounded-md text-gray-600 transition hover:bg-gray-100 hover:text-blue-700"
                    aria-label="Gruppe bearbeiten"
                    :disabled="isSubmitting || isEditSubmitting || isDeletingGroup"
                    @click="openEditForm(group)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
                      <path d="M13.586 2.586a2 2 0 0 1 2.828 2.828l-8.25 8.25a2 2 0 0 1-.878.513l-3 1a1 1 0 0 1-1.264-1.264l1-3a2 2 0 0 1 .513-.878l8.25-8.25Zm1.414 1.414a.5.5 0 0 0-.707 0L5.99 12.303a.5.5 0 0 0-.128.22l-.62 1.858 1.859-.62a.5.5 0 0 0 .219-.128L15.623 5.33A.5.5 0 0 0 15 4Z" />
                    </svg>
                  </button>
                </td>
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

    <div
      v-if="showEditForm"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      @click.self="closeEditForm"
    >
      <form
        class="w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-white rounded-lg shadow-xl p-6 md:p-8 fade-in"
        @submit.prevent="updateGroup"
      >
        <div class="flex items-start justify-between mb-6">
          <h2 class="text-xl font-semibold text-gray-900">Gruppe bearbeiten</h2>
          <button
            type="button"
            class="text-gray-500 hover:text-gray-700 text-xl leading-none"
            aria-label="Popup schliessen"
            :disabled="isEditSubmitting || isDeletingGroup"
            @click="closeEditForm"
          >
            x
          </button>
        </div>
        <p
          v-if="editErrorMessage"
          class="mb-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700"
        >
          {{ editErrorMessage }}
        </p>

        <p v-if="isLoadingEditData" class="mb-4 text-sm text-gray-500">Gruppendaten werden geladen...</p>

        <div v-else class="space-y-5">
          <div>
            <label for="editGroupName" class="block text-sm font-medium text-gray-700 mb-1">
              Gruppenname <span class="text-red-600">*</span>
            </label>
            <input
              id="editGroupName"
              v-model.trim="editForm.groupName"
              type="text"
              required
              :disabled="isEditSubmitting || isDeletingGroup"
              placeholder="z. B. Laufgruppe Mittwoch"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div>
            <label for="editDescription" class="block text-sm font-medium text-gray-700 mb-1">
              Beschreibung (optional)
            </label>
            <textarea
              id="editDescription"
              v-model.trim="editForm.description"
              rows="3"
              :disabled="isEditSubmitting || isDeletingGroup"
              placeholder="Kurze Beschreibung der Gruppe"
              class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div>
            <label for="editMemberInput" class="block text-sm font-medium text-gray-700 mb-1">
              Gruppenmitglieder hinzufuegen
            </label>
            <div class="flex gap-2">
              <input
                id="editMemberInput"
                v-model.trim="editMemberInput"
                type="text"
                :disabled="isEditSubmitting || isDeletingGroup"
                placeholder="Benutzername oder E-Mail-Adresse"
                class="flex-1 rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                @keyup.enter.prevent="addEditMember"
              />
              <button
                type="button"
                :disabled="isEditSubmitting || isDeletingGroup"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                :class="{ 'cursor-not-allowed opacity-70': isEditSubmitting || isDeletingGroup }"
                @click="addEditMember"
              >
                +
              </button>
            </div>

            <ul v-if="editForm.members.length" class="mt-3 space-y-2">
              <li
                v-for="(member, index) in editForm.members"
                :key="`${member}-${index}`"
                class="flex items-center justify-between rounded-lg bg-gray-50 px-3 py-2 text-sm text-gray-700"
              >
                <span>{{ member }}</span>
                <button
                  type="button"
                  :disabled="isEditSubmitting || isDeletingGroup"
                  class="text-red-600 hover:text-red-700"
                  @click="removeEditMember(index)"
                >
                  Entfernen
                </button>
              </li>
            </ul>
          </div>
        </div>

        <div class="mt-8 pt-5 border-t border-gray-200 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <button
            type="button"
            :disabled="isEditSubmitting || isLoadingEditData || isDeletingGroup"
            class="w-full md:w-auto px-5 py-2.5 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
            :class="{ 'cursor-not-allowed opacity-70': isEditSubmitting || isLoadingEditData || isDeletingGroup }"
            @click="deleteGroup"
          >
            {{ isDeletingGroup ? 'Gruppe wird geloescht...' : 'Gruppe loeschen' }}
          </button>
          <button
            type="submit"
            :disabled="isEditSubmitting || isLoadingEditData || isDeletingGroup"
            class="w-full md:w-auto px-5 py-2.5 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
            :class="{ 'cursor-not-allowed opacity-70': isEditSubmitting || isLoadingEditData || isDeletingGroup }"
          >
            {{ isEditSubmitting ? 'Gruppe wird gespeichert...' : 'Aenderungen speichern' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { onMounted, reactive, ref } from 'vue'
import { groupService, type CreateGroupRequest, type Group, type UpdateGroupRequest } from '../api/groupService'

const showCreateForm = ref(false)
const memberInput = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const groups = ref<Group[]>([])
const isLoadingGroups = ref(false)
const groupsError = ref('')
const showEditForm = ref(false)
const editMemberInput = ref('')
const isEditSubmitting = ref(false)
const isLoadingEditData = ref(false)
const editErrorMessage = ref('')
const editingGroupId = ref<number | null>(null)
const isDeletingGroup = ref(false)

const form = reactive({
  groupName: '',
  description: '',
  members: [] as string[],
})

const editForm = reactive({
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

const resetEditForm = () => {
  editForm.groupName = ''
  editForm.description = ''
  editForm.members = []
  editMemberInput.value = ''
  editingGroupId.value = null
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

const closeEditForm = () => {
  if (isEditSubmitting.value || isLoadingEditData.value || isDeletingGroup.value) return
  editErrorMessage.value = ''
  showEditForm.value = false
  resetEditForm()
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

const addEditMember = () => {
  const value = editMemberInput.value.trim()
  if (!value) return

  editForm.members.push(value)
  editMemberInput.value = ''
}

const removeEditMember = (index: number) => {
  editForm.members.splice(index, 1)
}

const extractMembers = (groupDetails: unknown): string[] => {
  if (!groupDetails || typeof groupDetails !== 'object') return []

  const details = groupDetails as Record<string, unknown>
  const rawMembers = details.members
  if (!Array.isArray(rawMembers)) return []

  return rawMembers
    .map((member) => {
      if (typeof member === 'string') return member
      if (!member || typeof member !== 'object') return ''

      const data = member as Record<string, unknown>
      const candidate = data.username ?? data.email ?? data.name
      return typeof candidate === 'string' ? candidate : ''
    })
    .filter((member) => member.length > 0)
}

const openEditForm = async (group: Group) => {
  if (isSubmitting.value || isEditSubmitting.value || isDeletingGroup.value) return

  resetEditForm()
  editErrorMessage.value = ''
  successMessage.value = ''
  showEditForm.value = true
  isLoadingEditData.value = true

  try {
    const groupDetails = await groupService.getGroup(group.id)
    const details = groupDetails as Group & { members?: unknown }

    editingGroupId.value = group.id
    editForm.groupName = (details.name || group.name || '').trim()
    editForm.description = (details.description || group.description || '').trim()
    editForm.members = extractMembers(details)
  } catch (error) {
    showEditForm.value = false
    resetEditForm()

    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      groupsError.value = backendMessage || 'Gruppendetails konnten nicht geladen werden.'
    } else {
      groupsError.value = 'Gruppendetails konnten nicht geladen werden.'
    }
  } finally {
    isLoadingEditData.value = false
  }
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

const updateGroup = async () => {
  const groupId = editingGroupId.value
  const name = editForm.groupName.trim()
  if (!groupId || !name || isEditSubmitting.value || isLoadingEditData.value) return

  isEditSubmitting.value = true
  editErrorMessage.value = ''

  try {
    const payload: UpdateGroupRequest = { name }
    const description = editForm.description.trim()
    if (description) payload.description = description
    payload.members = [...editForm.members]

    await groupService.updateGroup(groupId, payload)
    await loadGroups()

    successMessage.value = 'Gruppe wurde erfolgreich aktualisiert.'
    showEditForm.value = false
    resetEditForm()
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      editErrorMessage.value = backendMessage || 'Gruppe konnte nicht aktualisiert werden.'
    } else {
      editErrorMessage.value = 'Gruppe konnte nicht aktualisiert werden.'
    }
  } finally {
    isEditSubmitting.value = false
  }
}

const deleteGroup = async () => {
  const groupId = editingGroupId.value
  if (!groupId || isDeletingGroup.value || isEditSubmitting.value || isLoadingEditData.value) return

  const confirmed = window.confirm('Moechtest du diese Gruppe wirklich loeschen?')
  if (!confirmed) return

  isDeletingGroup.value = true
  editErrorMessage.value = ''

  try {
    await groupService.deleteGroup(groupId)
    await loadGroups()

    successMessage.value = 'Gruppe wurde erfolgreich geloescht.'
    showEditForm.value = false
    resetEditForm()
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      editErrorMessage.value = backendMessage || 'Gruppe konnte nicht geloescht werden.'
    } else {
      editErrorMessage.value = 'Gruppe konnte nicht geloescht werden.'
    }
  } finally {
    isDeletingGroup.value = false
  }
}

onMounted(() => {
  void loadGroups()
})
</script>
