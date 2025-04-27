import { isAxiosError } from "axios";
import type { HangarOrganization, HangarProject, Version, User, ProjectPageTable } from "~/types/backend";
import { useDataLoader } from "~/composables/useDataLoader";

// this middleware takes care of fetching the "important" data for pages, like user/project/org/version/page, based on route params
// it also handles 404s and redirects to the proper casing
export default defineNuxtRouteMiddleware(async (to, from) => {
  // don't call on router.replace when we just update the query
  if (import.meta.client && to.path === from?.path) {
    return;
  }

  // if we are in an error state, dont waste time loading this data
  const error = useError();
  if (error.value) {
    return;
  }

  const promises: Promise<any>[] = [];

  const { loader: userLoader, data: user } = useDataLoader("user");
  const userName = userLoader("user", to, from, (userName) => useApi<User>("users/" + userName + "?resolveId=false"), promises);

  const { loader: projectLoader, data: project } = useDataLoader("project");
  const projectName = projectLoader(
    "project",
    to,
    from,
    (projectName) => useInternalApi<HangarProject>("projects/project/" + projectName + "?resolveId=false"),
    promises
  );

  // TODO ideally we only make this request _after_ the user request returned and we know we need to fetch an org
  // alternatively we could make a new controller that returns both
  const { loader: organizationLoader } = useDataLoader("organization");
  organizationLoader(
    "user",
    to,
    from,
    (organizationName) => useInternalApi<HangarOrganization>("organizations/org/" + organizationName + "?resolveId=false"),
    promises,
    true
  );

  const { loader: versionLoader, data: version } = useDataLoader("version");
  const versionName = versionLoader(
    "version",
    to,
    from,
    (versionName) => {
      if ("project" in to.params) {
        return useApi<Version>(`projects/${to.params.project}/versions/${versionName}?resolveId=false"`);
      }
      throw createError({ statusCode: 500, statusMessage: "No project param?!" });
    },
    promises
  );

  const { loader: pageLoader, data: page } = useDataLoader("page");
  const pageName = pageLoader(
    "page",
    to,
    from,
    (pagePath) => {
      if ("project" in to.params) {
        return useInternalApi<ProjectPageTable>(`pages/page/${to.params.project}/` + pagePath.toString().replaceAll(",", "/"));
      }
      throw createError({ statusCode: 500, statusMessage: "No project param?!" });
    },
    promises
  ) as string[] | undefined;

  if (import.meta.server && promises?.length) {
    try {
      await Promise.all(promises);
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 404) {
        throw createError({ statusCode: 404, statusMessage: "Not found" });
      } else {
        console.error("Failed to load data for route " + to.fullPath, err);
        throw createError({ statusCode: 500, statusMessage: "Failed to load data" });
      }
    }

    // check if we need to redirect to a proper casing
    let newPath = to.fullPath;
    if (userName && user.value && user.value.name !== userName) {
      newPath = newPath.replace(userName, user.value.name);
    }
    if (projectName && project.value && project.value.name !== projectName) {
      newPath = newPath.replace(projectName, project.value.name);
    }
    if (versionName && version.value && version.value.name !== versionName) {
      newPath = newPath.replace(versionName, version.value.name);
    }
    if (pageName) {
      const pageSlug = pageName.join("/");
      if (page.value && page.value.slug !== pageSlug) {
        newPath = newPath.replace(pageSlug, page.value.slug);
      }
    }
    // check if we need to redirect to proper owner
    if (projectName && userName && project.value && project.value.namespace.owner !== userName) {
      newPath = newPath.replace(userName, project.value.namespace.owner);
    }

    // do redirect
    if (newPath !== to.fullPath) {
      console.log("Redirect to " + newPath + " from (" + to.fullPath + ")");
      return navigateTo(newPath, { redirectCode: 301 });
    }
  }
});
