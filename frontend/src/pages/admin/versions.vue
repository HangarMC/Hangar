<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { computed, Ref, ref } from "vue";
import { cloneDeep, isEqual } from "lodash-es";
import { useHead } from "@vueuse/head";
import { PlatformVersion } from "hangar-internal";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useBackendData } from "~/store/backendData";
import { useInternalApi } from "~/composables/useApi";
import InputTag from "~/lib/components/ui/InputTag.vue";
import Button from "~/lib/components/design/Button.vue";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import Card from "~/lib/components/design/Card.vue";
import Table from "~/lib/components/design/Table.vue";
import { useSeo } from "~/composables/useSeo";
import { useNotificationStore } from "~/lib/store/notification";
import { definePageMeta } from "#imports";
import { Platform } from "~/types/enums";

definePageMeta({
  globalPermsRequired: ["MANUAL_VALUE_CHANGES"],
});

const i18n = useI18n();
const route = useRoute();
const router = useRouter();
const notification = useNotificationStore();

const platformMap = useBackendData.platforms;
const platforms = platformMap ? [...platformMap.values()] : [];
const loading = ref<boolean>(false);

useHead(useSeo(i18n.t("platformVersions.title"), null, route, null));

const fullVersions: Ref<Record<Platform, string[]>> = ref({
  PAPER: [],
  WATERFALL: [],
  VELOCITY: [],
});
reset();

function versions(versions: PlatformVersion[]): string[] {
  const fullVersions = [];
  for (const version of versions) {
    fullVersions.push(version.version, ...version.subVersions);
  }
  return fullVersions;
}

async function save() {
  loading.value = true;
  const data: { [key: string]: string[] } = {};
  for (const pl of platforms || []) {
    data[pl.enumName] = fullVersions.value[pl.enumName];
  }
  try {
    await useInternalApi("admin/platformVersions", "post", data);
    notification.success(i18n.t("platformVersions.success"));
    router.go(0);
  } catch (e: any) {
    loading.value = false;
    handleRequestError(e);
  }
}

function reset() {
  for (const platform of useBackendData.platforms.values()) {
    fullVersions.value[platform.enumName] = versions(platform.possibleVersions);
  }
}
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
              <InputTag v-model="fullVersions[platform.enumName]"></InputTag>
            </td>
          </tr>
        </tbody>
      </Table>

      <template #footer>
        <span class="flex justify-end items-center gap-2">
          Updates may take a while to take effect!
          <Button @click="reset">{{ i18n.t("general.reset") }}</Button>
          <Button :disabled="loading" @click="save"> {{ i18n.t("platformVersions.saveChanges") }}</Button>
        </span>
      </template>
    </Card>
  </div>
</template>
