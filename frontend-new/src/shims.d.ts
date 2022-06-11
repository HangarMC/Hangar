declare interface Window {
  // extend the window
}

// with vite-plugin-md, markdowns can be treat as Vue components
declare module "*.md" {
  import type { ComponentOptions } from "vue";
  const component: ComponentOptions;
  export default component;
}

declare module "domain" {
  const active: Domain;
  declare interface Domain {
    context: Record<string, any> | null;
  }
}
