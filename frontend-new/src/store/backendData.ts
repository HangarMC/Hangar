import { defineStore } from "pinia";
import { computed, ref } from "vue";

import { IPlatform, IProjectCategory, IPrompt } from "hangar-internal";
import { NamedPermission, Platform, ProjectCategory, Prompt } from "~/types/enums";

import { Announcement as AnnouncementObject, Announcement, IPermission } from "hangar-api";
import { useInternalApi } from "~/composables/useApi";

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
  const projectCategories = ref<Map<ProjectCategory, IProjectCategory>>(new Map<ProjectCategory, IProjectCategory>());
  const permissions = ref<Map<NamedPermission, IPermission>>(new Map<NamedPermission, IPermission>());
  const platforms = ref<Map<Platform, IPlatform>>(new Map<Platform, IPlatform>());
  const validations = ref<Validations | null>(null);
  const prompts = ref<Map<Prompt, IPrompt>>(new Map<Prompt, IPrompt>());
  const announcements = ref<Announcement[]>([]);
  const licenses = ref<string[]>([]);

  async function initBackendData() {
    try {
      // todo make these run concurrently to speed up stuff
      const categoryResult = await useInternalApi<IProjectCategory[]>("data/categories", false);
      for (const c of categoryResult) {
        // TODO translate
        //c.title = useI18n().t("project.category." + c.apiName) as string;
        c.title = "project.category." + c.apiName;
      }
      projectCategories.value = convertToMap<ProjectCategory, IProjectCategory>(categoryResult, (value) => value.apiName);

      const permissionResultTemp = await useInternalApi<{ value: NamedPermission; frontendName: string; permission: string }[]>("data/permissions", false);
      const permissionResult: IPermission[] = permissionResultTemp.map(({ value, frontendName, permission }) => ({
        value,
        frontendName,
        permission: BigInt("0b" + permission),
      }));
      permissions.value = convertToMap<NamedPermission, IPermission>(permissionResult, (value) => value.value);

      const platformResult: IPlatform[] = await useInternalApi<IPlatform[]>("data/platforms", false);
      platforms.value = convertToMap<Platform, IPlatform>(platformResult, (value) => value.name.toUpperCase());

      const promptsResult: IPrompt[] = await useInternalApi<IPrompt[]>("data/prompts", false);
      prompts.value = convertToMap<Prompt, IPrompt>(promptsResult, (value) => value.name);

      licenses.value = await useInternalApi<string[]>("data/licenses", false);

      announcements.value = await useInternalApi<AnnouncementObject[]>("data/announcements", false);
    } catch (e) {
      console.error("ERROR FETCHING BACKEND DATA");
      console.error(e);
    }
  }

  const visibleCategories = computed(() => [...projectCategories.value.values()].filter((value) => value.visible));
  const visiblePlatforms = computed(() => [...platforms.value.values()].filter((value) => value.visible));

  return { projectCategories, permissions, platforms, validations, prompts, licenses, announcements, initBackendData, visibleCategories, visiblePlatforms };
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
