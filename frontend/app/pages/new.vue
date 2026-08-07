<script setup lang="ts">
import type { Step } from "#shared/types/components/design/Steps";
import { Category, Tag } from "#shared/types/backend";
import type { NewProjectForm, ProjectSettingsForm } from "#shared/types/backend";
import { guidelinesLastUpdated } from "~/pages/guidelines.vue";

definePageMeta({
  loginRequired: true,
});

const i18n = useI18n();
const router = useRouter();
const route = useRoute("new");

const { projectOwners } = usePossibleOwners();
const projectCreationErrors = ref<string[]>([]);
const projectLoading = ref(true);
const form = ref<NewProjectForm>({
  category: Category.AdminTools,
  settings: {
    license: {} as ProjectSettingsForm["settings"]["license"],
    donation: {} as ProjectSettingsForm["settings"]["donation"],
    keywords: [],
    links: [],
    tags: [],
  } as unknown as ProjectSettingsForm["settings"],
} as NewProjectForm);

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

function toggleTag(tag: Tag) {
  const tags = form.value.settings.tags;
  const index = tags.indexOf(tag);
  if (index === -1) {
    tags.push(tag);
  } else {
    tags.splice(index, 1);
  }
}

const selectedStep = ref("tos");
const steps: Step[] = [
  {
    value: "tos",
    header: i18n.t("project.new.step1.title"),
    showBack: false,
    beforeNext: () => {
      const firstId = projectOwners.value?.[0]?.id;
      if (firstId) {
        form.value.ownerId = firstId;
      }
      return true;
    },
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

useSeo(computed(() => ({ title: "New Project", route })));

function createProject() {
  projectCreationErrors.value = [];
  projectLoading.value = true;
  if (!form.value.pageContent) {
    form.value.pageContent = "# " + form.value.name + "  \nWelcome to your new project!";
  }
  if (!isCustomLicense.value) {
    form.value.settings.license.name = undefined as unknown as string;
  }
  if (licenseUnset.value) {
    form.value.settings.license.url = undefined;
  }
  useInternalApi<string>("projects/create", "post", form.value)
    .then((u) => {
      router.push(u);
    })
    .catch((err) => {
      projectCreationErrors.value = [];
      if (err.response?.data.fieldErrors != undefined) {
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
  <Steps v-model="selectedStep" :steps="steps" button-lang-key="project.new.step" tracking-name="new-project">
    <template #tos>
      <p>{{ i18n.t("project.new.step1.text1") }}</p>
      <p class="mt-2 inline-flex flex-wrap items-center gap-x-2">
        <IconMdiFileDocumentAlert class="flex-shrink-0 text-gray-secondary" />
        <Link to="/guidelines">{{ i18n.t("project.new.step1.text2") }}</Link>
        <Tooltip>
          <template #content><PrettyTime :time="guidelinesLastUpdated" long /></template>
          <span class="text-sm text-gray-secondary">(Last updated <PrettyTime :time="guidelinesLastUpdated" short-relative />)</span>
        </Tooltip>
      </p>
    </template>
    <template #basic>
      <div>
        <p class="mb-4">{{ i18n.t("project.new.step2.description") }}</p>

        <div class="flex flex-wrap items-start gap-2">
          <div class="min-w-50 flex-1">
            <InputDropdown
              v-model="form.ownerId"
              :values="projectOwners"
              item-value="id"
              item-text="name"
              :label="i18n.t('project.new.step2.userSelect')"
              :rules="[required()]"
            />
          </div>
          <span class="pt-3 text-xl text-gray-secondary lt-md:hidden">/</span>
          <div class="min-w-50 flex-1">
            <InputText
              v-model.trim="form.name"
              :label="i18n.t('project.new.step2.projectName')"
              :maxlength="useBackendData.validations.project.name.max"
              name="name"
              counter
              :rules="[
                required(),
                maxLength()(useBackendData.validations.project.name.max!),
                pattern()(useBackendData.validations.project.name.regex!),
                validProjectName()(),
              ]"
            />
          </div>
        </div>

        <div class="mt-4">
          <InputText
            v-model.trim="form.description"
            :label="i18n.t('project.new.step2.projectSummary')"
            :rules="[required()]"
            :maxlength="useBackendData.validations.project.desc.max"
            name="description"
            counter
          />
        </div>

        <div class="mt-4">
          <InputDropdown
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
      <div class="mt-4 flex items-center gap-2">
        <IconMdiLink class="flex-shrink-0 text-gray-secondary" />
        <span class="flex-shrink-0 text-lg font-bold">{{ i18n.t("project.new.step3.links") }}</span>
        <hr class="flex-1 border-gray-300 dark:border-gray-700" />
      </div>
      <ProjectLinksForm v-model="form.settings.links" class="mt-2" />
      <div class="mt-6 flex items-center gap-2">
        <IconMdiLicense class="flex-shrink-0 text-gray-secondary" />
        <span class="flex-shrink-0 text-lg font-bold">{{ i18n.t("project.new.step3.license") }}</span>
        <hr class="flex-1 border-gray-300 dark:border-gray-700" />
      </div>
      <div class="flex flex-wrap items-start gap-2">
        <div class="mt-2 flex-shrink-0">
          <InputDropdown v-model="form.settings.license.type" :values="useLicenseOptions" :label="i18n.t('project.new.step3.type')" :rules="[required()]" />
        </div>
        <div v-if="isCustomLicense" class="mt-2 min-w-60 flex-1">
          <InputText
            v-model.trim="form.settings.license.name"
            :label="i18n.t('project.new.step3.customName')"
            :rules="[
              requiredIf()(isCustomLicense),
              maxLength()(useBackendData.validations.project.license.max!),
              pattern()(useBackendData.validations.project.license.regex!),
            ]"
          />
        </div>
        <div v-if="!licenseUnset" class="mt-2 min-w-60 flex-1">
          <InputText v-model.trim="form.settings.license.url" :label="i18n.t('project.new.step3.url')" :rules="[validUrl()]" />
        </div>
      </div>
      <div class="mt-6 flex items-center gap-2">
        <IconMdiTag class="flex-shrink-0 text-gray-secondary" />
        <span class="flex-shrink-0 text-lg font-bold">{{ i18n.t("project.new.step3.tags") }}</span>
        <hr class="flex-1 border-gray-300 dark:border-gray-700" />
      </div>
      <p class="mb-1">{{ i18n.t("project.new.step3.description2") }}</p>
      <div class="mt-2 flex flex-wrap gap-2">
        <Tooltip v-for="tag in Object.values(Tag)" :key="tag">
          <template #content>{{ i18n.t("project.settings.tags." + tag + ".description") }}</template>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-md border px-3 py-1.5 text-sm font-semibold transition-colors"
            :class="
              form.settings.tags.includes(tag)
                ? 'border-primary-500 background-card color-primary'
                : 'border-gray-300 hover:background-card dark:border-gray-700'
            "
            :aria-pressed="form.settings.tags.includes(tag)"
            @click="toggleTag(tag)"
          >
            <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
            <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
            <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" />
            {{ i18n.t("project.settings.tags." + tag + ".title") }}
          </button>
        </Tooltip>
      </div>
      <div class="mt-4 flex items-center gap-2">
        <IconMdiCloudSearch class="flex-shrink-0 text-gray-secondary" />
        <span class="flex-shrink-0 text-lg font-bold">{{ i18n.t("project.new.step3.keywords") }}</span>
        <hr class="flex-1 border-gray-300 dark:border-gray-700" />
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
        <div v-if="projectLoading" class="my-8 flex items-center justify-center gap-3 text-gray-secondary">
          <Spinner class="stroke-current" :diameter="20" :stroke="2" />
          {{ i18n.t("project.new.step4.creating") }}
        </div>
        <template v-else-if="projectCreationErrors && projectCreationErrors.length > 0">
          <Alert type="danger">
            <div>
              <div class="font-bold">{{ i18n.t("project.new.error.create") }}</div>
              <ul class="mt-1 list-disc pl-4">
                <li v-for="error in projectCreationErrors" :key="error">{{ error }}</li>
              </ul>
            </div>
          </Alert>
          <div class="mt-3 flex justify-center">
            <Button @click="createProject">
              <IconMdiRefresh />
              {{ i18n.t("general.retry") }}
            </Button>
          </div>
        </template>
        <div v-else class="my-2 text-lg font-bold">
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
