import type { AxiosInstance } from "axios";
import axios from "axios";
import { axiosLog } from "~/composables/useLog";

const options = {
  baseURL: import.meta.env.SSR ? import.meta.env.HANGAR_PROXY_HOST : import.meta.env.HANGAR_PUBLIC_HOST,
};
axiosLog("axios options", options);
export const useAxios: AxiosInstance = axios.create(options);
