<script lang="ts" setup>
import type { Platform, HangarProject, Version, PlatformData } from "#shared/types/backend";

const props = defineProps<{
  project: HangarProject;
  version: Version;
  platform: PlatformData;
}>();

const i18n = useI18n();
const router = useRouter();

const loading = ref(false);
const selectedVersions = ref(props.version.platformDependencies[props.platform.name.toUpperCase() as Platform]);
const v = useVuelidate();

async function save() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  useInternalApi(`versions/version/${props.project.id}/${props.version.id}/savePlatformVersions`, "post", {
    platform: props.platform.name.toUpperCase(),
    versions: selectedVersions.value,
  })
    .catch((err) => handleRequestError(err))
    .then(async () => {
      await router.go(0);
    })
    .finally(() => {
      loading.value = false;
    });
}
</script>

<template>
  <Modal :title="i18n.t('version.platformSelect.title', [platform.name])" window-classes="w-180">
    <p class="mb-3 text-sm text-gray-secondary">{{ i18n.t("version.platformSelect.sub", [platform.name]) }}</p>
    <VersionSelector
      v-model="selectedVersions"
      :versions="platform.platformVersions"
      toolbar
      :rules="[required('Select at least one platform version!'), minLength()(1)]"
    />
    <template #activator="{ on }">
      <Button variant="ghost" tone="neutral" size="sm" icon-only v-bind="$attrs" :title="i18n.t('general.edit')" :aria-label="i18n.t('general.edit')" v-on="on"
        ><IconMdiPencil
      /></Button>
    </template>
    <template #footer="{ on }">
      <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
      <Button :disabled="loading" :loading @click="save">{{ i18n.t("general.save") }}</Button>
    </template>
  </Modal>
</template>
