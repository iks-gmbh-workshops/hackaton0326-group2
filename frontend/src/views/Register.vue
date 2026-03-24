<template>
  <div class="mx-auto max-w-md rounded-xl border border-gray-200 bg-white p-8 shadow-sm">
    <h1 class="mb-6 text-2xl font-bold text-gray-900">Registrierung</h1>

    <form class="space-y-4" @submit.prevent="onSubmit">
      <div>
        <label for="username" class="mb-1 block text-sm font-medium text-gray-700">
          Benutzername (Pflicht)
        </label>
        <input
          id="username"
          v-model="form.username"
          type="text"
          required
          class="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
          autocomplete="username"
        />
      </div>

      <div>
        <label for="email" class="mb-1 block text-sm font-medium text-gray-700">
          E-Mail-Adresse (Pflicht)
        </label>
        <input
          id="email"
          v-model="form.email"
          type="email"
          required
          class="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
          autocomplete="email"
        />
      </div>

      <div>
        <label for="password" class="mb-1 block text-sm font-medium text-gray-700">
          Passwort (Pflicht)
        </label>
        <input
          id="password"
          v-model="form.password"
          type="password"
          required
          class="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
          autocomplete="new-password"
        />
      </div>

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
        <span>Ich stimme den AGB von hackathon-b zu. (Pflicht)</span>
      </label>

      <button
        type="submit"
        :disabled="isSubmitting"
        class="w-full rounded-md bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700"
        :class="{ 'cursor-not-allowed opacity-70': isSubmitting }"
      >
        {{ isSubmitting ? 'Registrierung laeuft...' : 'Registrieren' }}
      </button>
    </form>

    <p v-if="successMessage" class="mt-4 rounded-md border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">
      {{ successMessage }}
    </p>
    <p v-if="errorMessage" class="mt-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
      {{ errorMessage }}
    </p>

    <p class="mt-6 text-sm text-gray-600">
      Bereits registriert?
      <router-link to="/login" class="font-medium text-blue-600 hover:text-blue-700">
        Zur Anmeldung
      </router-link>
    </p>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import axios from 'axios'
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
