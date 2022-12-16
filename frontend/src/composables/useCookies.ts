import { createCookies, useCookies as cookies } from "@vueuse/integrations/useCookies";
import * as cookie from "cookie";
import { useRequestEvent } from "#imports";

export const useCookies = () => {
  if (import.meta.env.SSR) {
    const event = useRequestEvent();
    const req = event.node.req;
    const res = event.node.res;
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
