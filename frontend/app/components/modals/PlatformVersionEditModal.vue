<script lang="ts" setup>
import type { Platform, HangarProject, Version, PlatformData } from "#shared/types/backend";

const props = defineProps<{
  project: HangarProject;
  version: Version;
  platform: PlatformData;
}>();

const i18n = useI18n();
const router = useRouter();
const notification = useNotificationStore();

const projectVersion = computed(() => {
  return props.version;
});

const loading = ref(false);
const selectedVersions = ref(projectVersion.value?.platformDependencies[props.platform.name.toUpperCase() as Platform]);
const search = ref("");
const showAllVersions = ref(false);
const v = useVuelidate();

async function save(close: () => void) {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/savePlatformVersions`, "post", {
      platform: props.platform.name.toUpperCase(),
      versions: selectedVersions.value,
    });
    notification.success("Saved!");
    close();
    await router.go(0);
  } catch (err) {
    handleRequestError(err);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <Modal
    :title="i18n.t('version.edit.platformVersions', [platform.name])"
    window-classes="w-full max-w-2xl !rounded-xl border border-gray-200 dark:border-gray-800 shadow-lg !bg-white dark:!bg-charcoal-900"
    close-button-right
  >
    <template #default="{ on }">
      <p class="mb-4 text-sm leading-relaxed text-gray">Select every {{ platform.name }} version supported by this release.</p>

      <div class="mb-3 flex flex-col gap-2 sm:flex-row sm:items-center">
        <div class="relative min-w-0 flex-grow">
          <IconMdiMagnify class="pointer-events-none absolute top-3 left-3 text-gray" />
          <input
            v-model.trim="search"
            type="search"
            class="h-10.5 w-full rounded-lg border border-transparent bg-gray-100 px-9 py-2 outline-none transition-colors hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-600"
            placeholder="Search versions"
          />
        </div>
        <Button button-type="secondary" size="medium" class="flex-shrink-0" @click="showAllVersions = !showAllVersions">
          {{ showAllVersions ? "Group versions" : "Show patch versions" }}
        </Button>
      </div>

      <div class="max-h-96 overflow-y-auto rounded-lg border border-gray-200 bg-gray-50 p-2 dark:border-gray-800 dark:bg-charcoal-600">
        <VersionSelector
          v-model="selectedVersions"
          :versions="platform.platformVersions"
          :version-search-query="search"
          :show-all-versions="showAllVersions"
          open
          :rules="[required('Select at least one platform version!'), minLength()(1)]"
        />
      </div>

      <div class="mt-2 flex flex-col gap-3 pt-2 sm:flex-row sm:items-center sm:justify-between">
        <span class="text-sm text-gray">{{ selectedVersions?.length || 0 }} versions selected</span>
        <div class="flex justify-end gap-2">
          <Button button-type="secondary" size="medium" :disabled="loading" @click="on.click">Cancel</Button>
          <Button size="medium" :loading="loading" :disabled="loading || !selectedVersions?.length" @click="save(on.click)">
            <IconMdiContentSave class="mr-1" />
            Save
          </Button>
        </div>
      </div>
    </template>
    <template #activator="{ on }">
      <Button button-type="secondary" class="!h-9 !w-9 !p-0 text-sm" aria-label="Edit platform versions" v-bind="$attrs" v-on="on">
        <IconMdiPencil />
      </Button>
    </template>
  </Modal>
</template>
