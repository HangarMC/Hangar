<script lang="ts" setup>
import { cloneDeep } from "lodash-es";
import { useVuelidate } from "@vuelidate/core";
import { Cropper, type CropperResult } from "vue-advanced-cropper";
import type { Tab } from "~/types/components/design/Tabs";
import InputText from "~/components/ui/InputText.vue";
import { NamedPermission, Tag, Visibility } from "~/types/backend";
import type { HangarProject, HangarUser, PaginatedResultUser, ProjectSettings, Category } from "~/types/backend";

import "vue-advanced-cropper/dist/style.css";

definePageMeta({
  projectPermsRequired: ["EditSubjectSettings"],
});

const route = useRoute("user-project-settings-slug");
const router = useRouter();
const i18n = useI18n();
const v = useVuelidate();
const notificationStore = useNotificationStore();
const props = defineProps<{
  project?: HangarProject;
  user?: HangarUser;
}>();

const selectedTab = ref(route.params.slug?.[0] || "general");
const tabs = ref([
  { value: "general", header: i18n.t("project.settings.tabs.general") },
  { value: "links", header: i18n.t("project.settings.tabs.links") },
  { value: "banners", header: i18n.t("project.settings.tabs.banners") },
  // { value: "donation", header: i18n.t("project.settings.tabs.donation") },
] satisfies Tab<string>[]);

if (hasPerms(NamedPermission.IsSubjectOwner) || hasPerms(NamedPermission.DeleteProject) || hasPerms(NamedPermission.HardDeleteProject)) {
  tabs.value.push({ value: "management", header: i18n.t("project.settings.tabs.management") });
}

const form = reactive({
  settings: undefined,
  description: undefined,
  category: undefined,
} as { settings?: ProjectSettings; description?: string; category?: Category });

watch(
  () => props.project,
  (val) => {
    form.settings = cloneDeep(val?.settings);
    form.description = val?.description;
    form.category = val?.category;

    if (form.settings && !form.settings?.license?.type) {
      form.settings.license.type = "Unspecified";
    }
    if (form.settings && !form.settings?.links) {
      form.settings.links = [];
    }
  },
  { immediate: true }
);

const hasCustomIcon = computed(() => props.project?.avatarUrl?.includes("project"));
const projectIcon = ref<File | undefined>();
const cropperInput = ref();
const cropperResult = ref();
const imgSrc = ref(props.project?.avatarUrl);
let reader: FileReader | undefined;
onMounted(() => {
  reader = new FileReader();
  reader.addEventListener(
    "load",
    () => {
      cropperInput.value = reader?.result;
    },
    false
  );
});

watch(projectIcon, (newValue) => {
  if (!newValue) return;
  cropperResult.value = newValue;
  reader?.readAsDataURL(newValue);
});

function changeImage({ canvas }: CropperResult) {
  canvas?.toBlob((blob) => {
    cropperResult.value = blob;
  });
}

const newName = ref<string | null | undefined>("");
const newNameField = useTemplateRef("newNameField");
const loading = reactive({
  save: false,
  uploadIcon: false,
  resetIcon: false,
  rename: false,
  transfer: false,
});

const isCustomLicense = computed(() => form.settings?.license?.type === "Other");
const isUnspecifiedLicense = computed(() => form.settings?.license?.type === "Unspecified");

watch(route, (val) => (selectedTab.value = val.params.slug?.[0] || "general"), { deep: true });
watch(selectedTab, (val) => router.replace("/" + route.params.user + "/" + route.params.project + "/settings/" + val));

const search = ref<string>("");
const result = ref<string[]>([]);
async function doSearch(val: unknown) {
  result.value = [];
  const users = await useApi<PaginatedResultUser>("users", "get", {
    query: val,
    limit: 25,
    offset: 0,
  });
  result.value = users.result.map((u) => u.name);
}

async function save() {
  if (!(await v.value.$validate())) return;
  loading.save = true;
  try {
    if (form.settings && !isCustomLicense.value) {
      form.settings.license.name = undefined as unknown as string;
    }
    if (form.settings && isUnspecifiedLicense.value) {
      form.settings.license.url = undefined;
    }

    await useInternalApi(`projects/project/${route.params.project}/settings`, "post", {
      ...form,
    });
    await router.go(0);
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.save = false;
}

async function transfer() {
  loading.transfer = true;
  try {
    await useInternalApi<string>(`projects/project/${route.params.project}/transfer`, "post", {
      content: search.value,
    });
    notificationStore.success(i18n.t("project.settings.success.transferRequest", [search.value]));
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.transfer = false;
}

async function rename() {
  loading.rename = true;
  try {
    const newSlug = await useInternalApi<string>(`projects/project/${route.params.project}/rename`, "post", {
      content: newName.value,
    });
    await notificationStore.success(i18n.t("project.settings.success.rename", [newName.value]));
    await router.push("/" + route.params.user + "/" + newSlug);
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.rename = false;
}

async function softDelete(comment: string) {
  try {
    await useInternalApi(`projects/project/${props.project?.id}/manage/delete`, "post", {
      content: comment,
    });
    await notificationStore.success(i18n.t("project.settings.success.softDelete"));
    if (hasPerms(NamedPermission.HardDeleteProject)) {
      router.go(0);
    } else {
      await router.push("/");
    }
  } catch (err: any) {
    handleRequestError(err);
  }
}

async function hardDelete(comment: string) {
  try {
    await useInternalApi(`projects/project/${props.project?.id}/manage/hardDelete`, "post", {
      content: comment,
    });
    await notificationStore.success(i18n.t("project.settings.success.hardDelete"));
    await router.push("/");
  } catch (err: any) {
    handleRequestError(err);
  }
}

async function uploadIcon() {
  if (!cropperResult.value) {
    return;
  }

  const data = new FormData();
  data.append("projectIcon", cropperResult.value);
  loading.uploadIcon = true;
  try {
    const response = await useInternalApi<string | null>(`projects/project/${route.params.project}/saveIcon`, "post", data);
    imgSrc.value = URL.createObjectURL(cropperResult.value); // set temporary source so it changes right away
    projectIcon.value = undefined;
    cropperInput.value = undefined;
    cropperResult.value = undefined;
    await (response
      ? notificationStore.success(i18n.t("project.settings.success.changedIconWarn", [response]))
      : notificationStore.success(i18n.t("project.settings.success.changedIcon")));
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.uploadIcon = false;
}

async function resetIcon() {
  loading.resetIcon = true;
  try {
    const response = await useInternalApi<string | null>(`projects/project/${route.params.project}/resetIcon`, "post");
    await (response
      ? notificationStore.success(i18n.t("project.settings.success.resetIconWarn", [response]))
      : notificationStore.success(i18n.t("project.settings.success.resetIcon")));
    imgSrc.value = props.user?.avatarUrl; // set temporary source so it changes right away
    projectIcon.value = undefined;
    cropperInput.value = undefined;
    cropperResult.value = undefined;
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.resetIcon = false;
}

const shieldIoStyle = ref("flat");
const mcBannersStyle = ref("DARK_GUNMETAL");
const bannerUrls = computed(() => ({
  author: `https://api.mcbanners.com/banner/author/hangar/${props.project?.namespace?.slug}/banner.png?background__template=${mcBannersStyle.value}`,
  resource: `https://api.mcbanners.com/banner/resource/hangar/${props.project?.namespace?.slug}/banner.png?background__template=${mcBannersStyle.value}`,
  downloads: `https://img.shields.io/hangar/dt/${props.project?.namespace?.slug}?link=https%3A%2F%2Fhangar.papermc.io%2F${props.project?.namespace?.owner}%2F${props.project?.namespace?.slug}&style=${shieldIoStyle.value}`,
  stars: `https://img.shields.io/hangar/stars/${props.project?.namespace?.slug}?link=https%3A%2F%2Fhangar.papermc.io%2F${props.project?.namespace?.owner}%2F${props.project?.namespace?.slug}&style=${shieldIoStyle.value}`,
  views: `https://img.shields.io/hangar/views/${props.project?.namespace?.slug}?link=https%3A%2F%2Fhangar.papermc.io%2F${props.project?.namespace?.owner}%2F${props.project?.namespace?.slug}&style=${shieldIoStyle.value}`,
}));

function copyToClipboard(event: any, url: string, type: string = "url") {
  const clipboardData = event.clipboardData || event.originalEvent?.clipboardData || navigator.clipboard;
  if (type === "markdown") {
    url = `[![${props.project.name}](${url})](https://hangar.papermc.io/${props.project.namespace.owner}/${props.project.namespace.slug})`;
  }
  clipboardData.writeText(url);
  notificationStore.success(i18n.t("project.settings.banners.copied"));
}

useSeo(
  computed(() => ({
    title: i18n.t("project.settings.title") + " | " + props.project?.name,
    route,
    description: props.project?.description,
    image: props.project?.avatarUrl,
  }))
);
</script>

<template>
  <div class="flex gap-4 flex-col md:flex-row">
    <Card class="basis-full md:basis-9/12">
      <template #header>
        <div class="flex justify-between lt-sm:items-center">
          {{ i18n.t("project.settings.title") }}
          <div class="text-lg">
            <Button :disabled="v.$error" :loading="loading.save" @click="save">
              <IconMdiCheck />
              {{ i18n.t("project.settings.save") }}
            </Button>
          </div>
        </div>
      </template>

      <!-- setting icons -->
      <Tabs v-model="selectedTab" :tabs="tabs">
        <template #general>
          <ProjectSettingsSection title="project.settings.category" description="project.settings.categorySub">
            <InputSelect v-model="form.category" :values="useCategoryOptions" :rules="[required()]" i18n-text-values />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.description" description="project.settings.descriptionSub">
            <InputText
              v-model="form.description"
              counter
              :maxlength="useBackendData.validations?.project?.desc?.max || 120"
              :rules="[required(), maxLength()(useBackendData.validations?.project?.desc?.max || 120)]"
            />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.keywords" description="project.settings.keywordsSub">
            <InputTag
              v-if="form.settings"
              v-model="form.settings.keywords"
              counter
              :maxlength="useBackendData.validations?.project?.keywords?.max || 5"
              :tag-maxlength="useBackendData.validations?.project?.keywordName?.max || 16"
              :label="i18n.t('project.new.step3.keywords')"
              :rules="[maxLength()(useBackendData.validations?.project?.keywords?.max || 5), noDuplicated()(() => form.settings?.keywords)]"
            />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.tags.title" description="project.settings.tagsSub">
            <template v-if="form.settings">
              <InputCheckbox v-for="tag in Object.values(Tag)" :key="tag" v-model="form.settings.tags" :value="tag">
                <template #label>
                  <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
                  <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
                  <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" />
                  <span class="ml-1">{{ i18n.t("project.settings.tags." + tag + ".title") }}</span>
                  <Tooltip>
                    <template #content> {{ i18n.t("project.settings.tags." + tag + ".description") }} </template>
                    <IconMdiHelpCircleOutline class="ml-1 text-gray-500 dark:text-gray-400 text-sm" />
                  </Tooltip>
                </template>
              </InputCheckbox>
            </template>
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.license" description="project.settings.licenseSub">
            <div class="flex md:gap-2 lt-md:flex-wrap">
              <div class="basis-full" :md="isCustomLicense ? 'basis-4/12' : 'basis-6/12'">
                <InputSelect
                  v-if="form.settings"
                  v-model="form.settings.license.type"
                  :values="useLicenseOptions"
                  :label="i18n.t('project.settings.licenseType')"
                />
              </div>
              <div v-if="isCustomLicense" class="basis-full md:basis-8/12">
                <InputText
                  v-if="form.settings"
                  v-model.trim="form.settings.license.name"
                  :label="i18n.t('project.settings.licenseCustom')"
                  :rules="[
                    requiredIf()(isCustomLicense),
                    maxLength()(useBackendData.validations.project.license.max!),
                    pattern()(useBackendData.validations.project.license.regex!),
                  ]"
                />
              </div>
              <div v-if="!isUnspecifiedLicense" class="basis-full" :md="isCustomLicense ? 'basis-full' : 'basis-6/12'">
                <InputText v-if="form.settings" v-model.trim="form.settings.license.url" :label="i18n.t('project.settings.licenseUrl')" :rules="[validUrl()]" />
              </div>
            </div>
          </ProjectSettingsSection>
          <ProjectSettingsSection>
            <div class="grid grid-cols-3 grid-rows-[1fr,1fr,min-content] gap-2 w-full">
              <div class="col-span-2 row-span-1">
                <h2 class="text-lg font-semibold">{{ i18n.t("project.settings.icon") }}</h2>
                <p>{{ i18n.t("project.settings.iconSub") }}</p>
              </div>
              <div class="col-span-2">
                <InputFile v-model="projectIcon" accept="image/png, image/jpeg, image/webp" show-size />
              </div>
              <Button :disabled="!cropperResult" :loading="loading.uploadIcon" @click="uploadIcon">
                <IconMdiUpload />
                {{ i18n.t("project.settings.iconUpload") }}
              </Button>
              <Button :disabled="!hasCustomIcon" :loading="loading.resetIcon" @click="resetIcon">
                <IconMdiCached />
                {{ i18n.t("project.settings.iconReset") }}
              </Button>
              <div class="col-span-1 col-start-3 row-start-1 row-span-3" :class="{ 'justify-self-center': !cropperInput }">
                <cropper
                  v-if="cropperInput"
                  :src="cropperInput"
                  class="h-150px"
                  :min-height="150"
                  :canvas="{
                    imageSmoothingQuality: 'high',
                    maxWidth: 256,
                    maxHeight: 256,
                  }"
                  :stencil-props="{
                    handlers: {},
                    movable: false,
                    scalable: false,
                    aspectRatio: 1,
                  }"
                  :resize-image="{
                    adjustStencil: false,
                  }"
                  image-restriction="stencil"
                  @change="changeImage"
                />
                <img v-else id="project-icon-preview" width="150" height="150" alt="Project Icon" :src="imgSrc" />
              </div>
            </div>
          </ProjectSettingsSection>
        </template>
        <template #links>
          <ProjectSettingsSection title="project.settings.links.title" description="project.settings.links.sub">
            <ProjectLinksForm v-if="form.settings" v-model="form.settings.links" />
          </ProjectSettingsSection>
        </template>
        <template #management>
          <ProjectSettingsSection v-if="hasPerms(NamedPermission.IsSubjectOwner)" title="project.settings.rename" description="project.settings.renameSub">
            <div class="flex items-center">
              <InputText ref="newNameField" v-model.trim="newName" :label="i18n.t('project.settings.newName')" :rules="[validProjectName()()]" />
              <Button :disabled="!newName || newNameField?.validation?.$invalid" :loading="loading.rename" class="ml-2" @click="rename">
                <IconMdiRenameBox class="mr-2" />
                {{ i18n.t("project.settings.rename") }}
              </Button>
            </div>
          </ProjectSettingsSection>
          <ProjectSettingsSection v-if="hasPerms(NamedPermission.IsSubjectOwner)" title="project.settings.transfer" description="project.settings.transferSub">
            <div class="flex items-center">
              <InputAutocomplete id="membersearch" v-model="search" :values="result" :label="i18n.t('project.settings.transferTo')" @search="doSearch" />
              <Button :disabled="search.length === 0" :loading="loading.transfer" class="ml-2" @click="transfer">
                <IconMdiRenameBox class="mr-2" />
                {{ i18n.t("project.settings.transfer") }}
              </Button>
            </div>
          </ProjectSettingsSection>
          <ProjectSettingsSection
            v-if="hasPerms(NamedPermission.DeleteProject) && project?.visibility !== Visibility.SoftDelete"
            title="project.settings.delete"
            description="project.settings.deleteSub"
            class="bg-red-200 dark:(bg-red-900 text-white) rounded-md p-4"
          >
            <TextAreaModal :title="i18n.t('project.settings.delete')" :label="i18n.t('general.comment')" :submit="softDelete" require-input>
              <template #activator="{ on }">
                <Button button-type="red" v-on="on">{{ i18n.t("project.settings.delete") }}</Button>
              </template>
            </TextAreaModal>
          </ProjectSettingsSection>
          <ProjectSettingsSection
            v-if="hasPerms(NamedPermission.HardDeleteProject)"
            title="project.settings.hardDelete"
            description="project.settings.hardDeleteSub"
            class="bg-red-200 dark:(bg-red-900 text-white) rounded-md p-4"
          >
            <TextAreaModal :title="i18n.t('project.settings.hardDelete')" :label="i18n.t('general.comment')" :submit="hardDelete" require-input>
              <template #activator="{ on }">
                <Button button-type="red" v-on="on">{{ i18n.t("project.settings.hardDelete") }}</Button>
              </template>
            </TextAreaModal>
          </ProjectSettingsSection>
        </template>
        <!--<template #donation>
          <Alert type="info" class="my-4">Coming Soon!</Alert>
          <ProjectSettingsSection title="project.settings.donation.enable">
            <InputCheckbox v-model="form.settings.donation.enable" :label="i18n.t('project.settings.donation.enableSub')" disabled />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.donation.subject" description="project.settings.donation.subjectSub">
            <InputText
              v-model="form.settings.donation.subject"
              :label="i18n.t('project.settings.donation.subjectLabel')"
              :rules="[requiredIf()(form.settings.donation.enable)]"
              disabled
            />
          </ProjectSettingsSection>
        </template>-->
        <template #banners>
          <ProjectSettingsSection title="project.settings.banners.mcbanners" description="project.settings.banners.mcbannersSub">
            <div class="mb-2">
              <InputSelect
                v-model="mcBannersStyle"
                :values="[
                  'BLUE_RADIAL',
                  'BURNING_ORANGE',
                  'MANGO',
                  'MOONLIGHT_PURPLE',
                  'ORANGE_RADIAL',
                  'VELVET',
                  'YELLOW',
                  'MALACHITE_GREEN',
                  'DARK_GUNMETAL',
                  'PURPLE_TAUPE',
                  'LIGHT_MODE',
                ]"
                :label="i18n.t('project.settings.banners.style')"
              />
            </div>
            <div class="mb-1">{{ i18n.t("project.settings.banners.author") }}:</div>
            <img :src="bannerUrls.author" alt="" />
            <div class="flex gap-2 my-2">
              <Button @click="copyToClipboard($event, bannerUrls.author, 'markdown')">{{ i18n.t("project.settings.banners.markdown") }}</Button>
              <Button @click="copyToClipboard($event, bannerUrls.author)">{{ i18n.t("project.settings.banners.url") }}</Button>
            </div>
            <div class="mb-1">{{ i18n.t("project.settings.banners.resource") }}:</div>
            <img :src="bannerUrls.resource" alt="" />
            <div class="flex gap-2 my-2">
              <Button @click="copyToClipboard($event, bannerUrls.resource, 'markdown')">{{ i18n.t("project.settings.banners.markdown") }}</Button>
              <Button @click="copyToClipboard($event, bannerUrls.resource)">{{ i18n.t("project.settings.banners.url") }}</Button>
            </div>
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.banners.shields" description="project.settings.banners.shieldsSub">
            <div class="mb-2">
              <InputSelect
                v-model="shieldIoStyle"
                :values="['flat', 'flat-square', 'plastic', 'for-the-badge', 'social']"
                :label="i18n.t('project.settings.banners.style')"
              />
            </div>
            <div class="mb-1">{{ i18n.t("project.settings.banners.downloads") }}:</div>
            <img :src="bannerUrls.downloads" alt="" />
            <div class="flex gap-2 my-2">
              <Button @click="copyToClipboard($event, bannerUrls.downloads, 'markdown')">{{ i18n.t("project.settings.banners.markdown") }}</Button>
              <Button @click="copyToClipboard($event, bannerUrls.downloads)">{{ i18n.t("project.settings.banners.url") }}</Button>
            </div>
            <div class="mb-1">{{ i18n.t("project.settings.banners.stars") }}:</div>
            <img :src="bannerUrls.stars" alt="" />
            <div class="flex gap-2 my-2">
              <Button @click="copyToClipboard($event, bannerUrls.stars, 'markdown')">{{ i18n.t("project.settings.banners.markdown") }}</Button>
              <Button @click="copyToClipboard($event, bannerUrls.stars)">{{ i18n.t("project.settings.banners.url") }}</Button>
            </div>
            <div class="mb-1">{{ i18n.t("project.settings.banners.views") }}:</div>
            <img :src="bannerUrls.views" alt="" />
            <div class="flex gap-2 my-2">
              <Button @click="copyToClipboard($event, bannerUrls.views, 'markdown')">{{ i18n.t("project.settings.banners.markdown") }}</Button>
              <Button @click="copyToClipboard($event, bannerUrls.views)">{{ i18n.t("project.settings.banners.url") }}</Button>
            </div>
          </ProjectSettingsSection>
        </template>
      </Tabs>
    </Card>
    <MemberList :members="project?.members || []" :author="project?.namespace?.owner" :slug="project?.name" class="basis-full md:basis-3/12 h-max" />
  </div>
</template>
