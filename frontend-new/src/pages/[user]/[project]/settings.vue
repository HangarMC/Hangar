<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { useRoute, useRouter } from "vue-router";
import { HangarProject } from "hangar-internal";
import { useI18n } from "vue-i18n";
import Card from "~/components/design/Card.vue";
import MemberList from "~/components/projects/MemberList.vue";
import VisibilityChangerModal from "~/components/modals/VisibilityChangerModal.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import Button from "~/components/design/Button.vue";
import Tabs from "~/components/design/Tabs.vue";
import { computed, reactive, ref, watch } from "vue";
import InputSelect, { Option } from "~/components/ui/InputSelect.vue";
import { useBackendDataStore } from "~/store/backendData";
import InputText from "~/components/ui/InputText.vue";
import { cloneDeep } from "lodash-es";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import InputFile from "~/components/ui/InputFile.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { useNotificationStore } from "~/store/notification";
import InputTag from "~/components/ui/InputTag.vue";
import TextAreaModal from "~/components/modals/TextAreaModal.vue";
import ProjectSettingsSection from "~/components/projects/ProjectSettingsSection.vue";

const route = useRoute();
const router = useRouter();
const i18n = useI18n();
const ctx = useContext();
const backendData = useBackendDataStore();
const props = defineProps<{
  project: HangarProject;
}>();

const selectedTab = ref(route.hash.substring(1) || "general");
const tabs = [
  { value: "general", header: i18n.t("project.settings.tabs.general") },
  { value: "optional", header: i18n.t("project.settings.tabs.optional") },
  { value: "management", header: i18n.t("project.settings.tabs.management") },
  { value: "donation", header: i18n.t("project.settings.tabs.donation") },
];

const form = reactive({
  settings: cloneDeep(props.project.settings),
  description: props.project.description,
  category: props.project.category,
});
const projectIcon = ref<File | null>(null);
const newName = ref<string | null>("");
const nameErrors = ref<string[]>([]);
const loading = reactive({
  save: false,
  uploadIcon: false,
  resetIcon: false,
  rename: false,
});

const isCustomLicense = computed(() => form.settings.license.type === "(custom)");
const licenses = computed<Option[]>(() =>
  backendData.licenses.map<Option>((l) => {
    return { value: l, text: l };
  })
);
const categories = computed<Option[]>(() =>
  backendData.visibleCategories.map<Option>((c) => {
    return { value: c.apiName, text: i18n.t(c.title) };
  })
);

watch(route, (val) => (selectedTab.value = val.hash.replace("#", "")), { deep: true });
watch(selectedTab, (val) => history.replaceState({}, "", route.path + "#" + val));

watch(newName, async (val) => {
  if (!val) {
    nameErrors.value = [];
  } else {
    try {
      await useInternalApi("projects/validateName", false, "get", {
        userId: props.project.owner.userId,
        value: val,
      });
      nameErrors.value = [];
    } catch (err) {
      nameErrors.value = [];
      if (!err.response?.data.isHangarApiException) {
        return;
      }
      nameErrors.value.push(i18n.t(err.response.data.message));
    }
  }
});

async function save() {
  loading.save = true;
  try {
    await useInternalApi(`projects/project/${route.params.user}/${route.params.project}/settings`, true, "post", {
      ...form,
    });
    await router.go(0);
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
  loading.save = false;
}

async function rename() {
  loading.rename = true;
  try {
    const newSlug = await useInternalApi<string>(`projects/project/${route.params.user}/${route.params.project}/rename`, true, "post", {
      content: newName.value,
    });
    useNotificationStore().success(i18n.t("project.settings.success.rename", [newName.value]));
    await router.push(route.params.user + "/" + newSlug);
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
  loading.rename = false;
}

async function softDelete(comment: string) {
  try {
    await useInternalApi(`projects/project/${props.project.id}/manage/delete`, true, "post", {
      content: comment,
    });
    useNotificationStore().success(i18n.t("project.settings.success.softDelete"));
    await router.go(0);
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
}

async function hardDelete(comment: string) {
  try {
    await useInternalApi(`projects/project/${props.project.id}/manage/hardDelete`, true, "post", {
      content: comment,
    });
    useNotificationStore().success(i18n.t("project.settings.success.hardDelete"));
    await router.push("/");
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
}

async function uploadIcon() {
  const data = new FormData();
  data.append("projectIcon", projectIcon.value!);
  loading.uploadIcon = true;
  try {
    await useInternalApi(`projects/project/${route.params.user}/${route.params.project}/saveIcon`, true, "post", data);
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
  loading.uploadIcon = false;
}

async function resetIcon() {
  loading.resetIcon = true;
  try {
    await useInternalApi(`projects/project/${route.params.user}/${route.params.project}/resetIcon`, true, "post");
    useNotificationStore().success(i18n.t("project.settings.success.resetIcon"));
    document
      .getElementById("project-icon-preview")!
      .setAttribute("src", `${projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)}?noCache=${Math.random()}`);
    await router.go(0);
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
  loading.resetIcon = false;
}

function onFileChange() {
  if (projectIcon.value) {
    const reader = new FileReader();
    reader.onload = (ev) => {
      document.getElementById("project-icon-preview")!.setAttribute("src", ev.target!.result as string);
    };
    reader.readAsDataURL(projectIcon.value);
  } else {
    document.getElementById("project-icon-preview")!.setAttribute("src", projectIconUrl(props.project.namespace.owner, props.project.namespace.slug));
  }
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
        <div class="flex justify-between">
          {{ i18n.t("project.settings.title") }}
          <div class="text-lg">
            <VisibilityChangerModal
              v-if="hasPerms(NamedPermission.SEE_HIDDEN)"
              type="project"
              :prop-visibility="project.visibility"
              :post-url="`projects/visibility/${project.id}`"
            ></VisibilityChangerModal>
            <Button class="ml-2" @click="save">
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
            <InputSelect v-model="form.category" :values="categories" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.description" description="project.settings.descriptionSub">
            <InputText v-model="form.description" counter :maxlength="backendData.validations?.project?.desc?.max" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.forum">
            <InputCheckbox v-model="form.settings.forumSync" :label="i18n.t('project.settings.forumSub')" />
          </ProjectSettingsSection>
          <ProjectSettingsSection>
            <div class="grid grid-cols-3 grid-rows-[1fr,1fr,min-content] gap-2 w-full">
              <div class="col-span-2 row-span-1">
                <h2 class="text-lg font-semibold">{{ i18n.t("project.settings.icon") }}</h2>
                <p>{{ i18n.t("project.settings.iconSub") }}</p>
              </div>
              <div class="col-span-2">
                <InputFile v-model="projectIcon" accept="image/png, image/jpeg" show-size @change="onFileChange" />
              </div>
              <Button :disabled="!projectIcon || loading.uploadIcon" @click="uploadIcon">
                <IconMdiUpload />
                {{ i18n.t("project.settings.iconUpload") }}
              </Button>
              <Button :disabled="loading.resetIcon" @click="resetIcon">
                <IconMdiUpload />
                {{ i18n.t("project.settings.iconReset") }}
              </Button>
              <div class="col-span-1 col-start-3 row-start-1 row-span-3">
                <img
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
        <template #optional>
          <ProjectSettingsSection title="project.settings.keywords" description="project.settings.keywordsSub" optional>
            <InputTag
              v-model="form.settings.keywords"
              counter
              :maxlength="backendData.validations.project.keywords.max"
              :label="i18n.t('project.new.step3.keywords')"
            />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.homepage" description="project.settings.homepageSub" optional>
            <InputText v-model.trim="form.settings.homepage" :label="i18n.t('project.new.step3.homepage')"></InputText>
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.issues" description="project.settings.issuesSub" optional>
            <InputText v-model.trim="form.settings.issues" :label="i18n.t('project.new.step3.issues')" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.source" description="project.settings.sourceSub" optional>
            <InputText v-model.trim="form.settings.source" :label="i18n.t('project.new.step3.source')" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.support" description="project.settings.supportSub" optional>
            <InputText v-model.trim="form.settings.support" :label="i18n.t('project.new.step3.support')" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.license" description="project.settings.licenseSub" optional>
            <div class="flex">
              <div class="basis-full" :md="isCustomLicense ? 'basis-4/12' : 'basis-6/12'">
                <InputSelect v-model="form.settings.license.type" :values="licenses" :label="i18n.t('project.settings.licenseType')" />
              </div>
              <div v-if="isCustomLicense" class="basis-full md:basis-8/12">
                <InputText v-model.trim="form.settings.license.name" :label="i18n.t('project.settings.licenseCustom')" />
              </div>
              <div class="basis-full" :md="isCustomLicense ? 'basis-full' : 'basis-6/12'">
                <InputText v-model.trim="form.settings.license.url" :label="i18n.t('project.settings.licenseUrl')" />
              </div>
            </div>
          </ProjectSettingsSection>
        </template>
        <template #management>
          <ProjectSettingsSection title="project.settings.rename" description="project.settings.renameSub">
            <div class="flex">
              <InputText v-model.trim="newName" :error-messages="nameErrors" />
              <Button :disabled="!newName || loading.rename || nameErrors.length > 0" class="ml-2" @click="rename">
                <IconMdiRenameBox class="mr-2" />
                {{ i18n.t("project.settings.rename") }}
              </Button>
            </div>
          </ProjectSettingsSection>
          <ProjectSettingsSection
            v-if="hasPerms(NamedPermission.DELETE_PROJECT)"
            title="project.settings.delete"
            description="project.settings.deleteSub"
            class="bg-red-200 dark:(bg-red-900 text-white) rounded-md p-4"
          >
            <TextAreaModal :title="i18n.t('project.settings.delete')" :label="i18n.t('general.comment')" :submit="softDelete">
              <template #activator="{ on }">
                <Button v-on="on">{{ i18n.t("project.settings.delete") }}</Button>
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
                <Button v-on="on">{{ i18n.t("project.settings.hardDelete") }}</Button>
              </template>
            </TextAreaModal>
          </ProjectSettingsSection>
        </template>
        <template #donation>
          <ProjectSettingsSection title="project.settings.donation.enable" description="project.settings.donation.enableSub">
            <InputCheckbox v-model="form.settings.donation.enable" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.donation.email" description="project.settings.donation.emailSub">
            <InputText v-model="form.settings.donation.email" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.donation.defaultAmount" description="project.settings.donation.defaultAmountSub">
            <InputText v-model.number="form.settings.donation.defaultAmount" type="number" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.donation.oneTimeAmounts" description="project.settings.donation.oneTimeAmountsSub">
            <InputTag v-model="form.settings.donation.oneTimeAmounts" />
          </ProjectSettingsSection>
          <ProjectSettingsSection title="project.settings.donation.monthlyAmounts" description="project.settings.donation.monthlyAmountsSub">
            <InputTag v-model="form.settings.donation.monthlyAmounts" />
          </ProjectSettingsSection>
        </template>
      </Tabs>
    </Card>
    <MemberList :model-value="project.members" class="basis-full md:basis-3/12" />
  </div>
</template>

<route lang="yaml">
meta:
  requireProjectPerm: ["EDIT_SUBJECT_SETTINGS"]
</route>
