<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { HangarProject, IPlatform, PendingVersion, ProjectChannel } from "hangar-internal";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import Steps, { Step } from "~/lib/components/design/Steps.vue";
import { computed, reactive, ref } from "vue";
import Alert from "~/lib/components/design/Alert.vue";
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
      const currentNonNullURLValue = url.value ?? "";
      return file.value == null && (currentNonNullURLValue === "" || !validUrl().$validator(currentNonNullURLValue, undefined, undefined));
    }),
  },
  {
    value: "basic",
    header: t("version.new.steps.2.header"),
    beforeNext: async () => {
      await preload();
      return true;
    },
    disableNext: computed(() => (selectedPlatforms.value?.length ?? 0) < 1),
  },
  { value: "dependencies", header: t("version.new.steps.3.header") },
  {
    value: "changelog",
    header: t("version.new.steps.4.header"),
    beforeNext: async () => {
      return createVersion();
    },
  },
];

const file = ref<File | null>();
const url = ref<string | null>();
const pendingVersion = ref<PendingVersion>();
const channels = ref<ProjectChannel[]>([]);
const selectedPlatforms = ref<Platform[]>([]);
const descriptionEditor = ref();
const loading = reactive({
  create: false,
  submit: false,
});

const isFile = computed(() => pendingVersion.value?.isFile);

const currentChannel = computed(() => channels.value.find((c) => c.name === pendingVersion.value?.channelName));

const platforms = computed<IPlatform[]>(() => {
  return [...backendData.platforms!.values()];
});
const selectedPlatformsData = computed<IPlatform[]>(() => {
  const result: IPlatform[] = [];
  for (const platformName of selectedPlatforms.value) {
    result.push(backendData.platforms!.get(platformName as Platform)!);
  }
  return result;
});

const platformVersionRules = computed(() => {
  return !pendingVersion.value?.isFile ? [] : [(v: string[]) => !!v.length || "Error"];
});

async function preload() {
  if (!pendingVersion.value) {
    return;
  }

  for (const platform in pendingVersion.value!.platformDependencies) {
    // Get last platform and plugin dependency data for the last version of the same channel/any other channel if not found
    useInternalApi<LastDependencies>(`versions/version/${props.project.namespace.owner}/${props.project.namespace.slug}/lastdependencies`, true, "get", {
      channel: pendingVersion.value?.channelName,
      platform: platform,
    })
      .then((v) => {
        if (!v || !pendingVersion.value) {
          return;
        }

        pendingVersion.value!.platformDependencies[platform as Platform] = v.platformDependencies;
        pendingVersion.value!.pluginDependencies[platform as Platform] = v.pluginDependencies;
      })
      .catch<any>((e) => handleRequestError(e, ctx, i18n));
  }
}

async function createPendingVersion() {
  loading.create = true;
  const data: FormData = new FormData();
  if (url.value) {
    data.append("url", url.value);
  } else if (file.value) {
    data.append("pluginFile", file.value);
  } else {
    return false;
  }
  channels.value = await useInternalApi<ProjectChannel[]>(`channels/${route.params.user}/${route.params.project}`, false).catch<any>((e) =>
    handleRequestError(e, ctx, i18n)
  );
  pendingVersion.value = await useInternalApi<PendingVersion>(`versions/version/${props.project.id}/upload`, true, "post", data).catch<any>((e) =>
    handleRequestError(e, ctx, i18n)
  );
  for (const platformDependenciesKey in pendingVersion.value?.platformDependencies) {
    if (pendingVersion.value?.platformDependencies[platformDependenciesKey as Platform].length) {
      selectedPlatforms.value.push(platformDependenciesKey as Platform);
    }
  }
  loading.create = false;
  return pendingVersion.value !== undefined;
}

async function createVersion() {
  if (!pendingVersion.value) {
    return false;
  }

  loading.submit = true;
  pendingVersion.value!.description = descriptionEditor.value.rawEdited;
  pendingVersion.value!.channelColor = currentChannel.value!.color;
  pendingVersion.value!.channelFlags = currentChannel.value!.flags;

  // played around trying to get this to happen in jackson's deserialization, but couldn't figure it out.
  for (const platform in pendingVersion.value.platformDependencies) {
    if (pendingVersion.value!.platformDependencies[platform as Platform].length < 1) {
      delete pendingVersion.value!.platformDependencies[platform as Platform];
    }
  }
  for (const platform in pendingVersion.value.pluginDependencies) {
    if (pendingVersion.value!.pluginDependencies[platform as Platform].length < 1) {
      delete pendingVersion.value!.pluginDependencies[platform as Platform];
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
    return true;
  } catch (e: any) {
    handleRequestError(e, ctx, i18n);
    return false;
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

function togglePlatform(platform: Platform) {
  if (selectedPlatforms.value.includes(platform)) {
    selectedPlatforms.value = selectedPlatforms.value.filter((p) => p !== platform);
  } else {
    selectedPlatforms.value.push(platform);
  }
}

const selectedUploadTab = ref("file");
const selectedUploadTabs: Tab[] = [
  { value: "file", header: i18n.t("version.new.form.file") },
  { value: "url", header: i18n.t("version.new.form.url") },
];

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
      <p>{{ t("version.new.form.artifactTitle") }}</p>
      <!--<Alert class="my-4 text-white" type="info">{{ t("version.new.form.externalLinkAlert") }}</Alert>-->

      <Tabs v-model="selectedUploadTab" :tabs="selectedUploadTabs" :vertical="false" class="mt-2 max-w-150">
        <template #file>
          <InputFile v-model="file" accept=".jar,.zip" />
        </template>
        <template #url>
          <InputText v-model="url" :label="t('version.new.form.externalUrl')" :rules="[validUrl()]" />
        </template>
      </Tabs>
    </template>
    <template #basic>
      <div class="flex flex-wrap gap-x-2">
        <!-- TODO validate version string against existing versions. complex because they only have to be unique per-platform -->
        <div class="basis-full mt-2 md:basis-4/12">
          <InputText
            v-model="pendingVersion.versionString"
            :label="t('version.new.form.versionString')"
            :rules="[required()]"
            :disabled="isFile"
            :maxlength="backendData.validations.version.max"
            counter
          />
        </div>
        <div v-if="isFile" class="basis-full mt-2 <md:mt-4 md:basis-4/12">
          <InputText :model-value="pendingVersion.fileInfo.name" :label="t('version.new.form.fileName')" disabled />
        </div>
        <div v-if="isFile" class="basis-full mt-2 <md:mt-4 md:basis-2/12">
          <InputText :model-value="formatSize(pendingVersion.fileInfo.sizeBytes)" :label="t('version.new.form.fileSize')" disabled />
        </div>
        <div v-else class="basis-full mt-2 <md:mt-4 md:basis-6/12">
          <InputText v-model="pendingVersion.externalUrl" :label="t('version.new.form.externalUrl')" />
        </div>
      </div>
      <div class="flex flex-wrap space-x-2 items-center mt-4">
        <div class="basis-4/12">
          <InputSelect v-model="pendingVersion.channelName" :values="channels" item-text="name" item-value="name" :label="t('version.new.form.channel')" />
        </div>
        <div class="basis-4/12">
          <ChannelModal :project-id="project.id" @create="addChannel">
            <template #activator="{ on, attrs }">
              <Button class="basis-4/12" v-bind="attrs" size="medium" v-on="on">
                {{ t("version.new.form.addChannel") }}
                <IconMdiPlus />
              </Button>
            </template>
          </ChannelModal>
        </div>
        <!-- todo: forum integration -->
        <!--<div class="basis-4/12 mt-2">
          <InputCheckbox v-model="pendingVersion.forumPost" :label="t('version.new.form.forumPost')" />
        </div>-->
      </div>

      <h2 class="mt-5 mb-2 text-xl">{{ t("version.new.form.platforms") }}</h2>
      <div v-for="platform in platforms" :key="platform.name" class="ml-2">
        <InputCheckbox
          :model-value="selectedPlatforms.includes(platform.enumName)"
          :rules="platformVersionRules"
          :label="platform.name"
          @update:modelValue="togglePlatform(platform.enumName)"
        >
          <PlatformLogo :platform="platform.enumName" :size="24" class="mr-1" />
        </InputCheckbox>
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
        :rules="[required(t('version.new.form.release.bulletin'))]"
      />
    </template>
  </Steps>
</template>

<route lang="yaml">
meta:
  requireProjectPerm: ["CREATE_VERSION"]
</route>
