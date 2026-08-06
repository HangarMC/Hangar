<script lang="ts" setup>
import type { Platform, HangarProject, Version, PlatformData } from "#shared/types/backend";

const props = defineProps<{
  project: HangarProject;
  version: Version;
  platform: PlatformData;
}>();

const i18n = useI18n();
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
  <Modal :title="i18n.t('version.edit.platformVersions', [platform.name])" window-classes="w-200">
    <VersionSelector
      v-model="selectedVersions"
      :versions="platform.platformVersions"
      open
      :rules="[required('Select at least one platform version!'), minLength()(1)]"
    />
    <template #activator="{ on }">
      <Button variant="ghost" tone="neutral" size="sm" icon-only v-bind="$attrs" :title="i18n.t('general.edit')" :aria-label="i18n.t('general.edit')" v-on="on"
        ><IconMdiPencil
      /></Button>
    </template>
    <template #footer="{ on }">
      <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
      <Button :disabled="loading" @click="save">{{ i18n.t("general.save") }}</Button>
    </template>
  </Modal>
</template>
