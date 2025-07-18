import "vue-i18n";

declare global {
  interface Window {
    hangarLoaded?: boolean;
    hangarDebug?: Record<string, () => any>;
    umami: {
      track: (eventName: string, eventData: Record<string, unknown>) => void;
      identify: (sessionData: Record<string, unknown>) => void;
    };
  }
}

declare module "#app" {
  interface PageMeta {
    dataLoader_user?: boolean;
    dataLoader_userData?: boolean;
    dataLoader_project?: boolean;
  }
}
