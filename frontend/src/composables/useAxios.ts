import type { AxiosInstance } from "axios";

export function useAxios() {
  return useNuxtApp().$axios as AxiosInstance;
}
