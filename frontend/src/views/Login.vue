<template>
  <div class="mx-auto max-w-md rounded-xl border border-gray-200 bg-white p-8 shadow-sm">
    <h1 class="mb-6 text-2xl font-bold text-gray-900">Anmeldung</h1>

    <form class="space-y-4" @submit.prevent="onSubmit">
      <UsernameField id="username" v-model="username" required />
      <PasswordField id="password" v-model="password" required autocomplete="current-password" />

      <button
        type="submit"
        :disabled="isSubmitting"
        class="w-full rounded-md bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700"
        :class="{ 'cursor-not-allowed opacity-70': isSubmitting }"
      >
        {{ isSubmitting ? 'Anmeldung laeuft...' : 'Anmelden' }}
      </button>
    </form>

    <p v-if="successMessage" class="mt-4 rounded-md border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">
      {{ successMessage }}
    </p>
    <p v-if="errorMessage" class="mt-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
      {{ errorMessage }}
    </p>

    <p class="mt-4 text-center text-sm text-gray-600">
      Noch kein Konto?
      <router-link to="/register" class="font-medium text-blue-600 hover:text-blue-700">
        Registrieren
      </router-link>
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { authService } from '../api/authService'
import UsernameField from '../components/fields/UsernameField.vue'
import PasswordField from '../components/fields/PasswordField.vue'

const username = ref('')
const password = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const router = useRouter()

const onSubmit = async () => {
  if (isSubmitting.value) return

  isSubmitting.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const response = await authService.login({
      email: username.value.trim(),
      password: password.value
    })

    localStorage.setItem('auth_token', response.token)
    localStorage.setItem('auth_role', response.role)
    localStorage.setItem('auth_email', response.email)
    localStorage.setItem('auth_display_name', response.displayName)

    successMessage.value = 'Anmeldung erfolgreich'
    await router.push('/')
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const backendMessage = (error.response?.data as { message?: string } | undefined)?.message
      errorMessage.value = backendMessage || 'Anmeldung fehlgeschlagen'
    } else {
      errorMessage.value = 'Anmeldung fehlgeschlagen'
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>
