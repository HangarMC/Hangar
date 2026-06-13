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
const notification = useNotificationStore();
const v = useVuelidate();

const projectVersion = computed(() => {
  return props.version;
});

const loading = ref(false);
const depTable = useTemplateRef("depTable");
const modal = useTemplateRef("modal");
const pluginDependencies = ref<Version["pluginDependencies"]>({});

async function save(close: () => void) {
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
    notification.success("Saved!");
    close();
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
  <Modal
    ref="modal"
    :title="i18n.t('version.edit.pluginDeps', [platform.name])"
    window-classes="w-full max-w-2xl !rounded-xl border border-gray-200 dark:border-gray-800 shadow-lg !bg-white dark:!bg-charcoal-900"
    close-button-right
  >
    <template #default="{ on }">
      <p class="mb-4 text-sm leading-relaxed text-gray">Add projects or external plugins that this {{ platform.name }} release depends on.</p>

      <div class="max-h-[28rem] overflow-y-auto">
        <DependencyTable ref="depTable" :platform="platform.enumName" :plugin-dependencies="pluginDependencies" />
      </div>

      <div class="mt-4 flex justify-end gap-2 pt-4">
        <Button button-type="secondary" size="medium" :disabled="loading" @click="on.click">Cancel</Button>
        <Button size="medium" :loading="loading" :disabled="loading || v.$error" @click="save(on.click)">
          <IconMdiContentSave class="mr-1" />
          Save
        </Button>
      </div>
    </template>
    <template #activator="{ on }">
      <Button
        v-if="hasPerms(NamedPermission.EditVersion)"
        button-type="secondary"
        class="!h-9 !w-9 !p-0 text-sm"
        aria-label="Edit plugin dependencies"
        v-on="on"
      >
        <IconMdiPencil />
      </Button>
    </template>
  </Modal>
</template>
