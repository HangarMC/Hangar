import { promises as fs } from "fs";
import { defineNuxtModule } from "@nuxt/kit";
import type { BackendData } from "hangar-api";
import type { AxiosInstance } from "axios";
import axios from "axios";
import type { IProjectCategory } from "hangar-internal";
// noinspection ES6PreferShortImport
import { backendDataLog } from "../composables/useLog";

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
      const state = await loadState(moduleOptions.path);

      if (!needsRefresh(state, moduleOptions.ttl, moduleOptions.serverUrl, moduleOptions.version)) return;

      state.meta = {
        lastGenerated: new Date().toISOString(),
        apiUrl: moduleOptions.serverUrl,
        version: moduleOptions.version,
      };

      await generateBackendData(state, moduleOptions.path);
    });
  },
});

async function generateBackendData(state: BackendData, path: string, retry = true) {
  const axiosInstance = prepareAxios(state.meta.apiUrl);

  try {
    backendDataLog("Generating backend data (" + state.meta.apiUrl + ")...");
    await loadData(state, axiosInstance);

    backendDataLog("state", state);

    await fs.writeFile(path, JSON.stringify(state));

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
    if (retry) {
      backendDataLog("Try running against production...");
      state.meta.apiUrl = "https://hangar.papermc.io";
      await generateBackendData(state, path, false);
    } else {
      await fs.writeFile(path, JSON.stringify({}));
    }
  }
}

async function loadState(path: string): Promise<BackendData> {
  let state = {} as BackendData;
  try {
    const data = await fs.readFile(path, "utf8");
    state = JSON.parse(data);
  } catch {
    // File doesn't exist, create folder
    await fs.mkdir("./generated", { recursive: true });
  }
  return state;
}

function needsRefresh(state: BackendData, ttl: number, serverUrl: string, version: number) {
  if (
    // Skip regeneration if within TTL...
    state?.meta?.lastGenerated &&
    new Date(state.meta.lastGenerated).getTime() + ttl > Date.now() &&
    // ...but only if the API URL is the same
    state.meta.apiUrl === serverUrl &&
    // ...and the version is the same
    state.meta.version === version
  ) {
    backendDataLog("Not generating backend data since its still valid, lastGenerated was", new Date(state.meta.lastGenerated));
    return false;
  }
  return true;
}

function prepareAxios(serverUrl: string): AxiosInstance {
  const axiosInstance = axios.create({
    headers: {
      "user-agent": `Hangar Backend Data Generator (bots@papermc.io)`,
    },
    baseURL: serverUrl + "/api/internal/data",
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
  return axiosInstance;
}

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
    security,
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
      axiosInstance.get<typeof state.security>("/security"),
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
  state.security = security as typeof state.security;
}
