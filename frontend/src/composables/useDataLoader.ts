import type { RouteLocationNormalized } from "vue-router";
import type { HangarOrganization, HangarProject, Version, User, ProjectPageTable, GlobalData } from "~/types/backend";
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

// TODO check every handling of the reject stuff (for both composables)
export function useDataLoader<K extends keyof DataLoaderTypes>(key: K) {
  const data = useState<DataLoaderTypes[K] | undefined>(key);

  function loader(
    param: routeParams | undefined,
    to: RouteLocationNormalized,
    from: RouteLocationNormalized,
    loader: (param: string) => Promise<DataLoaderTypes[K]>,
    promises: Promise<any>[],
    lenient = false
  ) {
    const meta = to.meta["dataLoader_" + key];
    if (meta || key === "globalData") {
      const oldParam = param && param in from.params ? (from.params[param as never] as string) : undefined;
      const newParam = param && param in to.params ? (to.params[param as never] as string) : undefined;
      if (data.value && oldParam === newParam) {
        console.log("skip loading", key); // TODO test this
        return newParam;
      } else if (!param || newParam) {
        // sanitize a bit to make undertow happy
        const regex = /["#<>\\^`{|}]/;
        if (newParam && regex.test(newParam)) {
          throw createError({ statusCode: 404, statusMessage: "Not found" });
        }

        promises.push(
          new Promise<void>(async (resolve, reject) => {
            console.log("load loading", key, newParam);
            const result = await loader(newParam!).catch((err) => {
              if (lenient) resolve();
              else reject(err);
            });
            // await new Promise((resolve) => setTimeout(resolve, 5000));
            if (result) {
              data.value = result;
              console.log("load loaded", key, newParam);
              resolve();
            }
          })
        );
        return newParam;
      }
      console.warn("dataLoader " + key + " is miss configured for " + to.path + "! (no param " + param + ")");
      return;
    } else {
      data.value = undefined;
      return;
    }
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
  defaultValue?: T | undefined
) {
  // state tracking is twofold.
  // `state` is used store data in the nuxt payload, so it will be shared between server and client side and on client side navigation
  const state = useState<Record<string, T | undefined>>("useData", () => ({}));
  // `data` is used to store a reference into the state, using the current key. it points to the data we want to return
  // we are not using a computed here, since consumers might manually want to update the data. this kinda corrupts the cache, but we can't do much about it
  const data = ref<T | undefined>();

  const status = ref<"idle" | "loading" | "success" | "error">("idle");
  let promise: Promise<void> | undefined;

  function refresh() {
    console.log("refresh", key(params()));
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
    setState(defaultValue ?? undefined);

    if (skip(params)) {
      console.log("skip", key(params));
      status.value = "idle";
      return;
    }

    return Sentry.startSpan(
      { op: "hangar.data", name: key(params) },
      () =>
        new Promise<void>(async (resolve, reject) => {
          console.log("load", key(params));
          try {
            const result = await loader(params);
            // await new Promise((resolve) => setTimeout(resolve, 5000));
            console.log("loaded", key(params));
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
        console.log("server prefetch", key(params()));
        await promise;
        console.log("server prefetch done", key(params()));
      });
    }
  }

  // when the key changes, we move the data from the old key to the new key
  watch(
    () => key(params()),
    (newKey, oldKey) => {
      if (newKey === oldKey) {
        return;
      }
      const oldState = state.value[oldKey];
      state.value[newKey] = oldState;
      state.value[oldKey] = undefined;
      data.value = oldState;
      console.log("watchKey", newKey, oldKey);
    }
  );

  // when the params change, we load the new data
  watchDebounced(
    params,
    (newParams, oldParams) => {
      if (checkEqual(newParams, oldParams)) {
        console.log("equals");
        return;
      }
      console.log("watch", key(params()), newParams, oldParams, newParams === oldParams, checkEqual(newParams, oldParams));
      load(params());
    },
    { debounce: 250 }
  );

  return { data, status, refresh, promise };
}

function checkEqual(a: Record<string, unknown> | string, b: Record<string, unknown> | string) {
  if (!a) {
    return !b;
  } else if (!b) {
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
