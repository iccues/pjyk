import { createRouter, createWebHistory } from 'vue-router'
import MainPage from "../pages/MainPage.vue";
import AnimeListPage from "../pages/AnimeListPage.vue";
import AdminHomePage from '../pages/admin/AdminHomePage.vue';
import AdminListPage from '../pages/admin/AdminListPage.vue';

const routes = [
    { path: '/', component: MainPage },
    { path: '/anime/list', component: AnimeListPage },
    { path: '/admin', component: AdminHomePage },
    { path: '/admin/list', component: AdminListPage }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
