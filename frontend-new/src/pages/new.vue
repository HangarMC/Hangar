<script setup lang="ts">
import { ProjectOwner, ProjectSettingsForm } from "hangar-internal";
import { ProjectCategory } from "~/types/enums";
import { handleRequestError } from "~/composables/useErrorHandling";
import { Ref, ref, watch } from "vue";
import { useInternalApi } from "~/composables/useApi";
import { useContext } from "vite-ssr/vue";
import PageTitle from "~/components/design/PageTitle.vue";
import { useBackendDataStore } from "~/store/backendData";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";

interface NewProjectForm extends ProjectSettingsForm {
  ownerId: ProjectOwner["userId"];
  name: string;
  pageContent: string | null;
}

const ctx = useContext();
const i18n = useI18n();
const store = useBackendDataStore();
const router = useRouter();
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
  console.log(form);
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
      //TODO
      /*if (!err.response?.data.isHangarApiException) {
        return handleRequestError(err, ctx, i18n);
      }*/
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

<!-- todo: rules, icon, and labels on selects (aside from actual design)-->
<template>
  <PageTitle>New Project</PageTitle>
  <input v-model="projectName" class="p-4" placeholder="Project name" />
  <br />
  {{ error }}
  <br />
  <select v-model="form.ownerId" class="p-4">
    <option v-for="owner in projectOwners" :key="owner.userId" :value="owner.userId">
      {{ owner.name }}
    </option>
  </select>
  <select v-model="form.settings.license" class="p-4">
    <option v-for="license in licenses" :key="license">
      {{ license }}
    </option>
  </select>

  <br />
  <h1 class="p-4">Description</h1>
  <textarea v-model="form.description" class="p-8"></textarea>

  <br />
  <h1 class="p-4">Page Conent</h1>
  <textarea v-model="form.pageContent" class="p-8"></textarea>

  <br />
  <select v-model="form.category" class="p-4">
    <option v-for="category in visibleCategories" :key="category.apiName" :value="category.apiName">
      {{ category.title }}
    </option>
  </select>

  <br />
  <button class="p-4" @click="createProject">Create</button>
  <br />
  <template v-if="projectCreationErrors.length !== 0">{{ projectCreationErrors }}</template>

  <!-- todo: custom license, license url, issues, keywords, homepage, markdown converter/page content -->
</template>

<route lang="yaml">
meta:
  requireLoggedIn: true
</route>
