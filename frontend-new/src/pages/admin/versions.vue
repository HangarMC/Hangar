<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { computed, ref } from "vue";
import { useBackendDataStore } from "~/store/backendData";
import { useInternalApi } from "~/composables/useApi";
import { cloneDeep, isEqual } from "lodash-es";
import InputTag from "~/components/ui/InputTag.vue";
import Button from "~/components/design/Button.vue";
import PageTitle from "~/components/design/PageTitle.vue";
import Card from "~/components/design/Card.vue";
import Table from "~/components/design/Table.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useNotificationStore } from "~/store/notification";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const notifcation = useNotificationStore();

const platformMap = useBackendDataStore().platforms;
const originalPlatforms = platformMap ? [...platformMap.values()] : [];
const platforms = ref(cloneDeep(originalPlatforms));
const loading = ref<boolean>(false);

useHead(useSeo(i18n.t("platformVersions.title"), null, route, null));

async function save() {
  loading.value = true;
  const data: { [key: string]: string[] } = {};
  for (const pl of platforms.value || []) {
    data[pl.enumName] = pl.possibleVersions;
  }
  const result = await useInternalApi("admin/platformVersions", true, "post", data).catch((e) => handleRequestError(e, ctx, i18n));
  if (result) {
    notifcation.success(i18n.t("platformVersions.success"));
    // this.$nuxt.refresh(); // TODO refresh
  } else {
    loading.value = false;
  }
}

function reset() {
  platforms.value = cloneDeep(originalPlatforms);
}

const hasChanged = computed(() => !isEqual(platforms.value, originalPlatforms));
</script>

<template>
  <PageTitle>{{ i18n.t("platformVersions.title") }}</PageTitle>
  <Card>
    <Table class="w-full">
      <thead>
        <tr>
          <th>{{ i18n.t("platformVersions.platform") }}</th>
          <th>{{ i18n.t("platformVersions.versions") }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="platform in platforms" :key="platform.name">
          <td>{{ platform.name }}</td>
          <td>
            <InputTag v-model="platform.possibleVersions"></InputTag>
          </td>
        </tr>
      </tbody>
    </Table>

    <template #footer>
      <span class="flex justify-end">
        <Button :disabled="!hasChanged" @click="reset">{{ i18n.t("general.reset") }}</Button>
        <Button :disabled="loading || !hasChanged" class="ml-2" @click="save"> {{ i18n.t("platformVersions.saveChanges") }}</Button>
      </span>
    </template>
  </Card>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["MANUAL_VALUE_CHANGES"]
</route>
