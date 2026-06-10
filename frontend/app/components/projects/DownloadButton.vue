<script lang="ts" setup>
import type { Platform, HangarProject, Version, PinnedVersion } from "#shared/types/backend";

const i18n = useI18n();

const props = withDefaults(
  defineProps<{
    project: HangarProject;
    small?: boolean;
    showVersions?: boolean;
    showSinglePlatform?: boolean;
    dropdownPlacement?: "bottom" | "top" | "left" | "right" | "bottom-end" | "bottom-start";
    // Define either version and platform or pinnedVersion, or neither to use main channel versions
    platform?: Platform;
    version?: Version;
    pinnedVersion?: PinnedVersion;
    showFileSize?: boolean;
    fixedWidth?: boolean;
  }>(),
  {
    small: false,
    showVersions: true,
    showSinglePlatform: true,
    dropdownPlacement: "bottom-end",
    platform: undefined,
    version: undefined,
    pinnedVersion: undefined,
    showFileSize: false,
    fixedWidth: false,
  }
);

function downloadLink(platform: string | undefined, version: Version | PinnedVersion | undefined) {
  if (!version || !platform) return;
  return version.downloads[platform]?.externalUrl ?? version.downloads[platform]?.downloadUrl;
}

function isExternal(platform: string | undefined, version: Version | PinnedVersion | undefined): boolean {
  if (!version || !platform) return false;
  return !!version.downloads[platform]?.externalUrl;
}

const singlePlatform = computed<Platform | undefined>(() => {
  if (props.version) {
    const keys = Object.keys(props.version.downloads);
    if (keys.length === 1) {
      return keys[0] as Platform;
    }
  } else if (props.project?.mainChannelVersions) {
    const keys = Object.keys(props.project.mainChannelVersions);
    if (keys.length === 1) {
      return keys[0] as Platform;
    }
  }
  return props.platform;
});
const singleVersion = computed<Version | undefined>(() => {
  if (!props.version && props.project?.mainChannelVersions && singlePlatform.value) {
    return props.project.mainChannelVersions[singlePlatform.value];
  }
  return props.version;
});

const platformDownloadLink = computed(() => downloadLink(singlePlatform.value, singleVersion.value));

function trackDownload(platform: string, version: { id?: number; versionId?: number }) {
  // hangar version has id, pinned version has versionId...
  const id = version.id || version.versionId;
  useInternalApi(`versions/version/${id}/${platform}/track`);
}

function formatVersionRange(versions?: string[]): string {
  if (!versions) return "";
  // In download buttons, only show the latest version/version range + the remaining amount
  return versions.length > 1 ? i18n.t("version.page.shortVersions", [versions.at(-1), versions.length - 1]) : versions[0]!;
}
</script>

<template>
  <div class="flex items-center">
    <div v-if="pinnedVersion">
      <div v-if="pinnedVersion && Object.keys(pinnedVersion.downloads).length === 1">
        <a
          v-for="(_, p) in pinnedVersion.platformDependenciesFormatted"
          :key="p"
          class="flex items-center"
          :href="downloadLink(p, pinnedVersion)"
          target="_blank"
          rel="noopener noreferrer"
          @click="trackDownload(p, pinnedVersion)"
          @click.middle="trackDownload(p, pinnedVersion)"
          v-on="useTracking('download-link', { pinned: true, dropdown: false, mainchannel: false, platform: p, project: project.name })"
        >
          <Button :size="small ? 'medium' : 'large'" :class="{ 'w-14': fixedWidth && small }">
            <IconMdiDownloadOutline />
            <span v-if="!small" class="ml-1">{{ i18n.t("version.page.download") }}</span>
            <IconMdiOpenInNew v-if="isExternal(p, pinnedVersion)" :class="{ 'text-lg pb-1.75': !small }" />
          </Button>
        </a>
      </div>

      <DropdownButton
        v-else
        :button-size="small ? 'medium' : 'large'"
        :button-class="fixedWidth && small ? 'h-10.5 w-10.5 !p-0' : small ? '' : 'min-w-52'"
        :button-arrow="!fixedWidth"
        :match-width="!small"
        :spread-arrow="!fixedWidth"
      >
        <template #button-label>
          <span class="items-center inline-flex">
            <IconMdiDownloadOutline />
            <span v-if="!small" class="ml-1">{{ i18n.t("version.page.download") }}</span>
          </span>
        </template>
        <template #default="{ close }">
          <div class="box-border flex w-full flex-col gap-1">
            <a
              v-for="(v, p) in pinnedVersion.platformDependenciesFormatted"
              :key="p"
              class="flex min-w-0 items-center rounded-lg border border-transparent px-2 py-1 font-semibold decoration-none transition-all duration-250 hover:bg-gray-100 hover:border-gray-300 dark:hover:bg-gray-800 dark:hover:border-gray-700"
              :href="downloadLink(p, pinnedVersion)"
              target="_blank"
              rel="noopener noreferrer"
              @click="
                trackDownload(p, pinnedVersion);
                close();
              "
              @click.middle="trackDownload(p, pinnedVersion)"
              v-on="useTracking('download-link', { pinned: false, dropdown: true, mainchannel: false, platform: p, project: project.name })"
            >
              <PlatformLogo :platform="p as Platform" :size="24" class="mr-1 flex-shrink-0" />
              <span class="whitespace-nowrap">{{ usePlatformName(p) }}</span>
              <span
                v-if="showVersions"
                class="ml-2 max-w-24 truncate rounded-md bg-gray-200 px-1.5 py-0.5 text-0.65rem font-medium text-gray-600 dark:bg-gray-700 dark:text-gray-300"
                :title="formatVersionRange(v)"
              >
                {{ formatVersionRange(v) }}
              </span>
              <IconMdiOpenInNew v-if="isExternal(p, pinnedVersion)" class="ml-0.5 text-sm pb-0.5" />
            </a>
          </div>
        </template>
      </DropdownButton>
    </div>

    <a
      v-else-if="singlePlatform && singleVersion"
      :href="platformDownloadLink"
      target="_blank"
      rel="noopener noreferrer"
      @click="trackDownload(singlePlatform, singleVersion)"
      @click.middle="trackDownload(singlePlatform, singleVersion)"
      v-on="useTracking('download-link', { pinned: false, dropdown: false, mainchannel: false, platform: singlePlatform, project: project.name })"
    >
      <Button :size="small ? 'medium' : 'large'" :class="{ 'h-10.5 w-10.5 !p-0': fixedWidth && small }">
        <div class="flex flex-col" :class="{ '-mb-0.5': showSinglePlatform }">
          <div class="inline-flex items-center">
            <IconMdiDownloadOutline />
            <span v-if="!small" class="ml-1">
              {{ !!singleVersion.downloads[singlePlatform]?.externalUrl ? i18n.t("version.page.downloadExternal") : i18n.t("version.page.download") }}
            </span>
          </div>
          <div v-if="showSinglePlatform" class="inline-flex justify-center items-center font-normal text-0.75rem">
            <PlatformLogo :platform="singlePlatform" :size="15" class="mr-1 flex-shrink-0" />
            <span v-if="singleVersion?.platformDependencies && showVersions">
              {{ formatVersionRange(singleVersion?.platformDependenciesFormatted[singlePlatform]) }}
            </span>
          </div>
        </div>
      </Button>
    </a>

    <DropdownButton
      v-else-if="version"
      :button-size="small ? 'medium' : 'large'"
      :button-class="fixedWidth && small ? 'h-10.5 w-10.5 !p-0' : small ? '' : 'min-w-52'"
      :button-arrow="!fixedWidth"
      :match-width="!small"
      :placement="dropdownPlacement"
      :spread-arrow="!fixedWidth"
    >
      <template #button-label>
        <span class="items-center inline-flex">
          <IconMdiDownloadOutline />
          <span v-if="!small" class="ml-1">{{ i18n.t("version.page.download") }}</span>
        </span>
      </template>
      <template #default="{ close }">
        <div class="box-border flex w-full flex-col gap-1">
          <a
            v-for="(v, p) in version.downloads"
            :key="p"
            :href="downloadLink(p, version)"
            class="flex min-w-0 items-center rounded-lg border border-transparent px-2 py-1 font-semibold decoration-none transition-all duration-250 hover:bg-gray-100 hover:border-gray-300 dark:hover:bg-gray-800 dark:hover:border-gray-700"
            target="_blank"
            rel="noopener noreferrer"
            @click="
              trackDownload(p, version);
              close();
            "
            @click.middle="trackDownload(p, version)"
            v-on="useTracking('download-link', { pinned: false, dropdown: true, mainchannel: false, platform: p, project: project.name })"
          >
            <PlatformLogo :platform="p as Platform" :size="24" class="mr-1 flex-shrink-0" />
            <span class="whitespace-nowrap">{{ usePlatformName(p) }}</span>
            <span
              v-if="showVersions && version.platformDependencies"
              class="ml-2 max-w-24 truncate rounded-md bg-gray-200 px-1.5 py-0.5 text-0.65rem font-medium text-gray-600 dark:bg-gray-700 dark:text-gray-300"
              :title="formatVersionRange(version.platformDependenciesFormatted[p])"
            >
              {{ formatVersionRange(version.platformDependenciesFormatted[p]) }}
            </span>
            <span v-if="v.fileInfo?.sizeBytes" class="ml-1 text-xs font-normal text-gray"> {{ formatSize(v.fileInfo.sizeBytes) }} </span>
            <IconMdiOpenInNew v-if="v.externalUrl" class="ml-0.5 text-sm pb-0.5" />
          </a>
        </div>
      </template>
    </DropdownButton>

    <DropdownButton
      v-else-if="project.mainChannelVersions && Object.keys(project.mainChannelVersions).length > 0"
      :button-size="small ? 'medium' : 'large'"
      :button-class="small ? '' : 'min-w-52'"
      match-width
      spread-arrow
    >
      <template #button-label>
        <span class="items-center inline-flex">
          <IconMdiDownloadOutline />
          <span v-if="!small" class="ml-1">{{ i18n.t("version.page.download") }}</span>
        </span>
      </template>
      <template #default="{ close }">
        <div class="box-border flex w-full flex-col gap-1">
          <a
            v-for="(v, p) in project.mainChannelVersions"
            :key="p"
            class="flex min-w-0 items-center rounded-lg border border-transparent px-2 py-1 font-semibold decoration-none transition-all duration-250 hover:bg-gray-100 hover:border-gray-300 dark:hover:bg-gray-800 dark:hover:border-gray-700"
            :href="downloadLink(p, v)"
            target="_blank"
            rel="noopener noreferrer"
            @click="
              trackDownload(p, v);
              close();
            "
            @click.middle="trackDownload(p, v)"
            v-on="useTracking('download-link', { pinned: false, dropdown: true, mainchannel: true, platform: p, project: project.name })"
          >
            <PlatformLogo :platform="p as Platform" :size="24" class="mr-1 flex-shrink-0" />
            <span class="whitespace-nowrap">{{ usePlatformName(p) }}</span>
            <span
              v-if="v.platformDependencies && showVersions"
              class="ml-2 max-w-24 truncate rounded-md bg-gray-200 px-1.5 py-0.5 text-0.65rem font-medium text-gray-600 dark:bg-gray-700 dark:text-gray-300"
              :title="formatVersionRange(v.platformDependenciesFormatted[p])"
            >
              {{ formatVersionRange(v.platformDependenciesFormatted[p]) }}
            </span>
            <IconMdiOpenInNew v-if="v.downloads[p]?.externalUrl" class="ml-0.5 text-sm pb-0.5" />
          </a>
        </div>
      </template>
    </DropdownButton>
  </div>
</template>
