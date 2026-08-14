import type { RouteLocationNormalized } from "vue-router";
import type { HangarOrganization, HangarProject, Version, User, ProjectPageTable, GlobalData } from "#shared/types/backend";
import * as Sentry from "@sentry/nuxt";

type routeParams = "user" | "project" | "version" | "page";
type DataLoaderTypes = {
  user: User;
  project: HangarProject;
  version: Version;
  organization: HangarOrganization;
  page: ProjectPageTable;
  globalData: GlobalData;
};

function paramKey(param: unknown): string | undefined {
  if (param === undefined) return undefined;
  return Array.isArray(param) ? param.join("/") : String(param);
}

// TODO check every handling of the reject stuff (for both composables)
export function useDataLoader<K extends keyof DataLoaderTypes>(key: K) {
  const data = useState<DataLoaderTypes[K] | undefined>(key);
  const loadedParam = useState<string | undefined>(`dataLoader:${key}:param`);
  const requestId = useState<number>(`dataLoader:${key}:request`, () => 0);

  function loader(
    param: routeParams | undefined,
    to: RouteLocationNormalized,
    _from: RouteLocationNormalized,
    loader: (param: string) => Promise<DataLoaderTypes[K] | undefined>,
    promises: Promise<any>[],
    lenient = false
  ) {
    const meta = to.meta["dataLoader_" + key];
    if (meta || key === "globalData") {
      const newParam = param && param in to.params ? (to.params[param as never] as string) : undefined;
      // array-valued params (the catch-all "page" segment) get reactive-wrapped by useState, so raw === never matches
      const newParamKey = paramKey(newParam);
      if (data.value && paramKey(loadedParam.value) === newParamKey) {
        dataLoaderLog("skip loading", key); // TODO test this
        return newParam;
      }
      if (!param || newParam) {
        // sanitize a bit to make undertow happy
        const regex = /["#<>\\^`{|}]/;
        if (newParam && regex.test(newParam)) {
          throw createError({ statusCode: 404, statusMessage: "Not found" });
        }

        loadedParam.value = newParam;
        data.value = undefined;
        const currentRequestId = ++requestId.value;
        promises.push(
          new Promise<void>(async (resolve, reject) => {
            dataLoaderLog("load loading", key, newParam);
            const result = await loader(newParam!).catch((err) => {
              if (lenient) resolve();
              else reject(err);
            });
            // await new Promise((resolve) => setTimeout(resolve, 5000));
            if (result && requestId.value === currentRequestId && paramKey(loadedParam.value) === newParamKey) {
              data.value = result;
              dataLoaderLog("load loaded", key, newParam);
            }
            resolve();
          })
        );
        return newParam;
      }
      console.warn("dataLoader " + key + " is miss configured for " + to.path + "! (no param " + param + ")");
    } else {
      requestId.value++;
      // blanking the data would skeleton the outgoing page; dropping the param already forces a reload later
      loadedParam.value = undefined;
    }
    return;
  }

  return { loader, data };
}

export function useData<T, P extends Record<string, unknown> | string>(
  params: () => P,
  key: (params: P) => string,
  loader: (params: P) => Promise<T>,
  server = true,
  skip: (params: P) => boolean = () => false,
  callback: (params: P) => void = () => {},
  defaultValue?: T | undefined,
  /**
  Keep the previous result on screen while revalidating instead of blanking it.
  */
  keepPreviousData = false
) {
  // state tracking is twofold.
  // `state` is used store data in the nuxt payload, so it will be shared between server and client side and on client side navigation
  const state = useState<Record<string, T | undefined>>("useData", () => ({}));
  // `data` is used to store a reference into the state, using the current key. it points to the data we want to return
  // we are not using a computed here, since consumers might manually want to update the data. this kinda corrupts the cache, but we can't do much about it
  const data = ref<T | undefined>();

  const status = ref<"idle" | "loading" | "success" | "error">("idle");
  // eslint-disable-next-line unicorn/no-declarations-before-early-exit
  let promise: Promise<void> | undefined;

  function refresh() {
    dataLoaderLog("refresh", key(params()));
    return load(params());
  }

  function setState(newState?: T) {
    state.value[key(params())] = newState;
    data.value = newState;
  }

  if (import.meta.server && !server) {
    setState(defaultValue ?? undefined);
    return { data, status, refresh };
  }

  function load(params: P) {
    status.value = "loading";
    if (!keepPreviousData) {
      setState(defaultValue ?? undefined);
    }

    if (skip(params)) {
      dataLoaderLog("skip", key(params));
      setState(defaultValue ?? undefined);
      status.value = "idle";
      return;
    }

    return Sentry.startSpan(
      { op: "hangar.data", name: key(params) },
      () =>
        new Promise<void>(async (resolve, reject) => {
          dataLoaderLog("load", key(params));
          try {
            const result = await loader(params);
            // await new Promise((resolve) => setTimeout(resolve, 5000));
            dataLoaderLog("loaded", key(params));
            setState(result);
            status.value = "success";
            callback(params);
            resolve();
          } catch (err) {
            status.value = "error";
            callback(params);
            reject(err);
          }
        })
    );
  }

  // load initial state
  data.value = state.value[key(params())];
  // if we have no state, queue a load
  if (data.value === undefined) {
    promise = load(params());

    // if on server (and we dont wanna skip server fetching, we need await the promise onServerPrefetch)
    if (import.meta.server && server && promise) {
      onServerPrefetch(async () => {
        dataLoaderLog("server prefetch", key(params()));
        await promise;
        dataLoaderLog("server prefetch done", key(params()));
      });
    }
  }

  // `state` is shared app-wide, so the old value must never be written under the new key
  watch(
    () => key(params()),
    (newKey, oldKey) => {
      if (newKey === oldKey) {
        return;
      }
      const oldState = state.value[oldKey];
      state.value[oldKey] = undefined;
      data.value = state.value[newKey] ?? (keepPreviousData ? oldState : defaultValue);
      dataLoaderLog("watchKey", newKey, oldKey);
    }
  );

  // when the params change, we load the new data
  watchDebounced(
    params,
    (newParams, oldParams) => {
      if (checkEqual(newParams, oldParams)) {
        dataLoaderLog("equals");
        return;
      }
      dataLoaderLog("watch", key(params()), newParams, oldParams, newParams === oldParams, checkEqual(newParams, oldParams));
      load(params());
    },
    { debounce: 250 }
  );

  return { data, status, refresh, promise };
}

function checkEqual(a: Record<string, unknown> | string, b: Record<string, unknown> | string) {
  if (!a) {
    return !b;
  }
  if (!b) {
    return false;
  }

  if (typeof a === "string" || typeof b === "string") {
    return a === b;
  }

  const keys1 = Object.keys(a);
  const keys2 = Object.keys(b);

  if (keys1.length !== keys2.length) {
    return false;
  }

  for (const key of keys1) {
    if (a[key] !== b[key]) {
      if (typeof a[key] === "object" && typeof b[key] === "object") {
        if (!checkEqual(a[key] as Record<string, unknown>, b[key] as Record<string, unknown>)) {
          return false;
        }
      } else {
        return false;
      }
    }
  }

  return true;
}
