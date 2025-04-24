<script lang="ts" setup>
import type { Platform, HangarProject, Version, PinnedVersion } from "~/types/backend";

const i18n = useI18n();

const props = withDefaults(
  defineProps<{
    project: HangarProject;
    small?: boolean;
    showVersions?: boolean;
    showSinglePlatform?: boolean;
    // Define either version and platform or pinnedVersion, or neither to use main channel versions
    platform?: Platform;
    version?: Version;
    pinnedVersion?: PinnedVersion;
    showFileSize?: boolean;
  }>(),
  {
    small: false,
    showVersions: true,
    showSinglePlatform: true,
    platform: undefined,
    version: undefined,
    pinnedVersion: undefined,
    showFileSize: false,
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

function formatVersionRange(versions: string[]): string {
  // In download buttons, only show the latest version/version range + the remaining amount
  return versions.length > 1 ? i18n.t("version.page.shortVersions", [versions.at(-1), versions.length - 1]) : versions[0];
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
          <Button :size="small ? 'medium' : 'large'">
            <IconMdiDownloadOutline />
            <span v-if="!small" class="ml-1">{{ i18n.t("version.page.download") }}</span>
            <IconMdiOpenInNew v-if="isExternal(p, pinnedVersion)" :class="{ 'text-lg pb-1.75': !small }" />
          </Button>
        </a>
      </div>

      <DropdownButton v-else :button-size="small ? 'medium' : 'large'">
        <template #button-label>
          <span class="items-center inline-flex">
            <IconMdiDownloadOutline />
            <span v-if="!small" class="ml-1">{{ i18n.t("version.page.download") }}</span>
          </span>
        </template>
        <DropdownItem
          v-for="(v, p) in pinnedVersion.platformDependenciesFormatted"
          :key="p"
          class="flex items-center"
          :href="downloadLink(p, pinnedVersion)"
          target="_blank"
          rel="noopener noreferrer"
          @click="trackDownload(p, pinnedVersion)"
          @click.middle="trackDownload(p, pinnedVersion)"
          v-on="useTracking('download-link', { pinned: false, dropdown: true, mainchannel: false, platform: p, project: project.name })"
        >
          <PlatformLogo :platform="p as Platform" :size="24" class="mr-1 flex-shrink-0" />
          {{ useBackendData.platforms.get(p as Platform)?.name }}
          <span v-if="showVersions" class="ml-1">({{ formatVersionRange(v) }})</span>
          <IconMdiOpenInNew v-if="isExternal(p, pinnedVersion)" class="ml-0.5 text-sm pb-0.5" />
        </DropdownItem>
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
      <Button :size="small ? 'medium' : 'large'">
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

    <DropdownButton v-else-if="version" :button-size="small ? 'medium' : 'large'">
      <template #button-label>
        <span class="items-center inline-flex">
          <IconMdiDownloadOutline />
          <span v-if="!small" class="ml-1">{{ i18n.t("version.page.download") }}</span>
        </span>
      </template>
      <DropdownItem
        v-for="(v, p) in version.downloads"
        :key="p"
        :href="downloadLink(p, version)"
        class="flex items-center"
        target="_blank"
        rel="noopener noreferrer"
        @click="trackDownload(p, version)"
        @click.middle="trackDownload(p, version)"
        v-on="useTracking('download-link', { pinned: false, dropdown: true, mainchannel: false, platform: p, project: project.name })"
      >
        <PlatformLogo :platform="p as Platform" :size="24" class="mr-1 flex-shrink-0" />
        {{ useBackendData.platforms.get(p as Platform)?.name }}
        <span v-if="showVersions && version.platformDependencies" class="ml-1">({{ formatVersionRange(version.platformDependenciesFormatted[p]) }})</span>
        <span v-if="v.fileInfo?.sizeBytes" class="ml-1"> ({{ formatSize(v.fileInfo.sizeBytes) }}) </span>
        <IconMdiOpenInNew v-if="v.externalUrl" class="ml-0.5 text-sm pb-0.5" />
      </DropdownItem>
    </DropdownButton>

    <DropdownButton v-else-if="project.mainChannelVersions && Object.keys(project.mainChannelVersions).length > 0" :button-size="small ? 'medium' : 'large'">
      <template #button-label>
        <span class="items-center inline-flex">
          <IconMdiDownloadOutline />
          <span v-if="!small" class="ml-1">{{ i18n.t("version.page.download") }}</span>
        </span>
      </template>
      <DropdownItem
        v-for="(v, p) in project.mainChannelVersions"
        :key="p"
        class="flex items-center"
        :href="downloadLink(p, v)"
        target="_blank"
        rel="noopener noreferrer"
        @click="trackDownload(p, v)"
        @click.middle="trackDownload(p, v)"
        v-on="useTracking('download-link', { pinned: false, dropdown: true, mainchannel: true, platform: p, project: project.name })"
      >
        <PlatformLogo :platform="p as Platform" :size="24" class="mr-1 flex-shrink-0" />
        {{ useBackendData.platforms.get(p as Platform)?.name }}
        <span v-if="v.platformDependencies && showVersions" class="ml-1">({{ formatVersionRange(v.platformDependenciesFormatted[p]) }})</span>
        <IconMdiOpenInNew v-if="v.downloads[p]?.externalUrl" class="ml-0.5 text-sm pb-0.5" />
      </DropdownItem>
    </DropdownButton>
  </div>
</template>
