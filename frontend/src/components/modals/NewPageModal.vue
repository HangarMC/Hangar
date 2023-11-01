<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import type { HangarProjectPage } from "hangar-internal";
import { computed, inject, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useVuelidate } from "@vuelidate/core";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { useBackendData } from "~/store/backendData";
import InputText from "~/components/ui/InputText.vue";
import InputSelect from "~/components/ui/InputSelect.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import type { Option } from "~/types/components/ui/InputSelect";
import { maxLength, minLength, pattern, required, validPageName } from "~/composables/useValidationHelpers";

const props = defineProps<{
  projectId: number;
  pages: HangarProjectPage[];
}>();

const i18n = useI18n();
const route = useRoute();
const router = useRouter();
const v = useVuelidate();

const updateProjectPagesCallback = inject<(pages: HangarProjectPage[]) => void>("updateProjectPages");
const modal = ref<any | null>(null); // Filled by vue

const pageRoots = computed(() => [{ value: -1, text: "<none>" }, ...flatDeep(props.pages, "")]);
const name = ref("");
const parent = ref<number | null>(null);
const loading = ref<boolean>(false);

const body = computed(() => ({
  projectId: props.projectId,
  name: name.value,
  parentId: parent.value === -1 ? null : parent.value,
}));
const rules = [
  required(),
  maxLength()(useBackendData.validations.project.pageName.max),
  minLength()(useBackendData.validations.project.pageName.min),
  pattern()(useBackendData.validations.project.pageName.regex),
  validPageName()(body),
];

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
    if (!(await v.value.$validate())) return;
    const slug = await useInternalApi<string>(`pages/create/${props.projectId}`, "post", {
      name: name.value,
      parentId: parent.value === -1 ? null : parent.value,
    });

    name.value = "";
    parent.value = null;

    if (updateProjectPagesCallback) {
      updateProjectPagesCallback(await useInternalApi<HangarProjectPage[]>(`pages/list/${props.projectId}`, "get"));
    }

    await router.push(`/${route.params.user}/${route.params.project}/pages/${slug}`);
  } catch (e) {
    handleRequestError(e);
  }
  loading.value = false;

  modal.value?.close();
}
</script>

<template>
  <Modal ref="modal" :title="i18n.t('page.new.title')" window-classes="w-120">
    <div class="flex flex-col">
      <InputText
        v-model.trim="name"
        :label="i18n.t('page.new.name')"
        counter
        :maxlength="useBackendData.validations.project.pageName.max"
        :minlength="useBackendData.validations.project.pageName.min"
        :rules="rules"
      />
      <InputSelect v-model="parent" :values="pageRoots" :label="i18n.t('page.new.parent')" class="pt-2 pb-1" />
    </div>
    <div>
      <Button class="mt-3" :disabled="loading" @click="createPage">{{ i18n.t("general.create") }}</Button>
    </div>
    <template #activator="{ on }">
      <Button v-bind="$attrs" class="mr-1 h-[32px]" size="small" v-on="on">
        <IconMdiPlus />
      </Button>
    </template>
  </Modal>
</template>
