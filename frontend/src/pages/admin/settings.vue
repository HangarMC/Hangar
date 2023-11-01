<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import type { Ref } from "vue";
import { ref } from "vue";
import { useHead } from "@unhead/vue";
import type { PlatformVersion } from "hangar-internal";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useBackendData } from "~/store/backendData";
import { useInternalApi } from "~/composables/useApi";
import InputTag from "~/components/ui/InputTag.vue";
import Button from "~/components/design/Button.vue";
import PageTitle from "~/components/design/PageTitle.vue";
import Card from "~/components/design/Card.vue";
import Table from "~/components/design/Table.vue";
import { useSeo } from "~/composables/useSeo";
import { useNotificationStore } from "~/store/notification";
import { definePageMeta } from "#imports";
import type { Platform } from "~/types/enums";
import InputText from "~/components/ui/InputText.vue";
import { integer } from "~/composables/useValidationHelpers";

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
resetPlatformVersions();

function versions(versions: PlatformVersion[]): string[] {
  const fullVersions = [];
  for (const version of versions) {
    if (version.subVersions.length !== 0) {
      fullVersions.push(...version.subVersions);
    } else {
      fullVersions.push(version.version);
    }
  }
  return fullVersions;
}

async function savePlatformVersions() {
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

function resetPlatformVersions() {
  for (const platform of useBackendData.platforms.values()) {
    fullVersions.value[platform.enumName] = versions(platform.possibleVersions);
  }
}

const roles = ref([...useBackendData.orgRoles, ...useBackendData.globalRoles, ...useBackendData.projectRoles]);
async function saveRoles() {
  loading.value = true;
  const data = [];
  for (const role of roles.value) {
    data.push({ roleId: role.roleId, title: role.title, color: role.color, rank: role.rank });
  }
  try {
    await useInternalApi("admin/roles", "post", roles.value);
    notification.success("Updated roles!");
    router.go(0);
  } catch (e: any) {
    loading.value = false;
    handleRequestError(e);
  }
}

async function rescanSafeLinks() {
  loading.value = true;
  try {
    const errors = await useInternalApi("admin/scanSafeLinks", "post", undefined, { timeout: 120_000 });
    console.log(errors);
    notification.success("Updated!");
  } catch (e: any) {
    loading.value = false;
    handleRequestError(e);
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
          Updates need a rebuild + redeploy to take affect!
          <Button @click="resetPlatformVersions">{{ i18n.t("general.reset") }}</Button>
          <Button :disabled="loading" @click="savePlatformVersions"> {{ i18n.t("platformVersions.saveChanges") }}</Button>
        </span>
      </template>
    </Card>
    <Card class="mt-5">
      <PageTitle>Roles</PageTitle>
      <Table class="w-full">
        <thead>
          <tr>
            <th>Title</th>
            <th>Category</th>
            <th>Color</th>
            <th>Rank</th>
            <th>As tag</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="role in roles" :key="role.roleId">
            <td>
              <InputText v-model="role.title" />
            </td>
            <td>{{ role.roleCategory }}</td>
            <td>
              <InputText v-model="role.color" />
            </td>
            <td>
              <InputText v-model="role.rank" :rules="[integer()]" />
            </td>
            <td>
              <Tag :color="{ background: role.color }" :name="role.title" class="ml-1" />
            </td>
          </tr>
        </tbody>
      </Table>

      <template #footer>
        <span class="flex justify-end items-center gap-2">
          <Button :disabled="loading" @click="saveRoles"> {{ i18n.t("platformVersions.saveChanges") }}</Button>
        </span>
      </template>
    </Card>
    <Card class="mt-5">
      <PageTitle>Rescan versions for safe links</PageTitle>
      Approves all versions with only external links that are safe as per config
      <br />
      <Button button-type="red" :disabled="loading" @click="rescanSafeLinks">Run</Button>
    </Card>
  </div>
</template>
