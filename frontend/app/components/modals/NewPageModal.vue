<script lang="ts" setup>
import type { Option } from "#shared/types/components/ui/InputSelect";
import type { HangarProjectPage } from "#shared/types/backend";

const props = defineProps<{
  projectId: number;
  pages: HangarProjectPage[];
}>();

const i18n = useI18n();
const route = useRoute("user-project");
const router = useRouter();
const v = useVuelidate();

const updateProjectPagesCallback = inject<(pages: HangarProjectPage[]) => void>("updateProjectPages");
const modal = useTemplateRef("modal");

const pageRoots = computed(() => [{ value: -1, text: "<none>" }, ...flatDeep(props.pages, "")]);
const loading = ref<boolean>(false);

const body = reactive({
  projectId: props.projectId,
  name: "",
  parentId: undefined as number | undefined,
});
const rules = [
  required(),
  maxLength()(useBackendData.validations.project.pageName.max!),
  minLength()(useBackendData.validations.project.pageName.min!),
  pattern()(useBackendData.validations.project.pageName.regex!),
  validPageName()(body),
];

function flatDeep(pages: HangarProjectPage[], prefix: string): Option<number>[] {
  let ps: Option<number>[] = [];
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
      name: body.name,
      parentId: body.parentId === -1 ? undefined : body.parentId,
    });

    body.name = "";
    body.parentId = undefined;

    if (updateProjectPagesCallback) {
      updateProjectPagesCallback(await useInternalApi<HangarProjectPage[]>(`pages/list/${props.projectId}`, "get"));
    }

    await router.push(`/${route.params.user}/${route.params.project}/pages/${slug}`);
  } catch (err) {
    handleRequestError(err);
  }
  loading.value = false;

  modal.value?.close();
}
</script>

<template>
  <Modal ref="modal" :title="i18n.t('page.new.title')" window-classes="w-120">
    <div class="flex flex-col">
      <InputText
        v-model.trim="body.name"
        :label="i18n.t('page.new.name')"
        counter
        :maxlength="useBackendData.validations.project.pageName.max"
        :minlength="useBackendData.validations.project.pageName.min"
        :rules="rules"
      />
      <InputSelect v-model="body.parentId" :values="pageRoots" :label="i18n.t('page.new.parent')" class="pt-2 pb-1" />
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
