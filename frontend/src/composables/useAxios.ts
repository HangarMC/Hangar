import type { AxiosInstance } from "axios";
import { useNuxtApp } from "#imports";

export function useAxios() {
  return useNuxtApp().$axios as AxiosInstance;
}
