<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { computed, ref } from "vue";
import { cloneDeep, isEqual } from "lodash-es";
import { useHead } from "@vueuse/head";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useBackendDataStore } from "~/store/backendData";
import { useInternalApi } from "~/composables/useApi";
import InputTag from "~/lib/components/ui/InputTag.vue";
import Button from "~/lib/components/design/Button.vue";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import Card from "~/lib/components/design/Card.vue";
import Table from "~/lib/components/design/Table.vue";
import { useSeo } from "~/composables/useSeo";
import { useNotificationStore } from "~/lib/store/notification";
import { definePageMeta } from "#imports";

definePageMeta({
  globalPermsRequired: ["MANUAL_VALUE_CHANGES"],
});

const i18n = useI18n();
const route = useRoute();
const router = useRouter();
const notification = useNotificationStore();

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
  try {
    await useInternalApi("admin/platformVersions", true, "post", data);
    notification.success(i18n.t("platformVersions.success"));
    router.go(0);
  } catch (e: any) {
    loading.value = false;
    handleRequestError(e, i18n);
  }
}

function reset() {
  platforms.value = cloneDeep(originalPlatforms);
}

const hasChanged = computed(() => !isEqual(platforms.value, originalPlatforms));
</script>

<template>
  <div>
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
  </div>
</template>
