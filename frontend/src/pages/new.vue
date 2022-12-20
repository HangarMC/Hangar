<script setup lang="ts">
import { ProjectOwner, ProjectSettingsForm } from "hangar-internal";
import { computed, type Ref, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useHead } from "@vueuse/head";
import { useVuelidate } from "@vuelidate/core";
import { ProjectCategory } from "~/types/enums";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useInternalApi } from "~/composables/useApi";
import { useBackendDataStore } from "~/store/backendData";
import { useSeo } from "~/composables/useSeo";
import Steps, { Step } from "~/lib/components/design/Steps.vue";
import { useSettingsStore } from "~/store/useSettingsStore";
import InputSelect from "~/lib/components/ui/InputSelect.vue";
import InputText from "~/lib/components/ui/InputText.vue";
import InputTag from "~/lib/components/ui/InputTag.vue";
import Tabs, { Tab } from "~/lib/components/design/Tabs.vue";
import Button from "~/lib/components/design/Button.vue";
import Markdown from "~/components/Markdown.vue";
import InputTextarea from "~/lib/components/ui/InputTextarea.vue";
import { required, maxLength, pattern, url, requiredIf } from "~/lib/composables/useValidationHelpers";
import { validProjectName } from "~/composables/useHangarValidations";
import Spinner from "~/lib/components/design/Spinner.vue";
import Link from "~/lib/components/design/Link.vue";
import { usePossibleOwners } from "~/composables/useApiHelper";
import { definePageMeta } from "#imports";

interface NewProjectForm extends ProjectSettingsForm {
  ownerId: ProjectOwner["userId"];
  name: string;
  pageContent: string | null;
}

definePageMeta({
  loginRequired: true,
});

const i18n = useI18n();
const backendData = useBackendDataStore();
const router = useRouter();
const route = useRoute();
const settings = useSettingsStore();

const projectOwners = await usePossibleOwners();
const projectCreationErrors: Ref<string[]> = ref([]);
const projectLoading = ref(true);
const form = ref<NewProjectForm>({
  category: ProjectCategory.ADMIN_TOOLS,
  settings: {
    license: {} as ProjectSettingsForm["settings"]["license"],
    donation: {} as ProjectSettingsForm["settings"]["donation"],
    keywords: [],
  } as unknown as ProjectSettingsForm["settings"],
} as NewProjectForm);

if (projectOwners.value) {
  form.value.ownerId = projectOwners.value[0].userId;
}

const converter = ref({
  bbCode: "",
  markdown: "",
  loading: false,
});

const rules = {
  name: {
    required,
  },
};
const v = useVuelidate(rules, form);

const unspecifiedLicenseName = "Unspecified";
form.value.settings.license.type = unspecifiedLicenseName;
const isCustomLicense = computed(() => form.value.settings.license.type === "Other");
const licenseUnset = computed(() => form.value.settings.license.type === unspecifiedLicenseName);

const selectedStep = ref("tos");
const steps: Step[] = [
  {
    value: "tos",
    header: i18n.t("project.new.step1.title"),
    showBack: () => false,
  },
  {
    value: "basic",
    header: i18n.t("project.new.step2.title"),
    disableNext: computed(() => {
      return !form.value.name || !form.value.description;
    }),
  },
  { value: "additional", header: i18n.t("project.new.step3.title") },
  {
    value: "import",
    header: i18n.t("project.new.step4.title"),
    beforeNext: () => {
      createProject();
      return true;
    },
  },
  { value: "finishing", header: i18n.t("project.new.step5.title"), showNext: () => false, showBack: () => false },
];

const selectBBCodeTab = ref("convert");
const bbCodeTabs: Tab[] = [
  { value: "convert", header: i18n.t("project.new.step4.convert") },
  { value: "preview", header: i18n.t("project.new.step4.preview"), disable: () => !converter.value.markdown },
  { value: "tutorial", header: i18n.t("project.new.step4.tutorial") },
];
function saveButtonDisabled() {
  return form.value.pageContent === converter.value.markdown || (!form.value.pageContent && converter.value.markdown.length === 0);
}

useHead(useSeo("New Project", null, route, null));

function convertBBCode() {
  converter.value.loading = true;
  useInternalApi<string>("pages/convert-bbcode", "post", {
    content: converter.value.bbCode,
  })
    .then((markdown) => {
      converter.value.markdown = markdown;
    })
    .catch((e) => handleRequestError(e, i18n))
    .finally(() => {
      converter.value.loading = false;
    });
}

function createProject() {
  projectCreationErrors.value = [];
  projectLoading.value = true;
  if (!form.value.pageContent) {
    form.value.pageContent = "# " + form.value.name + "  \nWelcome to your new project!";
  }
  useInternalApi<string>("projects/create", "post", form.value)
    .then((u) => {
      router.push(u);
    })
    .catch((err) => {
      projectCreationErrors.value = [];
      if (err.response?.data.fieldErrors != null) {
        for (const e of err.response.data.fieldErrors) {
          projectCreationErrors.value.push(i18n.t(e.errorMsg));
        }
      }

      handleRequestError(err, i18n, "project.new.error.create");
    })
    .finally(() => {
      projectLoading.value = false;
    });
}
</script>

<template>
  <Steps v-model="selectedStep" :steps="steps" button-lang-key="project.new.step">
    <template #tos>
      <!-- eslint-disable-next-line vue/no-v-html -->
      <p v-html="i18n.t('project.new.step1.text1')" />
      <!-- eslint-disable-next-line vue/no-v-html -->
      <Link to="/guidelines"><p v-html="i18n.t('project.new.step1.text2')" /></Link>
    </template>
    <template #basic>
      <div class="flex flex-wrap">
        <p class="basis-full mb-4">{{ i18n.t("project.new.step2.description") }}</p>
        <div class="basis-full md:basis-4/12">
          <InputSelect
            v-model="form.ownerId"
            :values="projectOwners"
            item-value="id"
            item-text="name"
            :label="i18n.t('project.new.step2.userSelect')"
            :rules="[required()]"
          />
        </div>
        <span class="text-3xl <md:hidden mr-2 -ml-4">/</span>
        <div class="basis-full md:basis-4/12 <md:mt-4">
          <InputText
            v-model.trim="form.name"
            :label="i18n.t('project.new.step2.projectName')"
            :maxlength="backendData.validations.project.name.max"
            counter
            :rules="[
              required(),
              maxLength()(backendData.validations.project.name.max),
              pattern()(backendData.validations.project.name.regex),
              validProjectName()(() => form.ownerId),
            ]"
          />
        </div>
        <div class="basis-full mt-4">
          <InputText
            v-model.trim="form.description"
            :label="i18n.t('project.new.step2.projectSummary')"
            :rules="[required()]"
            :maxlength="backendData.validations.project.desc.max"
            counter
          />
        </div>
        <div class="basis-full md:basis-4/12 mt-4">
          <InputSelect
            v-model="form.category"
            :values="backendData.categoryOptions"
            :label="i18n.t('project.new.step2.projectCategory')"
            :rules="[required()]"
            i18n-text-values
          />
        </div>
      </div>
    </template>
    <template #additional>
      <p>{{ i18n.t("project.new.step3.description") }}</p>
      <div class="text-lg mt-4 flex gap-2 items-center">
        <IconMdiLink />
        {{ i18n.t("project.new.step3.links") }}
        <hr />
      </div>
      <div class="flex flex-wrap">
        <div class="basis-full mt-4"><InputText v-model.trim="form.settings.homepage" :label="i18n.t('project.new.step3.homepage')" :rules="[url()]" /></div>
        <div class="basis-full mt-4"><InputText v-model.trim="form.settings.issues" :label="i18n.t('project.new.step3.issues')" :rules="[url()]" /></div>
        <div class="basis-full mt-4"><InputText v-model.trim="form.settings.source" :label="i18n.t('project.new.step3.source')" :rules="[url()]" /></div>
        <div class="basis-full mt-4"><InputText v-model.trim="form.settings.support" :label="i18n.t('project.new.step3.support')" :rules="[url()]" /></div>
        <div class="basis-full mt-4"><InputText v-model.trim="form.settings.wiki" :label="i18n.t('project.new.step3.wiki')" :rules="[url()]" /></div>
      </div>
      <div class="text-lg mt-6 flex gap-2 items-center">
        <IconMdiLicense />
        {{ i18n.t("project.new.step3.license") }}
        <hr />
      </div>
      <div class="flex flex-wrap">
        <div class="basis-full mt-4" :md="isCustomLicense ? 'basis-4/12' : 'basis-6/12'">
          <InputSelect
            v-model="form.settings.license.type"
            :values="backendData.licenseOptions"
            :label="i18n.t('project.new.step3.type')"
            :rules="[required()]"
          />
        </div>
        <div v-if="isCustomLicense" class="basis-full md:basis-8/12 mt-4">
          <InputText v-model.trim="form.settings.license.name" :label="i18n.t('project.new.step3.customName')" :rules="[requiredIf()(isCustomLicense)]" />
        </div>
        <div v-if="!licenseUnset" class="basis-full mt-4" :md="isCustomLicense ? 'basis-full' : 'basis-6/12'">
          <InputText v-model.trim="form.settings.license.url" :label="i18n.t('project.new.step3.url')" :rules="[url()]" />
        </div>
      </div>
      <div class="text-lg mt-6 flex gap-2 items-center">
        <IconMdiCloudSearch />
        {{ i18n.t("project.new.step3.seo") }}
        <hr />
      </div>
      <div class="flex">
        <div class="mt-4 basis-full">
          <InputTag
            v-model="form.settings.keywords"
            :label="i18n.t('project.new.step3.keywords')"
            :rules="[maxLength()(backendData.validations.project.keywords.max)]"
            :maxlength="backendData.validations.project.keywords.max"
          />
        </div>
      </div>
    </template>
    <template #import>
      <p class="mb-4">{{ i18n.t("project.new.step4.description") }}</p>
      <Tabs v-model="selectBBCodeTab" :tabs="bbCodeTabs" :vertical="false">
        <template #convert>
          <div class="flex flex-wrap">
            <div class="basis-full"><InputTextarea v-model="converter.bbCode" :rows="6" :label="i18n.t('project.new.step4.convertLabels.bbCode')" /></div>
            <div class="basis-full flex justify-center my-4">
              <Button :disabled="converter.loading" size="large" @click="convertBBCode">
                <IconMdiChevronDoubleDown />
                {{ i18n.t("project.new.step4.convert") }}
                <IconMdiChevronDoubleDown />
              </Button>
            </div>
            <div class="basis-full"><InputTextarea v-model="converter.markdown" :rows="6" :label="i18n.t('project.new.step4.convertLabels.output')" /></div>
          </div>

          <div class="inline-flex items-center gap-2">
            <Button block class="my-2" :disabled="saveButtonDisabled()" @click="form.pageContent = converter.markdown">
              <IconMdiContentSave />
              {{ i18n.t("project.new.step4.saveAsHomePage") }}
            </Button>
            <transition>
              <span v-if="form.pageContent === converter.markdown" class="inline-flex items-center"
                >{{ i18n.t("project.new.step4.saved") }} <IconMdiCheck
              /></span>
            </transition>
          </div>
        </template>
        <template #preview>
          <Markdown :raw="converter.markdown" />
          <div class="inline-flex items-center gap-2">
            <Button block class="my-2" :disabled="saveButtonDisabled()" @click="form.pageContent = converter.markdown">
              <IconMdiContentSave />
              {{ i18n.t("project.new.step4.saveAsHomePage") }}
            </Button>
            <transition>
              <span v-if="form.pageContent === converter.markdown" class="inline-flex items-center"
                >{{ i18n.t("project.new.step4.saved") }} <IconMdiCheck
              /></span>
            </transition>
          </div>
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
      <div class="flex flex-col">
        <div v-if="projectLoading" class="text-center my-8"><Spinner class="stroke-red-500" :diameter="90" :stroke="6" /></div>
        <template v-else-if="projectCreationErrors && projectCreationErrors.length > 0">
          <div class="text-lg mt-2">
            {{ i18n.t("project.new.error.create") }}
            {{ projectCreationErrors }}
          </div>
          <div class="text-center mt-2"><Button @click="createProject"> Retry </Button></div>
        </template>
        <div v-else class="text-h5 mt-2">
          {{ i18n.t("project.new.step5.text") }}
        </div>
      </div>
    </template>
  </Steps>
</template>

<style lang="scss" scoped>
.v-enter-active {
  transition: opacity 0.5s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}
</style>
