<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { HangarProject } from "hangar-internal";
import { useI18n } from "vue-i18n";
import { computed, onMounted, reactive, ref, watch } from "vue";
import { cloneDeep } from "lodash-es";
import { useVuelidate } from "@vuelidate/core";
import { Cropper, type CropperResult } from "vue-advanced-cropper";
import { PaginatedResult, User } from "hangar-api";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import Card from "~/lib/components/design/Card.vue";
import MemberList from "~/components/projects/MemberList.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission, Visibility } from "~/types/enums";
import Button from "~/lib/components/design/Button.vue";
import Tabs from "~/lib/components/design/Tabs.vue";
import InputSelect from "~/lib/components/ui/InputSelect.vue";
import { useBackendDataStore } from "~/store/backendData";
import InputText from "~/lib/components/ui/InputText.vue";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import InputFile from "~/lib/components/ui/InputFile.vue";
import { useApi, useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useNotificationStore } from "~/lib/store/notification";
import InputTag from "~/lib/components/ui/InputTag.vue";
import TextAreaModal from "~/lib/components/modals/TextAreaModal.vue";
import ProjectSettingsSection from "~/components/projects/ProjectSettingsSection.vue";
import { maxLength, required, requiredIf, url } from "~/lib/composables/useValidationHelpers";
import { validProjectName } from "~/composables/useHangarValidations";

import "vue-advanced-cropper/dist/style.css";
import InputAutocomplete from "~/lib/components/ui/InputAutocomplete.vue";
import { definePageMeta } from "#imports";

definePageMeta({
  projectPermsRequired: ["EDIT_SUBJECT_SETTINGS"],
});

const route = useRoute();
const router = useRouter();
const i18n = useI18n();
const backendData = useBackendDataStore();
const v = useVuelidate();
const notificationStore = useNotificationStore();
const props = defineProps<{
  project: HangarProject;
}>();

const selectedTab = ref(route.hash.substring(1) || "general");
const tabs = [
  { value: "general", header: i18n.t("project.settings.tabs.general") },
  { value: "links", header: i18n.t("project.settings.tabs.links") },
  { value: "management", header: i18n.t("project.settings.tabs.management") },
  { value: "donation", header: i18n.t("project.settings.tabs.donation") },
];

const form = reactive({
  settings: cloneDeep(props.project.settings),
  description: props.project.description,
  category: props.project.category,
});
if (!form.settings.license.type) {
  form.settings.license.type = "Unspecified";
}

const projectIcon = ref<File | null>(null);
const cropperInput = ref();
const cropperResult = ref();
let reader: FileReader | null = null;
onMounted(async () => {
  reader = new FileReader();
  reader.addEventListener(
    "load",
    () => {
      cropperInput.value = reader?.result;
    },
    false
  );
  await loadIconIntoCropper();
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

async function loadIconIntoCropper() {
  const response = await fetch(projectIconUrl(props.project.namespace.owner, props.project.namespace.slug, false), {
    headers: {
      "Cache-Control": "no-cache",
    },
  });
  const data = await response.blob();
  reader?.readAsDataURL(data);
}

const newName = ref<string | null>("");
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

watch(route, (val) => (selectedTab.value = val.hash.replace("#", "")), { deep: true });
watch(selectedTab, (val) => history.replaceState({}, "", route.path + "#" + val));

const search = ref<string>("");
const result = ref<string[]>([]);
async function doSearch(val: string) {
  result.value = [];
  const users = await useApi<PaginatedResult<User>>("users", "get", {
    query: val,
    limit: 25,
    offset: 0,
  });
  result.value = users.result.map((u) => u.name);
}

async function save() {
  loading.save = true;
  try {
    if (!(await v.value.$validate())) return;
    await useInternalApi(`projects/project/${route.params.user}/${route.params.project}/settings`, "post", {
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
    const newSlug = await useInternalApi<string>(`projects/project/${route.params.user}/${route.params.project}/rename`, "post", {
      content: newName.value,
    });
    notificationStore.success(i18n.t("project.settings.success.rename", [newName.value]));
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
    useNotificationStore().success(i18n.t("project.settings.success.softDelete"));
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
    notificationStore.success(i18n.t("project.settings.success.hardDelete"));
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
    const response = await useInternalApi<string | null>(`projects/project/${route.params.user}/${route.params.project}/saveIcon`, "post", data);
    cropperResult.value = null;
    await loadIconIntoCropper();
    if (response) {
      useNotificationStore().success(i18n.t("project.settings.success.changedIconWarn", [response]));
    } else {
      useNotificationStore().success(i18n.t("project.settings.success.changedIcon"));
    }
  } catch (e: any) {
    handleRequestError(e);
  }
  loading.uploadIcon = false;
}

async function resetIcon() {
  loading.resetIcon = true;
  try {
    const response = await useInternalApi<string | null>(`projects/project/${route.params.user}/${route.params.project}/resetIcon`, "post");
    if (response) {
      useNotificationStore().success(i18n.t("project.settings.success.resetIconWarn", [response]));
    } else {
      useNotificationStore().success(i18n.t("project.settings.success.resetIcon"));
    }
    projectIcon.value = null;
    await loadIconIntoCropper();
  } catch (e: any) {
    handleRequestError(e);
  }
  loading.resetIcon = false;
}

useHead(
  useSeo(
    i18n.t("project.settings.title") + " | " + props.project.name,
    props.project.description,
    route,
    projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)
  )
);
</script>

<template>
  <div class="flex gap-4 flex-col md:flex-row">
    <Card class="basis-full md:basis-9/12">
      <template #header>
        <div class="flex justify-between <sm:items-center">
          {{ i18n.t("project.settings.title") }}
          <div class="text-lg">
            <Button class="ml-2" :disabled="v.$invalid" :loading="loading.save" @click="save">
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
            <InputSelect v-model="form.category" :values="backendData.categoryOptions" :rules="[required()]" i18n-text-values />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.description" description="project.settings.descriptionSub">
            <InputText
              v-model="form.description"
              counter
              :maxlength="backendData.validations?.project?.desc?.max"
              :rules="[required(), maxLength()(backendData.validations?.project?.desc?.max)]"
            />
          </ProjectSettingsSection>
          <!-- todo: forums integration -->
          <!--<ProjectSettingsSection title="project.settings.forum">
            <InputCheckbox v-model="form.settings.forumSync" :label="i18n.t('project.settings.forumSub')" />
          </ProjectSettingsSection>-->
          <ProjectSettingsSection title="project.settings.keywords" description="project.settings.keywordsSub">
            <InputTag
              v-model="form.settings.keywords"
              counter
              :maxlength="backendData.validations?.project.keywords.max"
              :label="i18n.t('project.new.step3.keywords')"
              :rules="[maxLength()(backendData.validations?.project.keywords.max)]"
            />
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
              <Button :loading="loading.resetIcon" @click="resetIcon">
                <IconMdiUpload />
                {{ i18n.t("project.settings.iconReset") }}
              </Button>
              <div class="col-span-1 col-start-3 row-start-1 row-span-3">
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
                <img
                  v-else
                  id="project-icon-preview"
                  width="150"
                  height="150"
                  alt="Project Icon"
                  :src="projectIconUrl(project.namespace.owner, project.namespace.slug)"
                />
              </div>
            </div>
          </ProjectSettingsSection>
        </template>
        <template #links>
          <ProjectSettingsSection title="project.settings.homepage" description="project.settings.homepageSub">
            <InputText v-model.trim="form.settings.homepage" :label="i18n.t('project.new.step3.homepage')" :rules="[url()]"></InputText>
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.issues" description="project.settings.issuesSub">
            <InputText v-model.trim="form.settings.issues" :label="i18n.t('project.new.step3.issues')" :rules="[url()]" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.source" description="project.settings.sourceSub">
            <InputText v-model.trim="form.settings.source" :label="i18n.t('project.new.step3.source')" :rules="[url()]" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.support" description="project.settings.supportSub">
            <InputText v-model.trim="form.settings.support" :label="i18n.t('project.new.step3.support')" :rules="[url()]" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.support" description="project.settings.wikiSub">
            <InputText v-model.trim="form.settings.wiki" :label="i18n.t('project.new.step3.wiki')" :rules="[url()]" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.license" description="project.settings.licenseSub">
            <div class="flex">
              <div class="basis-full" :md="isCustomLicense ? 'basis-4/12' : 'basis-6/12'">
                <InputSelect v-model="form.settings.license.type" :values="backendData.licenseOptions" :label="i18n.t('project.settings.licenseType')" />
              </div>
              <div v-if="isCustomLicense" class="basis-full md:basis-8/12">
                <InputText v-model.trim="form.settings.license.name" :label="i18n.t('project.settings.licenseCustom')" />
              </div>
              <div v-if="!isUnspecifiedLicense" class="basis-full" :md="isCustomLicense ? 'basis-full' : 'basis-6/12'">
                <InputText v-model.trim="form.settings.license.url" :label="i18n.t('project.settings.licenseUrl')" :rules="[url()]" />
              </div>
            </div>
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
              <Button :disabled="!newName || newNameField.v.$invalid" :loading="loading.rename" class="ml-2" @click="rename">
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
            <TextAreaModal :title="i18n.t('project.settings.delete')" :label="i18n.t('general.comment')" :submit="softDelete">
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
            <TextAreaModal :title="i18n.t('project.settings.hardDelete')" :label="i18n.t('general.comment')" :submit="hardDelete">
              <template #activator="{ on }">
                <Button button-type="red" v-on="on">{{ i18n.t("project.settings.hardDelete") }}</Button>
              </template>
            </TextAreaModal>
          </ProjectSettingsSection>
        </template>
        <template #donation>
          <ProjectSettingsSection title="project.settings.donation.enable">
            <InputCheckbox v-model="form.settings.donation.enable" :label="i18n.t('project.settings.donation.enableSub')" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.donation.subject" description="project.settings.donation.subjectSub">
            <InputText
              v-model="form.settings.donation.subject"
              :label="i18n.t('project.settings.donation.subjectLabel')"
              :rules="[requiredIf()(form.settings.donation.enable)]"
            />
          </ProjectSettingsSection>
        </template>
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
