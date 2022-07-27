<script lang="ts" setup>
import Button from "~/lib/components/design/Button.vue";
import { useI18n } from "vue-i18n";
import { HangarProject, PinnedVersion } from "hangar-internal";
import { computed } from "vue";
import { Platform } from "~/types/enums";
import DropdownButton from "~/lib/components/design/DropdownButton.vue";
import { useBackendDataStore } from "~/store/backendData";
import DropdownItem from "~/lib/components/design/DropdownItem.vue";
import PlatformLogo from "~/components/logos/platforms/PlatformLogo.vue";
import { PlatformVersionDownload } from "hangar-api";

const i18n = useI18n();
const backendData = useBackendDataStore();

interface DownloadableVersion {
  name: string;
  downloads: Record<Platform, PlatformVersionDownload>;
}

const props = withDefaults(
  defineProps<{
    project: HangarProject;
    small?: boolean;
    showVersions?: boolean;
    // Define either version and platform or pinnedVersion, or neither to use main channel versions
    platform?: Platform;
    version?: DownloadableVersion;
    pinnedVersion?: PinnedVersion;
  }>(),
  {
    small: false,
    showVersions: true,
    platform: undefined,
    version: undefined,
    pinnedVersion: undefined,
  }
);

function downloadLink(platform: Platform, version: DownloadableVersion) {
  if (version && version.downloads[platform]?.externalUrl) {
    return version.downloads[platform].externalUrl;
  }

  const versionString = version.name;
  //TODO as normal path then using the api
  const path = `/api/v1/projects/${props.project.namespace.owner}/${props.project.namespace.slug}/versions/${versionString}/${platform.toLowerCase()}/download`;
  return import.meta.env.SSR ? path : `${window.location.protocol}//${window.location.host}${path}`;
}

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
        {{ backendData.platforms.get(p).name }}
        <span v-if="showVersions" class="ml-1">({{ v }})</span>
      </DropdownItem>
    </DropdownButton>

    <a v-else-if="platform && version" :href="downloadLink(platform, version)" target="_blank" rel="noopener noreferrer">
      <Button :size="small ? 'medium' : 'large'">
        <IconMdiDownloadOutline />
        <span v-if="!small" class="ml-1">{{ external ? i18n.t("version.page.downloadExternal") : i18n.t("version.page.download") }}</span>
      </Button>
    </a>

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
        {{ backendData.platforms.get(p)?.name }}
        <span v-if="v.platformDependencies && showVersions" class="ml-1">({{ v.platformDependenciesFormatted[p] }})</span>
      </DropdownItem>
    </DropdownButton>
  </div>
</template>
