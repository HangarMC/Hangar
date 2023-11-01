<script lang="ts" setup>
import { useHead } from "@unhead/vue";
import type { HangarProject, IPlatform, PendingVersion, ProjectChannel } from "hangar-internal";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { computed, reactive, type Ref, ref } from "vue";
import { remove } from "lodash-es";
import { useVuelidate } from "@vuelidate/core";
import { useSeo } from "~/composables/useSeo";
import Steps from "~/components/design/Steps.vue";
import InputFile from "~/components/ui/InputFile.vue";
import InputText from "~/components/ui/InputText.vue";
import InputSelect from "~/components/ui/InputSelect.vue";
import Button from "~/components/design/Button.vue";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";

import { MarkdownEditor } from "#components";
import { maxFileSize, maxLength, minLength, noDuplicated, pattern, required, requiredIf, url as validUrl } from "~/composables/useValidationHelpers";
import { useInternalApi } from "~/composables/useApi";
import type { Platform } from "~/types/enums";
import { handleRequestError } from "~/composables/useErrorHandling";
import { formatSize } from "~/composables/useFile";
import ChannelModal from "~/components/modals/ChannelModal.vue";
import { useBackendData } from "~/store/backendData";
import DependencyTable from "~/components/projects/DependencyTable.vue";
import VersionSelector from "~/components/VersionSelector.vue";
import Tabs from "~/components/design/Tabs.vue";
import PlatformLogo from "~/components/logos/platforms/PlatformLogo.vue";
import { useProjectChannels } from "~/composables/useApiHelper";
import { definePageMeta } from "#imports";
import type { Step } from "~/types/components/design/Steps";
import type { Tab } from "~/types/components/design/Tabs";
import InputGroup from "~/components/ui/InputGroup.vue";
import { useNotificationStore } from "~/store/notification";
import Alert from "~/components/design/Alert.vue";
import Link from "~/components/design/Link.vue";

definePageMeta({
  projectPermsRequired: ["CREATE_VERSION"],
});

const route = useRoute();
const router = useRouter();
const i18n = useI18n();
const t = i18n.t;
const notification = useNotificationStore();
const props = defineProps<{
  project: HangarProject;
}>();

const selectedStep = ref("artifact");
const steps: Step[] = [
  {
    value: "artifact",
    header: t("version.new.steps.1.header"),
    beforeNext: async () => {
      if (!(await v.value.$validate())) {
        return false;
      }
      return createPendingVersion();
    },
    disableNext: computed(() => v.value.$errors.length > 0 || v.value.$pending),
  },
  {
    value: "basic",
    header: t("version.new.steps.2.header"),
    disableNext: computed(() => v.value.$errors.length > 0 || v.value.$pending),
    beforeNext: async () => await v.value.$validate(),
    showBack: false,
  },
  {
    value: "dependencies",
    header: t("version.new.steps.3.header"),
    beforeNext: async () => {
      if (!(await v.value.$validate())) {
        return false;
      }

      if (!pendingVersion.value || !dependencyTables.value) {
        await notification.error("No pending version?!");
        return false;
      }

      for (let i = 0; i < selectedPlatforms.value.length; i++) {
        const platform = selectedPlatforms.value[i];
        const dependencyTable = dependencyTables.value[i];
        pendingVersion.value.pluginDependencies[platform as Platform] = dependencyTable.dependencies;
      }

      return true;
    },
    disableNext: computed(() => v.value.$errors.length > 0 || v.value.$pending),
  },
  {
    value: "changelog",
    header: t("version.new.steps.4.header"),
    beforeNext: async () => {
      if (!(await v.value.$validate())) {
        return false;
      }
      await createVersion();
      return false; // createVersion already hijacks the beforeNext logic, cannot move next on final step.
    },
    disableNext: computed(() => v.value.$errors.length > 0 || v.value.$pending),
    beforeBack: () => {
      if (descriptionEditor.value) {
        lastDescription.value = descriptionEditor.value.rawEdited;
      }
      return true;
    },
  },
];

interface PlatformFile {
  platforms: Platform[];
  selectedTab: string;
  file?: File;
  url?: string;
}

const platformFiles: Ref<PlatformFile[]> = ref([{ platforms: [], selectedTab: "file" }]);
const selectedUploadTabs: Tab[] = [
  { value: "file", header: i18n.t("version.new.form.file") },
  { value: "url", header: i18n.t("version.new.form.url") },
];

function addPlatformFile() {
  platformFiles.value.push({ platforms: [], selectedTab: "file" });
}

function removePlatformFile(id: number) {
  platformFiles.value.splice(id, 1);
}

const dependencyTables = ref();
const pendingVersion: Ref<PendingVersion | undefined> = ref<PendingVersion>();
const channels = (await useProjectChannels(route.params.project as string)).data;
const selectedPlatforms = ref<Platform[]>([]);
const descriptionEditor = ref();
const lastDescription = ref();
const loading = reactive({
  create: false,
  submit: false,
});

const descriptionToLoad = computed(() => {
  if (lastDescription.value) {
    return lastDescription.value;
  }
  return pendingVersion.value?.description ?? "";
});

const selectedChannel = ref<string>("Release");
const currentChannel = computed(() => channels.value.find((c) => c.name === selectedChannel.value));

const platforms = computed<IPlatform[]>(() => {
  return [...useBackendData.platforms.values()];
});
const selectedPlatformsData = computed<IPlatform[]>(() => {
  const result: IPlatform[] = [];
  for (const platformName of selectedPlatforms.value) {
    const iPlatform = useBackendData.platforms.get(platformName);
    if (iPlatform) {
      result.push(iPlatform);
    }
  }
  return result;
});

const artifactURLRules = (platformFile: PlatformFile) => [validUrl(), requiredIf()(() => platformFile.selectedTab === "url")];
const fileRules = (platformFile: PlatformFile) => [
  requiredIf("File is required")(() => platformFile.selectedTab === "file"),
  maxFileSize()(useBackendData.validations.project.maxFileSize),
];
const platformRules = [required("Select at least one platform!"), minLength()(1), noDuplicated()(() => platformFiles.value.flatMap((f) => f.platforms))];
const versionRules = [required(), pattern()(useBackendData.validations.version.regex), maxLength()(useBackendData.validations.version.max)];
const platformVersionRules = [required("Select at least one platform version!"), minLength()(1)];
const changelogRules = [requiredIf()(() => selectedStep.value === "changelog")];

const v = useVuelidate();

async function createPendingVersion() {
  selectedPlatforms.value.splice(0);

  loading.create = true;
  const formData: FormData = new FormData();
  const data = [];
  for (const platformFile of platformFiles.value) {
    data.push({ platforms: platformFile.platforms, externalUrl: platformFile.url });
    for (const platform of platformFile.platforms) {
      selectedPlatforms.value.push(platform);
    }
    if (platformFile.file) {
      formData.append("files", platformFile.file);
    }
  }

  formData.append("channel", selectedChannel.value);
  formData.append(
    "data",
    new Blob([JSON.stringify(data)], {
      type: "application/json",
    })
  );

  pendingVersion.value = await useInternalApi<PendingVersion>(`versions/version/${props.project.id}/upload`, "post", formData, { timeout: 45000 }).catch<any>(
    (e) => handleRequestError(e)
  );
  loading.create = false;

  if (pendingVersion.value && currentChannel.value) {
    pendingVersion.value.channelName = currentChannel.value.name;
    pendingVersion.value.channelDescription = currentChannel.value.description;
    pendingVersion.value.channelColor = currentChannel.value.color;
    pendingVersion.value.channelFlags = currentChannel.value.flags;
  }

  return pendingVersion.value !== undefined;
}

async function createVersion() {
  if (!pendingVersion.value || !currentChannel.value) {
    return;
  }

  loading.submit = true;
  pendingVersion.value.description = descriptionEditor.value.rawEdited;
  pendingVersion.value.channelDescription = currentChannel.value.description;
  pendingVersion.value.channelColor = currentChannel.value.color;
  pendingVersion.value.channelFlags = currentChannel.value.flags;

  // played around trying to get this to happen in jackson's deserialization, but couldn't figure it out.
  for (const platform in pendingVersion.value.platformDependencies) {
    if (pendingVersion.value.platformDependencies[platform as Platform].length < 1) {
      delete pendingVersion.value.platformDependencies[platform as Platform];
    }
  }
  for (const platform in pendingVersion.value.pluginDependencies) {
    if (pendingVersion.value.pluginDependencies[platform as Platform].length < 1) {
      delete pendingVersion.value.pluginDependencies[platform as Platform];
    }
  }

  for (const platform in pendingVersion.value.platformDependencies) {
    if (!selectedPlatforms.value.includes(platform as Platform)) {
      delete pendingVersion.value.platformDependencies[platform as Platform];
      delete pendingVersion.value.pluginDependencies[platform as Platform];
    }
  }

  try {
    await useInternalApi(`versions/version/${props.project.id}/create`, "post", pendingVersion.value, { timeout: 45000 });
    await router.push(`/${route.params.user}/${route.params.project}/versions`);
  } catch (e: any) {
    handleRequestError(e);
  } finally {
    loading.submit = false;
  }
}

function addChannel(channel: ProjectChannel) {
  remove(channels.value, (c) => c.temp);
  channels.value.push(Object.assign({ temp: true }, channel));
  selectedChannel.value = channel.name;
}

function togglePlatform(platformFile: PlatformFile, platform: Platform) {
  if (platformFile.platforms.includes(platform)) {
    platformFile.platforms = platformFile.platforms.filter((p) => p !== platform);
  } else {
    platformFile.platforms.push(platform);
  }
  platformFile.platforms.sort((a, b) => platforms.value.findIndex((p) => p.enumName === a) - platforms.value.findIndex((p) => p.enumName === b));
}

useHead(useSeo(i18n.t("version.new.title") + " | " + props.project.name, props.project.description, route, props.project.avatarUrl));
</script>

<template>
  <div>
    <Steps v-model="selectedStep" :steps="steps" button-lang-key="version.new.steps.">
      <template #artifact>
        <p class="mb-4">{{ t("version.new.form.artifactTitle") }}</p>
        <div class="flex mb-8 items-center">
          <div class="basis-full md:basis-4/12">
            <InputSelect
              v-model="selectedChannel"
              :values="channels"
              item-text="name"
              item-value="name"
              :label="t('version.new.form.channel')"
              :rules="[required()]"
            />
          </div>
          <div class="basis-full md:(basis-4/12) ml-2">
            <ChannelModal :project-id="project.id" @create="addChannel">
              <template #activator="{ on }">
                <Button class="basis-4/12" size="medium" v-on="on">
                  <IconMdiPlus />
                  {{ t("version.new.form.addChannel") }}
                </Button>
              </template>
            </ChannelModal>
          </div>
        </div>

        <div v-for="(platformFile, idx) in platformFiles" :key="idx" class="mb-6">
          <div class="space-x-2 inline-flex items-center">
            <span class="text-xl">{{ t("version.new.form.artifactNumber", [idx + 1]) }}</span>
            <Button v-if="platformFiles.length !== 1" button-type="red" @click="removePlatformFile(idx)"><IconMdiDelete /></Button>
          </div>
          <div class="items-center">
            <Tabs v-model="platformFile.selectedTab" :tabs="selectedUploadTabs" :vertical="false" class="max-w-150">
              <template #file>
                <InputFile v-model="platformFile.file" accept=".jar,.zip" :rules="fileRules(platformFile)" />
              </template>
              <template #url>
                <InputText v-model="platformFile.url" :label="t('version.new.form.externalUrl')" :rules="artifactURLRules(platformFile)" />
              </template>
            </Tabs>
            <div class="mt-4">
              <InputGroup v-model="platformFile.platforms" label="Platforms" :rules="platformRules" :silent-errors="false">
                <div v-for="platform in platforms" :key="platform.name">
                  <InputCheckbox
                    :model-value="platformFile.platforms.includes(platform.enumName)"
                    :label="platform.name"
                    @update:model-value="togglePlatform(platformFile, platform.enumName)"
                  >
                    <PlatformLogo :platform="platform.enumName" :size="24" class="mr-1" />
                  </InputCheckbox>
                </div>
              </InputGroup>
            </div>
          </div>
        </div>
        <Button :disabled="useBackendData.platforms.size !== 0 && platformFiles.length >= useBackendData.platforms.size" @click="addPlatformFile()">
          <IconMdiPlus /> Add file/url for another platform
        </Button>
      </template>
      <template #basic>
        <p class="mb-4">{{ i18n.t("version.new.form.versionDescription") }}</p>
        <div class="flex flex-wrap mt-2 md:-space-x-2 lt-md:space-y-2">
          <!-- TODO validate version string against existing versions - now super easy! -->
          <div v-if="pendingVersion" class="basis-full md:basis-4/12 items-center">
            <InputText
              v-model="pendingVersion.versionString"
              :label="t('version.new.form.versionString')"
              :rules="versionRules"
              :maxlength="useBackendData.validations.version.max"
              counter
            />
          </div>
        </div>

        <p class="mt-8 text-xl">{{ t("version.new.form.addedArtifacts") }}</p>
        <div v-for="(pendingFile, idx) in pendingVersion?.files" :key="idx" class="mb-2">
          <div class="flex flex-wrap items-center mt-4 gap-2">
            <div v-if="pendingFile.fileInfo" class="basis-full lt-md:mt-4 md:basis-4/12">
              <InputText :model-value="pendingFile.fileInfo.name" :label="t('version.new.form.fileName')" disabled />
            </div>
            <div v-if="pendingFile.fileInfo" class="basis-full lt-md:mt-4 md:(basis-2/12)">
              <InputText :model-value="String(formatSize(pendingFile.fileInfo.sizeBytes))" :label="t('version.new.form.fileSize')" disabled />
            </div>
            <div v-else class="basis-full lt-md:mt-4 md:basis-6/12">
              <InputText v-model="pendingFile.externalUrl" :label="t('version.new.form.externalUrl')" disabled />
            </div>
            <div class="ml-2 flex flex-wrap items-center">
              <div v-for="platform in pendingFile.platforms" :key="platform">
                <PlatformLogo :platform="platform" :size="30" class="mr-1" />
              </div>
            </div>
          </div>
        </div>
      </template>
      <template #dependencies>
        <p class="mb-4">{{ i18n.t("version.new.form.platformVersionsDescription") }}</p>
        <h2 class="text-xl mt-2 mb-2">{{ t("version.new.form.platformVersions") }}</h2>
        <div class="flex flex-wrap space-y-5 mb-8">
          <div v-for="platform in selectedPlatformsData" :key="platform.enumName" class="basis-full">
            <span class="text-lg inline-flex items-center"><PlatformLogo :platform="platform.enumName" :size="25" class="mr-1" /> {{ platform.name }}</span>
            <div class="ml-1">
              <VersionSelector
                v-if="pendingVersion"
                v-model="pendingVersion.platformDependencies[platform.enumName]"
                :versions="platform.possibleVersions"
                :rules="platformVersionRules"
                open
              />
            </div>
          </div>
        </div>

        <h2 class="text-xl mb-3">{{ t("version.new.form.dependencies") }}</h2>
        <div class="flex flex-wrap space-y-7">
          <div v-for="platform in selectedPlatformsData" :key="platform.enumName" class="basis-full">
            <span class="text-lg inline-flex items-center"><PlatformLogo :platform="platform.enumName" :size="25" class="mr-1" /> {{ platform.name }}</span>
            <DependencyTable
              v-if="pendingVersion"
              ref="dependencyTables"
              :key="`${platform.name}-deps-table`"
              :platform="platform.enumName"
              :version="pendingVersion"
            />
          </div>
        </div>
      </template>
      <template #changelog>
        <h2 class="text-xl mt-2">{{ t("version.new.form.changelogTitle") }}</h2>
        <ClientOnly>
          <MarkdownEditor
            ref="descriptionEditor"
            :label="t('version.new.form.release.bulletin')"
            :raw="descriptionToLoad"
            editing
            no-padding-top
            :deletable="false"
            :cancellable="false"
            :saveable="false"
            class="mt-4"
            max-height="250px"
            :rules="changelogRules"
          />
        </ClientOnly>
      </template>
    </Steps>
    <Alert type="neutral" class="mt-4">
      {{ t("version.new.gradle-plugin-info") }}&nbsp; <Link href="https://github.com/HangarMC/hangar-publish-plugin">hangar-publish-plugin</Link>!
    </Alert>
  </div>
</template>
