<script lang="ts" setup>
import type { AnnouncementTable, GlobalNotificationTable, Platform, PlatformVersion } from "#shared/types/backend";
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

const platforms = computed(() => globalData.value?.platforms ?? []);

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
// The backend upserts instead of replacing, so only rows that were never saved can be dropped again.
const savedAnnouncements = ref(announcements.value.length);

function addAnnouncement() {
  announcements.value.push({
    id: announcements.value.length + 1,
    text: "",
    color: "#000000",
    createdAt: new Date().toISOString(),
    createdBy: authStore.user!.id,
  });
}

function removeAnnouncement(index: number) {
  announcements.value.splice(index, 1);
}

async function updateAnnouncements() {
  loading.value = true;
  try {
    await useInternalApi("globalData/announcements", "post", announcements.value);
    announcements.value = await useInternalApi<AnnouncementTable[]>("globalData/announcements");
    savedAnnouncements.value = announcements.value.length;
    notification.success("Updated announcements!");
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.value = false;
}

const notifications = ref(await useInternalApi<GlobalNotificationTable[]>("globalData/notifications"));
const savedNotifications = ref(notifications.value.length);

function addNotification() {
  notifications.value.push({
    id: notifications.value.length + 1,
    key: "",
    content: "",
    color: "#000000",
    activeFrom: new Date().toISOString(),
    activeTo: new Date().toISOString(),
    createdAt: new Date().toISOString(),
    createdBy: authStore.user!.id,
  });
}

function removeNotification(index: number) {
  notifications.value.splice(index, 1);
}

async function updateNotifications() {
  loading.value = true;
  try {
    await useInternalApi("globalData/notifications", "post", notifications.value);
    notifications.value = await useInternalApi<GlobalNotificationTable[]>("globalData/notifications");
    savedNotifications.value = notifications.value.length;
    notification.success("Updated notifications!");
  } catch (err: any) {
    handleRequestError(err);
  }
  loading.value = false;
}
</script>

<template>
  <div>
    <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold">{{ i18n.t("nav.user.adminSettings") }}</h1>
        <p class="mt-1 text-gray-secondary">Platform versions, announcements and other global configuration.</p>
      </div>
    </div>

    <Card flat padding="none" class="mb-4">
      <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <h2 class="flex-grow text-lg font-bold">{{ i18n.t("platformVersions.title") }}</h2>
        <span class="text-sm text-gray-secondary tabular-nums">{{ platforms.length }}</span>
      </div>

      <ul v-if="platforms.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
        <li v-for="platform in platforms" :key="platform.name" class="flex flex-col gap-3 px-4 py-3 sm:flex-row sm:items-center">
          <div class="flex items-center gap-2 sm:w-44 sm:flex-shrink-0">
            <PlatformLogo :platform="platform.enumName" :size="18" class="flex-shrink-0" />
            <span class="font-semibold">{{ platform.name }}</span>
            <Chip tone="neutral" class="tabular-nums">{{ fullVersions[platform.enumName]?.length ?? 0 }}</Chip>
          </div>
          <div class="min-w-0 flex-1">
            <InputTag v-model="fullVersions[platform.enumName]" />
          </div>
        </li>
      </ul>
      <div v-else class="flex flex-col items-center px-4 py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiInformationOutline />
        </div>
        <p class="text-gray-secondary">No platforms configured.</p>
      </div>

      <div class="flex flex-wrap items-center justify-end gap-2 border-t border-gray-300 px-4 py-3 dark:border-gray-700">
        <Button variant="outline" tone="neutral" @click="resetPlatformVersions">
          <IconMdiRestore />
          {{ i18n.t("general.reset") }}
        </Button>
        <Button :disabled="loading" @click="savePlatformVersions">
          <IconMdiContentSave />
          {{ i18n.t("platformVersions.saveChanges") }}
        </Button>
      </div>
    </Card>

    <Card flat padding="none" class="mb-4">
      <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <h2 class="flex-grow text-lg font-bold">Announcements</h2>
        <span class="text-sm text-gray-secondary tabular-nums">{{ announcements.length }}</span>
      </div>

      <ul v-if="announcements.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
        <li v-for="(announcement, index) in announcements" :key="announcement.id" class="px-4 py-3">
          <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
            <div class="min-w-0 flex-1">
              <InputText v-model="announcement.text" label="Text" />
            </div>
            <div class="flex items-center gap-2 sm:w-52 sm:flex-shrink-0">
              <span
                class="h-6 w-6 flex-shrink-0 border border-gray-300 rounded dark:border-gray-700"
                :style="{ backgroundColor: announcement.color }"
                aria-hidden="true"
              />
              <InputText v-model="announcement.color" label="Color" />
            </div>
            <Button
              v-if="index >= savedAnnouncements"
              variant="ghost"
              tone="danger"
              size="sm"
              icon-only
              class="self-end sm:self-center"
              :title="i18n.t('general.delete')"
              :aria-label="i18n.t('general.delete')"
              @click="removeAnnouncement(index)"
            >
              <IconMdiTrashCanOutline />
            </Button>
          </div>
          <div class="mt-1.5 flex flex-wrap items-center gap-2 text-xs text-gray-secondary">
            <Chip v-if="index >= savedAnnouncements" tone="amber">Unsaved</Chip>
            <span v-else class="tabular-nums">
              {{ announcement.createdBy }} &middot; <PrettyTime :time="announcement.createdAt" />
            </span>
          </div>
        </li>
      </ul>
      <div v-else class="flex flex-col items-center px-4 py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiBullhornOutline />
        </div>
        <p class="text-gray-secondary">No announcements yet.</p>
      </div>

      <div class="flex flex-wrap items-center justify-end gap-2 border-t border-gray-300 px-4 py-3 dark:border-gray-700">
        <Button variant="outline" tone="neutral" @click="addAnnouncement">
          <IconMdiPlus />
          Add new
        </Button>
        <Button :disabled="loading" @click="updateAnnouncements">
          <IconMdiContentSave />
          Update Announcements
        </Button>
      </div>
    </Card>

    <Card flat padding="none" class="mb-4">
      <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <h2 class="flex-grow text-lg font-bold">Global notifications</h2>
        <span class="text-sm text-gray-secondary tabular-nums">{{ notifications.length }}</span>
      </div>

      <ul v-if="notifications.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
        <li v-for="(noti, index) in notifications" :key="noti.id" class="px-4 py-3">
          <div class="flex items-start gap-3">
            <div class="grid min-w-0 flex-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <InputText v-model="noti.key" label="Key" />
              <InputText v-model="noti.content" label="Content" class="lg:col-span-3" />
              <div class="flex items-center gap-2">
                <span
                  class="h-6 w-6 flex-shrink-0 border border-gray-300 rounded dark:border-gray-700"
                  :style="{ backgroundColor: noti.color }"
                  aria-hidden="true"
                />
                <InputText v-model="noti.color" label="Color" />
              </div>
              <InputDate v-model="noti.activeFrom" label="Active From" time />
              <InputDate v-model="noti.activeTo" label="Active To" time />
            </div>
            <Button
              v-if="index >= savedNotifications"
              variant="ghost"
              tone="danger"
              size="sm"
              icon-only
              class="flex-shrink-0"
              :title="i18n.t('general.delete')"
              :aria-label="i18n.t('general.delete')"
              @click="removeNotification(index)"
            >
              <IconMdiTrashCanOutline />
            </Button>
          </div>
          <div class="mt-1.5 flex flex-wrap items-center gap-2 text-xs text-gray-secondary">
            <Chip v-if="index >= savedNotifications" tone="amber">Unsaved</Chip>
            <span v-else class="tabular-nums">{{ noti.createdBy }} &middot; <PrettyTime :time="noti.createdAt" /></span>
          </div>
        </li>
      </ul>
      <div v-else class="flex flex-col items-center px-4 py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiBellOutline />
        </div>
        <p class="text-gray-secondary">No global notifications yet.</p>
      </div>

      <div class="flex flex-wrap items-center justify-end gap-2 border-t border-gray-300 px-4 py-3 dark:border-gray-700">
        <Button variant="outline" tone="neutral" @click="addNotification">
          <IconMdiPlus />
          Add new
        </Button>
        <Button :disabled="loading" @click="updateNotifications">
          <IconMdiContentSave />
          Update Global Notifications
        </Button>
      </div>
    </Card>

    <Card flat padding="none" class="mb-4">
      <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <h2 class="flex-grow text-lg font-bold">{{ i18n.t("pages.headers.roles") }}</h2>
        <span class="text-sm text-gray-secondary tabular-nums">{{ roles.length }}</span>
      </div>

      <ul class="divide-y divide-gray-300 dark:divide-gray-700">
        <li v-for="role in roles" :key="role.roleId" class="flex flex-col gap-3 px-4 py-3 lg:flex-row lg:items-center">
          <div class="grid min-w-0 flex-1 gap-3 sm:grid-cols-3">
            <InputText v-model="role.title" label="Title" />
            <InputText v-model="role.color" label="Color" />
            <InputText v-model="role.rank" label="Rank" :rules="[integer()]" />
          </div>
          <div class="flex items-center gap-2 lg:w-56 lg:flex-shrink-0">
            <Tag :color="{ background: role.color }" :name="role.title" />
            <span class="truncate text-xs text-gray-secondary">{{ role.roleCategory }}</span>
          </div>
        </li>
      </ul>

      <div class="flex flex-wrap items-center justify-end gap-2 border-t border-gray-300 px-4 py-3 dark:border-gray-700">
        <Button :disabled="loading" @click="saveRoles">
          <IconMdiContentSave />
          {{ i18n.t("platformVersions.saveChanges") }}
        </Button>
      </div>
    </Card>

    <Card flat padding="none">
      <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <h2 class="flex-grow text-lg font-bold">Maintenance</h2>
      </div>

      <ul class="divide-y divide-gray-300 dark:divide-gray-700">
        <li class="flex flex-col gap-3 px-4 py-3 sm:flex-row sm:items-center">
          <div class="min-w-0 flex-1">
            <p class="font-semibold">Rescan versions for safe links</p>
            <p class="mt-0.5 text-sm text-gray-secondary">Approves all versions with only external links that are safe as per config</p>
          </div>
          <Button variant="outline" tone="danger" class="self-end sm:self-center" :disabled="loading" @click="rescanSafeLinks">Run</Button>
        </li>
        <li class="flex flex-col gap-3 px-4 py-3 sm:flex-row sm:items-center">
          <div class="min-w-0 flex-1">
            <p class="font-semibold">Fix broken (user) avatars</p>
            <p class="mt-0.5 text-sm text-gray-secondary">Goes thru all users with avatar_url = null and tries to fix it</p>
          </div>
          <div class="flex items-center gap-3 self-end sm:self-center">
            <InputCheckbox v-model="forceFixAvatars" label="Force" />
            <Button variant="outline" tone="danger" :disabled="loading" @click="fixAvatars">Run</Button>
          </div>
        </li>
      </ul>
    </Card>
  </div>
</template>
