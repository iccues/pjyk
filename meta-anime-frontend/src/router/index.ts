import { createRouter, createWebHistory } from 'vue-router'
import { authGuard } from '@/auth/authGuard';

const routes = [
    {
        path: '/',
        component: () => import('@/pages/MainPage.vue'),
    },
    {
        path: '/anime/list',
        component: () => import('@/pages/AnimeListPage.vue'),
    },
    {
        path: '/admin',
        component: () => import('@/pages/admin/AdminHomePage.vue'),
        meta: { requiresAuth: true },
    },
    {
        path: '/admin/list',
        component: () => import('@/pages/admin/AdminListPage.vue'),
        meta: { requiresAuth: true },
    },
    {
        path: '/admin/auth/callback',
        component: () => import('@/pages/auth/Callback.vue'),
    },
    {
        path: '/admin/auth/login',
        component: () => import('@/pages/auth/Login.vue'),
    },
    {
        path: '/admin/auth',
        component: () => import('@/pages/auth/AuthHome.vue'),
        meta: { requiresAuth: true },
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach(authGuard);

export default router
