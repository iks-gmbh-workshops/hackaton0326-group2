<template>
  <div class="mx-auto max-w-3xl space-y-6">
    <div class="border-b border-gray-200 pb-6">
      <h1 class="text-3xl font-bold text-gray-900">Profilverwaltung</h1>
      <p class="mt-2 text-gray-600">Bearbeite hier deine Profildaten.</p>
    </div>

      <p v-if="successMessage" class="mt-6 rounded-md border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">
        {{ successMessage }}
      </p>
      <p v-if="errorMessage" class="mt-3 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
        {{ errorMessage }}
      </p>

    <section ref="profileRoot" class="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
      <div class="space-y-4">
        <div
          v-for="field in profileFields"
          :key="field.key"
          class="flex items-start gap-4 rounded-lg border border-gray-200 p-4"
        >
          <div class="w-44 shrink-0 pt-2">
            <label class="text-sm font-medium text-gray-700" :for="`field-${field.key}`">{{ field.label }}</label>
          </div>

          <div v-if="isEditing(field.key)" class="min-w-0 flex-1 space-y-2" :data-profile-field="field.key">
            <input
              :id="`field-${field.key}`"
              :type="field.inputType"
              v-model="form[field.key]"
              class="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
              :autocomplete="field.autocomplete"
              @keydown.enter.prevent="finishEdit(field.key)"
              @blur="finishEdit(field.key)"
            />
            <p v-if="field.key === 'password'" class="text-xs text-gray-500">
              Bei Passwortänderung muss das Feld "Passwort bestätigen" identisch sein.
            </p>
          </div>

          <p v-else class="min-w-0 flex-1 pt-2 text-base text-gray-900" :data-profile-field="field.key">
            {{ displayValue(field.key) }}
          </p>

          <div class="pt-1">
            <button
              type="button"
              class="rounded-md p-2 text-gray-500 hover:bg-gray-100 hover:text-blue-600"
              :aria-label="`${field.label} bearbeiten`"
              @click="startEdit(field.key)"
            >
              <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path
                  d="M16.862 3.487a2.1 2.1 0 0 1 2.97 2.971L8.22 18.07l-4.008 1.037 1.037-4.007L16.862 3.487Z"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="1.8"
                />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <div class="mt-6 flex flex-wrap items-center gap-3">
        <button
          type="button"
          class="rounded-md bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700"
          :class="{ 'cursor-not-allowed opacity-70': isSaving || isDeleting }"
          :disabled="isSaving || isDeleting"
          @click="saveProfile"
        >
          {{ isSaving ? 'Speichern laeuft...' : 'Speichern' }}
        </button>

        <button
          v-if="hasChanges"
          type="button"
          class="rounded-md bg-red-600 px-4 py-2 font-medium text-white hover:bg-red-700"
          @click="discardChanges"
        >
          Änderungen verwerfen
        </button>
      </div>

      <div class="mt-4">
        <button
          type="button"
          class="rounded-md border border-red-300 bg-white px-4 py-2 font-medium text-red-700 hover:bg-red-50"
          :class="{ 'cursor-not-allowed opacity-70': isDeleting || isSaving }"
          :disabled="isDeleting || isSaving"
          @click="deleteProfile"
        >
          {{ isDeleting ? 'Profil wird geloescht...' : 'Profil dauerhaft l\u00f6schen' }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authService, type ProfileResponse, type UpdateProfileRequest } from '../api/authService'

type ProfileFieldKey = 'displayName' | 'email' | 'password' | 'passwordConfirm' | 'firstName' | 'lastName'

type ProfileForm = Record<ProfileFieldKey, string>

type PersistedProfile = {
  displayName: string
  email: string
  firstName: string
  lastName: string
}

type ProfileFieldConfig = {
  key: ProfileFieldKey
  label: string
  inputType: string
  autocomplete: string
}

const profileFields: ProfileFieldConfig[] = [
  { key: 'displayName', label: 'Name', inputType: 'text', autocomplete: 'username' },
  { key: 'email', label: 'E-Mailadresse', inputType: 'email', autocomplete: 'email' },
  { key: 'password', label: 'Passwort', inputType: 'password', autocomplete: 'new-password' },
  {
    key: 'passwordConfirm',
    label: 'Passwort bestätigen',
    inputType: 'password',
    autocomplete: 'new-password'
  },
  { key: 'firstName', label: 'Vorname', inputType: 'text', autocomplete: 'given-name' },
  { key: 'lastName', label: 'Nachname', inputType: 'text', autocomplete: 'family-name' }
]

const profileRoot = ref<HTMLElement | null>(null)
const router = useRouter()
const isSaving = ref(false)
const isDeleting = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const editingField = ref<ProfileFieldKey | null>(null)

const form = reactive<ProfileForm>({
  displayName: '',
  email: '',
  password: '',
  passwordConfirm: '',
  firstName: '',
  lastName: ''
})

const persisted = ref<PersistedProfile>({
  displayName: '',
  email: '',
  firstName: '',
  lastName: ''
})

const normalize = (value: string): string => value.trim()

const hasChanges = computed(() => {
  return (
    normalize(form.displayName) !== persisted.value.displayName ||
    normalize(form.email) !== persisted.value.email ||
    normalize(form.firstName) !== persisted.value.firstName ||
    normalize(form.lastName) !== persisted.value.lastName ||
    form.password.length > 0 ||
    form.passwordConfirm.length > 0
  )
})

const isEditing = (key: ProfileFieldKey) => editingField.value === key

const displayValue = (key: ProfileFieldKey): string => {
  if (key === 'password' || key === 'passwordConfirm') {
    return form[key] ? '••••••••' : 'Nicht gesetzt'
  }
  return form[key] || '-'
}

const readString = (payload: ProfileResponse | null, keys: string[]): string => {
  if (!payload || typeof payload !== 'object') return ''
  const record = payload as Record<string, unknown>

  for (const key of keys) {
    const value = record[key]
    if (typeof value === 'string') return value
  }
  return ''
}

const applyPersistedToForm = () => {
  form.displayName = persisted.value.displayName
  form.email = persisted.value.email
  form.firstName = persisted.value.firstName
  form.lastName = persisted.value.lastName
  form.password = ''
  form.passwordConfirm = ''
}

const syncPersistedStorage = (profile: PersistedProfile) => {
  localStorage.setItem('auth_display_name', profile.displayName)
  localStorage.setItem('auth_email', profile.email)
  localStorage.setItem('auth_first_name', profile.firstName)
  localStorage.setItem('auth_last_name', profile.lastName)
}

const loadProfile = async () => {
  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const response = await authService.getMyProfile()
    persisted.value = {
      displayName:
        normalize(readString(response, ['displayName', 'username', 'name'])) ||
        normalize(localStorage.getItem('auth_display_name') || ''),
      email: normalize(readString(response, ['email'])) || normalize(localStorage.getItem('auth_email') || ''),
      firstName:
        normalize(readString(response, ['firstName', 'firstname'])) ||
        normalize(localStorage.getItem('auth_first_name') || ''),
      lastName:
        normalize(readString(response, ['lastName', 'lastname'])) ||
        normalize(localStorage.getItem('auth_last_name') || '')
    }
  } catch {
    persisted.value = {
      displayName: normalize(localStorage.getItem('auth_display_name') || ''),
      email: normalize(localStorage.getItem('auth_email') || ''),
      firstName: normalize(localStorage.getItem('auth_first_name') || ''),
      lastName: normalize(localStorage.getItem('auth_last_name') || '')
    }
    errorMessage.value = 'Profil konnte nicht vom Server geladen werden. Lokale Daten werden angezeigt.'
  } finally {
    applyPersistedToForm()
    isLoading.value = false
  }
}

const startEdit = (field: ProfileFieldKey) => {
  successMessage.value = ''
  errorMessage.value = ''
  editingField.value = field
}

const finishEdit = (field: ProfileFieldKey) => {
  if (editingField.value !== field) return
  editingField.value = null
}

const handleOutsideClick = (event: MouseEvent) => {
  const current = editingField.value
  if (!current || !profileRoot.value) {
    return
  }

  const target = event.target as Node | null
  if (!target) return

  const activeRow = profileRoot.value.querySelector(`[data-profile-field="${current}"]`)
  if (activeRow && !activeRow.contains(target)) {
    finishEdit(current)
  }
}

const handleWindowBlur = () => {
  if (editingField.value) {
    finishEdit(editingField.value)
  }
}

const validatePassword = (): boolean => {
  if (!form.password && !form.passwordConfirm) return true

  if (!form.password || !form.passwordConfirm) {
    errorMessage.value = 'Bitte Passwort und Passwort bestätigen ausfüllen.'
    return false
  }

  if (form.password !== form.passwordConfirm) {
    errorMessage.value = 'Passwort und Passwort bestätigen müssen identisch sein.'
    return false
  }

  return true
}

const saveProfile = async () => {
  if (isSaving.value || isLoading.value) return

  successMessage.value = ''
  errorMessage.value = ''

  if (!validatePassword()) {
    return
  }

  const payload: UpdateProfileRequest = {
    displayName: normalize(form.displayName),
    email: normalize(form.email),
    firstName: normalize(form.firstName),
    lastName: normalize(form.lastName)
  }

  if (form.password) {
    payload.password = form.password
  }

  isSaving.value = true
  try {
    const response = await authService.updateMyProfile(payload)
    const nextPersisted: PersistedProfile = {
      displayName: normalize(readString(response, ['displayName', 'username', 'name'])) || payload.displayName,
      email: normalize(readString(response, ['email'])) || payload.email,
      firstName: normalize(readString(response, ['firstName', 'firstname'])) || payload.firstName || '',
      lastName: normalize(readString(response, ['lastName', 'lastname'])) || payload.lastName || ''
    }

    persisted.value = nextPersisted
    applyPersistedToForm()
    syncPersistedStorage(nextPersisted)
    editingField.value = null
    successMessage.value = 'Profil wurde gespeichert.'
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      errorMessage.value = backendMessage || 'Profil konnte nicht gespeichert werden.'
    } else {
      errorMessage.value = 'Profil konnte nicht gespeichert werden.'
    }
  } finally {
    isSaving.value = false
  }
}

const discardChanges = () => {
  applyPersistedToForm()
  editingField.value = null
  errorMessage.value = ''
  successMessage.value = 'Änderungen wurden verworfen.'
}

const deleteProfile = async () => {
  if (isDeleting.value || isSaving.value || isLoading.value) return

  const confirmed = window.confirm(
    'M\u00f6chten Sie Ihre Mitgliedschaft wirklich beenden? Mit Beendigung Ihrer Mitgliedschaft werden umgehend alle Ihre Daten aus drumdibum gel\u00f6scht.'
  )
  if (!confirmed) return

  isDeleting.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    await authService.deleteMyProfile()
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_display_name')
    localStorage.removeItem('auth_email')
    localStorage.removeItem('auth_first_name')
    localStorage.removeItem('auth_last_name')
    localStorage.removeItem('auth_role')
    await router.push('/login')
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      errorMessage.value = backendMessage || 'Profil konnte nicht gel\u00f6scht werden.'
    } else {
      errorMessage.value = 'Profil konnte nicht gel\u00f6scht werden.'
    }
  } finally {
    isDeleting.value = false
  }
}

onMounted(async () => {
  await loadProfile()
  document.addEventListener('mousedown', handleOutsideClick)
  window.addEventListener('blur', handleWindowBlur)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleOutsideClick)
  window.removeEventListener('blur', handleWindowBlur)
})
</script>
