<script lang="ts" setup>
import Button from "~/components/design/Button.vue";
import { useI18n } from "vue-i18n";
import { HangarProject, HangarVersion, PinnedVersion } from "hangar-internal";
import { computed, ref } from "vue";
import { Platform } from "~/types/enums";
import DropdownButton from "~/components/design/DropdownButton.vue";
import { useBackendDataStore } from "~/store/backendData";
import DropdownItem from "~/components/design/DropdownItem.vue";
import PlatformLogo from "~/components/logos/PlatformLogo.vue";

const i18n = useI18n();
const backendData = useBackendDataStore();

interface DownloadableVersion {
  name: string;
  externalUrl: string | null;
}

const props = withDefaults(
  defineProps<{
    project: HangarProject;
    recommended?: boolean;
    small?: boolean;
    // Define either version and platform or pinnedVersion
    platform?: Platform;
    version?: DownloadableVersion;
    pinnedVersion?: PinnedVersion;
  }>(),
  {
    recommended: false,
    small: false,
  }
);

function downloadLink(platform: Platform, version: DownloadableVersion) {
  if (version && version.externalUrl) {
    return version.externalUrl;
  }

  const versionString = props.recommended ? "recommended" : version.name;
  const path = `/api/v1/projects/${props.project.namespace.owner}/${props.project.namespace.slug}/versions/${versionString}/${platform.toLowerCase()}/download`;
  return import.meta.env.SSR ? path : `${window.location.protocol}//${window.location.host}${path}`;
}

const external = computed(() => false);
</script>

<template>
  <div class="flex items-center">
    <DropdownButton v-if="recommended" :button-size="small ? 'medium' : 'large'">
      <template #button-label>
        <span class="items-center inline-flex">
          <IconMdiDownloadOutline />
          <span v-if="!small" class="ml-1">{{ external ? i18n.t("version.page.downloadExternal") : i18n.t("version.page.download") }}</span>
        </span>
      </template>
      <DropdownItem
        v-for="(url, pl, idx) in project.recommendedVersions"
        :key="`${pl}-${idx}`"
        class="flex items-center"
        :href="url || downloadLink(pl, null)"
        target="_blank"
        rel="noopener noreferrer"
      >
        <PlatformLogo :platform="pl" :size="24" class="mr-1" />
        {{ backendData.platforms.get(pl).name }}
      </DropdownItem>
    </DropdownButton>

    <DropdownButton v-else-if="pinnedVersion" :button-size="small ? 'medium' : 'large'">
      <template #button-label>
        <span class="items-center inline-flex">
          <IconMdiDownloadOutline />
          <span v-if="!small" class="ml-1">{{ external ? i18n.t("version.page.downloadExternal") : i18n.t("version.page.download") }}</span>
        </span>
      </template>
      <DropdownItem
        v-for="platform in pinnedVersion.platforms"
        :key="platform"
        class="flex items-center"
        :href="downloadLink(platform, pinnedVersion)"
        target="_blank"
        rel="noopener noreferrer"
      >
        <PlatformLogo :platform="platform" :size="24" class="mr-1" />
        {{ backendData.platforms?.get(platform).name }}
      </DropdownItem>
    </DropdownButton>

    <a v-else :href="downloadLink(platform, version)" target="_blank" rel="noopener noreferrer">
      <Button :size="small ? 'medium' : 'large'">
        <IconMdiDownloadOutline />
        <span v-if="!small" class="ml-1">{{ external ? i18n.t("version.page.downloadExternal") : i18n.t("version.page.download") }}</span>
      </Button>
    </a>
  </div>
</template>
