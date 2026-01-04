import { createApp } from "vue";
import App from "@/App.vue";
import router from "@/router";
import "@/index.css";
import "element-plus/dist/index.css";
import { initRem } from "./utils/rem";

initRem();

const app = createApp(App);
app.use(router);
app.mount("#app");
