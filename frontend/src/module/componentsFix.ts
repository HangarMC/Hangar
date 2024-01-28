import { defineNuxtModule } from "@nuxt/kit";

export default defineNuxtModule({
  meta: {
    name: "componentsFix",
    configKey: "componentsFix",
  },
  setup(_, nuxt) {
    nuxt.hook("components:extend", (components) => {
      components.filter((component) => component.pascalName === "Link").forEach((component) => (component.priority = 1337));
    });
  },
});
