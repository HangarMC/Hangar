import { createCookies, useCookies as cookies } from "@vueuse/integrations";
import { useRequest, useResponse } from "~/composables/useResReq";
import * as cookie from "cookie";

export const useCookies = () => {
  if (import.meta.env.SSR) {
    const req = useRequest();
    const res = useResponse();
    const cookies = createCookies(req)();
    cookies.addChangeListener((change) => {
      if (!res || res.headersSent) {
        return;
      }

      if (change.value === undefined) {
        res.setHeader("Set-Cookie", change.name + "=");
      } else {
        res.setHeader("Set-Cookie", cookie.serialize(change.name, change.value, change.options));
      }
    });
    return cookies;
  } else {
    return cookies();
  }
};
