import { defineNuxtModule } from "@nuxt/kit";

export default defineNuxtModule({
  meta: {
    name: "componentsFix",
    configKey: "componentsFix",
  },
  setup(_, nuxt) {
    nuxt.hook("components:extend", (components) => {
      components.find((component) => component.pascalName === "Link")!.priority = 1337;
    });
  },
});
