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
  <div class="flex gap-4">
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
          <div>
            <h2 class="text-lg">{{ i18n.t("project.settings.category") }}</h2>
            <p>{{ i18n.t("project.settings.categorySub") }}</p>
            <InputSelect v-model="form.category" :values="categories" />
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">{{ i18n.t("project.settings.description") }}</h2>
            <p>{{ i18n.t("project.settings.descriptionSub") }}</p>
            <InputText v-model="form.description" counter :maxlength="backendData.validations?.project?.desc?.max" />
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">{{ i18n.t("project.settings.forum") }}</h2>
            <InputCheckbox v-model="form.settings.forumSync" :label="i18n.t('project.settings.forumSub')" />
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">{{ i18n.t("project.settings.icon") }}</h2>
            <div class="flex">
              <div>
                <p>{{ i18n.t("project.settings.iconSub") }}</p>
                <InputFile v-model="projectIcon" accept="image/png, image/jpeg" show-size @change="onFileChange" />
                <Button :disabled="!projectIcon || loading.uploadIcon" @click="uploadIcon">
                  <IconMdiUpload />
                  {{ i18n.t("project.settings.iconUpload") }}
                </Button>
                <Button :disabled="loading.resetIcon" @click="resetIcon">
                  <IconMdiUpload />
                  {{ i18n.t("project.settings.iconReset") }}
                </Button>
              </div>
              <div>
                <img
                  id="project-icon-preview"
                  width="150"
                  height="150"
                  alt="Project Icon"
                  :src="projectIconUrl(project.namespace.owner, project.namespace.slug)"
                />
              </div>
            </div>
          </div>
        </template>
        <template #optional>
          <div>
            <h2 class="text-lg">
              {{ i18n.t("project.settings.keywords") }}&nbsp;<small>{{ i18n.t("project.settings.optional") }}</small>
            </h2>
            <p>{{ i18n.t("project.settings.keywordsSub") }}</p>
            <InputTag
              v-model="form.settings.keywords"
              counter
              :maxlength="backendData.validations.project.keywords.max"
              :label="i18n.t('project.new.step3.keywords')"
            />
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">
              {{ i18n.t("project.settings.homepage") }}&nbsp;<small>{{ i18n.t("project.settings.optional") }}</small>
            </h2>
            <p>{{ i18n.t("project.settings.homepageSub") }}</p>
            <InputText v-model.trim="form.settings.homepage" :label="i18n.t('project.new.step3.homepage')"></InputText>
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">
              {{ i18n.t("project.settings.issues") }}&nbsp;<small>{{ i18n.t("project.settings.optional") }}</small>
            </h2>
            <p>{{ i18n.t("project.settings.issuesSub") }}</p>
            <InputText v-model.trim="form.settings.issues" :label="i18n.t('project.new.step3.issues')" />
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">
              {{ i18n.t("project.settings.source") }}&nbsp;<small>{{ i18n.t("project.settings.optional") }}</small>
            </h2>
            <p>{{ i18n.t("project.settings.sourceSub") }}</p>
            <InputText v-model.trim="form.settings.source" :label="i18n.t('project.new.step3.source')" />
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">
              {{ i18n.t("project.settings.support") }}&nbsp;<small>{{ i18n.t("project.settings.optional") }}</small>
            </h2>
            <p>{{ i18n.t("project.settings.supportSub") }}</p>
            <InputText v-model.trim="form.settings.support" :label="i18n.t('project.new.step3.support')" />
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">
              {{ i18n.t("project.settings.license") }}&nbsp;<small>{{ i18n.t("project.settings.optional") }}</small>
            </h2>
            <p>{{ i18n.t("project.settings.licenseSub") }}</p>
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
          </div>
        </template>
        <template #management>
          <div>
            <h2 class="text-lg">{{ i18n.t("project.settings.rename") }}</h2>
            <p>{{ i18n.t("project.settings.renameSub") }}</p>
            <div class="flex">
              <InputText v-model.trim="newName" :error-messages="nameErrors" />
              <Button :disabled="!newName || loading.rename || nameErrors.length > 0" class="ml-2" @click="rename">
                <IconMdiRenameBox class="mr-2" />
                {{ i18n.t("project.settings.rename") }}
              </Button>
            </div>
          </div>
          <hr class="my-1" />
          <div v-if="hasPerms(NamedPermission.DELETE_PROJECT)">
            <div class="flex">
              <div class="flex-shrink">
                <h2 class="text-lg">{{ i18n.t("project.settings.delete") }}</h2>
                <p>{{ i18n.t("project.settings.deleteSub") }}</p>
              </div>
              <div class="flex-grow">
                <TextAreaModal :title="i18n.t('project.settings.delete')" :label="i18n.t('general.comment')" :submit="softDelete">
                  <template #activator="{ on }">
                    <Button v-on="on">{{ i18n.t("project.settings.delete") }}</Button>
                  </template>
                </TextAreaModal>
              </div>
            </div>
          </div>
          <hr v-if="hasPerms(NamedPermission.HARD_DELETE_PROJECT)" class="my-1" />
          <div v-if="hasPerms(NamedPermission.HARD_DELETE_PROJECT)" class="bg-red-500 p-4">
            <div class="flex">
              <div class="flex-shrink">
                <h2 class="text-lg">{{ i18n.t("project.settings.hardDelete") }}</h2>
                <p>{{ i18n.t("project.settings.hardDeleteSub") }}</p>
              </div>
              <div class="flex-grow">
                <TextAreaModal :title="i18n.t('project.settings.hardDelete')" :label="i18n.t('general.comment')" :submit="hardDelete">
                  <template #activator="{ on }">
                    <Button v-on="on">{{ i18n.t("project.settings.hardDelete") }}</Button>
                  </template>
                </TextAreaModal>
              </div>
            </div>
          </div>
        </template>
        <template #donation>
          <div>
            <h2 class="text-lg">{{ i18n.t("project.settings.donation.enable") }}</h2>
            <p>{{ i18n.t("project.settings.donation.enableSub") }}</p>
            <InputCheckbox v-model="form.settings.donation.enable" />
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">{{ i18n.t("project.settings.donation.email") }}</h2>
            <p>{{ i18n.t("project.settings.donation.emailSub") }}</p>
            <InputText v-model="form.settings.donation.email" />
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">{{ i18n.t("project.settings.donation.defaultAmount") }}</h2>
            <p>{{ i18n.t("project.settings.donation.defaultAmountSub") }}</p>
            <InputText v-model.number="form.settings.donation.defaultAmount" type="number" />
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">{{ i18n.t("project.settings.donation.oneTimeAmounts") }}</h2>
            <p>{{ i18n.t("project.settings.donation.oneTimeAmountsSub") }}</p>
            <InputTag v-model="form.settings.donation.oneTimeAmounts" />
          </div>
          <hr class="my-1" />
          <div>
            <h2 class="text-lg">{{ i18n.t("project.settings.donation.monthlyAmounts") }}</h2>
            <p>{{ i18n.t("project.settings.donation.monthlyAmountsSub") }}</p>
            <InputTag v-model="form.settings.donation.monthlyAmounts" />
          </div>
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
