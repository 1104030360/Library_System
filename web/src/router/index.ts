import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('@/views/UserView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/AdminView.vue'),
      meta: { requiresAuth: true, requiresDirector: true }  // 僅館長
    },
    {
      path: '/staff',
      name: 'staff',
      component: () => import('@/views/StaffView.vue'),
      meta: { requiresAuth: true, requiresStaff: true }  // 僅館員
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/add-book',
      name: 'add-book',
      component: () => import('@/views/AddBookView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/book/:id',
      name: 'book-detail',
      component: () => import('@/views/BookDetailView.vue'),
      // 公開頁面，訪客可以查看書籍詳情、評分和評論，但不能進行評分和評論
    },
    {
      path: '/history',
      name: 'borrow-history',
      component: () => import('@/views/BorrowHistoryView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      // 公開頁面，不需要登入即可訪問
    },
  ],
})

// Route guard for authentication and authorization
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 如果需要認證，先嘗試檢查認證狀態（重要：避免 store 狀態未初始化）
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    try {
      await authStore.checkAuth()
    } catch (error) {
      // 認證失敗，跳轉到登入頁面
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }
  }

  // 再次檢查：如果仍未登入，跳轉到登入頁面
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }

  // Check if route requires director permission (僅館長)
  if (to.meta.requiresDirector) {
    if (!authStore.isAdmin) {
      next({ name: 'user' })
      return
    }
  }

  // Check if route requires staff permission (僅館員)
  if (to.meta.requiresStaff) {
    if (!authStore.isStaff) {
      next({ name: 'user' })
      return
    }
  }

  // Check if route requires admin permission (館長或館員)
  if (to.meta.requiresAdmin) {
    if (!authStore.isAdmin && !authStore.isStaff) {
      // 非管理員重定向到使用者頁面
      next({ name: 'user' })
      return
    }
  }

  next()
})

export default router
