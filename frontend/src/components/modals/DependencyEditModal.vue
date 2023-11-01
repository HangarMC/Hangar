<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import type { HangarProject, HangarVersion, IPlatform } from "hangar-internal";
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import type { DependencyVersion, PluginDependency } from "hangar-api";
import { cloneDeep } from "lodash-es";
import { hasPerms } from "~/composables/usePerm";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import type { Platform } from "~/types/enums";
import { NamedPermission } from "~/types/enums";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useInternalApi } from "~/composables/useApi";
import DependencyTable from "~/components/projects/DependencyTable.vue";

const props = defineProps<{
  project: HangarProject;
  version: HangarVersion;
  platform: IPlatform;
}>();

const i18n = useI18n();
const route = useRoute();
const router = useRouter();

const projectVersion = computed(() => {
  return props.version;
});

const loading = ref(false);
const depTable = ref();
const modal = ref();
const formVersion = ref<DependencyVersion>({
  pluginDependencies: {} as Record<Platform, PluginDependency[]>,
});

const validInput = computed(() => {
  if (depTable.value && hasInvalidDependency(depTable.value.dependencies)) {
    return false;
  }
  for (const platform in formVersion.value.pluginDependencies) {
    if (hasInvalidDependency(formVersion.value.pluginDependencies[platform as Platform])) {
      return false;
    }
  }
  return true;
});

function hasInvalidDependency(dependencies: PluginDependency[]) {
  return dependencies.some((dependency) => {
    // noinspection SuspiciousTypeOfGuard
    if (dependency.namespace && typeof dependency.namespace === "string") {
      // TODO: should get proper validation checks on input
      return true;
    }
    return !dependency.name || (!dependency.externalUrl && !dependency.namespace);
  });
}

async function save() {
  loading.value = true;
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/savePluginDependencies`, "post", {
      platform: props.platform.enumName,
      pluginDependencies: depTable.value.dependencies,
    });
    await router.go(0);
  } catch (e) {
    handleRequestError(e);
  }
  loading.value = false;
}

onMounted(() =>
  watch(
    () => modal.value.isOpen,
    (val) => {
      formVersion.value.pluginDependencies =
        val && projectVersion.value ? cloneDeep(projectVersion.value.pluginDependencies) : ({} as Record<Platform, PluginDependency[]>);
      if (depTable.value) {
        depTable.value.reset();
      }
    }
  )
);
</script>

<template>
  <Modal ref="modal" :title="i18n.t('version.edit.pluginDeps', [platform.name])" window-classes="w-200">
    <DependencyTable ref="depTable" :platform="platform.enumName" :version="formVersion" />

    <Button button-type="primary" class="mt-3" :disabled="loading || !validInput" @click="save">{{ i18n.t("general.save") }}</Button>
    <template #activator="{ on }">
      <Button v-if="hasPerms(NamedPermission.EDIT_VERSION)" class="text-sm" v-on="on"><IconMdiPencil /></Button>
    </template>
  </Modal>
</template>
