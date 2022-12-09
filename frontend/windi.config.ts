import { defineConfig } from "vite-plugin-windicss";
import WindiConfig from "./src/lib/config/windi.config.js";

export default defineConfig({
  presets: [WindiConfig],
});
