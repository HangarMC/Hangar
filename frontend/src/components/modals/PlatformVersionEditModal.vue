<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import type { HangarProject, HangarVersion, IPlatform } from "hangar-internal";
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useVuelidate } from "@vuelidate/core";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import type { Platform } from "~/types/enums";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useInternalApi } from "~/composables/useApi";
import VersionSelector from "~/components/VersionSelector.vue";
import { minLength, required } from "~/composables/useValidationHelpers";

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
const selectedVersions = ref(projectVersion.value?.platformDependencies[props.platform.name.toUpperCase() as Platform]);
const v = useVuelidate();

async function save() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/savePlatformVersions`, "post", {
    platform: props.platform.name.toUpperCase(),
    versions: selectedVersions.value,
  })
    .catch((e) => handleRequestError(e))
    .then(async () => {
      await router.go(0);
    })
    .finally(() => {
      loading.value = false;
    });
}
</script>

<template>
  <Modal :title="i18n.t('version.edit.platformVersions', [platform.name])" window-classes="w-200">
    <VersionSelector
      v-model="selectedVersions"
      :versions="platform.possibleVersions"
      open
      :rules="[required('Select at least one platform version!'), minLength()(1)]"
    />

    <Button class="mt-3" :disabled="loading" @click="save">{{ i18n.t("general.save") }}</Button>
    <template #activator="{ on }">
      <Button class="text-sm" v-bind="$attrs" v-on="on"><IconMdiPencil /></Button>
    </template>
  </Modal>
</template>
