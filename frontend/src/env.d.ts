/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly HANGAR_CONFIG_ENV: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
