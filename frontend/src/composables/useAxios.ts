import type { AxiosInstance } from "axios";
import axios from "axios";
import { axiosLog } from "~/lib/composables/useLog";
import { useConfig } from "~/lib/composables/useConfig";

const config = useConfig();
const options = {
  baseURL: import.meta.env.SSR ? config.proxyHost : config.publicHost,
};
axiosLog("axios options", options);
export const useAxios: AxiosInstance = axios.create(options);
