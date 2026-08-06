<script lang="ts" setup>
import { cloneDeep } from "lodash-es";
import { useVuelidate } from "@vuelidate/core";
import { NamedPermission } from "#shared/types/backend";
import type { HangarProject, PlatformData, Version } from "#shared/types/backend";
import { DependencyTable } from "#components";

const props = defineProps<{
  project: HangarProject;
  version: Version;
  platform: PlatformData;
}>();

const i18n = useI18n();
const router = useRouter();
const v = useVuelidate();

const projectVersion = computed(() => {
  return props.version;
});

const loading = ref(false);
const depTable = useTemplateRef("depTable");
const modal = useTemplateRef("modal");
const pluginDependencies = ref<Version["pluginDependencies"]>({});

async function save() {
  if (!(await v.value.$validate())) {
    return;
  }
  loading.value = true;
  try {
    if (!depTable.value) return;
    for (const dep of depTable.value.dependencies) {
      if (dep.mode === "file") {
        delete dep.externalUrl;
      }
    }
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/savePluginDependencies`, "post", {
      platform: props.platform.enumName,
      pluginDependencies: depTable.value.dependencies,
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
      pluginDependencies.value = val && projectVersion.value ? cloneDeep(projectVersion.value.pluginDependencies) : ({} as Version["pluginDependencies"]);
      if (depTable.value) {
        depTable.value.reset();
      }
    }
  )
);
</script>

<template>
  <Modal ref="modal" :title="i18n.t('version.edit.pluginDeps', [platform.name])" window-classes="w-200">
    <DependencyTable ref="depTable" :platform="platform.enumName" :plugin-dependencies="pluginDependencies" />
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
      <Button :disabled="loading || v.$error" @click="save">{{ i18n.t("general.save") }}</Button>
    </template>
  </Modal>
</template>
