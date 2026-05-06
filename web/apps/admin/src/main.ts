import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { createApp } from "vue";

import App from "@/App.vue";
import router from "@/router";

import "@/index.css";
import "element-plus/dist/index.css";

const app = createApp(App);

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      refetchOnReconnect: false,
      refetchOnMount: false,
      refetchInterval: false,
      staleTime: 0,
      gcTime: 0,
    },
  },
});

app.use(router);
app.use(VueQueryPlugin, { queryClient });
app.mount("#app");
