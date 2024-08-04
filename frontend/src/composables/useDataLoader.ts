import type { RouteLocationNormalized } from "vue-router";

type dataLoaders = "user" | "project" | "version" | "organization";
type routeParams = "user" | "project" | "version" | "organization";

// TODO check every handling of the reject stuff (for both composables)

export function useDataLoader<T>(key: dataLoaders) {
  const data = useState<T | undefined>(key);

  function loader(
    param: routeParams,
    to: RouteLocationNormalized,
    from: RouteLocationNormalized,
    loader: (param: string) => Promise<T>,
    promises: Promise<any>[]
  ) {
    const meta = to.meta["dataLoader_" + key];
    if (meta) {
      const oldParam = key in from.params ? (from.params[param as never] as string) : undefined;
      const newParam = key in to.params ? (to.params[param as never] as string) : undefined;
      if (data.value && oldParam === newParam) {
        console.log("skip loading", key); // TODO test this
        return newParam;
      } else if (newParam) {
        promises.push(
          new Promise<void>(async (resolve, reject) => {
            console.log("load loading", param);
            const result = await loader(newParam).catch(reject);
            // await new Promise((resolve) => setTimeout(resolve, 5000));
            if (result) {
              data.value = result;
              console.log("load loaded", param);
              resolve();
            }
          })
        );
        return newParam;
      }
      console.log("dataLoader " + key + " is miss configured for " + to.path + "!");
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
  // TODO make this reactive somehow
  const data = useState<T | undefined>(key(params()));
  const status = ref<"idle" | "loading" | "success" | "error">("idle");

  function refresh() {
    return load(params());
  }

  if (import.meta.server && !server) {
    return { data, status, refresh };
  }

  function load(params: P) {
    status.value = "loading";
    if (defaultValue) {
      data.value = defaultValue;
    } else {
      data.value = undefined;
    }

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
        data.value = result;
        status.value = "success";
        callback(params);
        resolve();
      } else {
        status.value = "error";
      }
    });
  }

  if (!data.value) {
    const promise = load(params());

    if (import.meta.server && server && promise) {
      onServerPrefetch(async () => {
        console.log("server prefetch", key(params()));
        await promise;
      });
    }
  }

  watchDebounced(
    params,
    (newParams, oldParams) => {
      if (shallowEqual(newParams, oldParams)) {
        console.log("equals");
        return;
      }
      console.log("watch", newParams, oldParams, newParams === oldParams);
      load(params());
    },
    { debounce: 250 }
  );

  return { data, status, refresh };
}

function shallowEqual(a: Record<string, unknown> | string, b: Record<string, unknown> | string) {
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
      return false;
    }
  }

  return true;
}
