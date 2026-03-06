import { createApp } from "vue";
import App from "@/App.vue";
import router from "@/router";
import "@/index.css";
import { initRem } from "./utils/rem";
import { createHead } from "@unhead/vue/client";

initRem();

const app = createApp(App);
const head = createHead();

app.use(router);
app.use(head);
app.mount("#app");
