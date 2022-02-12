import type { Ref } from "vue";
import { onDeactivated, onMounted, onUnmounted, ref } from "vue";
import { useContext } from "vite-ssr/vue";

export async function useInitialState<T>(key: string, handler: (type: "server" | "client") => Promise<T>, blocking = false) {
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
        const data = await handler("server");
        initialState[key] = data;
        responseValue.value = data;
    } else {
        // if on server, check if we already have data and use that
        if (initialState[key]) {
            responseValue.value = initialState[key];
        } else {
            // else do the request ourselves, blocking if needed
            const fn = async () => await handler("client");
            if (blocking) {
                await fn();
            } else {
                onMounted(fn);
            }
        }
    }

    return responseValue;
}
