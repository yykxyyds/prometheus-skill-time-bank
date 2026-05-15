import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    name: 'Dashboard',
    meta: { requiresAuth: true },
    component: () => import('../views/Dashboard.vue')
  },
  {
    path: '/users',
    name: 'Users',
    meta: { requiresAuth: true },
    component: () => import('../views/admin/Users.vue')
  },
  {
    path: '/appeals',
    name: 'Appeals',
    meta: { requiresAuth: true },
    component: () => import('../views/admin/Appeals.vue')
  },
  {
    path: '/announcements',
    name: 'Announcements',
    meta: { requiresAuth: true },
    component: () => import('../views/admin/Announcements.vue')
  },
  {
    path: '/skills',
    name: 'Skills',
    meta: { requiresAuth: true },
    component: () => import('../views/admin/Skills.vue')
  },
  {
    path: '/bounties',
    name: 'Bounties',
    meta: { requiresAuth: true },
    component: () => import('../views/admin/Bounties.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const store = useAdminStore()
  if (to.meta.requiresAuth && !store.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && store.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
