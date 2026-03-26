<template>
  <div class="mx-auto max-w-md rounded-xl border border-gray-200 bg-white p-8 shadow-sm">
    <h1 class="app-section-title mb-6">Registrierung</h1>

    <form class="space-y-4" @submit.prevent="onSubmit">
      <UsernameField
        id="username"
        v-model="form.username"
        label="Benutzername (Pflicht)"
        :disabled="isSubmitting"
      />

      <EmailField
        id="email"
        v-model="form.email"
        label="E-Mail-Adresse (Pflicht)"
        :disabled="isSubmitting"
      />

      <PasswordField
        id="password"
        v-model="form.password"
        label="Passwort (Pflicht)"
        autocomplete="new-password"
        :disabled="isSubmitting"
      />

      <div>
        <label for="firstName" class="mb-1 block text-sm font-medium text-gray-700">
          Vorname (optional)
        </label>
        <input
          id="firstName"
          v-model="form.firstName"
          type="text"
          class="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
          autocomplete="given-name"
        />
      </div>

      <div>
        <label for="lastName" class="mb-1 block text-sm font-medium text-gray-700">
          Nachname (optional)
        </label>
        <input
          id="lastName"
          v-model="form.lastName"
          type="text"
          class="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
          autocomplete="family-name"
        />
      </div>

      <label class="flex items-start gap-2 text-sm text-gray-700">
        <input
          v-model="form.termsAccepted"
          type="checkbox"
          required
          class="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
        />
        <span>Ich stimme den <a href="https://www.drumdibum.de/terms-and-conditions" target="_blank" class="app-link-primary">AGB</a> von hackathon-b zu. (Pflicht)</span>
      </label>

      <button
        type="submit"
        :disabled="isSubmitting"
        class="app-btn-auth"
        :class="{ 'cursor-not-allowed opacity-70': isSubmitting }"
      >
        {{ isSubmitting ? 'Registrierung laeuft...' : 'Registrieren' }}
      </button>
    </form>

    <p v-if="successMessage" class="app-alert-success mt-4">
      {{ successMessage }}
    </p>
    <p v-if="errorMessage" class="app-alert-error mt-4">
      {{ errorMessage }}
    </p>

    <p class="mt-6 text-sm text-gray-600">
      Bereits registriert?
      <router-link to="/login" class="app-link-primary">
        Zur Anmeldung
      </router-link>
    </p>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import axios from 'axios'
import EmailField from '@/components/fields/EmailField.vue'
import PasswordField from '@/components/fields/PasswordField.vue'
import UsernameField from '@/components/fields/UsernameField.vue'
import { authService } from '../api/authService'

const form = reactive({
  username: '',
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  termsAccepted: false
})

const isSubmitting = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const onSubmit = async () => {
  if (isSubmitting.value) return

  isSubmitting.value = true
  successMessage.value = ''
  errorMessage.value = ''

  try {
    await authService.register({
      email: form.email.trim(),
      password: form.password,
      displayName: form.username.trim(),
      agbAccepted: form.termsAccepted
    })

    successMessage.value = 'Registrierung erfolgreich'
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      errorMessage.value = backendMessage || 'Registrierung fehlgeschlagen'
    } else {
      errorMessage.value = 'Registrierung fehlgeschlagen'
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>
