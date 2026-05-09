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
    path: '/bounty/create',
    name: 'BountyCreate',
    meta: { requiresAuth: true },
    component: () => import('../views/bounty/Create.vue')
  },
  {
    path: '/bounty/:id',
    name: 'BountyDetail',
    component: () => import('../views/bounty/Detail.vue')
  },
  {
    path: '/orders/buyer',
    name: 'BuyerOrders',
    meta: { requiresAuth: true },
    component: () => import('../views/order/OrderList.vue')
  },
  {
    path: '/orders/seller',
    name: 'SellerOrders',
    meta: { requiresAuth: true },
    component: () => import('../views/order/OrderList.vue')
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    meta: { requiresAuth: true },
    component: () => import('../views/order/Detail.vue')
  },
  {
    path: '/appeal/create',
    name: 'AppealCreate',
    meta: { requiresAuth: true },
    component: () => import('../views/user/Appeal.vue')
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
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.requiresAdmin) {
    next('/')
  } else {
    next()
  }
})

export default router
