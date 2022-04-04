<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { useBackendDataStore } from "~/store/backendData";
import { useContext } from "vite-ssr/vue";
import { HangarProjectPage } from "hangar-internal";
import { computed, ref, watch } from "vue";
import InputText from "~/components/ui/InputText.vue";
import InputSelect, { Option } from "~/components/ui/InputSelect.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { AxiosError } from "axios";
import { useRoute, useRouter } from "vue-router";

const props = defineProps<{
  projectId: number;
  pages: HangarProjectPage[];
}>();

const i18n = useI18n();
const ctx = useContext();
const route = useRoute();
const router = useRouter();
const backendData = useBackendDataStore();

const pageRoots = computed(() => flatDeep(props.pages, ""));
const name = ref("");
const nameErrorMessages = ref<string[]>([]);
const parent = ref<number | null>(null);
const validateLoading = ref<boolean>(false);
const loading = ref<boolean>(false);

watch(name, async () => {
  if (!name.value) return;
  validateLoading.value = true;
  nameErrorMessages.value = [];
  await useInternalApi("pages/checkName", true, "get", {
    projectId: props.projectId,
    name: name.value,
    parentId: parent.value,
  })
    .catch((err: AxiosError) => {
      if (!err.response?.data.isHangarApiException) {
        return handleRequestError(err, ctx, i18n);
      }
      nameErrorMessages.value.push(i18n.t(err.response.data.message));
    })
    .finally(() => {
      validateLoading.value = false;
    });
});

function flatDeep(pages: HangarProjectPage[], prefix: string): Option[] {
  let ps: Option[] = [];
  for (const page of pages) {
    if (page.children.length > 0) {
      ps = [...ps, ...flatDeep(page.children, prefix + "-")];
    }
    ps.unshift({ value: page.id, text: prefix + page.name });
  }
  return ps;
}

async function createPage() {
  try {
    loading.value = true;
    const slug = await useInternalApi<string>(`pages/create/${props.projectId}`, true, "post", {
      name: name.value,
      parentId: parent.value,
    });
    await router.push(`/${route.params.user}/${route.params.project}/pages/${slug}`);
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
  loading.value = false;
}
</script>

<template>
  <Modal :title="i18n.t('page.new.title')">
    <template #default="{ on }">
      <div class="flex flex-col">
        <InputText
          v-model.trim="name"
          :label="i18n.t('page.new.name')"
          :error-messages="nameErrorMessages"
          counter
          :maxlength="backendData.validations.project.pageName.max"
          :minlength="backendData.validations.project.pageName.min"
        />
        <InputSelect v-model="parent" :values="pageRoots" :label="i18n.t('page.new.parent')" />
      </div>
      <div>
        <Button class="mt-2 ml-2" :disabled="validateLoading || loading" @click="createPage">{{ i18n.t("general.create") }}</Button>
        <Button button-type="gray" class="mt-2" v-on="on">{{ i18n.t("general.close") }}</Button>
      </div>
    </template>
    <template #activator="{ on }">
      <Button v-bind="$attrs" class="mr-1 h-[32px]" size="small" v-on="on">
        <IconMdiPlus />
      </Button>
    </template>
  </Modal>
</template>
