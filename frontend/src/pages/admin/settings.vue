<script lang="ts" setup>
import type { Platform, PlatformVersion } from "~/types/backend";

definePageMeta({
  globalPermsRequired: ["ManualValueChanges"],
});

const i18n = useI18n();
const route = useRoute("admin-settings");
const router = useRouter();
const notification = useNotificationStore();

const platformMap = useBackendData.platforms;
const platforms = platformMap ? [...platformMap.values()] : [];
const loading = ref<boolean>(false);

useSeo(computed(() => ({ title: i18n.t("platformVersions.title"), route })));

const fullVersions = ref<Record<Platform, string[]>>({
  PAPER: [],
  WATERFALL: [],
  VELOCITY: [],
});
resetPlatformVersions();

function versions(versions: PlatformVersion[]): string[] {
  const fullVersions = [];
  for (const version of versions) {
    if (version.subVersions.length > 0) {
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
  } catch (err: any) {
    loading.value = false;
    handleRequestError(err);
  }
}

function resetPlatformVersions() {
  for (const platform of useBackendData.platforms.values()) {
    fullVersions.value[platform.enumName] = versions(platform.platformVersions);
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
  } catch (err: any) {
    loading.value = false;
    handleRequestError(err);
  }
}

async function rescanSafeLinks() {
  loading.value = true;
  try {
    const errors = await useInternalApi("admin/scanSafeLinks", "post", undefined, { timeout: 120_000 });
    console.log(errors);
    notification.success("Updated!");
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.value = false;
}

const forceFixAvatars = ref(false);
async function fixAvatars() {
  loading.value = true;
  try {
    const result = await useInternalApi<number>("admin/fixAvatars?force=" + forceFixAvatars.value, "post", undefined, { timeout: 120_000 });
    notification.success("Updated " + result + " avatars!");
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.value = false;
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
              <InputTag v-model="fullVersions[platform.enumName]" />
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
              <InputText v-model.number="role.rank as unknown as string" :rules="[integer()]" />
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
      <Button button-type="red" class="mt-2" :disabled="loading" @click="rescanSafeLinks">Run</Button>
    </Card>
    <Card class="mt-5">
      <PageTitle>Fix broken (user) avatars </PageTitle>
      Goes thru all users with avatar_url = null and tries to fix it
      <br />
      <InputCheckbox v-model="forceFixAvatars" label="Force" />
      <Button button-type="red" class="mt-2" :disabled="loading" @click="fixAvatars">Run</Button>
    </Card>
  </div>
</template>
