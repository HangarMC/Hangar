import { useContext } from "vite-ssr";
import * as domain from "~/composables/useDomain";
import { Context } from "vite-ssr/vue";

export const useRequest: () => Context["request"] | null = () => {
  if (import.meta.env.SSR) {
    if (domain.isActive()) {
      return domain.get<Context["request"]>("req");
    }
    const ctx = useContext();
    if (ctx) {
      return ctx.request;
    }
    console.error("request null!");
    console.trace();
    return null;
  }

  console.error("useRequest called on client?!");
  console.trace();
  return null;
};
export const useResponse: () => Context["response"] | null = () => {
  if (import.meta.env.SSR) {
    if (domain.isActive()) {
      return domain.get<Context["response"]>("res");
    }
    const ctx = useContext();
    if (ctx) {
      return ctx.response;
    }
    console.error("response null!");
    console.trace();
    return null;
  }

  console.error("useResponse called on client?!");
  console.trace();
  return null;
};
