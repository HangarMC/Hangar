import type { RouteLocationNormalized } from "vue-router";
import type { ExtendedProjectPage, HangarOrganization, HangarProject, HangarVersion, User } from "~/types/backend";

type routeParams = "user" | "project" | "version" | "page";
type DataLoaderTypes = {
  user: User;
  project: HangarProject;
  version: HangarVersion;
  organization: HangarOrganization;
  page: ExtendedProjectPage;
};

// TODO check every handling of the reject stuff (for both composables)
export function useDataLoader<K extends keyof DataLoaderTypes>(key: K) {
  const data = useState<DataLoaderTypes[K] | undefined>(key);

  function loader(
    param: routeParams,
    to: RouteLocationNormalized,
    from: RouteLocationNormalized,
    loader: (param: string) => Promise<DataLoaderTypes[K]>,
    promises: Promise<any>[]
  ) {
    const meta = to.meta["dataLoader_" + key];
    if (meta) {
      const oldParam = param in from.params ? (from.params[param as never] as string) : undefined;
      const newParam = param in to.params ? (to.params[param as never] as string) : undefined;
      if (data.value && oldParam === newParam) {
        console.log("skip loading", key); // TODO test this
        return newParam;
      } else if (newParam) {
        promises.push(
          new Promise<void>(async (resolve, reject) => {
            console.log("load loading", key);
            const result = await loader(newParam).catch(reject);
            // await new Promise((resolve) => setTimeout(resolve, 5000));
            if (result) {
              data.value = result;
              console.log("load loaded", key);
              resolve();
            }
          })
        );
        return newParam;
      }
      console.warn("dataLoader " + key + " is miss configured for " + to.path + "! (no param " + param + ")");
      return undefined;
    } else {
      data.value = undefined;
      return undefined;
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
  defaultValue: T | undefined = undefined
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
    return load(params());
  }

  function setState(newState?: T) {
    state.value[key(params())] = newState;
    data.value = newState;
  }

  if (import.meta.server && !server) {
    return { data, status, refresh };
  }

  function load(params: P) {
    status.value = "loading";
    setState(defaultValue || undefined);

    if (skip(params)) {
      console.log("skip", key(params));
      status.value = "idle";
      return undefined;
    }
    return new Promise<void>(async (resolve, reject) => {
      console.log("load", key(params));
      const result = await loader(params).catch(reject);
      //await new Promise((resolve) => setTimeout(resolve, 5000));
      console.log("loaded", key(params));
      if (result) {
        setState(result);
        status.value = "success";
        callback(params);
        resolve();
      } else {
        status.value = "error";
      }
    });
  }

  // load initial state
  data.value = state.value[key(params())];
  // if we have no state, queue a load
  if (!data.value) {
    promise = load(params());

    // if on server (and we dont wanna skip server fetching, we need await the promise onServerPrefetch)
    if (import.meta.server && server && promise) {
      onServerPrefetch(async () => {
        console.log("server prefetch", key(params()));
        await promise;
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

  for (let key of keys1) {
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
