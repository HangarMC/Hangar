<script setup lang="ts">
import type { ProjectSettingsForm, NewProjectForm } from "hangar-internal";
import { computed, type Ref, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useHead } from "@unhead/vue";
import { useVuelidate } from "@vuelidate/core";
import { ProjectCategory, Tag } from "~/types/enums";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useInternalApi } from "~/composables/useApi";
import { useBackendData, useCategoryOptions, useLicenseOptions } from "~/store/backendData";
import { useSeo } from "~/composables/useSeo";
import Steps from "~/components/design/Steps.vue";
import { useSettingsStore } from "~/store/useSettingsStore";
import InputSelect from "~/components/ui/InputSelect.vue";
import InputText from "~/components/ui/InputText.vue";
import InputTag from "~/components/ui/InputTag.vue";
import Button from "~/components/design/Button.vue";
import { required, maxLength, pattern, url, requiredIf, noDuplicated } from "~/composables/useValidationHelpers";
import { validProjectName } from "~/composables/useHangarValidations";
import Spinner from "~/components/design/Spinner.vue";
import Link from "~/components/design/Link.vue";
import { usePossibleOwners } from "~/composables/useApiHelper";
import { definePageMeta } from "#imports";
import type { Step } from "~/types/components/design/Steps";
import IconMdiFileDocumentAlert from "~icons/mdi/file-document-alert";
import Alert from "~/components/design/Alert.vue";
import ProjectLinksForm from "~/components/projects/ProjectLinksForm.vue";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import Tooltip from "~/components/design/Tooltip.vue";

definePageMeta({
  loginRequired: true,
});

const i18n = useI18n();
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
    links: [],
    tags: [],
  } as unknown as ProjectSettingsForm["settings"],
} as NewProjectForm);

if (projectOwners.value) {
  form.value.ownerId = projectOwners.value[0].userId;
}

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
    showBack: false,
  },
  {
    value: "basic",
    header: i18n.t("project.new.step2.title"),
    disableNext: computed(() => {
      return v.value.$errors.length > 0 || v.value.$pending;
    }),
    beforeNext: async () => {
      return await v.value.$validate();
    },
  },
  {
    value: "additional",
    header: i18n.t("project.new.step3.title"),
    disableNext: computed(() => {
      return v.value.$errors.length > 0 || v.value.$pending;
    }),
    beforeNext: async () => {
      if (!(await v.value.$validate())) {
        return false;
      }
      createProject();
      return true;
    },
  },
  { value: "finishing", header: i18n.t("project.new.step4.title"), showNext: false, showBack: false },
];

useHead(useSeo("New Project", null, route, null));

function createProject() {
  projectCreationErrors.value = [];
  projectLoading.value = true;
  if (!form.value.pageContent) {
    form.value.pageContent = "# " + form.value.name + "  \nWelcome to your new project!";
  }
  if (!isCustomLicense.value) {
    form.value.settings.license.name = null;
  }
  if (licenseUnset.value) {
    form.value.settings.license.url = null;
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

      handleRequestError(err);
    })
    .finally(() => {
      projectLoading.value = false;
    });
}
</script>

<template>
  <Steps v-model="selectedStep" :steps="steps" button-lang-key="project.new.step">
    <template #tos>
      <div class="flex-col flex inline-flex">
        <Alert class="mb-4">
          <template #icon="{ clazz }">
            <IconMdiFolderPlusOutline :class="clazz" />
          </template>
          <Link to="/tools/importer">
            <span class="text-white font-normal">
              {{ i18n.t("project.new.step1.importer_text") }}
            </span>
          </Link>
        </Alert>
        <p>{{ i18n.t("project.new.step1.text1") }}</p>
        <p class="inline-flex items-center space-x-2 mb-2">
          <IconMdiFileDocumentAlert />
          <Link to="/guidelines">
            {{ i18n.t("project.new.step1.text2") }}
          </Link>
        </p>
      </div>
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
        <span class="text-3xl lt-md:hidden ml-2 mr-2">/</span>
        <div class="basis-full md:basis-4/12 lt-md:mt-4">
          <InputText
            v-model.trim="form.name"
            :label="i18n.t('project.new.step2.projectName')"
            :maxlength="useBackendData.validations.project.name.max"
            counter
            :rules="[
              required(),
              maxLength()(useBackendData.validations.project.name.max),
              pattern()(useBackendData.validations.project.name.regex),
              validProjectName()(() => form.ownerId),
            ]"
          />
        </div>
        <div class="basis-full mt-4">
          <InputText
            v-model.trim="form.description"
            :label="i18n.t('project.new.step2.projectSummary')"
            :rules="[required()]"
            :maxlength="useBackendData.validations.project.desc.max"
            counter
          />
        </div>
        <div class="basis-full md:basis-4/12 mt-4">
          <InputSelect
            v-model="form.category"
            :values="useCategoryOptions"
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
      <ProjectLinksForm v-model="form.settings.links" class="mt-2" />
      <div class="text-lg mt-6 flex gap-2 items-center">
        <IconMdiLicense />
        {{ i18n.t("project.new.step3.license") }}
        <hr />
      </div>
      <div class="flex md:gap-2 lt-md:flex-wrap">
        <div class="basis-full mt-2" :md="isCustomLicense ? 'basis-4/12' : 'basis-6/12'">
          <InputSelect v-model="form.settings.license.type" :values="useLicenseOptions" :label="i18n.t('project.new.step3.type')" :rules="[required()]" />
        </div>
        <div v-if="isCustomLicense" class="basis-full md:basis-8/12 mt-2">
          <InputText
            v-model.trim="form.settings.license.name"
            :label="i18n.t('project.new.step3.customName')"
            :rules="[
              requiredIf()(isCustomLicense),
              maxLength()(useBackendData.validations.project.license.max),
              pattern()(useBackendData.validations.project.license.regex),
            ]"
          />
        </div>
        <div v-if="!licenseUnset" class="basis-full mt-2" :md="isCustomLicense ? 'basis-full' : 'basis-6/12'">
          <InputText v-model.trim="form.settings.license.url" :label="i18n.t('project.new.step3.url')" :rules="[url()]" />
        </div>
      </div>
      <div class="text-lg mt-6 flex gap-2 items-center">
        <IconMdiTag />
        {{ i18n.t("project.new.step3.tags") }}
        <hr />
      </div>
      <p class="mb-1">{{ i18n.t("project.new.step3.description2") }}</p>
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
      <div class="text-lg mt-4 flex gap-2 items-center">
        <IconMdiCloudSearch />
        {{ i18n.t("project.new.step3.keywords") }}
        <hr />
      </div>
      <div class="flex">
        <div class="mt-2 basis-full">
          <InputTag
            v-model="form.settings.keywords"
            :label="i18n.t('project.new.step3.keywords')"
            :tag-maxlength="useBackendData.validations?.project?.keywordName?.max || 16"
            :rules="[maxLength()(useBackendData?.validations?.project?.keywords?.max || 5), noDuplicated()(() => form.settings.keywords)]"
            :maxlength="useBackendData?.validations?.project?.keywords?.max || 5"
            counter
          />
        </div>
      </div>
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
        <div v-else class="text-h5 mt-2 mb-2">
          {{ i18n.t("project.new.step4.text") }}
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
