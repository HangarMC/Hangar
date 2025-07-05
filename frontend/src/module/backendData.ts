import { promises as fs } from "node:fs";
import { defineNuxtModule } from "@nuxt/kit";
import { type AxiosInstance, isAxiosError } from "axios";
import axios from "axios";
// noinspection ES6PreferShortImport
import { backendDataLog } from "../composables/useLog";
import type { RoleData, Security, Validations, CategoryData, PermissionData, VisibilityData, ColorData, FlagReasonData, PromptData } from "~/types/backend";
import type { ServerBackendData } from "~/types/backendData";

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

async function generateBackendData(state: ServerBackendData, path: string, retry = true) {
  const axiosInstance = prepareAxios(state.meta.apiUrl);

  try {
    backendDataLog("Generating backend data (" + state.meta.apiUrl + ")...");
    await loadData(state, axiosInstance);

    backendDataLog("state", state);

    await fs.writeFile(path, JSON.stringify(state));

    backendDataLog("Backend data generated!");
  } catch (err) {
    if (isAxiosError(err)) {
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

async function loadState(path: string): Promise<ServerBackendData> {
  let state = {} as ServerBackendData;
  try {
    const data = await fs.readFile(path, "utf8");
    state = JSON.parse(data);
  } catch {
    // File doesn't exist, create folder
    await fs.mkdir("./generated", { recursive: true });
  }
  return state;
}

function needsRefresh(state: ServerBackendData, ttl: number, serverUrl: string, version: number) {
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
    if (isAxiosError(err) && err.response?.status === 404) {
      backendDataLog("Couldn't load " + err.request?.path + ", skipping");
      return;
    }
    throw err;
  });
  return axiosInstance;
}

async function loadData(state: ServerBackendData, axiosInstance: AxiosInstance) {
  const result = await Promise.all([
    axiosInstance.get<CategoryData[]>("/categories"),
    axiosInstance.get<PermissionData[]>("/permissions"),
    axiosInstance.get<Validations>("/validations"),
    axiosInstance.get<PromptData[]>("/prompts"),
    axiosInstance.get<VisibilityData[]>("/visibilities"),
    axiosInstance.get<string[]>("/licenses"),
    axiosInstance.get<RoleData[]>("/orgRoles"),
    axiosInstance.get<RoleData[]>("/projectRoles"),
    axiosInstance.get<RoleData[]>("/globalRoles"),
    axiosInstance.get<ColorData[]>("/channelColors"),
    axiosInstance.get<FlagReasonData[]>("/flagReasons"),
    axiosInstance.get<string[]>("/loggedActions"),
    axiosInstance.get<Security>("/security"),
  ]);
  const [
    projectCategories,
    permissions,
    validations,
    prompts,
    visibilities,
    licenses,
    orgRoles,
    projectRoles,
    globalRoles,
    channelColors,
    flagReasons,
    loggedActions,
    security,
  ] = result.map((it) => it?.data || it);

  state.projectCategories = projectCategories as typeof state.projectCategories;
  state.permissions = permissions as typeof state.permissions;
  state.validations = validations as typeof state.validations;
  state.prompts = prompts as typeof state.prompts;
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
