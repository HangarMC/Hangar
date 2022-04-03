<script setup lang="ts">
import { ProjectOwner, ProjectSettingsForm } from "hangar-internal";
import { ProjectCategory } from "~/types/enums";
import { handleRequestError } from "~/composables/useErrorHandling";
import { computed, Ref, ref, watch } from "vue";
import { useInternalApi } from "~/composables/useApi";
import { useContext } from "vite-ssr/vue";
import PageTitle from "~/components/design/PageTitle.vue";
import { useBackendDataStore } from "~/store/backendData";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";
import Steps, { Step } from "~/components/design/Steps.vue";
import Card from "~/components/design/Card.vue";
import { useSettingsStore } from "~/store/settings";
import InputSelect from "~/components/ui/InputSelect.vue";
import InputText from "~/components/ui/InputText.vue";
import InputTag from "~/components/ui/InputTag.vue";
import Tabs, { Tab } from "~/components/design/Tabs.vue";
import Button from "~/components/design/Button.vue";
import Markdown from "~/components/Markdown.vue";
import InputTextarea from "~/components/ui/InputTextarea.vue";

interface NewProjectForm extends ProjectSettingsForm {
  ownerId: ProjectOwner["userId"];
  name: string;
  pageContent: string | null;
}

const ctx = useContext();
const i18n = useI18n();
const store = useBackendDataStore();
const router = useRouter();
const route = useRoute();
const settings = useSettingsStore();
const visibleCategories = store.visibleCategories;

let projectOwners!: ProjectOwner[];
let licenses!: string[];
const error = ref("");
const projectCreationErrors: Ref<string[]> = ref([]);
const form: NewProjectForm = {
  category: ProjectCategory.ADMIN_TOOLS,
  settings: {
    license: {} as ProjectSettingsForm["settings"]["license"],
    donation: {} as ProjectSettingsForm["settings"]["donation"],
    keywords: [],
  } as unknown as ProjectSettingsForm["settings"],
} as NewProjectForm;
const projectName = ref(form.name);

await asyncData();
form.ownerId = projectOwners[0].userId;

const converter = {
  bbCode: "",
  markdown: "",
  loading: false,
};

const isCustomLicense = computed(() => form.settings.license.type === "(custom)");

const selectedStep = ref("tos");
const steps: Step[] = [
  { value: "tos", header: i18n.t("project.new.step1.title") },
  { value: "basic", header: i18n.t("project.new.step2.title") },
  { value: "additional", header: i18n.t("project.new.step3.title") },
  { value: "import", header: i18n.t("project.new.step4.title") },
  // TODO buttons need to be disabled here
  { value: "finishing", header: i18n.t("project.new.step5.title") },
];

const selectBBCodeTab = ref("convert");
const bbCodeTabs: Tab[] = [
  { value: "convert", header: i18n.t("project.new.step4.convert") },
  // TODO tab needs to be disabled if no markdown was entered
  { value: "preview", header: i18n.t("project.new.step4.preview") },
  { value: "tutorial", header: i18n.t("project.new.step4.tutorial") },
];

useHead(useSeo("New Project", null, route, null));

function convertBBCode() {
  converter.loading = true;
  useInternalApi<string>("pages/convert-bbcode", false, "post", {
    content: converter.bbCode,
  })
    .then((markdown) => {
      converter.markdown = markdown;
    })
    .catch((e) => handleRequestError(e, ctx, i18n))
    .finally(() => {
      converter.loading = false;
    });
}

function createProject() {
  projectCreationErrors.value = [];
  useInternalApi<string>("projects/create", true, "post", form)
    .then((url) => {
      router.push(url);
    })
    .catch((err) => {
      projectCreationErrors.value = [];
      if (err.response?.data.fieldErrors != null) {
        for (let e of err.response.data.fieldErrors) {
          projectCreationErrors.value.push(i18n.t(e.errorMsg));
        }
      }

      handleRequestError(err, ctx, i18n, "project.new.error.create");
    });
}

watch(projectName, (newName) => {
  if (!newName) {
    error.value = "";
    return;
  }
  useInternalApi("projects/validateName", false, "get", {
    userId: form.ownerId,
    value: newName,
  })
    .then(() => {
      error.value = "";
      form.name = newName;
    })
    .catch((e) => {
      if (!e.response?.data.isHangarApiException) {
        return handleRequestError(e, ctx, i18n);
      }
      error.value = i18n.t(e.response?.data.message);
    });
});

async function asyncData() {
  const data = await Promise.all([useInternalApi("projects/possibleOwners"), useInternalApi("data/licenses", false)]).catch((e) => {
    handleRequestError(e, ctx, i18n);
  });
  if (typeof data === "undefined") {
    return;
  }
  projectOwners = data[0] as ProjectOwner[];
  licenses = data[1] as string[];
}
</script>

<!-- todo: rules, icon -->
<template>
  <Steps v-model="selectedStep" :steps="steps" button-lang-key="project.new.step">
    <template #tos>
      <!-- eslint-disable-next-line vue/no-v-html -->
      <p v-html="i18n.t('project.new.step1.text')" />
    </template>
    <template #basic>
      <div class="flex flex-wrap">
        <div class="basis-full md:basis-6/12">
          <InputSelect v-model="form.ownerId" :values="projectOwners" :label="i18n.t('project.new.step2.userSelect')" />
        </div>
        <div class="basis-full md:basis-6/12">
          <InputText v-model.trim="form.name" :error-messages="nameErrors" :label="i18n.t('project.new.step2.projectName')" />
        </div>
        <div class="basis-full md:basis-8/12"><InputText v-model.trim="form.description" :label="i18n.t('project.new.step2.projectSummary')" /></div>
        <div class="basis-full md:basis-4/12">
          <InputSelect v-model="form.category" :values="visibleCategories" :label="i18n.t('project.new.step2.projectCategory')" />
        </div>
      </div>
    </template>
    <template #additional>
      <div class="text-lg">
        <IconMdiLink />
        {{ i18n.t("project.new.step3.links") }}
        <hr />
      </div>
      <div class="flex flex-wrap">
        <div class="basis-full"><InputText v-model.trim="form.settings.homepage" :label="i18n.t('project.new.step3.homepage')" /></div>
        <div class="basis-full"><InputText v-model.trim="form.settings.issues" :label="i18n.t('project.new.step3.issues')" /></div>
        <div class="basis-full"><InputText v-model.trim="form.settings.source" :label="i18n.t('project.new.step3.source')" /></div>
        <div class="basis-full"><InputText v-model.trim="form.settings.support" :label="i18n.t('project.new.step3.support')" /></div>
      </div>
      <div class="text-lg">
        <IconMdiLicense />
        {{ i18n.t("project.new.step3.license") }}
        <hr />
      </div>
      <div class="flex flex-wrap">
        <div class="basis-full" :md="isCustomLicense ? 'basis-4/12' : 'basis-6/12'">
          <InputSelect v-model="form.settings.license.type" :values="licenses" :label="i18n.t('project.new.step3.type')" />
        </div>
        <div v-if="isCustomLicense" class="basis-full md:basis-8/12">
          <InputText v-model.trim="form.settings.license.name" :label="i18n.t('project.new.step3.customName')" />
        </div>
        <div class="basis-full" :md="isCustomLicense ? 'basis-full' : 'basis-6/12'">
          <InputText v-model.trim="form.settings.license.url" :label="i18n.t('project.new.step3.url')" />
        </div>
      </div>
      <div class="text-lg">
        <IconMdiCloudSearch />
        {{ i18n.t("project.new.step3.seo") }}
        <hr />
      </div>
      <div class="flex">
        <InputTag v-model="form.settings.keywords" :label="i18n.t('project.new.step3.keywords')" />
      </div>
    </template>
    <template #import>
      <Tabs v-model="selectBBCodeTab" :tabs="bbCodeTabs">
        <template #convert>
          <InputTextarea v-model="converter.bbCode" :rows="6" :label="i18n.t('project.new.step4.convertLabels.bbCode')" />
          <div>
            <Button :disabled="converter.loading" @click="convertBBCode">
              <IconMdiChevronDoubleDown />
              {{ i18n.t("project.new.step4.convert") }}
              <IconMdiChevronDoubleDown />
            </Button>
          </div>
          <InputTextarea v-model="converter.markdown" :rows="6" :label="i18n.t('project.new.step4.convertLabels.output')" />
        </template>
        <template #preview>
          <Button block color="primary" class="my-2" :disabled="form.pageContent === converter.markdown" @click="saveAsHomePage">
            <IconMdiContentSave />
            {{ i18n.t("project.new.step4.saveAsHomePage") }}
          </Button>
          <Markdown :raw="converter.markdown" />
        </template>
        <template #tutorial>
          {{ i18n.t("project.new.step4.tutorialInstructions.line1") }}<br />
          {{ i18n.t("project.new.step4.tutorialInstructions.line2") }}<br />
          <img src="https://i.imgur.com/8CyLMf3.png" alt="Edit Project" /><br />
          {{ i18n.t("project.new.step4.tutorialInstructions.line3") }}<br />
          <img src="https://i.imgur.com/FLVIuQK.png" width="425" height="198" alt="Show BBCode" /><br />
          {{ i18n.t("project.new.step4.tutorialInstructions.line4") }}<br />
        </template>
      </Tabs>
    </template>
    <template #finishing>
      <!-- todo loader -->
      <!--<v-progress-circular v-if="projectLoading" indeterminate color="red" size="50" />-->
      <span v-if="projectLoading">Loading....</span>
      <div v-if="!projectError" class="text-h5 mt-2">
        {{ i18n.t("project.new.step5.text") }}
      </div>
      <template v-else>
        <div class="text-lg mt-2">
          {{ i18n.t("project.new.error.create") }}
        </div>
        <Button @click="retry"> Retry </Button>
      </template>
    </template>
  </Steps>
</template>

<route lang="yaml">
meta:
  requireLoggedIn: true
</route>
