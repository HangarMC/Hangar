<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/lib/components/design/Button.vue";
import Modal from "~/lib/components/modals/Modal.vue";
import { HangarProject, HangarVersion } from "hangar-internal";
import { useContext } from "vite-ssr/vue";
import { computed, ref } from "vue";
import { Platform } from "~/types/enums";
import { useBackendDataStore } from "~/store/backendData";
import { useRoute, useRouter } from "vue-router";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useInternalApi } from "~/composables/useApi";
import InputTag from "~/lib/components/ui/InputTag.vue";

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
    <InputTag v-model="selectedVersions" :options="platform?.possibleVersions" />

    <Button class="mt-3" :disabled="loading" @click="save">{{ i18n.t("general.save") }}</Button>
    <template #activator="{ on }">
      <Button class="text-sm" v-on="on"><IconMdiPencil /></Button>
    </template>
  </Modal>
</template>
