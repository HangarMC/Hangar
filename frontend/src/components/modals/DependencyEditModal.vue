<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { hasPerms } from "~/composables/usePerm";
import Button from "~/lib/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
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
  versions: Map<Platform, HangarVersion>;
}>();

const i18n = useI18n();
const ctx = useContext();
const route = useRoute();
const router = useRouter();
const backendData = useBackendDataStore();

const platform = computed(() => {
  return backendData.platforms?.get((route.params.platform as string).toUpperCase() as Platform);
});
const projectVersion = computed(() => {
  return props.versions.get((route.params.platform as string).toUpperCase() as Platform);
});

const loading = ref(false);
const depTable = ref();
const modal = ref();
const formVersion = ref<DependencyVersion>({
  pluginDependencies: {} as Record<Platform, PluginDependency[]>,
});

async function save() {
  loading.value = true;

  let deps: PluginDependency[] = [];
  if (formVersion.value.pluginDependencies[platform.value?.name?.toUpperCase() as Platform]) {
    deps.push(...formVersion.value.pluginDependencies[platform.value?.name?.toUpperCase() as Platform]);
  }
  deps.push(...depTable.value.newDeps);
  deps = deps.filter((d) => !depTable.value.deletedDeps.includes(d.name));

  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/savePluginDependencies`, true, "post", {
      platform: platform.value?.name?.toUpperCase(),
      pluginDependencies: deps,
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
  <Modal ref="modal" :title="i18n.t('version.edit.platformVersions', [platform?.name])" :small="false">
    <DependencyTable ref="depTable" :platform="platform?.name?.toUpperCase()" :version="formVersion" />

    <Button button-type="primary" class="mt-3" :disabled="loading" @click="save">{{ i18n.t("general.save") }}</Button>
    <template #activator="{ on }">
      <Button v-if="hasPerms(NamedPermission.EDIT_VERSION)" class="text-sm" v-on="on"><IconMdiPencil /></Button>
    </template>
  </Modal>
</template>
