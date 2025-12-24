import publicRoutes from './public'
import adminRoutes from './admin';
import { createRouter, createWebHistory } from 'vue-router'
import { authGuard } from '@/auth/authGuard';

const routes = [
    ...publicRoutes,
    ...adminRoutes,
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach(authGuard);

export default router
