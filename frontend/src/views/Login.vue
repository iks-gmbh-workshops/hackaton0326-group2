<template>
  <div class="mx-auto max-w-md rounded-xl border border-gray-200 bg-white p-8 shadow-sm">
    <h1 class="app-section-title mb-6">Anmeldung</h1>

    <form class="space-y-4" @submit.prevent="onSubmit">
      <EmailField
        id="Email"
        v-model="email"
        label="Email"
        :disabled="isSubmitting"
      />

      <PasswordField
        id="password"
        v-model="password"
        label="Passwort"
        autocomplete="current-password"
        :disabled="isSubmitting"
      />

      <button
        type="submit"
        :disabled="isSubmitting"
        class="app-btn-auth"
        :class="{ 'cursor-not-allowed opacity-70': isSubmitting }"
      >
        {{ isSubmitting ? 'Anmeldung laeuft...' : 'Anmelden' }}
      </button>
    </form>

    <p v-if="successMessage" class="app-alert-success mt-4">
      {{ successMessage }}
    </p>
    <p v-if="errorMessage" class="app-alert-error mt-4">
      {{ errorMessage }}
    </p>

    <p class="mt-4 text-center text-sm text-gray-600">
      Noch kein Konto?
      <router-link to="/register" class="app-link-primary">
        Registrieren
      </router-link>
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'
import PasswordField from '@/components/fields/PasswordField.vue'
import { authService } from '../api/authService'
import EmailField from '@/components/fields/EmailField.vue'

const email = ref('')
const password = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const router = useRouter()
const route = useRoute()

const onSubmit = async () => {
  if (isSubmitting.value) return

  isSubmitting.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const response = await authService.login({
      email: email.value.trim(),
      password: password.value
    })

    localStorage.setItem('auth_token', response.token)
    localStorage.setItem('auth_role', response.role)
    localStorage.setItem('auth_email', response.email)
    localStorage.setItem('auth_display_name', response.displayName)

    successMessage.value = 'Anmeldung erfolgreich'
    const redirectTarget = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.push(redirectTarget)
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
