import type { Ref } from "vue";
import { onDeactivated, onMounted, onUnmounted, ref } from "vue";
import { useContext } from "vite-ssr/vue";
import { initalStateLog } from "~/composables/useLog";

export async function useInitialState<T>(key: string, handler: (type: "server" | "client") => Promise<T>, blocking = false): Promise<Ref<T | null>> {
  const { initialState } = useContext();
  const responseValue = ref(initialState[key] || null) as Ref<T | null>;

  // remove data from initialState when component unmounts or deactivates
  const removeState = () => {
    if (!import.meta.env.SSR) {
      initialState[key] = null;
    }
  };

  onUnmounted(removeState);
  onDeactivated(removeState);

  if (import.meta.env.SSR) {
    // if on server, block until data can be stored in initialState
    initalStateLog("do request " + key);
    const data = await handler("server");
    initalStateLog("done " + key);
    initialState[key] = data;
    responseValue.value = data;
  } else {
    // if on client, check if we already have data and use that
    if (initialState[key]) {
      initalStateLog("found " + key);
      responseValue.value = initialState[key];
    } else {
      // else do the request ourselves, blocking if needed
      const fn = async () => {
        responseValue.value = await handler("client");
        initalStateLog("done " + key);
      };
      if (blocking) {
        initalStateLog("block " + key);
        await fn();
      } else {
        initalStateLog("onMounted " + key);
        onMounted(fn);
      }
    }
  }

  return responseValue;
}
