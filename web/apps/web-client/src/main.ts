import { createHead } from "@unhead/vue/client";
import install from "@urql/vue";
import { createApp } from "vue";

import "@/index.css";
import App from "@/App.vue";
import router from "@/router";

import { client } from "./graphql/client";
import { initRem } from "./utils/rem";

initRem();

const app = createApp(App);
const head = createHead();

app.use(router);
app.use(head);
app.use(install, client);
app.mount("#app");
