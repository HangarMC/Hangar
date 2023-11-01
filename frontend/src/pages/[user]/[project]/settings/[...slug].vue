<script lang="ts" setup>
import { useHead } from "@unhead/vue";
import { useRoute, useRouter } from "vue-router";
import type { HangarProject, HangarUser } from "hangar-internal";
import { useI18n } from "vue-i18n";
import { computed, onMounted, reactive, ref, watch } from "vue";
import { cloneDeep } from "lodash-es";
import { useVuelidate } from "@vuelidate/core";
import { Cropper, type CropperResult } from "vue-advanced-cropper";
import type { PaginatedResult, User } from "hangar-api";
import { useSeo } from "~/composables/useSeo";
import Card from "~/components/design/Card.vue";
import MemberList from "~/components/projects/MemberList.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission, Tag, Visibility } from "~/types/enums";
import Button from "~/components/design/Button.vue";
import Tabs from "~/components/design/Tabs.vue";
import InputSelect from "~/components/ui/InputSelect.vue";
import { useBackendData, useCategoryOptions, useLicenseOptions } from "~/store/backendData";
import InputText from "~/components/ui/InputText.vue";
import InputFile from "~/components/ui/InputFile.vue";
import { useApi, useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useNotificationStore } from "~/store/notification";
import InputTag from "~/components/ui/InputTag.vue";
import TextAreaModal from "~/components/modals/TextAreaModal.vue";
import ProjectSettingsSection from "~/components/projects/ProjectSettingsSection.vue";
import { maxLength, required, pattern, requiredIf, url, noDuplicated } from "~/composables/useValidationHelpers";
import { validProjectName } from "~/composables/useHangarValidations";

import "vue-advanced-cropper/dist/style.css";
import InputAutocomplete from "~/components/ui/InputAutocomplete.vue";
import { definePageMeta } from "#imports";
import type { Tab } from "~/types/components/design/Tabs";
import ProjectLinksForm from "~/components/projects/ProjectLinksForm.vue";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import Tooltip from "~/components/design/Tooltip.vue";

definePageMeta({
  projectPermsRequired: ["EDIT_SUBJECT_SETTINGS"],
});

const route = useRoute();
const router = useRouter();
const i18n = useI18n();
const v = useVuelidate();
const notificationStore = useNotificationStore();
const props = defineProps<{
  project: HangarProject;
  user: HangarUser;
}>();

const selectedTab = ref(route.params.slug?.[0] || "general");
const tabs: Tab[] = [
  { value: "general", header: i18n.t("project.settings.tabs.general") },
  { value: "links", header: i18n.t("project.settings.tabs.links") },
  { value: "management", header: i18n.t("project.settings.tabs.management") },
  // { value: "donation", header: i18n.t("project.settings.tabs.donation") },
];

const form = reactive({
  settings: cloneDeep(props.project.settings),
  description: props.project.description,
  category: props.project.category,
});
if (!form.settings.license.type) {
  form.settings.license.type = "Unspecified";
}
if (!form.settings.links) {
  form.settings.links = [];
}

const hasCustomIcon = computed(() => props.project.avatarUrl.includes("project"));
const projectIcon = ref<File | null>(null);
const cropperInput = ref();
const cropperResult = ref();
const imgSrc = ref(props.project.avatarUrl);
let reader: FileReader | null = null;
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
  if (!newValue) return null;
  cropperResult.value = newValue;
  reader?.readAsDataURL(newValue);
});

function changeImage({ canvas }: CropperResult) {
  canvas?.toBlob((blob) => {
    cropperResult.value = blob;
  });
}

const newName = ref<string | null | undefined>("");
const newNameField = ref<InstanceType<typeof InputText> | null>(null);
const loading = reactive({
  save: false,
  uploadIcon: false,
  resetIcon: false,
  rename: false,
  transfer: false,
});

const isCustomLicense = computed(() => form.settings.license.type === "Other");
const isUnspecifiedLicense = computed(() => form.settings.license.type === "Unspecified");

watch(route, (val) => (selectedTab.value = val.params.slug?.[0] || "general"), { deep: true });
watch(selectedTab, (val) => router.replace("/" + route.params.user + "/" + route.params.project + "/settings/" + val));

const search = ref<string>("");
const result = ref<string[]>([]);
async function doSearch(val: unknown) {
  result.value = [];
  const users = await useApi<PaginatedResult<User>>("users", "get", {
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
    if (!isCustomLicense.value) {
      form.settings.license.name = null;
    }
    if (isUnspecifiedLicense.value) {
      form.settings.license.url = null;
    }

    await useInternalApi(`projects/project/${route.params.project}/settings`, "post", {
      ...form,
    });
    await router.go(0);
  } catch (e: any) {
    handleRequestError(e);
  }
  loading.save = false;
}

async function transfer() {
  loading.transfer = true;
  try {
    await useInternalApi<string>(`projects/project/${route.params.user}/${route.params.project}/transfer`, "post", {
      content: search.value,
    });
    notificationStore.success(i18n.t("project.settings.success.transferRequest", [search.value]));
  } catch (e: any) {
    handleRequestError(e);
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
  } catch (e: any) {
    handleRequestError(e);
  }
  loading.rename = false;
}

async function softDelete(comment: string) {
  try {
    await useInternalApi(`projects/project/${props.project.id}/manage/delete`, "post", {
      content: comment,
    });
    await notificationStore.success(i18n.t("project.settings.success.softDelete"));
    if (hasPerms(NamedPermission.HARD_DELETE_PROJECT)) {
      router.go(0);
    } else {
      await router.push("/");
    }
  } catch (e: any) {
    handleRequestError(e);
  }
}

async function hardDelete(comment: string) {
  try {
    await useInternalApi(`projects/project/${props.project.id}/manage/hardDelete`, "post", {
      content: comment,
    });
    await notificationStore.success(i18n.t("project.settings.success.hardDelete"));
    await router.push("/");
  } catch (e: any) {
    handleRequestError(e);
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
    projectIcon.value = null;
    cropperInput.value = null;
    cropperResult.value = null;
    await (response
      ? notificationStore.success(i18n.t("project.settings.success.changedIconWarn", [response]))
      : notificationStore.success(i18n.t("project.settings.success.changedIcon")));
  } catch (e: any) {
    handleRequestError(e);
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
    imgSrc.value = props.user.avatarUrl; // set temporary source so it changes right away
    projectIcon.value = null;
    cropperInput.value = null;
    cropperResult.value = null;
  } catch (e: any) {
    handleRequestError(e);
  }
  loading.resetIcon = false;
}

useHead(useSeo(i18n.t("project.settings.title") + " | " + props.project.name, props.project.description, route, props.project.avatarUrl));
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
              v-model="form.settings.keywords"
              counter
              :maxlength="useBackendData.validations?.project?.keywords?.max || 5"
              :tag-maxlength="useBackendData.validations?.project?.keywordName?.max || 16"
              :label="i18n.t('project.new.step3.keywords')"
              :rules="[maxLength()(useBackendData.validations?.project?.keywords?.max || 5), noDuplicated()(() => form.settings.keywords)]"
            />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.tags.title" description="project.settings.tagsSub">
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
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.license" description="project.settings.licenseSub">
            <div class="flex md:gap-2 lt-md:flex-wrap">
              <div class="basis-full" :md="isCustomLicense ? 'basis-4/12' : 'basis-6/12'">
                <InputSelect v-model="form.settings.license.type" :values="useLicenseOptions" :label="i18n.t('project.settings.licenseType')" />
              </div>
              <div v-if="isCustomLicense" class="basis-full md:basis-8/12">
                <InputText
                  v-model.trim="form.settings.license.name"
                  :label="i18n.t('project.settings.licenseCustom')"
                  :rules="[
                    requiredIf()(isCustomLicense),
                    maxLength()(useBackendData.validations.project.license.max),
                    pattern()(useBackendData.validations.project.license.regex),
                  ]"
                />
              </div>
              <div v-if="!isUnspecifiedLicense" class="basis-full" :md="isCustomLicense ? 'basis-full' : 'basis-6/12'">
                <InputText v-model.trim="form.settings.license.url" :label="i18n.t('project.settings.licenseUrl')" :rules="[url()]" />
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
                <InputFile v-model="projectIcon" accept="image/png, image/jpeg" show-size />
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
            <ProjectLinksForm v-model="form.settings.links" />
          </ProjectSettingsSection>
        </template>
        <template #management>
          <ProjectSettingsSection v-if="hasPerms(NamedPermission.IS_SUBJECT_OWNER)" title="project.settings.rename" description="project.settings.renameSub">
            <div class="flex items-center">
              <InputText
                ref="newNameField"
                v-model.trim="newName"
                :label="i18n.t('project.settings.newName')"
                :rules="[validProjectName()(() => project.owner.userId)]"
              />
              <Button :disabled="!newName || newNameField.validation.$invalid" :loading="loading.rename" class="ml-2" @click="rename">
                <IconMdiRenameBox class="mr-2" />
                {{ i18n.t("project.settings.rename") }}
              </Button>
            </div>
          </ProjectSettingsSection>
          <ProjectSettingsSection
            v-if="hasPerms(NamedPermission.IS_SUBJECT_OWNER)"
            title="project.settings.transfer"
            description="project.settings.transferSub"
          >
            <div class="flex items-center">
              <InputAutocomplete id="membersearch" v-model="search" :values="result" :label="i18n.t('project.settings.transferTo')" @search="doSearch" />
              <Button :disabled="search.length === 0" :loading="loading.transfer" class="ml-2" @click="transfer">
                <IconMdiRenameBox class="mr-2" />
                {{ i18n.t("project.settings.transfer") }}
              </Button>
            </div>
          </ProjectSettingsSection>
          <ProjectSettingsSection
            v-if="hasPerms(NamedPermission.DELETE_PROJECT) && project.visibility !== Visibility.SOFT_DELETE"
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
            v-if="hasPerms(NamedPermission.HARD_DELETE_PROJECT)"
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
      </Tabs>
    </Card>
    <MemberList
      :members="project.members"
      :author="project.owner.name"
      :slug="project.name"
      :owner="project.owner.userId"
      class="basis-full md:basis-3/12 h-max"
    />
  </div>
</template>
