import { useContext } from "vite-ssr";

let _req: any = null;
let _res: any = null;

export const set = (req: any, res: any) => {
  _req = req;
  _res = res;
};
export const unset = () => {
  _req = null;
  _res = null;
};
export const useRequest = () => _req || (import.meta.env.SSR ? useContext()?.request : null);
export const useResponse = () => _res || (import.meta.env.SSR ? useContext()?.response : null);
