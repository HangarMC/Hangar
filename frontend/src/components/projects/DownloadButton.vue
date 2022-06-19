<script lang="ts" setup>
import Button from "~/components/design/Button.vue";
import Tooltip from "~/components/design/Tooltip.vue";
import { useI18n } from "vue-i18n";
import { HangarProject, HangarVersion, IPlatform } from "hangar-internal";
import { computed, ref } from "vue";
import { Platform } from "~/types/enums";
import DropdownButton from "~/components/design/DropdownButton.vue";
import { useBackendDataStore } from "~/store/backendData";
import DropdownItem from "~/components/design/DropdownItem.vue";
import { useInternalApi } from "~/composables/useApi";
import Modal from "~/components/modals/Modal.vue";
import Alert from "~/components/design/Alert.vue";
import PlatformLogo from "~/components/logos/PlatformLogo.vue";

const i18n = useI18n();
const backendData = useBackendDataStore();

const props = withDefaults(
  defineProps<{
    project: HangarProject;
    copyButton?: boolean;
    platformSelection?: boolean;
    small?: boolean;
    version?: HangarVersion;
    platform?: IPlatform;
  }>(),
  {
    copyButton: true,
    platformSelection: false,
    small: true,
  }
);

const downloadNow = true;
const loading = ref<boolean>(false);
const selectedPlatform = ref<Platform | null>(null);
const copySuccessful = ref<boolean>(false);
const token = ref<string | null>(null);
const confirmModal = ref<Modal>(null);
const downloadLink = computed(() => {
  if (external.value) {
    return externalUrl.value;
  }

  const versionString = props.platformSelection ? "recommended" : props.version?.name;
  const platform = props.platformSelection ? selectedPlatform.value : props.platform?.name;
  const path = `/api/v1/projects/${props.project.namespace.owner}/${props.project.namespace.slug}/versions/${versionString}/${platform}/download`;
  return import.meta.env.SSR ? path : `${window.location.protocol}//${window.location.host}${path}`;
});

if (props.platformSelection) {
  const keys = props.project.recommendedVersions ? Object.keys(props.project.recommendedVersions) : [];
  selectedPlatform.value = keys.length > 0 ? Platform[keys[0] as keyof typeof Platform] : Platform.PAPER;
}

const external = computed(() =>
  props.platformSelection ? props.project.recommendedVersions[selectedPlatform.value!] !== null : props.version!.externalUrl !== null
);
const externalUrl = computed(() => (props.platformSelection ? props.project.recommendedVersions[selectedPlatform.value!] : props.version!.externalUrl));

function copyDownloadUrl() {
  const url = downloadLink.value;
  if (url) {
    navigator.clipboard.writeText(url);
    copySuccessful.value = true;
    setTimeout(() => (copySuccessful.value = false), 2000);
  }
}

async function checkAndDownloadPlatform(platform: any) {
  selectedPlatform.value = platform;
  await checkAndDownload();
}

async function checkAndDownload() {
  if (downloadNow) {
    return;
  }
  if (await requiresConfirmation()) {
    confirmModal.value?.open();
    return;
  }
  return download();
}

function download(): Promise<any> {
  if (external.value) {
    confirmModal.value?.close();
    return Promise.resolve(window.open(externalUrl.value || "", "_blank"));
  }
  const versionString = props.platformSelection ? "recommended" : props.version!.name;
  const platform = props.platformSelection ? selectedPlatform.value : props.platform!.name;
  window.open(
    `/api/internal/versions/version/${props.project.namespace.owner}/${props.project.namespace.slug}/versions/${versionString}/${platform}/download?token=${token.value}`,
    "_blank"
  );
  token.value = null;
  confirmModal.value?.close();
  return Promise.resolve();
}

async function requiresConfirmation() {
  if (token.value != null) {
    return true;
  }
  if (external.value) {
    return false;
  }
  loading.value = true;
  const versionString = props.platformSelection ? "recommended" : props.version!.name;
  const platform = props.platformSelection ? selectedPlatform.value : props.platform!.name.toLowerCase();
  try {
    await useInternalApi(
      `versions/version/${props.project.namespace.owner}/${props.project.namespace.slug}/versions/${versionString}/${platform}/downloadCheck`
    )
      .then(() => (loading.value = false))
      .catch((err) => {
        if (err.response && err.response.status === 428) {
          token.value = err.response.data;
        }
        throw err;
      });
    loading.value = false;
    return false;
  } catch (ex) {
    loading.value = false;
    return true;
  }
}
</script>

<template>
  <!-- todo make this actually look nice -->
  <div class="flex items-center">
    <!--  && Object.keys(project.recommendedVersions).length !== 1 -->
    <DropdownButton v-if="platformSelection" :button-size="small ? 'medium' : 'large'">
      <template #button-label>
        <span class="items-center inline-flex">
          <IconMdiDownloadOutline class="mr-1" />
          {{ external ? i18n.t("version.page.downloadExternal") : i18n.t("version.page.download") }}
        </span>
      </template>
      <DropdownItem
        v-for="(pl, i) in Object.keys(project.recommendedVersions)"
        :key="i"
        class="flex items-center"
        :href="downloadNow ? downloadLink : undefined"
        target="_blank"
        rel="noopener noreferrer"
        @click="checkAndDownloadPlatform(pl)"
      >
        <PlatformLogo :platform="pl" :size="24" class="mr-1" />
        {{ backendData.platforms.get(pl).name }}
      </DropdownItem>
    </DropdownButton>

    <a v-else :href="downloadNow ? downloadLink : undefined" target="_blank" rel="noopener noreferrer">
      <Button :size="small ? 'medium' : 'large'" :loading="loading" @click="checkAndDownload">
        <IconMdiDownloadOutline class="mr-1" />
        {{ external ? i18n.t("version.page.downloadExternal") : i18n.t("version.page.download") }}
      </Button>
    </a>

    <Modal ref="confirmModal" :title="i18n.t('version.page.confirmation.title', [project.name, version ? version.name : '', project.owner.name])">
      <Alert type="danger" class="my-2">
        {{ i18n.t("version.page.confirmation.alert") }}
      </Alert>
      <em>{{ i18n.t("version.page.confirmation.disclaimer") }}</em>

      <div class="mt-2 flex flex-wrap gap-2">
        <Button button-type="red" @click="download">{{ i18n.t("version.page.confirmation.agree") }}</Button>
        <Button button-type="secondary" @click="confirmModal?.close()">{{ i18n.t("version.page.confirmation.deny") }}</Button>
      </div>
    </Modal>

    <Tooltip v-if="copyButton && !downloadNow" :hover="false" :show="copySuccessful">
      <template #content>
        <span>{{ i18n.t("version.page.downloadUrlCopied") }}</span>
      </template>
      <Tooltip>
        <template #content>
          {{ i18n.t("version.page.downloadUrlHover") }}
        </template>
        <Button :size="small ? 'medium' : 'large'" class="ml-1" @click="copyDownloadUrl">
          <IconMdiContentCopy />
        </Button>
      </Tooltip>
    </Tooltip>
  </div>
</template>
