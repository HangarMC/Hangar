<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { HangarProject, HangarVersion } from "hangar-internal";
import { useContext } from "vite-ssr/vue";
import { computed, ref } from "vue";
import { Platform } from "~/types/enums";
import { useBackendDataStore } from "~/store/backendData";
import { useRoute, useRouter } from "vue-router";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useInternalApi } from "~/composables/useApi";

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
const selectedVersions = ref(projectVersion.value?.platformDependencies[platform.value?.name.toUpperCase() as Platform]);

async function save() {
  loading.value = true;
  useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/savePlatformVersions`, true, "post", {
    platform: platform.value?.name?.toUpperCase(),
    versions: selectedVersions.value,
  })
    .catch((e) => handleRequestError(e, ctx, i18n))
    .then(async () => {
      await router.go(0);
    })
    .finally(() => {
      loading.value = false;
    });
}
</script>

<template>
  <Modal :title="i18n.t('version.edit.platformVersions', [platform?.name])">
    <template #default="{ on }">
      <ul>
        <li v-for="version in platform?.possibleVersions" :key="version">
          <InputCheckbox v-model="selectedVersions" :label="version" :value="version" />
        </li>
      </ul>

      <Button button-type="secondary" class="mt-2" v-on="on">{{ i18n.t("general.close") }}</Button>
      <Button button-type="primary" class="mt-2 ml-2" :disabled="loading" @click="save">{{ i18n.t("general.save") }}</Button>
    </template>
    <template #activator="{ on }">
      <Button v-on="on"><IconMdiPencil /></Button>
    </template>
  </Modal>
</template>
