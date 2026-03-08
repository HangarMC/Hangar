import type { RouteLocationNormalized } from "vue-router";
import type { HangarOrganization, HangarProject, Version, User, ProjectPageTable, GlobalData } from "#shared/types/backend";

type routeParams = "user" | "project" | "version" | "page";
type DataLoaderTypes = {
  user: User;
  project: HangarProject;
  version: Version;
  organization: HangarOrganization;
  page: ProjectPageTable;
  globalData: GlobalData;
};

// Route-level data loader used by middleware to fetch data during navigation.
// Data is stored in Nuxt's useState for SSR payload sharing and also seeded
// into the Pinia Colada query cache so that subsequent useQuery calls are
// pre-populated.
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
        return newParam;
      } else if (!param || newParam) {
        // sanitize a bit to make undertow happy
        const regex = /["#<>\\^`{|}]/;
        if (newParam && regex.test(newParam)) {
          throw createError({ statusCode: 404, statusMessage: "Not found" });
        }

        promises.push(
          new Promise<void>(async (resolve, reject) => {
            const result = await loader(newParam!).catch((err) => {
              if (lenient) resolve();
              else reject(err);
            });
            if (result) {
              data.value = result;
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
