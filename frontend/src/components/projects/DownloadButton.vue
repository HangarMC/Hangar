<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { HangarProject, PinnedVersion } from "hangar-internal";
import { computed } from "vue";
import { PlatformVersionDownload } from "hangar-api";
import Button from "~/lib/components/design/Button.vue";
import { Platform } from "~/types/enums";
import DropdownButton from "~/lib/components/design/DropdownButton.vue";
import { useBackendData } from "~/store/backendData";
import DropdownItem from "~/lib/components/design/DropdownItem.vue";
import PlatformLogo from "~/components/logos/platforms/PlatformLogo.vue";

const i18n = useI18n();

interface DownloadableVersion {
  name: string;
  downloads: Record<Platform, PlatformVersionDownload>;
}

const props = withDefaults(
  defineProps<{
    project: HangarProject;
    small?: boolean;
    showVersions?: boolean;
    showSinglePlatform?: boolean;
    // Define either version and platform or pinnedVersion, or neither to use main channel versions
    platform?: Platform;
    version?: DownloadableVersion;
    pinnedVersion?: PinnedVersion;
  }>(),
  {
    small: false,
    showVersions: true,
    showSinglePlatform: true,
    platform: undefined,
    version: undefined,
    pinnedVersion: undefined,
  }
);

function downloadLink(platform: Platform | undefined, version: DownloadableVersion | undefined) {
  if (!version || !platform) return;
  return version.downloads[platform]?.externalUrl ? version.downloads[platform].externalUrl : version.downloads[platform].downloadUrl;
}

const singlePlatform = computed<Platform | undefined>(() => {
  if (props.project?.mainChannelVersions) {
    const keys = Object.keys(props.project.mainChannelVersions);
    if (keys.length === 1) {
      return keys[0] as Platform;
    }
  }
  return props.platform;
});
const singleVersion = computed<DownloadableVersion | undefined>(() => {
  if (!props.version && props.project?.mainChannelVersions && singlePlatform.value) {
    return props.project.mainChannelVersions[singlePlatform.value];
  }
  return props.version;
});

const platformDownloadLink = computed(() => downloadLink(singlePlatform.value, singleVersion.value));

const external = computed(() => false);
</script>

<template>
  <div class="flex items-center">
    <DropdownButton v-if="pinnedVersion" :button-size="small ? 'medium' : 'large'">
      <template #button-label>
        <span class="items-center inline-flex">
          <IconMdiDownloadOutline />
          <span v-if="!small" class="ml-1">{{ external ? i18n.t("version.page.downloadExternal") : i18n.t("version.page.download") }}</span>
        </span>
      </template>
      <DropdownItem
        v-for="(v, p) in pinnedVersion.platformDependenciesFormatted"
        :key="p"
        class="flex items-center"
        :href="downloadLink(p, pinnedVersion)"
        target="_blank"
        rel="noopener noreferrer"
      >
        <PlatformLogo :platform="p" :size="24" class="mr-1 flex-shrink-0" />
        {{ useBackendData.platforms.get(p)?.name }}
        <span v-if="showVersions" class="ml-1">({{ v }})</span>
      </DropdownItem>
    </DropdownButton>

    <a v-else-if="singlePlatform && singleVersion" :href="platformDownloadLink" target="_blank" rel="noopener noreferrer">
      <Button :size="small ? 'medium' : 'large'">
        <div class="flex flex-col" :class="{ '-mb-0.5': showSinglePlatform }">
          <div class="inline-flex items-center">
            <IconMdiDownloadOutline />
            <span v-if="!small" class="ml-1">{{ external ? i18n.t("version.page.downloadExternal") : i18n.t("version.page.download") }}</span>
          </div>
          <div v-if="showSinglePlatform" class="inline-flex justify-center items-center font-normal text-0.75rem">
            <PlatformLogo :platform="singlePlatform" :size="15" class="mr-1 flex-shrink-0" />
            <span v-if="singleVersion.platformDependencies && showVersions">
              {{ singleVersion.platformDependenciesFormatted[singlePlatform] }}
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
      >
        <PlatformLogo :platform="p" :size="24" class="mr-1 flex-shrink-0" />
        {{ useBackendData.platforms.get(p)?.name }}
        <span v-if="showVersions && version.platformDependencies" class="ml-1">({{ version.platformDependenciesFormatted[p] }})</span>
      </DropdownItem>
    </DropdownButton>

    <DropdownButton v-else :button-size="small ? 'medium' : 'large'">
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
      >
        <PlatformLogo :platform="p" :size="24" class="mr-1 flex-shrink-0" />
        {{ useBackendData.platforms.get(p)?.name }}
        <span v-if="v.platformDependencies && showVersions" class="ml-1">({{ v.platformDependenciesFormatted[p] }})</span>
      </DropdownItem>
    </DropdownButton>
  </div>
</template>
