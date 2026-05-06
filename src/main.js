import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import App from "./App.vue";
import router from "./router";
import "./styles/reset.css";
import "./styles/variables.css";
import "./styles/theme-ultramarines.css";
import "./styles/transitions.css";
import "./styles/a11y.css";

const app = createApp(App);
app.use(createPinia());
app.use(ElementPlus);
app.use(router);
app.mount("#app");