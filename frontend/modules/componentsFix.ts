import { defineNuxtModule } from "@nuxt/kit";

export default defineNuxtModule({
  meta: {
    name: "componentsFix",
    configKey: "componentsFix",
  },
  setup(_, nuxt) {
    nuxt.hook("components:extend", (components) => {
      for (const component of components.filter((component) => component.pascalName === "Link")) component.priority = 1337;
    });
  },
});
