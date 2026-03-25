<template>
  <div id="app" class="bg-gray-50 min-h-screen">
    <nav class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <h1 class="text-2xl font-bold text-blue-600">Hackathon-b</h1>
            </div>
          </div>

          <div v-if="isLoggedIn" class="flex items-center relative" ref="menuRoot">
            <button
              class="inline-flex items-center justify-center rounded-md border border-gray-300 bg-white p-2 text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
              @click="toggleMenu"
              aria-haspopup="true"
              :aria-expanded="menuOpen ? 'true' : 'false'"
              aria-label="Menue oeffnen"
            >
              <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>

            <div
              v-if="menuOpen"
              class="absolute right-0 top-12 z-20 w-56 rounded-md border border-gray-200 bg-white shadow-lg"
            >
              <router-link
                v-for="item in navItems"
                :key="item.to"
                :to="item.to"
                @click="menuOpen = false"
                class="block px-4 py-3 text-sm transition"
                :class="route.path === item.to ? 'bg-blue-50 text-blue-700 font-medium' : 'text-gray-700 hover:bg-gray-50'"
              >
                {{ item.label }}
              </router-link>
              <router-link
                v-if="isAdmin"
                to="/admin"
                @click="menuOpen = false"
                class="block px-4 py-3 text-sm transition"
                :class="route.path === '/admin' ? 'bg-blue-50 text-blue-700 font-medium' : 'text-gray-700 hover:bg-gray-50'"
              >
                Administration
              </router-link>
              <button
                type="button"
                @click="logout"
                class="block w-full px-4 py-3 text-left text-sm text-red-700 transition hover:bg-red-50"
              >
                Abmelden
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

type JwtPayload = {
  role?: string
  roles?: string[]
  authorities?: string[]
}

const route = useRoute()
const router = useRouter()
const menuOpen = ref(false)
const menuRoot = ref<HTMLElement | null>(null)
const isLoggedIn = ref(false)

const navItems = [
  { label: 'Startseite', to: '/' },
  { label: 'Gruppen', to: '/groups' },
  { label: 'Aktivitaeten', to: '/activities' },
  { label: 'Profilverwaltung', to: '/profile' }
]

const syncAuthState = () => {
  isLoggedIn.value = Boolean(localStorage.getItem('auth_token'))
}

const parseJwtPayload = (token: string): JwtPayload | null => {
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const normalized = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
    return JSON.parse(atob(normalized)) as JwtPayload
  } catch {
    return null
  }
}

const isAdmin = computed(() => {
  const localRole = localStorage.getItem('role')

  if (localRole?.toUpperCase() === 'ADMIN') {
    return true
  }

  const token = localStorage.getItem('auth_token')
  if (!token) {
    return false
  }

  const payload = parseJwtPayload(token)
  if (!payload) {
    return false
  }

  if (payload.role?.toUpperCase() === 'ADMIN') {
    return true
  }

  if (payload.roles?.some((role) => role.toUpperCase() === 'ADMIN')) {
    return true
  }

  return payload.authorities?.some((authority) => authority.toUpperCase() === 'ROLE_ADMIN') ?? false
})

const toggleMenu = () => {
  menuOpen.value = !menuOpen.value
}

const logout = async () => {
  const confirmed = window.confirm('Moechtest du dich wirklich abmelden?')
  if (!confirmed) {
    return
  }

  localStorage.removeItem('auth_token')
  localStorage.removeItem('auth_role')
  localStorage.removeItem('auth_email')
  localStorage.removeItem('auth_display_name')
  localStorage.removeItem('user_role')
  localStorage.removeItem('role')
  syncAuthState()
  menuOpen.value = false
  await router.push('/login')
}

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as Node | null
  if (!target || !menuRoot.value) {
    return
  }

  if (!menuRoot.value.contains(target)) {
    menuOpen.value = false
  }
}

const handleStorageChange = () => {
  syncAuthState()
}

watch(
  () => route.path,
  () => {
    syncAuthState()
    menuOpen.value = false
  }
)

onMounted(() => {
  syncAuthState()
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('storage', handleStorageChange)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('storage', handleStorageChange)
})
</script>
