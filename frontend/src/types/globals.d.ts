import "vue-i18n";

declare global {
  interface Window {
    hangarLoaded?: boolean;
    hangarDebug?: Record<string, () => any>;
  }
}

declare module "#app" {
  interface PageMeta {
    dataLoader_user?: boolean;
    dataLoader_userData?: boolean;
    dataLoader_project?: boolean;
  }
}
