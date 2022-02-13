import type { AxiosInstance } from "axios";
import axios from "axios";

export const useAxios: AxiosInstance = axios.create({
  baseURL: "http://localhost:3333",
});
