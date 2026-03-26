import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/invite/:token',
    name: 'InviteLanding',
    component: () => import('../views/InviteLanding.vue')
  },
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue')
  },
  {
    path: '/groups',
    name: 'Groups',
    component: () => import('../views/Groups.vue')
  },
  {
    path: '/activities',
    name: 'Activities',
    component: () => import('../views/Activities.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue')
  },
  {
    path: '/admin',
    name: 'Administration',
    component: () => import('../views/Administration.vue'),
    meta: {
      requiresAuth: true,
      requiresAdmin: true,
    },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const isAuthenticated = (): boolean => Boolean(localStorage.getItem('auth_token'))
const asNonEmptyString = (value: unknown): string => (typeof value === 'string' ? value.trim() : '')

const parseJwtPayload = (token: string): Record<string, unknown> | null => {
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const normalized = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
    return JSON.parse(atob(normalized)) as Record<string, unknown>
  } catch {
    return null
  }
}

const isAdmin = (): boolean => {
  const localRole = asNonEmptyString(localStorage.getItem('role')) || asNonEmptyString(localStorage.getItem('auth_role'))
  if (localRole.toUpperCase() === 'ADMIN') return true

  const token = localStorage.getItem('auth_token')
  if (!token) return false

  const payload = parseJwtPayload(token)
  if (!payload) return false

  const role = asNonEmptyString(payload.role)
  if (role.toUpperCase() === 'ADMIN') return true

  const roles = Array.isArray(payload.roles) ? payload.roles : []
  if (roles.some((entry) => asNonEmptyString(entry).toUpperCase() === 'ADMIN')) return true

  const authorities = Array.isArray(payload.authorities) ? payload.authorities : []
  return authorities.some((entry) => asNonEmptyString(entry).toUpperCase() === 'ROLE_ADMIN')
}

router.beforeEach((to) => {
  const loggedIn = isAuthenticated()
  const isAuthPage = to.path === '/login' || to.path === '/register'
  const redirectTarget = typeof to.query.redirect === 'string' ? to.query.redirect : '/'

  if (!loggedIn && !isAuthPage) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (loggedIn && isAuthPage) {
    return { path: redirectTarget }
  }

  if (to.meta.requiresAdmin && !isAdmin()) {
    return { path: '/' }
  }

  return true
})

export default router
