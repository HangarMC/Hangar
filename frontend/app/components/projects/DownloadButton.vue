<script lang="ts" setup>
import type { Platform, HangarProject, Version, PinnedVersion } from "#shared/types/backend";

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

function formatVersionRange(versions?: string[]): string {
  if (!versions) return "";
  // In download buttons, only show the latest version/version range + the remaining amount
  return versions.length > 1 ? i18n.t("version.page.shortVersions", [versions.at(-1), versions.length - 1]) : versions[0]!;
}

// joined here rather than in the template so a missing part can't leave a dangling separator
function downloadDetails(versionRange?: string[], sizeBytes?: number): string {
  const parts: string[] = [];
  if (props.showVersions) {
    const range = formatVersionRange(versionRange);
    if (range) parts.push(range);
  }
  if (sizeBytes) {
    parts.push(formatSize(sizeBytes));
  }
  return parts.join(" · ");
}
</script>

<template>
  <div class="flex items-center">
    <div v-if="pinnedVersion">
      <div v-if="pinnedVersion && Object.keys(pinnedVersion.downloads).length === 1">
        <Button
          v-for="(_, p) in pinnedVersion.platformDependenciesFormatted"
          :key="p"
          class="download-btn"
          :size="small ? 'sm' : 'lg'"
          :href="downloadLink(p, pinnedVersion)"
          target="_blank"
          rel="noopener noreferrer"
          @click="trackDownload(p, pinnedVersion)"
          @click.middle="trackDownload(p, pinnedVersion)"
          v-on="useTracking('download-link', { pinned: true, dropdown: false, mainchannel: false, platform: p, project: project.name })"
        >
          <IconMdiDownloadOutline class="download-icon" />
          <span v-if="!small">{{ i18n.t("version.page.download") }}</span>
          <IconMdiOpenInNew v-if="isExternal(p, pinnedVersion)" />
        </Button>
      </div>

      <DropdownButton v-else class="download-btn" :button-size="small ? 'sm' : 'lg'">
        <template #button-label>
          <span class="items-center inline-flex gap-1.5">
            <IconMdiDownloadOutline class="download-icon" />
            <span v-if="!small">{{ i18n.t("version.page.download") }}</span>
          </span>
          <span v-if="!small" class="download-divider" aria-hidden="true" />
        </template>
        <DropdownItem
          v-for="(v, p) in pinnedVersion.platformDependenciesFormatted"
          :key="p"
          :href="downloadLink(p, pinnedVersion)"
          target="_blank"
          rel="noopener noreferrer"
          @click="trackDownload(p, pinnedVersion)"
          @click.middle="trackDownload(p, pinnedVersion)"
          v-on="useTracking('download-link', { pinned: false, dropdown: true, mainchannel: false, platform: p, project: project.name })"
        >
          <PlatformLogo :platform="p as Platform" :size="20" class="flex-shrink-0" />
          <span class="min-w-0 flex-1">
            <span class="block leading-tight">{{ usePlatformName(p) }}</span>
            <span v-if="downloadDetails(v)" class="block text-xs font-normal leading-tight text-gray-secondary">{{ downloadDetails(v) }}</span>
          </span>
          <IconMdiOpenInNew v-if="isExternal(p, pinnedVersion)" class="flex-shrink-0 text-sm text-gray-secondary" />
        </DropdownItem>
      </DropdownButton>
    </div>

    <Button
      v-else-if="singlePlatform && singleVersion"
      :size="small ? 'sm' : 'lg'"
      class="download-btn"
      :class="{ '!h-auto py-1.5': showSinglePlatform }"
      :href="platformDownloadLink"
      target="_blank"
      rel="noopener noreferrer"
      @click="trackDownload(singlePlatform, singleVersion)"
      @click.middle="trackDownload(singlePlatform, singleVersion)"
      v-on="useTracking('download-link', { pinned: false, dropdown: false, mainchannel: false, platform: singlePlatform, project: project.name })"
    >
      <div class="flex flex-col items-center" :class="{ '-mb-0.5': showSinglePlatform }">
        <div class="inline-flex items-center gap-1.5">
          <IconMdiDownloadOutline class="download-icon" />
          <span v-if="!small">
            {{ !!singleVersion.downloads[singlePlatform]?.externalUrl ? i18n.t("version.page.downloadExternal") : i18n.t("version.page.download") }}
          </span>
        </div>
        <div v-if="showSinglePlatform" class="inline-flex items-center gap-1 font-normal text-0.75rem">
          <PlatformLogo :platform="singlePlatform" :size="15" class="flex-shrink-0" />
          <span v-if="singleVersion?.platformDependencies && showVersions">
            {{ formatVersionRange(singleVersion?.platformDependenciesFormatted[singlePlatform]) }}
          </span>
        </div>
      </div>
    </Button>

    <DropdownButton v-else-if="version" class="download-btn" :button-size="small ? 'sm' : 'lg'">
      <template #button-label>
        <span class="items-center inline-flex gap-1.5">
          <IconMdiDownloadOutline class="download-icon" />
          <span v-if="!small">{{ i18n.t("version.page.download") }}</span>
        </span>
        <span v-if="!small" class="download-divider" aria-hidden="true" />
      </template>
      <DropdownItem
        v-for="(v, p) in version.downloads"
        :key="p"
        :href="downloadLink(p, version)"
        target="_blank"
        rel="noopener noreferrer"
        @click="trackDownload(p, version)"
        @click.middle="trackDownload(p, version)"
        v-on="useTracking('download-link', { pinned: false, dropdown: true, mainchannel: false, platform: p, project: project.name })"
      >
        <PlatformLogo :platform="p as Platform" :size="20" class="flex-shrink-0" />
        <span class="min-w-0 flex-1">
          <span class="block leading-tight">{{ usePlatformName(p) }}</span>
          <span
            v-if="downloadDetails(version.platformDependenciesFormatted[p], v.fileInfo?.sizeBytes)"
            class="block text-xs font-normal leading-tight text-gray-secondary"
          >
            {{ downloadDetails(version.platformDependenciesFormatted[p], v.fileInfo?.sizeBytes) }}
          </span>
        </span>
        <IconMdiOpenInNew v-if="v.externalUrl" class="flex-shrink-0 text-sm text-gray-secondary" />
      </DropdownItem>
    </DropdownButton>

    <DropdownButton
      v-else-if="project.mainChannelVersions && Object.keys(project.mainChannelVersions).length > 0"
      class="download-btn"
      :button-size="small ? 'sm' : 'lg'"
    >
      <template #button-label>
        <span class="items-center inline-flex gap-1.5">
          <IconMdiDownloadOutline class="download-icon" />
          <span v-if="!small">{{ i18n.t("version.page.download") }}</span>
        </span>
        <span v-if="!small" class="download-divider" aria-hidden="true" />
      </template>
      <DropdownItem
        v-for="(v, p) in project.mainChannelVersions"
        :key="p"
        :href="downloadLink(p, v)"
        target="_blank"
        rel="noopener noreferrer"
        @click="trackDownload(p, v)"
        @click.middle="trackDownload(p, v)"
        v-on="useTracking('download-link', { pinned: false, dropdown: true, mainchannel: true, platform: p, project: project.name })"
      >
        <PlatformLogo :platform="p as Platform" :size="20" class="flex-shrink-0" />
        <span class="min-w-0 flex-1">
          <span class="block leading-tight">{{ usePlatformName(p) }}</span>
          <span v-if="downloadDetails(v.platformDependenciesFormatted[p])" class="block text-xs font-normal leading-tight text-gray-secondary">
            {{ downloadDetails(v.platformDependenciesFormatted[p]) }}
          </span>
        </span>
        <IconMdiOpenInNew v-if="v.downloads[p]?.externalUrl" class="flex-shrink-0 text-sm text-gray-secondary" />
      </DropdownItem>
    </DropdownButton>
  </div>
</template>
