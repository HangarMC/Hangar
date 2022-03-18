<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { computed, ref } from "vue";
import { useBackendDataStore } from "~/store/backendData";
import { useInternalApi } from "~/composables/useApi";
import { cloneDeep, isEqual } from "lodash-es";
import InputTag from "~/components/InputTag.vue";

const ctx = useContext();
const i18n = useI18n();
const { params } = useRoute();

const platformMap = useBackendDataStore().platforms;
const originalPlatforms = platformMap ? [...platformMap.values()] : [];
const platforms = ref(cloneDeep(originalPlatforms));
const loading = ref<boolean>(false);
console.log("load", platforms.value, originalPlatforms);

async function save() {
  loading.value = true;
  const data: { [key: string]: string[] } = {};
  for (const pl of platforms.value || []) {
    data[pl.enumName] = pl.possibleVersions;
  }
  const result = await useInternalApi("admin/platformVersions", true, "post", data).catch((e) => handleRequestError(e, ctx, i18n));
  if (result) {
    // TODO success notification
    // this.$util.success(i18n.t('platformVersions.success'));
    // this.$nuxt.refresh();
  } else {
    loading.value = false;
  }
}

function reset() {
  platforms.value = cloneDeep(originalPlatforms);
  console.log("reset", platforms.value, originalPlatforms);
}

const hasChanged = computed(() => !isEqual(platforms.value, originalPlatforms));
</script>

<template>
  <h1>{{ i18n.t("platformVersions.title") }}</h1>
  <table>
    <thead>
      <tr>
        <th>{{ i18n.t("platformVersions.platform") }}</th>
        <th>{{ i18n.t("platformVersions.versions") }}</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="platform in platforms" :key="platform.name">
        <td>{{ platform.name }}</td>
        <td><InputTag v-model="platform.possibleVersions"></InputTag></td>
      </tr>
    </tbody>
  </table>
  <button :disabled="!hasChanged" @click="reset">{{ i18n.t("general.reset") }}</button>
  <button :disabled="loading || !hasChanged" @click="save">{{ i18n.t("platformVersions.saveChanges") }}</button>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["MANUAL_VALUE_CHANGES"]
</route>
