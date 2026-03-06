import { createApp } from "vue";
import App from "@/App.vue";
import router from "@/router";
import "@/index.css";
import { initRem } from "./utils/rem";
import { createHead } from "@unhead/vue/client";
import install from "@urql/vue";
import { client } from "./graphql/client";

initRem();

const app = createApp(App);
const head = createHead();

app.use(router);
app.use(head);
app.use(install, client);
app.mount("#app");
