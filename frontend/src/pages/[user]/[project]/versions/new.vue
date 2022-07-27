<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { HangarProject, IPlatform, PendingVersion, ProjectChannel } from "hangar-internal";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import Steps, { Step } from "~/lib/components/design/Steps.vue";
import { computed, reactive, Ref, ref } from "vue";
import InputFile from "~/lib/components/ui/InputFile.vue";
import InputText from "~/lib/components/ui/InputText.vue";
import InputSelect from "~/lib/components/ui/InputSelect.vue";
import Button from "~/lib/components/design/Button.vue";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import MarkdownEditor from "~/components/MarkdownEditor.vue";
import { required, url as validUrl } from "~/lib/composables/useValidationHelpers";
import { useInternalApi } from "~/composables/useApi";
import { Platform } from "~/types/enums";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { formatSize } from "~/lib/composables/useFile";
import ChannelModal from "~/components/modals/ChannelModal.vue";
import { remove } from "lodash-es";
import { useBackendDataStore } from "~/store/backendData";
import DependencyTable from "~/components/projects/DependencyTable.vue";
import InputTag from "~/lib/components/ui/InputTag.vue";
import { LastDependencies } from "hangar-api";
import Tabs, { Tab } from "~/lib/components/design/Tabs.vue";
import PlatformLogo from "~/components/logos/platforms/PlatformLogo.vue";

const route = useRoute();
const router = useRouter();
const ctx = useContext();
const i18n = useI18n();
const t = i18n.t;
const backendData = useBackendDataStore();
const props = defineProps<{
  project: HangarProject;
}>();

const selectedStep = ref("artifact");
const steps: Step[] = [
  {
    value: "artifact",
    header: t("version.new.steps.1.header"),
    beforeNext: async () => {
      return createPendingVersion();
    },
    disableNext: computed(() => {
      const selectedPlatforms = new Set();
      for (const platformFile of platformFiles.value) {
        // Make sure it has at least one platform
        if (platformFile.platforms.length === 0) {
          return true;
        }

        // Make sure there's either a file or a (valid) URL
        if (!platformFile.file && (!platformFile.url || artifactURLRules.some((v) => !v.$validator(platformFile.url, undefined, undefined)))) {
          return true;
        }

        // Check for duplicated platforms
        for (const platform of platformFile.platforms) {
          if (selectedPlatforms.has(platform)) {
            return true;
          }
          selectedPlatforms.add(platform);
        }
      }
      return false;
    }),
  },
  {
    value: "basic",
    header: t("version.new.steps.2.header"),
    beforeNext: async () => {
      await preload();
      return true;
    },
    disableNext: computed(() => {
      return (
        (selectedPlatforms.value?.length ?? 0) < 1 ||
        versionRules.some((v) => {
          return !v.$validator(pendingVersion.value?.versionString, undefined, undefined);
        })
      );
    }),
  },
  {
    value: "dependencies",
    header: t("version.new.steps.3.header"),
    disableNext: computed(() => {
      return selectedPlatformsData.value.some((p) => (pendingVersion.value?.platformDependencies[p.enumName].length ?? 0) < 1);
    }),
  },
  {
    value: "changelog",
    header: t("version.new.steps.4.header"),
    beforeNext: async () => {
      await createVersion();
      return false; // createVersion already hijacks the beforeNext logic, cannot move next on final step.
    },
    disableNext: computed(() => {
      return changelogRules.some((v) => {
        return !v.$validator(descriptionEditor.value?.rawEdited ?? "", undefined, undefined);
      });
    }),
  },
];

interface PlatformFile {
  platforms: Platform[];
  //TODO refs?
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

const pendingVersion: Ref<PendingVersion | undefined> = ref<PendingVersion>();
const channels = ref<ProjectChannel[]>([]);
const selectedPlatforms = ref<Platform[]>([]);
const descriptionEditor = ref();
const loading = reactive({
  create: false,
  submit: false,
});

const currentChannel = computed(() => channels.value.find((c) => c.name === pendingVersion.value?.channelName));

const platforms = computed<IPlatform[]>(() => {
  return [...backendData.platforms.values()];
});
const selectedPlatformsData = computed<IPlatform[]>(() => {
  const result: IPlatform[] = [];
  for (const platformName of selectedPlatforms.value) {
    result.push(backendData.platforms.get(platformName as Platform)!);
  }
  return result;
});

const artifactURLRules = [validUrl()];
const versionRules = [required()];
const platformVersionRules = computed(() => {
  return [(v: string[]) => !!v.length || "Error"];
});
const changelogRules = [required(t("version.new.form.release.bulletin"))];

async function preload() {
  if (!pendingVersion.value) {
    return;
  }

  for (const platform in pendingVersion.value.platformDependencies) {
    // Get last platform and plugin dependency data for the last version of the same channel/any other channel if not found
    useInternalApi<LastDependencies>(`versions/version/${props.project.namespace.owner}/${props.project.namespace.slug}/lastdependencies`, true, "get", {
      channel: pendingVersion.value?.channelName,
      platform: platform,
    })
      .then((v) => {
        if (!v || !pendingVersion.value) {
          return;
        }

        const p: Platform = platform as Platform;
        if (v.platformDependencies.length !== 0) {
          pendingVersion.value.platformDependencies[p] = v.platformDependencies;
        }
        if (v.pluginDependencies.length !== 0) {
          pendingVersion.value.pluginDependencies[p] = v.pluginDependencies;
        }
      })
      .catch<any>((e) => handleRequestError(e, ctx, i18n));
  }
}

async function createPendingVersion() {
  loading.create = true;
  const files = [];
  for (const platformFile of platformFiles.value) {
    files.push({ platforms: platformFile.platforms, file: platformFile.file, externalUrl: platformFile.url });
    for (const platform of platformFile.platforms) {
      selectedPlatforms.value.push(platform);
    }
  }

  channels.value = await useInternalApi<ProjectChannel[]>(`channels/${route.params.user}/${route.params.project}`, false).catch<any>((e) =>
    handleRequestError(e, ctx, i18n)
  );
  pendingVersion.value = await useInternalApi<PendingVersion>(`versions/version/${props.project.id}/upload`, true, "post", files).catch<any>((e) =>
    handleRequestError(e, ctx, i18n)
  );
  loading.create = false;
  return pendingVersion.value !== undefined;
}

async function createVersion() {
  if (!pendingVersion.value || !currentChannel.value) {
    return;
  }

  loading.submit = true;
  pendingVersion.value.description = descriptionEditor.value.rawEdited;
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
    await useInternalApi(`versions/version/${props.project.id}/create`, true, "post", pendingVersion.value);
    await router.push(`/${route.params.user}/${route.params.project}/versions`);
  } catch (e: any) {
    handleRequestError(e, ctx, i18n);
  } finally {
    loading.submit = false;
  }
}

function addChannel(channel: ProjectChannel) {
  if (pendingVersion.value) {
    remove(channels.value, (c) => c.temp);
    channels.value.push(Object.assign({ temp: true }, channel));
    pendingVersion.value.channelName = channel.name;
  }
}

function togglePlatform(platformFile: PlatformFile, platform: Platform) {
  if (platformFile.platforms.includes(platform)) {
    platformFile.platforms = platformFile.platforms.filter((p) => p !== platform);
  } else {
    platformFile.platforms.push(platform);
  }
}

useHead(
  useSeo(
    i18n.t("version.new.title") + " | " + props.project.name,
    props.project.description,
    route,
    projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)
  )
);
</script>

<template>
  <Steps v-model="selectedStep" :steps="steps" button-lang-key="version.new.steps.">
    <template #artifact>
      <p class="mb-2">{{ t("version.new.form.artifactTitle") }}</p>

      <!-- todo: make prettier, translations -->
      <div v-for="(platformFile, idx) in platformFiles" :key="idx" class="mb-8">
        <span class="text-xl">File #{{ idx + 1 }}</span>
        <div class="flex flex-row items-center">
          <Tabs v-model="platformFile.selectedTab" :tabs="selectedUploadTabs" :vertical="false" class="max-w-150">
            <template #file>
              <InputFile v-model="platformFile.file" accept=".jar,.zip" />
            </template>
            <template #url>
              <InputText v-model="platformFile.url" :label="t('version.new.form.externalUrl')" :rules="artifactURLRules" />
            </template>
          </Tabs>
          <div class="mt-4 ml-8">
            <div v-for="platform in platforms" :key="platform.name">
              <InputCheckbox
                :model-value="platformFile.platforms.includes(platform.enumName)"
                :label="platform.name"
                @update:model-value="togglePlatform(platformFile, platform.enumName)"
              >
                <PlatformLogo :platform="platform.enumName" :size="24" class="mr-1" />
              </InputCheckbox>
            </div>
          </div>
          <Button v-if="platformFiles.length !== 1" class="ml-4 mt-4" @click="removePlatformFile(idx)"><IconMdiDelete /></Button>
        </div>
        <div></div>
      </div>
      <Button :disabled="platformFiles.length >= backendData.platforms.size" class="mt-4" @click="addPlatformFile()">
        <IconMdiPlus /> Add file/url for another platform
      </Button>
    </template>
    <template #basic>
      <div class="flex flex-wrap mt-2 md:-space-x-2 <md:space-y-2">
        <!-- TODO validate version string against existing versions - now super easy! -->
        <div class="basis-full md:basis-4/12 items-center">
          <InputText
            v-model="pendingVersion.versionString"
            :label="t('version.new.form.versionString')"
            :rules="versionRules"
            :maxlength="backendData.validations.version.max"
            counter
          />
        </div>
        <div class="basis-full md:basis-4/12">
          <InputSelect v-model="pendingVersion.channelName" :values="channels" item-text="name" item-value="name" :label="t('version.new.form.channel')" />
        </div>
        <div class="basis-full md:basis-4/12">
          <ChannelModal :project-id="project.id" @create="addChannel">
            <template #activator="{ on, attrs }">
              <Button class="basis-4/12" v-bind="attrs" size="medium" v-on="on">
                {{ t("version.new.form.addChannel") }}
                <IconMdiPlus />
              </Button>
            </template>
          </ChannelModal>
        </div>
      </div>

      <p class="mt-8 text-xl">{{ t("version.new.form.addedArtifacts") }}</p>
      <div v-for="(pendingFile, idx) in pendingVersion.files" :key="idx" class="mb-2">
        <div class="flex flex-wrap items-center mt-4">
          <div v-if="pendingFile.file" class="basis-full <md:mt-4 md:basis-4/12">
            <InputText :model-value="pendingFile.file.name" :label="t('version.new.form.fileName')" disabled />
          </div>
          <div v-if="pendingFile.file" class="basis-full <md:mt-4 md:basis-2/12">
            <InputText :model-value="formatSize(pendingFile.file.sizeBytes)" :label="t('version.new.form.fileSize')" disabled />
          </div>
          <div v-else class="basis-full <md:mt-4 md:basis-6/12">
            <InputText v-model="pendingFile.externalUrl" :label="t('version.new.form.externalUrl')" disabled />
          </div>
          <div v-for="platform in pendingFile.platforms" :key="platform">
            <PlatformLogo :platform="platform" size="30" class="mr-1" />
          </div>
        </div>
      </div>
    </template>
    <template #dependencies>
      <h2 class="text-xl mt-2 mb-2">{{ t("version.new.form.platformVersions") }}</h2>

      <div class="flex flex-wrap gap-y-3 mb-5">
        <div v-for="platform in selectedPlatformsData" :key="platform.name" class="basis-full">
          <div>{{ platform.name }}</div>
          <div class="mt-2">
            <InputTag v-model="pendingVersion.platformDependencies[platform.enumName]" :options="platform?.possibleVersions" :rules="platformVersionRules" />
          </div>
        </div>
      </div>

      <h2 class="text-xl mb-2">{{ t("version.new.form.dependencies") }}</h2>
      <div class="flex flex-wrap gap-y-3">
        <div v-for="platform in selectedPlatformsData" :key="platform.enumName" class="basis-full">
          <div>{{ platform.name }}</div>
          <DependencyTable :key="`${platform.name}-deps-table`" :platform="platform.enumName" :version="pendingVersion" :is-new="!pendingVersion.isFile" />
        </div>
      </div>
    </template>
    <template #changelog>
      <h2 class="text-xl mt-2">{{ t("version.new.form.changelogTitle") }}</h2>
      <MarkdownEditor
        ref="descriptionEditor"
        class="mt-2"
        :raw="pendingVersion?.description ?? ''"
        editing
        :deletable="false"
        :cancellable="false"
        :saveable="false"
        :rules="changelogRules"
      />
    </template>
  </Steps>
</template>

<route lang="yaml">
meta:
  requireProjectPerm: ["CREATE_VERSION"]
</route>
