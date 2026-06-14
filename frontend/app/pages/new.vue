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
const selectedOwner = computed(() => projectOwners.value.find((owner) => owner.id === form.value.ownerId));
const selectedCategory = computed(() => useCategoryOptions.value.find((option) => option.value === form.value.category));

function selectOwner(ownerId: number) {
  form.value.ownerId = ownerId;
}

function selectCategory(category: string) {
  form.value.category = category as Category;
}

function selectLicense(license: string) {
  form.value.settings.license.type = license;
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
      <div class="grid gap-10 lg:grid-cols-[minmax(0,1fr)_18rem]">
        <div>
          <h2 class="text-xl font-bold">Create a new project</h2>
          <p class="mt-1 max-w-2xl text-sm leading-relaxed text-gray">{{ i18n.t("project.new.step1.text1") }}</p>

          <div class="mt-5 space-y-3 text-sm">
            <p class="flex items-start gap-2">
              <IconMdiCheck class="mt-0.5 flex-shrink-0 color-primary" />
              <span>You have permission to publish and distribute this project.</span>
            </p>
            <p class="flex items-start gap-2">
              <IconMdiCheck class="mt-0.5 flex-shrink-0 color-primary" />
              <span>The project information you provide will be accurate and complete.</span>
            </p>
          </div>

          <Link
            to="/guidelines"
            class="group mt-5 flex max-w-2xl items-center gap-3 rounded-lg border border-gray-200 px-3 py-2.5 transition-colors hover:border-gray-300 hover:bg-gray-100 dark:border-gray-800 dark:hover:border-gray-700 dark:hover:bg-gray-800"
          >
            <IconMdiFileDocumentOutline class="flex-shrink-0 text-lg text-gray" />
            <span class="min-w-0 flex-grow">
              <span class="block font-semibold">{{ i18n.t("project.new.step1.text2") }}</span>
              <span class="block text-xs text-gray">Updated <PrettyTime :time="guidelinesLastUpdated" short-relative /></span>
            </span>
            <IconMdiChevronRight class="flex-shrink-0 text-gray transition-transform group-hover:translate-x-0.5" />
          </Link>
        </div>

        <aside class="border-gray-200 lg:border-l lg:pl-8 dark:border-gray-800">
          <IconMdiFolderPlusOutline class="text-2xl text-gray" />
          <h2 class="mt-2 text-lg font-bold">Already published elsewhere?</h2>
          <p class="mt-1 text-sm leading-relaxed text-gray">{{ i18n.t("project.new.step1.importer_text") }}</p>
          <Button
            to="/tools/importer"
            button-type="secondary"
            size="medium"
            class="mt-4 hover:!border-gray-300 hover:!bg-gray-100 dark:hover:!border-gray-700 dark:hover:!bg-gray-800"
          >
            Import
            <IconMdiArrowRight class="ml-1" />
          </Button>
        </aside>
      </div>
    </template>
    <template #basic>
      <div class="space-y-8">
        <div class="grid gap-8 lg:grid-cols-2">
          <section>
            <div class="mb-3">
              <div>
                <h2 class="text-lg font-bold">Project identity</h2>
                <p class="mt-1 text-sm text-gray">Choose the account that owns this project and its public URL.</p>
              </div>
            </div>
            <div class="grid gap-x-3 gap-y-1 sm:grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)]">
              <label class="text-sm font-semibold">{{ i18n.t("project.new.step2.userSelect") }}</label>
              <span class="hidden sm:block" />
              <label class="text-sm font-semibold">{{ i18n.t("project.new.step2.projectName") }}</label>

              <div class="h-11">
                <DropdownButton button-size="medium" button-type="transparent" button-class="!h-11 !py-2" match-width spread-arrow>
                  <template #button-label>
                    <span class="w-full truncate text-left">{{ selectedOwner?.name || i18n.t("project.new.step2.userSelect") }}</span>
                  </template>
                  <template #default="{ close }">
                    <DropdownItem
                      v-for="owner in projectOwners"
                      :key="owner.id"
                      :style="
                        form.ownerId === owner.id
                          ? {
                              backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                              borderColor: 'var(--primary-500)',
                            }
                          : {}
                      "
                      @click="
                        selectOwner(owner.id);
                        close();
                      "
                    >
                      {{ owner.name }}
                    </DropdownItem>
                  </template>
                </DropdownButton>
              </div>
              <span class="hidden h-11 items-center text-2xl text-gray sm:flex">/</span>
              <div class="h-11 [&>div>label]:!h-11 [&>div>label]:!py-0">
                <InputText
                  v-model.trim="form.name"
                  :placeholder="i18n.t('project.new.step2.projectName')"
                  :maxlength="useBackendData.validations.project.name.max"
                  counter
                  name="name"
                  :rules="[
                    required(),
                    maxLength()(useBackendData.validations.project.name.max!),
                    pattern()(useBackendData.validations.project.name.regex!),
                    validProjectName()(),
                  ]"
                />
              </div>
            </div>
          </section>

          <section>
            <div class="mb-3">
              <h2 class="text-lg font-bold">Classification</h2>
              <p class="mt-1 text-sm text-gray">Select the category that best describes the project.</p>
            </div>
            <DropdownButton button-size="medium" button-type="transparent" button-class="!h-11 !py-2" match-width spread-arrow>
              <template #button-label>
                <span class="w-full truncate text-left">{{ selectedCategory ? i18n.t(selectedCategory.text) : form.category }}</span>
              </template>
              <template #default="{ close }">
                <DropdownItem
                  v-for="category in useCategoryOptions"
                  :key="category.value"
                  :style="
                    form.category === category.value
                      ? {
                          backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                          borderColor: 'var(--primary-500)',
                        }
                      : {}
                  "
                  @click="
                    selectCategory(category.value);
                    close();
                  "
                >
                  {{ i18n.t(category.text) }}
                </DropdownItem>
              </template>
            </DropdownButton>
          </section>
        </div>

        <section>
          <div class="mb-3">
            <div>
              <h2 class="text-lg font-bold">Project summary</h2>
              <p class="mt-1 text-sm text-gray">Write a short description that explains what the project does.</p>
            </div>
          </div>
          <InputText
            v-model.trim="form.description"
            :label="i18n.t('project.new.step2.projectSummary')"
            :rules="[required()]"
            :maxlength="useBackendData.validations.project.desc.max"
            counter
            name="description"
          />
        </section>
      </div>
    </template>
    <template #additional>
      <div class="space-y-4">
        <p class="text-sm leading-relaxed text-gray">{{ i18n.t("project.new.step3.description") }}</p>

        <div class="grid grid-cols-1 items-start gap-8 xl:grid-cols-2">
          <section>
            <div>
              <h2 class="text-xl font-bold">{{ i18n.t("project.settings.keywords") }}</h2>
              <p class="mt-1 text-sm text-gray">{{ i18n.t("project.settings.keywordsSub") }}</p>
              <div class="mt-3">
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

            <div class="mt-6">
              <h2 class="text-xl font-bold">{{ i18n.t("project.settings.tags.title") }}</h2>
              <p class="mt-1 text-sm text-gray">{{ i18n.t("project.settings.tagsSub") }}</p>
              <div class="mt-3 grid gap-2 sm:grid-cols-3">
                <button
                  v-for="tag in Object.values(Tag)"
                  :key="tag"
                  type="button"
                  class="flex h-11 items-center rounded-lg border px-3 text-sm transition-all duration-200 hover:border-gray-600 hover:bg-gray-800/60"
                  :class="form.settings.tags.includes(tag) ? 'color-primary' : 'border-gray-200 dark:border-gray-800'"
                  :style="
                    form.settings.tags.includes(tag)
                      ? {
                          backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                          borderColor: 'var(--primary-500)',
                        }
                      : {}
                  "
                  @click="
                    form.settings.tags = form.settings.tags.includes(tag)
                      ? form.settings.tags.filter((currentTag) => currentTag !== tag)
                      : [...form.settings.tags, tag]
                  "
                >
                  <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
                  <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
                  <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" />
                  <span class="ml-1">{{ i18n.t("project.settings.tags." + tag + ".title") }}</span>
                  <Tooltip>
                    <template #content>{{ i18n.t("project.settings.tags." + tag + ".description") }}</template>
                    <IconMdiHelpCircleOutline class="ml-1 text-sm text-gray-500 dark:text-gray-400" />
                  </Tooltip>
                  <IconMdiCheck v-if="form.settings.tags.includes(tag)" class="ml-auto" />
                </button>
              </div>
            </div>
          </section>

          <section>
            <div>
              <h2 class="text-xl font-bold">{{ i18n.t("project.settings.license") }}</h2>
              <p class="mt-1 text-sm text-gray">{{ i18n.t("project.settings.licenseSub") }}</p>
              <div class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-[minmax(10rem,1fr)_minmax(0,2fr)]">
                <div>
                  <DropdownButton button-size="medium" button-type="transparent" button-class="!h-10.5 !py-2" match-width spread-arrow>
                    <template #button-label>
                      <span class="w-full truncate text-left">{{ form.settings.license.type }}</span>
                    </template>
                    <template #default="{ close }">
                      <DropdownItem
                        v-for="license in useLicenseOptions"
                        :key="license.value"
                        :style="
                          form.settings.license.type === license.value
                            ? {
                                backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                                borderColor: 'var(--primary-500)',
                              }
                            : {}
                        "
                        @click="
                          selectLicense(license.value);
                          close();
                        "
                      >
                        {{ license.text }}
                      </DropdownItem>
                    </template>
                  </DropdownButton>
                </div>
                <InputText
                  v-if="isCustomLicense"
                  v-model.trim="form.settings.license.name"
                  class="[&>label]:!h-10.5 [&>label]:!py-0"
                  :placeholder="i18n.t('project.settings.licenseCustom')"
                  :rules="[
                    requiredIf()(isCustomLicense),
                    maxLength()(useBackendData.validations.project.license.max!),
                    pattern()(useBackendData.validations.project.license.regex!),
                  ]"
                />
                <InputText
                  v-if="!licenseUnset"
                  v-model.trim="form.settings.license.url"
                  :class="['[&>label]:!h-10.5 [&>label]:!py-0', { 'sm:col-start-2': isCustomLicense }]"
                  :placeholder="i18n.t('project.settings.licenseUrl')"
                  :rules="[validUrl()]"
                />
              </div>
            </div>
          </section>
        </div>

        <section class="pt-4">
          <div class="mb-3">
            <div>
              <h2 class="text-xl font-bold">{{ i18n.t("project.new.step3.links") }}</h2>
              <p class="mt-1 text-sm text-gray">Add documentation, source code, support, or community links.</p>
            </div>
          </div>
          <ProjectLinksForm v-model="form.settings.links" />
        </section>
      </div>
    </template>
    <template #finishing>
      <div class="flex min-h-48 flex-col items-center justify-center text-center">
        <template v-if="projectCreationErrors && projectCreationErrors.length > 0">
          <span class="inline-flex h-12 w-12 items-center justify-center rounded-lg bg-red-500/15 text-2xl text-red-400">
            <IconMdiAlertOutline />
          </span>
          <h2 class="mt-3 text-xl font-bold">{{ i18n.t("project.new.error.create") }}</h2>
          <ul class="mt-2 text-sm text-red-400">
            <li v-for="error in projectCreationErrors" :key="error">{{ error }}</li>
          </ul>
          <Button class="mt-4" size="medium" @click="createProject">
            <IconMdiRefresh class="mr-1" />
            Retry
          </Button>
        </template>
        <template v-else>
          <span class="inline-flex h-12 w-12 items-center justify-center text-2xl color-primary">
            <IconMdiLoading class="animate-spin" />
          </span>
          <h2 class="mt-3 text-xl font-bold">{{ i18n.t("project.new.step4.text") }}</h2>
          <p class="mt-1 text-sm text-gray">Hang tight while we prepare your project.</p>
        </template>
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
