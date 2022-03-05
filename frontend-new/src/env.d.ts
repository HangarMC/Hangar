/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly HANGAR_PUBLIC_HOST: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
