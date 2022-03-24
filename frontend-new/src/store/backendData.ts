import { defineStore } from "pinia";
import { computed, ref } from "vue";

import { IPlatform, IProjectCategory, IPrompt, IVisibility } from "hangar-internal";
import { NamedPermission, Platform, ProjectCategory, Prompt } from "~/types/enums";

import { Announcement as AnnouncementObject, Announcement, IPermission } from "hangar-api";
import { fetchIfNeeded, useInternalApi } from "~/composables/useApi";

interface Validation {
  regex?: string;
  max?: number;
  min?: number;
}

interface Validations {
  project: {
    name: Validation;
    desc: Validation;
    keywords: Validation;
    channels: Validation;
    pageName: Validation;
    pageContent: Validation;
    maxPageCount: number;
    maxChannelCount: number;
  };
  org: Validation;
  userTagline: Validation;
  version: Validation;
  maxOrgCount: number;
  urlRegex: string;
}

export const useBackendDataStore = defineStore("backendData", () => {
  const projectCategories = ref<Map<ProjectCategory, IProjectCategory> | null>(null);
  const permissions = ref<Map<NamedPermission, IPermission> | null>(null);
  const platforms = ref<Map<Platform, IPlatform> | null>(null);
  const validations = ref<Validations | null>(null);
  const prompts = ref<Map<Prompt, IPrompt> | null>(null);
  const announcements = ref<Announcement[]>([]);
  const visibilities = ref<IVisibility[]>([]);
  const licenses = ref<string[]>([]);

  async function initBackendData() {
    try {
      // todo make these run concurrently to speed up stuff
      await fetchIfNeeded(async () => {
        const categoryResult = await useInternalApi<IProjectCategory[]>("data/categories", false);
        for (const c of categoryResult) {
          c.title = "project.category." + c.apiName;
        }
        return convertToMap<ProjectCategory, IProjectCategory>(categoryResult, (value) => value.apiName);
      }, projectCategories);

      await fetchIfNeeded(async () => {
        const permissionResultTemp = await useInternalApi<{ value: NamedPermission; frontendName: string; permission: string }[]>("data/permissions", false);
        const permissionResult: IPermission[] = permissionResultTemp.map(({ value, frontendName, permission }) => ({
          value,
          frontendName,
          permission: BigInt("0b" + permission),
        }));
        return convertToMap<NamedPermission, IPermission>(permissionResult, (value) => value.value);
      }, permissions);

      await fetchIfNeeded(async () => {
        const platformResult: IPlatform[] = await useInternalApi<IPlatform[]>("data/platforms", false);
        return convertToMap<Platform, IPlatform>(platformResult, (value) => value.name.toUpperCase());
      }, platforms);

      await fetchIfNeeded(async () => {
        const promptsResult: IPrompt[] = await useInternalApi<IPrompt[]>("data/prompts", false);
        return convertToMap<Prompt, IPrompt>(promptsResult, (value) => value.name);
      }, prompts);

      await fetchIfNeeded(async () => await useInternalApi<string[]>("data/licenses", false), licenses);

      await fetchIfNeeded(async () => await useInternalApi<AnnouncementObject[]>("data/announcements", false), announcements);

      await fetchIfNeeded(async () => await useInternalApi<IVisibility[]>("data/visibilities", false), visibilities);

      await fetchIfNeeded(async () => await useInternalApi("data/validations", false), validations);
    } catch (e) {
      console.error("ERROR FETCHING BACKEND DATA");
      console.error(e);
    }
  }

  const visibleCategories = computed(() => [...(projectCategories.value?.values() || [])].filter((value) => value.visible));
  const visiblePlatforms = computed(() => (platforms.value ? [...platforms.value.values()].filter((value) => value.visible) : []));

  return {
    projectCategories,
    permissions,
    platforms,
    validations,
    prompts,
    licenses,
    announcements,
    visibilities,
    initBackendData,
    visibleCategories,
    visiblePlatforms,
  };
});

function convertToMap<E, T>(values: T[], toStringFunc: (value: T) => string): Map<E, T> {
  const map = new Map<E, T>();
  for (const value of values) {
    const key: E = toStringFunc(value) as unknown as E;
    if (key == null) {
      throw new Error("Could not find an enum for " + value);
    }
    map.set(key, value);
  }
  return map;
}
