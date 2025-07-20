import { createApp } from "vue";
import ElementPlus from "element-plus";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import router from "./router";
import App from "./App.vue";
import "@/styles/index.scss";
import "element-plus/dist/index.css";

const app = createApp(App);

// Register all Element Plus icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

app.use(ElementPlus, { size: "default" });
app.use(router);
app.mount("#app");
