import { createCookies, useCookies as cookies } from "@vueuse/integrations/useCookies";
import { useRequest, useResponse } from "~/composables/useResReq";
import * as cookie from "cookie";

export const useCookies = () => {
  if (import.meta.env.SSR) {
    const req = useRequest();
    const res = useResponse();
    if (!req || !req.headers) {
      console.error("req null?!");
      console.trace();
    }
    const cookies = createCookies(req)();
    cookies.addChangeListener((change) => {
      if (!res || res.headersSent) {
        return;
      }

      const old = res.getHeader("set-cookie");
      const newCookie = change.value === undefined ? change.name + "=" : cookie.serialize(change.name, change.value, change.options);
      const val = old ? old + "; " + newCookie : newCookie;
      console.log("setting cookie header to " + val);
      res.setHeader("set-cookie", val);
    });
    return cookies;
  } else {
    return cookies();
  }
};
