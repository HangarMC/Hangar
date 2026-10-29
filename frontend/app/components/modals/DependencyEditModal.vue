<script lang="ts" setup>
import { cloneDeep } from "lodash-es";
import { useVuelidate } from "@vuelidate/core";
import { NamedPermission } from "#shared/types/backend";
import type { HangarProject, PlatformData, Version } from "#shared/types/backend";
import { DependencyEditor } from "#components";

const props = defineProps<{
  project: HangarProject;
  version: Version;
  platform: PlatformData;
}>();

const i18n = useI18n();
const router = useRouter();
const v = useVuelidate();

const loading = ref(false);
const editor = useTemplateRef("editor");
const modal = useTemplateRef("modal");
const pluginDependencies = ref<Version["pluginDependencies"]>({});

async function save() {
  if (!editor.value || editor.value.hasDuplicates || !(await v.value.$validate())) {
    return;
  }
  loading.value = true;
  try {
    for (const dep of editor.value.dependencies) {
      if (dep.mode === "file") {
        delete dep.externalUrl;
      }
    }
    await useInternalApi(`versions/version/${props.project.id}/${props.version.id}/savePluginDependencies`, "post", {
      platform: props.platform.enumName,
      pluginDependencies: editor.value.dependencies,
    });
    await router.go(0);
  } catch (err) {
    handleRequestError(err);
  }
  loading.value = false;
}

onMounted(() =>
  watch(
    () => modal.value?.isOpen,
    (val) => {
      pluginDependencies.value = val ? cloneDeep(props.version.pluginDependencies) : ({} as Version["pluginDependencies"]);
      if (editor.value) {
        editor.value.reset();
      }
    }
  )
);
</script>

<template>
  <Modal ref="modal" :title="i18n.t('version.deps.title', [platform.name])" window-classes="w-180">
    <p class="mb-3 text-sm text-gray-secondary">{{ i18n.t("version.deps.sub") }}</p>
    <DependencyEditor ref="editor" :platform="platform.enumName" :plugin-dependencies="pluginDependencies" />
    <template #activator="{ on }">
      <Button
        v-if="hasPerms(NamedPermission.EditVersion)"
        variant="ghost"
        tone="neutral"
        size="sm"
        icon-only
        :title="i18n.t('general.edit')"
        :aria-label="i18n.t('general.edit')"
        v-on="on"
        ><IconMdiPencil
      /></Button>
    </template>
    <template #footer="{ on }">
      <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
      <Button :disabled="loading || v.$error || editor?.hasDuplicates" :loading @click="save">{{ i18n.t("general.save") }}</Button>
    </template>
  </Modal>
</template>
