import { defineStore } from "pinia";
import { computed, ref } from "vue";

import { IPlatform, IProjectCategory, IPrompt, IVisibility, Color, FlagReason } from "hangar-internal";
import { Announcement as AnnouncementObject, Announcement, IPermission, Role } from "hangar-api";
import { NamedPermission, Platform, ProjectCategory, Prompt } from "~/types/enums";

import { fetchIfNeeded, useInternalApi } from "~/composables/useApi";
import { Option } from "~/lib/components/ui/InputSelect.vue";

interface Validation {
  regex?: string;
  max?: number;
  min?: number;
}

interface VersionInfo {
  time: string;
  commit: string;
  commitShort: string;
  version: string;
  committer: string;
  message: string;
  tag: string;
  behind: string;
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
  const projectCategories = ref<Map<ProjectCategory, IProjectCategory>>(new Map());
  const permissions = ref<Map<NamedPermission, IPermission>>(new Map());
  const platforms = ref<Map<Platform, IPlatform>>(new Map());
  const validations = ref<Validations | null>(null);
  const prompts = ref<Map<Prompt, IPrompt>>(new Map());
  const announcements = ref<Announcement[]>([]);
  const visibilities = ref<IVisibility[]>([]);
  const licenses = ref<string[]>([]);
  const orgRoles = ref<Role[]>([]);
  const projectRoles = ref<Role[]>([]);
  const globalRoles = ref<Role[]>([]);
  const channelColors = ref<Color[]>([]);
  const flagReasons = ref<FlagReason[]>([]);
  const versionInfo = ref<VersionInfo>();

  async function initBackendData() {
    try {
      // todo consider caching these in node server/globally, these are all always needed
      await Promise.all([
        fetchIfNeeded(async () => {
          const categoryResult = await useInternalApi<IProjectCategory[]>("data/categories");
          for (const c of categoryResult) {
            c.title = "project.category." + c.apiName;
          }
          return convertToMap<ProjectCategory, IProjectCategory>(categoryResult, (value) => value.apiName);
        }, projectCategories),

        fetchIfNeeded(async () => {
          const permissionResultTemp = await useInternalApi<{ value: NamedPermission; frontendName: string; permission: string }[]>("data/permissions");
          const permissionResult: IPermission[] = permissionResultTemp.map(({ value, frontendName, permission }) => ({
            value,
            frontendName,
            permission: BigInt("0b" + permission),
          }));
          return convertToMap<NamedPermission, IPermission>(permissionResult, (value) => value.value);
        }, permissions),

        fetchIfNeeded(async () => {
          const platformResult: IPlatform[] = await useInternalApi<IPlatform[]>("data/platforms");
          return convertToMap<Platform, IPlatform>(platformResult, (value) => value.name.toUpperCase());
        }, platforms),

        fetchIfNeeded(async () => {
          const promptsResult: IPrompt[] = await useInternalApi<IPrompt[]>("data/prompts");
          return convertToMap<Prompt, IPrompt>(promptsResult, (value) => value.name);
        }, prompts),

        fetchIfNeeded(() => useInternalApi<string[]>("data/licenses"), licenses),
        fetchIfNeeded(() => useInternalApi<AnnouncementObject[]>("data/announcements"), announcements),
        fetchIfNeeded(() => useInternalApi<IVisibility[]>("data/visibilities"), visibilities),
        fetchIfNeeded(() => useInternalApi<NonNullable<typeof validations.value>>("data/validations"), validations),
        fetchIfNeeded(() => useInternalApi<NonNullable<typeof orgRoles.value>>("data/orgRoles"), orgRoles),
        fetchIfNeeded(() => useInternalApi<NonNullable<typeof channelColors.value>>("data/channelColors"), channelColors),
        fetchIfNeeded(() => useInternalApi<NonNullable<typeof projectRoles.value>>("data/projectRoles"), projectRoles),
        fetchIfNeeded(() => useInternalApi<NonNullable<typeof globalRoles.value>>("data/globalRoles"), globalRoles),
        fetchIfNeeded(() => useInternalApi<NonNullable<typeof flagReasons.value>>("data/flagReasons"), flagReasons),
        fetchIfNeeded(() => useInternalApi<NonNullable<typeof versionInfo.value>>("data/version-info"), versionInfo),
      ]);
    } catch (e) {
      console.error("ERROR FETCHING BACKEND DATA");
      console.error(e);
    }
  }

  const visibleCategories = computed<IProjectCategory[]>(() => [...(projectCategories.value?.values() || [])].filter((value) => value.visible));
  const visiblePlatforms = computed(() => (platforms.value ? [...platforms.value.values()].filter((value) => value.visible) : []));

  const licenseOptions = computed<Option[]>(() => licenses.value.map<Option>((l) => ({ value: l, text: l })));
  const categoryOptions = computed<Option[]>(() => visibleCategories.value.map<Option>((c) => ({ value: c.apiName, text: c.title })));

  return {
    projectCategories,
    permissions,
    platforms,
    validations,
    prompts,
    licenses,
    announcements,
    visibilities,
    orgRoles,
    projectRoles,
    globalRoles,
    channelColors,
    flagReasons,
    initBackendData,
    visibleCategories,
    visiblePlatforms,
    licenseOptions,
    categoryOptions,
    versionInfo,
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
