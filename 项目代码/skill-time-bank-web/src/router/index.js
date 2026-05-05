import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/skill/:id',
    name: 'SkillDetail',
    component: () => import('../views/SkillDetail.vue')
  },
  {
    path: '/bounty',
    name: 'Bounty',
    component: () => import('../views/Bounty.vue')
  },
  {
    path: '/wallet',
    name: 'Wallet',
    meta: { requiresAuth: true },
    component: () => import('../views/user/Wallet.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    meta: { requiresAuth: true },
    component: () => import('../views/user/Profile.vue')
  },
  {
    path: '/profile/:id',
    name: 'UserProfile',
    component: () => import('../views/user/Profile.vue')
  },
  {
    path: '/my-skills',
    name: 'MySkills',
    meta: { requiresAuth: true },
    component: () => import('../views/user/MySkills.vue')
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    meta: { requiresAuth: true, requiresAdmin: true },
    component: () => import('../views/admin/Users.vue')
  },
  {
    path: '/admin/appeals',
    name: 'AdminAppeals',
    meta: { requiresAuth: true, requiresAdmin: true },
    component: () => import('../views/admin/Appeals.vue')
  },
  {
    path: '/admin/announcements',
    name: 'AdminAnnouncements',
    meta: { requiresAuth: true, requiresAdmin: true },
    component: () => import('../views/admin/Announcements.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/')
  } else {
    next()
  }
})

export default router
