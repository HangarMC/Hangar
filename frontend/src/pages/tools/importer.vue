<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { useHead } from "@unhead/vue";
import { useRoute } from "vue-router";
import { computed, ref } from "vue";
import type { NewProjectForm } from "hangar-internal";
import { useVuelidate } from "@vuelidate/core";
import { AxiosError } from "axios";
import { watchDebounced } from "@vueuse/core";
import PageTitle from "~/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import Steps from "~/components/design/Steps.vue";
import type { Step } from "~/types/components/design/Steps";
import Link from "~/components/design/Link.vue";
import type { SpigotAuthor, SpigotResource } from "~/composables/useProjectImporter";
import { convertSpigotProjects, getAllSpigotResourcesByAuthor, getSpigotAuthor } from "~/composables/useProjectImporter";
import InputText from "~/components/ui/InputText.vue";
import InputSelect from "~/components/ui/InputSelect.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import Spoiler from "~/components/design/Spoiler.vue";

import { MarkdownEditor } from "#components";
import InputTag from "~/components/ui/InputTag.vue";
import { useAuthStore } from "~/store/auth";
import { useBackendData, useCategoryOptions, useLicenseOptions } from "~/store/backendData";
import { minLength, maxLength, pattern, required, requiredIf, url, noDuplicated } from "~/composables/useValidationHelpers";
import { validProjectName } from "~/composables/useHangarValidations";
import Button from "~/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import Spinner from "~/components/design/Spinner.vue";
import { definePageMeta, reactive, usePossibleOwners } from "#imports";
import Alert from "~/components/design/Alert.vue";
import IconMdiFileDocumentAlert from "~icons/mdi/file-document-alert";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import InputGroup from "~/components/ui/InputGroup.vue";
import { ProjectCategory, Tag } from "~/types/enums"; // dont remove Tag, even if IntelliJ says its unused...
import ProjectLinksForm from "~/components/projects/ProjectLinksForm.vue";

definePageMeta({
  loginRequired: true,
});

const { t } = useI18n();
const route = useRoute();
const auth = useAuthStore();

const selectedStep = ref("intro");
const steps: Step[] = [
  {
    value: "intro",
    header: t("importer.step1.title"),
    showBack: false,
  },
  {
    value: "userSelection",
    header: t("importer.step2.title"),
    showBack: false,
    disableNext: computed(() => v.value.$errors.length > 0 || v.value.$pending),
    beforeNext: async () => {
      if (!(await v.value.$validate())) {
        return false;
      }
      spigotResources.value = [];
      if (spigotAuthor.value) {
        spigotResources.value = await getAllSpigotResourcesByAuthor(spigotAuthor.value.id);
        return true;
      } else {
        return false;
      }
    },
  },
  {
    value: "projectSelection",
    header: t("importer.step3.title"),
    showBack: true,
    disableNext: computed(() => v.value.$errors.length > 0 || v.value.$pending),
    showNext: computed(() => spigotResources.value?.length > 0),
    beforeNext: async () => {
      if (!(await v.value.$validate())) {
        return false;
      }
      hangarResources.value = [];
      if (auth.user) {
        const ownerId = auth.user.id;
        const selected = spigotResources.value.filter((r) => selectedSpigotResources.value.includes(r.id));
        hangarResources.value = await convertSpigotProjects(selected, ownerId);
        return true;
      } else {
        return false;
      }
    },
  },
  {
    value: "projectConversion",
    header: t("importer.step4.title"),
    disableNext: computed(() => v.value.$errors.length > 0 || v.value.$pending),
    beforeNext: async () => {
      if (!(await v.value.$validate())) {
        return false;
      }
      createProjects();
      return true;
    },
  },
  { value: "finishing", header: t("importer.step5.title"), showNext: false, showBack: false },
];

const projectOwners = await usePossibleOwners();
const username = ref();
const spigotAuthor = ref<SpigotAuthor | null>(null);
const spigotResources = ref<SpigotResource[]>([]);
const selectedSpigotResources = ref<string[]>([]);
const hangarResources = ref<NewProjectForm[]>([]);

const additionalRules = {
  spigotAuthor: {
    required,
  },
};
const additionalModel = {
  spigotAuthor: spigotAuthor.value,
};
const v = useVuelidate(additionalRules, additionalModel);

watchDebounced(
  username,
  async () => {
    spigotAuthor.value = null;
    try {
      spigotAuthor.value = await getSpigotAuthor(username.value);
    } catch (e: AxiosError) {
      // don't need popup about not found and wrong format
      if (!e.message?.includes("404") && !e.message?.includes("400")) {
        handleRequestError(e);
      }
    }
  },
  { debounce: 250 }
);

function remove(project: NewProjectForm) {
  hangarResources.value = hangarResources.value.filter((p) => p !== project);
}

function updatePageContent(project: NewProjectForm, raw: string) {
  project.pageContent = raw;
}

const status = reactive<Record<string, { project: NewProjectForm; loading: boolean; success: boolean; result: string; errors: string[] }>>({});

function createProjects() {
  for (const project of hangarResources.value) {
    createProject(project);
  }
}

function createProject(project: NewProjectForm) {
  status[project.name] = { project, loading: true, success: true, result: "", errors: [] };
  if (!project.pageContent) {
    project.pageContent = "# " + project.name + "  \nWelcome to your new project!";
  }
  if (!project.util.isCustomLicense.value) {
    project.settings.license.name = null;
  }
  if (project.util.licenseUnset.value) {
    project.settings.license.url = null;
  }
  useInternalApi<string>("projects/create", "post", project, { timeout: 10000 })
    .then((u) => {
      status[project.name].success = true;
      status[project.name].result = u;
    })
    .catch((err) => {
      if (err.response?.data.fieldErrors != null) {
        for (const e of err.response.data.fieldErrors) {
          status[project.name].errors.push(t(e.errorMsg));
        }
      } else {
        status[project.name].errors.push("Unknown error");
      }

      handleRequestError(err);
    })
    .finally(() => {
      status[project.name].loading = false;
    });
}

const loggedIn = useAuthStore().user !== null;

useHead(useSeo(t("importer.title"), null, route, null));
</script>

<template>
  <div>
    <PageTitle>{{ t("importer.title") }}</PageTitle>
    <p v-if="!loggedIn">{{ t("importer.notLoggedIn") }}</p>

    <Steps v-else v-model="selectedStep" :steps="steps" button-lang-key="importer.step">
      <template #intro>
        <p>{{ t("importer.step1.text1") }}</p>
        <p class="inline-flex items-center space-x-2">
          <IconMdiFileDocumentAlert />
          <Link to="/guidelines">
            {{ t("importer.step1.text2") }}
          </Link>
        </p>
      </template>
      <template #userSelection>
        <p class="mb-2">{{ t("importer.step2.text") }}</p>
        <InputText v-model="username" label="Username" :rules="[required()]" />
        <div v-if="spigotAuthor" class="flex items-center mt-2">
          <UserAvatar :username="spigotAuthor.username" :avatar-url="spigotAuthor.avatar" disable-link size="md" />
          <div class="ml-4">
            <div class="text-xl">{{ spigotAuthor.username }} (#{{ spigotAuthor.id }})</div>
            {{ spigotAuthor.resource_count }} resources
          </div>
        </div>
        <div v-else-if="username" class="mt-2">Nothing found...</div>
      </template>
      <template #projectSelection>
        <Alert v-if="spigotResources.length === 0">No projects found!</Alert>
        <InputGroup
          v-model="selectedSpigotResources"
          :rules="[required('Select at least one project!'), minLength()(1)]"
          :label="t('importer.step3.label')"
          :silent-errors="false"
        >
          <div class="grid gap-1" style="grid-template-columns: repeat(auto-fit, minmax(250px, 1fr))">
            <div v-for="project in spigotResources" :key="project.id" class="inline-flex">
              <InputCheckbox v-model="selectedSpigotResources" :label="project.title" :value="project.id" class="flex-shrink-0" />
            </div>
          </div>
        </InputGroup>
      </template>
      <template #projectConversion>
        <div v-for="project in hangarResources" :key="project.externalId" class="flex mb-4 pb-4 border-b-2px">
          <div class="flex flex-col justify-between">
            <UserAvatar :username="project.name" :avatar-url="project.avatarUrl" disable-link class="flex-shrink-0" />
            <div class="text-sm mt-4 w-min"><Button @click="remove(project)">Remove from conversion</Button></div>
          </div>
          <div class="ml-4 flex-grow">
            <div class="text-xl flex">
              <div class="basis-full md:basis-6/12">
                <InputSelect v-model="project.ownerId" :values="projectOwners" item-value="id" item-text="name" label="Create as" :rules="[required()]" />
              </div>
              <span class="text-3xl lt-md:hidden ml-2 mr-2">/</span>
              <div class="basis-full">
                <InputText
                  v-model="project.name"
                  label="Name"
                  :maxlength="useBackendData.validations.project.name.max"
                  counter
                  :rules="[
                    required(),
                    maxLength()(useBackendData.validations.project.name.max),
                    pattern()(useBackendData.validations.project.name.regex),
                    validProjectName()(() => project.ownerId),
                  ]"
                />
              </div>
            </div>
            <div class="mt-2">
              <InputText
                v-model="project.description"
                label="Description"
                :rules="[required()]"
                :maxlength="useBackendData.validations.project.desc.max"
                counter
              />
            </div>
            <div class="mt-2">
              <InputSelect
                v-model="project.category"
                :values="useCategoryOptions"
                label="Category"
                :rules="[required(), (category) => category !== ProjectCategory.UNDEFINED]"
                i18n-text-values
              />
            </div>
            <Spoiler title="Description" :open="false" class="!max-w-full mt-2">
              <template #content>
                <ClientOnly>
                  <MarkdownEditor
                    :raw="project.pageContent"
                    :editing="true"
                    :deletable="false"
                    :saveable="false"
                    :cancellable="false"
                    no-padding-top
                    @update:raw="(e) => updatePageContent(project, e)"
                  />
                </ClientOnly>
              </template>
            </Spoiler>
            <Spoiler title="Additional Information" :open="false" class="!max-w-full mt-2">
              <template #content>
                <p>{{ t("project.new.step3.description") }}</p>
                <div class="text-lg mt-4 flex gap-2 items-center">
                  <IconMdiLink />
                  {{ t("project.new.step3.links") }}
                  <hr />
                </div>
                <ProjectLinksForm v-model="project.settings.links" />

                <div class="text-lg mt-6 flex gap-2 items-center">
                  <IconMdiTag />
                  {{ t("project.new.step3.tags") }}
                  <hr />
                </div>
                <InputCheckbox v-for="tag in Object.values(Tag)" :key="tag" v-model="project.settings.tags" :value="tag">
                  <template #label>
                    <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
                    <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
                    <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" />
                    <span class="ml-1">{{ t("project.settings.tags." + tag + ".title") }}</span>
                  </template>
                </InputCheckbox>

                <div class="text-lg mt-6 flex gap-2 items-center">
                  <IconMdiLicense />
                  {{ t("project.new.step3.license") }}
                  <hr />
                </div>
                <div class="flex md:gap-2 lt-md:flex-wrap">
                  <div class="basis-full mt-4" :md="project.util.isCustomLicense ? 'basis-4/12' : 'basis-6/12'">
                    <InputSelect
                      v-model="project.settings.license.type"
                      :values="useLicenseOptions"
                      :label="t('project.new.step3.type')"
                      :rules="[required()]"
                    />
                  </div>
                  <div v-if="project.util.isCustomLicense" class="basis-full md:basis-8/12 mt-4">
                    <InputText
                      v-model.trim="project.settings.license.name"
                      :label="t('project.new.step3.customName')"
                      :rules="[
                        requiredIf()(project.util.isCustomLicense),
                        maxLength()(useBackendData.validations.project.license.max),
                        pattern()(useBackendData.validations.project.license.regex),
                      ]"
                    />
                  </div>
                  <div v-if="!project.util.licenseUnset" class="basis-full mt-4" :md="project.util.isCustomLicense ? 'basis-full' : 'basis-6/12'">
                    <InputText v-model.trim="project.settings.license.url" :label="t('project.new.step3.url')" :rules="[url()]" />
                  </div>
                </div>
                <div class="text-lg mt-6 flex gap-2 items-center">
                  <IconMdiCloudSearch />
                  {{ t("project.new.step3.keywords") }}
                  <hr />
                </div>
                <div class="flex">
                  <div class="mt-4 basis-full">
                    <InputTag
                      v-model="project.settings.keywords"
                      :label="t('project.new.step3.keywords')"
                      :tag-maxlength="useBackendData.validations?.project?.keywordName?.max || 16"
                      :rules="[maxLength()(useBackendData?.validations?.project?.keywords?.max || 5), noDuplicated()(() => project.settings.keywords)]"
                      :maxlength="useBackendData?.validations?.project?.keywords?.max || 5"
                      counter
                    />
                  </div>
                </div>
              </template>
            </Spoiler>
          </div>
        </div>
      </template>
      <template #finishing>
        <div class="flex flex-col">
          <div v-for="s in status" :key="s.project.name" class="text-center mb-2">
            <div class="text-xl font-bold">{{ s.project.name }}</div>
            <div v-if="s.loading"><Spinner class="stroke-red-500" :diameter="20" :stroke="2" /></div>
            <template v-else-if="s.errors && s.errors.length > 0">
              <div>
                {{ t("project.new.error.create") }}
                {{ s.errors }}
              </div>
              <div class="mt-2"><Button @click="createProject(s.project)"> Retry </Button></div>
            </template>
            <template v-else-if="s.success">
              Success!
              <Link :href="s.result" target="_blank">Open project</Link>
            </template>
          </div>
          <Alert class="text-center">Don't forget to upload your jar files!</Alert>
        </div>
      </template>
    </Steps>
  </div>
</template>
