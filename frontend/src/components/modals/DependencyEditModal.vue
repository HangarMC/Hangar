<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { hasPerms } from "~/composables/usePerm";
import Button from "~/lib/components/design/Button.vue";
import Modal from "~/lib/components/modals/Modal.vue";
import { HangarProject, HangarVersion } from "hangar-internal";
import { useContext } from "vite-ssr/vue";
import { computed, onMounted, ref, watch } from "vue";
import { NamedPermission, Platform } from "~/types/enums";
import { useBackendDataStore } from "~/store/backendData";
import { useRoute, useRouter } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useInternalApi } from "~/composables/useApi";
import DependencyTable from "~/components/projects/DependencyTable.vue";
import { DependencyVersion, PluginDependency } from "hangar-api";
import { cloneDeep } from "lodash-es";

const props = defineProps<{
  project: HangarProject;
  version: HangarVersion;
}>();

const i18n = useI18n();
const ctx = useContext();
const route = useRoute();
const router = useRouter();
const backendData = useBackendDataStore();

const platform = computed(() => {
  return backendData.platforms.get((route.params.platform as string).toUpperCase() as Platform);
});
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
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/savePluginDependencies`, true, "post", {
      platform: platform.value?.name?.toUpperCase(),
      pluginDependencies: depTable.value.dependencies,
    });
    await router.go(0);
  } catch (e) {
    handleRequestError(e, ctx, i18n);
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
  <Modal ref="modal" :title="i18n.t('version.edit.platformVersions', [platform?.name])" window-classes="w-200">
    <DependencyTable ref="depTable" :platform="platform?.enumName" :version="formVersion" />

    <Button button-type="primary" class="mt-3" :disabled="loading || !validInput" @click="save">{{ i18n.t("general.save") }}</Button>
    <template #activator="{ on }">
      <Button v-if="hasPerms(NamedPermission.EDIT_VERSION)" class="text-sm" v-on="on"><IconMdiPencil /></Button>
    </template>
  </Modal>
</template>
