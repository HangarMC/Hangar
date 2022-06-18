/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly HANGAR_PUBLIC_HOST: string;
  readonly HANGAR_PROXY_HOST: string;
  readonly HANGAR_AUTH_HOST: string;
  readonly HANGAR_PAYPAL_ENV: string;
  readonly HANGAR_PAYPAL_IPN: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
