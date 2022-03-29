/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly HANGAR_PUBLIC_HOST: string;
  readonly HANGAR_PROXY_HOST: string;
  readonly HANGAR_AUTH_HOST: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
