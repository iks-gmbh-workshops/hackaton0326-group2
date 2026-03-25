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

  return true
})

export default router
