import "vue-i18n";
import en from "@/i18n/locales/en.json";

declare global {
  interface Window {
    hangarLoaded?: boolean;
    hangarDebug?: Record<string, Function<void, any>>;
  }
}

type MainTranslations = typeof en;
declare module "vue-i18n" {
  export interface DefineLocaleMessage extends MainTranslations {}
}

declare module "#app" {
  interface PageMeta {
    dataLoader_user?: boolean;
    dataLoader_userData?: boolean;
    dataLoader_project?: boolean;
  }
}
