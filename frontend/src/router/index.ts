import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useAuthStore } from '@/stores/auth'

const hasAdminRole = (roles: string[] | undefined) =>
  roles?.some((r) =>
    ['ROLE_ADMIN', 'ROLE_GEAR_ADMIN', 'ROLE_EXPERT', 'ADMIN', 'GEAR_ADMIN', 'EXPERT'].includes(r)
  ) ?? false

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { guest: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { guest: true },
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          name: 'admin',
          component: () => import('@/views/admin/AdminDashboardView.vue'),
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('@/views/admin/AdminUserView.vue'),
        },
        {
          path: 'gears',
          name: 'admin-gears',
          component: () => import('@/views/admin/AdminGearView.vue'),
        },
        {
          path: 'role-apply',
          name: 'admin-role-apply',
          component: () => import('@/views/admin/AdminRoleApplyView.vue'),
        },
      ],
    },
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: false },
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/HomeView.vue'),
        },
        {
          path: 'gears',
          name: 'gears',
          component: () => import('@/views/gear/GearListView.vue'),
        },
        {
          path: 'gears/compare',
          name: 'gear-compare',
          component: () => import('@/views/gear/GearCompareView.vue'),
        },
        {
          path: 'gears/:id',
          name: 'gear-detail',
          component: () => import('@/views/gear/GearDetailView.vue'),
        },
        {
          path: 'plans',
          name: 'plans',
          component: () => import('@/views/plan/PlanListView.vue'),
        },
        {
          path: 'recommend',
          name: 'recommend',
          component: () => import('@/views/recommend/RecommendView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'user',
          name: 'user',
          component: () => import('@/views/user/UserCenterView.vue'),
          meta: { requiresAuth: true },
        },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  if (to.meta.guest && authStore.isLoggedIn) {
    next('/')
    return
  }
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
    return
  }
  if (to.meta.requiresAdmin && !hasAdminRole(authStore.user?.roles)) {
    next('/')
    return
  }
  next()
})

export default router
