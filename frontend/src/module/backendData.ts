import { promises as fs } from "fs";
import { defineNuxtModule } from "@nuxt/kit";
import { BackendData } from "hangar-api";
import axios, { AxiosInstance } from "axios";
import { IProjectCategory } from "hangar-internal";
// noinspection ES6PreferShortImport
import { backendDataLog } from "../lib/composables/useLog";

// inspired by knossos build hook: https://github.com/modrinth/knossos/blob/master/nuxt.config.js
export default defineNuxtModule({
  meta: {
    name: "backendData",
    configKey: "backendData",
  },
  defaults: {
    serverUrl: "https://hangar.papermc.dev",
    path: "./src/generated/backendData.json",
    ttl: 30 * 60 * 1000, // 30 min
    version: 1,
  },
  setup(moduleOptions, nuxt) {
    nuxt.hook("prepare:types", async () => {
      let state = {} as BackendData;
      try {
        const data = await fs.readFile(moduleOptions.path, "utf8");
        state = JSON.parse(data);
      } catch {
        // File doesn't exist, create folder
        await fs.mkdir("./generated", { recursive: true });
      }

      if (
        // Skip regeneration if within TTL...
        state?.meta?.lastGenerated &&
        new Date(state.meta.lastGenerated).getTime() + moduleOptions.ttl > Date.now() &&
        // ...but only if the API URL is the same
        state.meta.apiUrl === moduleOptions.serverUrl &&
        // ...and the version is the same
        state.meta.version === moduleOptions.version
      ) {
        backendDataLog("Not generating backend data since its still valid, lastGenerated was", new Date(state.meta.lastGenerated));
        return;
      }

      backendDataLog("Generating backend data...");
      state.meta = {
        lastGenerated: new Date().toISOString(),
        apiUrl: moduleOptions.serverUrl,
        version: moduleOptions.version,
      };

      const axiosInstance = axios.create({
        headers: {
          "user-agent": `Hangar Backend Data Generator (bots@papermc.io)`,
        },
        baseURL: moduleOptions.serverUrl + "/api/internal/data",
      });
      axiosInstance.interceptors.response.use(undefined, (err) => {
        if (axios.isAxiosError(err)) {
          if (err.response?.status === 404) {
            backendDataLog("Couldn't load " + err.request?.path + ", skipping");
            return null;
          }
        }
        throw err;
      });

      try {
        await loadData(state, axiosInstance);

        backendDataLog("state", state);

        await fs.writeFile(moduleOptions.path, JSON.stringify(state));

        backendDataLog("Backend data generated!");
      } catch (err) {
        if (axios.isAxiosError(err)) {
          const transformedError = {
            code: err?.code,
            requestUrl: err?.request?.path,
            status: err?.response?.status,
            data: err?.response?.data,
          };
          backendDataLog("Axios error while generating backend data:", transformedError);
        } else {
          backendDataLog("Unknown error while generating backend data:", err);
        }
      }
    });
  },
});

async function loadData(state: BackendData, axiosInstance: AxiosInstance) {
  const [
    projectCategories,
    permissions,
    platforms,
    validations,
    prompts,
    announcements,
    visibilities,
    licenses,
    orgRoles,
    projectRoles,
    globalRoles,
    channelColors,
    flagReasons,
    loggedActions,
  ] = (
    await Promise.all([
      axiosInstance.get<typeof state.projectCategories>("/categories"),
      axiosInstance.get<typeof state.permissions>("/permissions"),
      axiosInstance.get<typeof state.platforms>("/platforms"),
      axiosInstance.get<typeof state.validations>("/validations"),
      axiosInstance.get<typeof state.prompts>("/prompts"),
      axiosInstance.get<typeof state.announcements>("/announcements"),
      axiosInstance.get<typeof state.visibilities>("/visibilities"),
      axiosInstance.get<typeof state.licenses>("/licenses"),
      axiosInstance.get<typeof state.orgRoles>("/orgRoles"),
      axiosInstance.get<typeof state.projectRoles>("/projectRoles"),
      axiosInstance.get<typeof state.globalRoles>("/globalRoles"),
      axiosInstance.get<typeof state.channelColors>("/channelColors"),
      axiosInstance.get<typeof state.flagReasons>("/flagReasons"),
      axiosInstance.get<typeof state.loggedActions>("/loggedActions"),
    ])
  ).map((it) => it?.data || it);

  for (const c of projectCategories as unknown as IProjectCategory[]) {
    c.title = "project.category." + c.apiName;
  }

  state.projectCategories = projectCategories as typeof state.projectCategories;
  state.permissions = permissions as typeof state.permissions;
  state.platforms = platforms as typeof state.platforms;
  state.validations = validations as typeof state.validations;
  state.prompts = prompts as typeof state.prompts;
  state.announcements = announcements as typeof state.announcements;
  state.visibilities = visibilities as typeof state.visibilities;
  state.licenses = licenses as typeof state.licenses;
  state.orgRoles = orgRoles as typeof state.orgRoles;
  state.projectRoles = projectRoles as typeof state.projectRoles;
  state.globalRoles = globalRoles as typeof state.globalRoles;
  state.channelColors = channelColors as typeof state.channelColors;
  state.flagReasons = flagReasons as typeof state.flagReasons;
  state.loggedActions = loggedActions as typeof state.loggedActions;
}
