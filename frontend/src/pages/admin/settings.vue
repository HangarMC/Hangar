<script lang="ts" setup>
import type { AnnouncementTable, GlobalNotificationTable, Platform, PlatformVersion } from "~/types/backend";
import { useVisiblePlatforms } from "~/composables/useGlobalData";

definePageMeta({
  globalPermsRequired: ["ManualValueChanges"],
});

const i18n = useI18n();
const route = useRoute("admin-settings");
const router = useRouter();
const notification = useNotificationStore();
const globalData = useGlobalData();
const authStore = useAuthStore();

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
  for (const pl of useVisiblePlatforms.value) {
    data[pl.enumName] = fullVersions.value[pl.enumName];
  }
  try {
    await useInternalApi("globalData/platformVersions", "post", data);
    notification.success(i18n.t("platformVersions.success"));
    router.go(0);
  } catch (err: any) {
    loading.value = false;
    handleRequestError(err);
  }
}

function resetPlatformVersions() {
  if (!globalData.value?.platforms) {
    return;
  }
  for (const platform of globalData.value.platforms) {
    fullVersions.value[platform.enumName] = versions(platform.platformVersions);
  }
}

const roles = ref([...useBackendData.orgRoles, ...useBackendData.globalRoles, ...useBackendData.projectRoles]);
async function saveRoles() {
  loading.value = true;
  const data = [];
  for (const role of roles.value) {
    data.push({ roleId: role.roleId, title: role.title, color: role.color, rank: Number(role.rank) });
  }
  try {
    await useInternalApi("admin/roles", "post", data);
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

const announcements = ref(await useInternalApi<AnnouncementTable[]>("globalData/announcements"));
async function updateAnnouncements() {
  loading.value = true;
  try {
    await useInternalApi("globalData/announcements", "post", announcements.value);
    announcements.value = await useInternalApi<AnnouncementTable[]>("globalData/announcements");
    notification.success("Updated announcements!");
  } catch (err: any) {
    loading.value = false;
    handleRequestError(err);
  }
}

const notifications = ref(await useInternalApi<GlobalNotificationTable[]>("globalData/notifications"));
async function updateNotifications() {
  loading.value = true;
  try {
    await useInternalApi("globalData/notifications", "post", notifications.value);
    notifications.value = await useInternalApi<GlobalNotificationTable[]>("globalData/notifications");
    notification.success("Updated notifications!");
  } catch (err: any) {
    loading.value = false;
    handleRequestError(err);
  }
}
</script>

<template>
  <div>
    <PageTitle>Admin Settings</PageTitle>
    <Card>
      <PageTitle>{{ i18n.t("platformVersions.title") }}</PageTitle>
      <Table class="w-full">
        <thead>
          <tr>
            <th>{{ i18n.t("platformVersions.platform") }}</th>
            <th>{{ i18n.t("platformVersions.versions") }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="platform in globalData?.platforms" :key="platform.name">
            <td>{{ platform.name }}</td>
            <td>
              <InputTag v-model="fullVersions[platform.enumName]" />
            </td>
          </tr>
        </tbody>
      </Table>

      <template #footer>
        <span class="flex justify-end items-center gap-2">
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
      <Button button-type="red" class="mt-2" :disabled="loading" @click="rescanSafeLinks">Run</Button>
    </Card>
    <Card class="mt-5">
      <PageTitle>Fix broken (user) avatars </PageTitle>
      Goes thru all users with avatar_url = null and tries to fix it
      <br />
      <InputCheckbox v-model="forceFixAvatars" label="Force" />
      <Button button-type="red" class="mt-2" :disabled="loading" @click="fixAvatars">Run</Button>
    </Card>
    <Card class="mt-5">
      <PageTitle>Announcements</PageTitle>
      <Table class="w-full">
        <thead>
          <tr>
            <th>Text</th>
            <th>Color</th>
            <th>Created At</th>
            <th>Created By</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="announcement in announcements" :key="announcement.id">
            <td>
              <InputText v-model="announcement.text" label="Text" />
            </td>
            <td>
              <InputText v-model="announcement.color" label="Color" />
            </td>
            <td>
              <PrettyTime :time="announcement.createdAt" />
            </td>
            <td>
              {{ announcement.createdBy }}
            </td>
          </tr>
        </tbody>
      </Table>
      <template #footer>
        <span class="flex justify-end items-center gap-2">
          <Button
            @click="
              announcements.push({
                id: announcements.length + 1,
                text: '',
                color: '#000000',
                createdAt: new Date().toISOString(),
                createdBy: authStore.user!.id,
              })
            "
            >Add new</Button
          >
          <Button :disabled="loading" @click="updateAnnouncements">Update Announcements</Button>
        </span>
      </template>
    </Card>
    <Card class="mt-5">
      <PageTitle>Global notifications</PageTitle>
      <Table class="w-full">
        <thead>
          <tr>
            <th>key</th>
            <th>Content</th>
            <th>Color</th>
            <th>Active From</th>
            <th>Active To</th>
            <th>Created At</th>
            <th>Created By</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="noti in notifications" :key="noti.id">
            <td>
              <InputText v-model="noti.key" label="Key" />
            </td>
            <td>
              <InputText v-model="noti.content" label="Content" />
            </td>
            <td>
              <InputText v-model="noti.color" label="Color" />
            </td>
            <td>
              <InputDate v-model="noti.activeFrom" label="Active From" time />
            </td>
            <td>
              <InputDate v-model="noti.activeTo" label="Active To" time />
            </td>
            <td>
              <PrettyTime :time="noti.createdAt" />
            </td>
            <td>
              {{ noti.createdBy }}
            </td>
          </tr>
        </tbody>
      </Table>
      <template #footer>
        <span class="flex justify-end items-center gap-2">
          <Button
            @click="
              notifications.push({
                id: notifications.length + 1,
                key: '',
                content: '',
                color: '#000000',
                activeFrom: new Date().toISOString(),
                activeTo: new Date().toISOString(),
                createdAt: new Date().toISOString(),
                createdBy: authStore.user!.id,
              })
            "
          >
            Add new
          </Button>
          <Button :disabled="loading" @click="updateNotifications">Update Global Notifications</Button>
        </span>
      </template>
    </Card>
  </div>
</template>
