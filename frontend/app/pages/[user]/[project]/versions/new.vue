<script lang="ts" setup>
import type { Step } from "#shared/types/components/design/Steps";
import type { Tab } from "#shared/types/components/design/Tabs";
import type { HangarChannel, HangarProject, PendingVersion, Platform, PlatformData } from "#shared/types/backend";

definePageMeta({
  projectPermsRequired: ["CreateVersion"],
});

const globalData = useGlobalData();
const route = useRoute("user-project-versions-new");
const router = useRouter();
const i18n = useI18n();
const t = i18n.t;
const notification = useNotificationStore();
const props = defineProps<{
  project?: HangarProject;
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
        pendingVersion.value.pluginDependencies[platform as Platform] = dependencyTable!.dependencies;
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

const platformFiles = ref<PlatformFile[]>([{ platforms: [], selectedTab: "file" }]);
const selectedUploadTabs = [
  { value: "file", header: i18n.t("version.new.form.file") },
  { value: "url", header: i18n.t("version.new.form.url") },
] as const satisfies Tab<string>[];

function addPlatformFile() {
  platformFiles.value.push({ platforms: [], selectedTab: "file" });
}

function removePlatformFile(id: number) {
  platformFiles.value.splice(id, 1);
}

const dependencyTables = useTemplateRef("dependencyTables");
const pendingVersion = ref<PendingVersion>();
const { channels } = useProjectChannels(() => route.params.project);
const selectedPlatforms = ref<Platform[]>([]);
const descriptionEditor = useTemplateRef("descriptionEditor");
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
const currentChannel = computed(() => channels.value?.find((c) => c.name === selectedChannel.value));

const selectedPlatformsData = computed<PlatformData[]>(() => {
  const result: PlatformData[] = [];
  for (const platformName of selectedPlatforms.value) {
    const p = usePlatformData(platformName);
    if (p) {
      result.push(p);
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
const versionRules = [required(), pattern()(useBackendData.validations.version.regex!), maxLength()(useBackendData.validations.version.max!)];
const platformVersionRules = [required("Select at least one platform version!"), minLength()(1)];
const changelogRules = [requiredIf()(() => selectedStep.value === "changelog")];

const v = useVuelidate();

const timeout = ref(30_000);
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

  pendingVersion.value = await useInternalApi<PendingVersion>(`versions/version/${props.project?.id}/upload`, "post", formData, {
    timeout: timeout.value,
  }).catch<any>((err) => {
    if (err.code === "ECONNABORTED") {
      notification.error("The request timed out, please try again.");
      timeout.value = timeout.value * 2;
    } else {
      handleRequestError(err);
    }
  });
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
  pendingVersion.value.description = descriptionEditor.value!.rawEdited;
  pendingVersion.value.channelDescription = currentChannel.value.description;
  pendingVersion.value.channelColor = currentChannel.value.color;
  pendingVersion.value.channelFlags = currentChannel.value.flags;

  // played around trying to get this to happen in jackson's deserialization, but couldn't figure it out.
  for (const platform in pendingVersion.value.platformDependencies) {
    if (pendingVersion.value.platformDependencies[platform as Platform]?.length === 0) {
      delete pendingVersion.value.platformDependencies[platform as Platform];
    }
  }
  for (const platform in pendingVersion.value.pluginDependencies) {
    if (pendingVersion.value.pluginDependencies[platform as Platform]?.length === 0) {
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
    await useInternalApi(`versions/version/${props.project?.id}/create`, "post", pendingVersion.value, { timeout: 45_000 });
    await router.push(`/${route.params.user}/${route.params.project}/versions/${pendingVersion.value.versionString}`);
  } catch (err: any) {
    handleRequestError(err);
  } finally {
    loading.submit = false;
  }
}

function addChannel(channel: HangarChannel) {
  if (!channels.value) return;
  channels.value = channels.value.filter((c) => !c.temp);
  channels.value.push(Object.assign({ temp: true }, channel));
  selectedChannel.value = channel.name;
}

function togglePlatform(platformFile: PlatformFile, platform: Platform) {
  if (!globalData.value?.platforms) return;
  if (platformFile.platforms.includes(platform)) {
    platformFile.platforms = platformFile.platforms.filter((p) => p !== platform);
  } else {
    platformFile.platforms.push(platform);
  }
  platformFile.platforms.sort(
    (a, b) => globalData.value!.platforms.findIndex((p) => p.enumName === a) - globalData.value!.platforms.findIndex((p) => p.enumName === b)
  );
}

useSeo(
  computed(() => ({
    title: i18n.t("version.new.title") + " | " + props.project?.name,
    route,
    description: props.project?.description,
    image: props.project?.avatarUrl,
  }))
);
</script>

<template>
  <div>
    <Steps v-model="selectedStep" :steps="steps" button-lang-key="version.new.steps." tracking-name="new-version">
      <template #artifact>
        <p class="mb-4">{{ t("version.new.form.artifactTitle") }}</p>
        <div class="flex mb-8 items-center">
          <div class="basis-full md:basis-4/12">
            <InputSelect
              v-model="selectedChannel"
              :values="channels || []"
              item-text="name"
              item-value="name"
              name="channel"
              :label="t('version.new.form.channel')"
              :rules="[required()]"
            />
          </div>
          <div class="basis-full md:(basis-4/12) ml-2">
            <ChannelModal v-if="project" :project-id="project.id" @create="addChannel as unknown as HangarChannel">
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
                <InputFile v-model="platformFile.file" accept=".jar,.zip" name="file" :rules="fileRules(platformFile)" />
              </template>
              <template #url>
                <InputText v-model="platformFile.url" :label="t('version.new.form.externalUrl')" name="url" :rules="artifactURLRules(platformFile)" />
              </template>
            </Tabs>
            <div class="mt-4">
              <InputGroup v-model="platformFile.platforms" label="Platforms" :rules="platformRules" :silent-errors="false">
                <div v-for="platform in globalData?.platforms" :key="platform.name">
                  <InputCheckbox
                    :model-value="platformFile.platforms.includes(platform.enumName)"
                    :label="platform.name"
                    :name="platform.name + '-' + idx"
                    @update:model-value="togglePlatform(platformFile, platform.enumName)"
                  >
                    <PlatformLogo :platform="platform.enumName" :size="24" class="mr-1" />
                  </InputCheckbox>
                </div>
              </InputGroup>
            </div>
          </div>
        </div>
        <Button :disabled="!!globalData?.platforms?.length && platformFiles.length >= globalData.platforms.length" @click="addPlatformFile()">
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
              name="version"
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
                :versions="platform.platformVersions"
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
              :plugin-dependencies="pendingVersion.pluginDependencies"
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
