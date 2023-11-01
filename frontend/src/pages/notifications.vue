<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import type { HangarNotification, Invite } from "hangar-internal";
import { computed, ref, type Ref } from "vue";
import { useHead } from "@unhead/vue";
import type { PaginatedResult } from "hangar-api";
import { useInvites, useNotifications, useReadNotifications, useUnreadNotifications } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useInternalApi } from "~/composables/useApi";
import { useSeo } from "~/composables/useSeo";
import { useNotificationStore } from "~/store/notification";
import Card from "~/components/design/Card.vue";
import Button from "~/components/design/Button.vue";
import { lastUpdated } from "~/composables/useTime";
import IconMdiAlertOutline from "~icons/mdi/alert-outline";
import IconMdiInformationOutline from "~icons/mdi/information-outline";
import IconMdiMessageOutline from "~icons/mdi/message-outline";
import IconMdiCheck from "~icons/mdi/check";
import Pagination from "~/components/design/Pagination.vue";
import Tabs from "~/components/design/Tabs.vue";
import { definePageMeta } from "#imports";
import type { Tab } from "~/types/components/design/Tabs";
import { getRoleByValue } from "~/store/backendData";

definePageMeta({
  loginRequired: true,
});

const i18n = useI18n();
const route = useRoute();
const router = useRouter();
const notificationStore = useNotificationStore();

// TODO send in one
const unreadNotifications = await useUnreadNotifications();
const readNotifications = await useReadNotifications();
const allNotifications = await useNotifications();
const notifications: Ref<PaginatedResult<HangarNotification> | null> = ref(unreadNotifications.value);
const invites = await useInvites();

const selectedTab = ref("unread");
const selectedTabs: Tab[] = [
  { value: "unread", header: i18n.t("notifications.unread") },
  { value: "read", header: i18n.t("notifications.read") },
  { value: "all", header: i18n.t("notifications.all") },
];

const selectedInvitesTab = ref("all");
const selectedInvitesTabs: Tab[] = [
  { value: "all", header: i18n.t("notifications.invite.all") },
  { value: "projects", header: i18n.t("notifications.invite.projects") },
  { value: "organizations", header: i18n.t("notifications.invite.organizations") },
];

const filteredInvites = computed(() => {
  if (!invites || !invites.value) return [];
  switch (selectedInvitesTab.value) {
    case "projects":
      return invites.value.project;
    case "organizations":
      return invites.value.organization;
    default:
      return [...invites.value.project, ...invites.value.organization];
  }
});

useHead(useSeo("Notifications", null, route, null));

async function markAllAsRead() {
  await useInternalApi(`markallread`, "post").catch((e) => handleRequestError(e));
  if (!unreadNotifications.value) return;
  unreadNotifications.value.result = [];
  unreadNotifications.value.pagination.limit = 0;
  unreadNotifications.value.pagination.offset = 0;
  unreadNotifications.value.pagination.count = 0;
}

async function markNotificationRead(notification: HangarNotification, push = true) {
  await useInternalApi(`notifications/${notification.id}`, "post").catch((e) => handleRequestError(e));
  notification.read = true;
  if (!notifications.value) return;
  notifications.value.result = notifications.value.result.filter((n) => n !== notification);
  if (notification.action && push) {
    await router.push(notification.action);
  }
}

async function updateInvite(invite: Invite, status: "accept" | "decline") {
  await useInternalApi(`invites/${invite.type}/${invite.roleId}/${status}`, "post").catch((e) => handleRequestError(e));
  if (status === "accept") {
    invite.accepted = true;
  } else {
    if (!invites.value) return;
    invites.value[invite.type] = invites.value[invite.type].filter((i) => i.roleId !== invite.roleId);
  }
  notificationStore.success(i18n.t(`notifications.invite.msgs.${status}`, [invite.name]));
}

function updateSelectedNotifications() {
  if (selectedTab.value === "unread") {
    notifications.value = unreadNotifications.value;
  } else if (selectedTab.value === "read") {
    notifications.value = readNotifications.value;
  } else {
    notifications.value = allNotifications.value;
  }
}
</script>

<template>
  <div class="flex gap-4 flex-col md:flex-row">
    <Card class="basis-full md:basis-6/12" accent>
      <template #header>
        <h1>{{ i18n.t("notifications.title") }}</h1>
      </template>

      <!-- Abuse tabs a little -->
      <Tabs v-model="selectedTab" :tabs="selectedTabs" :vertical="false" @click="updateSelectedNotifications()" />
      <div v-if="notifications?.result.length === 0" class="text-lg">
        {{ i18n.t(`notifications.empty.${selectedTab}`) }}
      </div>

      <Pagination v-if="notifications?.result.length !== 0" :items="notifications?.result">
        <template #default="{ item }">
          <div class="p-1 mb-1 flex items-center">
            <div class="inline-flex items-center flex-grow">
              <span class="text-lg mr-2">
                <IconMdiInformationOutline v-if="item.type === 'info'" class="text-sky-600" />
                <IconMdiCheck v-else-if="item.type === 'success'" class="text-lime-600" />
                <IconMdiAlertOutline v-else-if="item.type === 'warning'" class="text-red-600" />
                <IconMdiMessageOutline v-else-if="item.type === 'neutral'" />
              </span>
              <NuxtLink v-if="item.action" :to="'/' + item.action" active-class="">
                {{ i18n.t(item.message[0], item.message.slice(1)) }}
                <div class="text-xs mt-1">{{ lastUpdated(new Date(item.createdAt)) }}</div>
              </NuxtLink>
              <span v-else>
                {{ i18n.t(item.message[0], item.message.slice(1)) }}
                <div class="text-xs mt-1">{{ lastUpdated(new Date(item.createdAt)) }}</div>
              </span>
            </div>
            <Button v-if="!item.read" class="ml-2" @click="markNotificationRead(item)"><IconMdiCheck /></Button>
          </div>
        </template>
      </Pagination>
      <Button v-if="notifications?.result.length > 1 && selectedTab === 'unread'" size="small" @click="markAllAsRead">
        {{ i18n.t("notifications.readAll") }}
      </Button>
    </Card>

    <Card class="basis-full md:basis-6/12" accent>
      <template #header>
        <h1>{{ i18n.t("notifications.invites") }}</h1>
      </template>
      <Tabs v-model="selectedInvitesTab" :tabs="selectedInvitesTabs" :vertical="false" />
      <Card v-for="(invite, index) in filteredInvites" :key="index">
        <span v-if="invite.representingOrg">
          {{ i18n.t(!invite.accepted ? "notifications.invitedOrg" : "notifications.inviteAcceptedOrg", [invite.representingOrg, invite.type]) }}:
        </span>
        <span v-else> {{ i18n.t(!invite.accepted ? "notifications.invited" : "notifications.inviteAccepted", [invite.type]) }}: </span>

        <NuxtLink :to="invite.url" exact class="mr-2">{{ invite.name }}</NuxtLink>
        <Tag :color="{ background: getRoleByValue(invite.role).color }" :name="getRoleByValue(invite.role).title" />
        <div v-if="!invite.accepted" class="-mt-4">
          <br />
          <Button class="mr-2" @click="updateInvite(invite, 'accept')">{{ i18n.t("notifications.invite.btns.accept") }}</Button>
          <Button @click="updateInvite(invite, 'decline')">{{ i18n.t("notifications.invite.btns.decline") }}</Button>
        </div>
      </Card>
      <div v-if="!filteredInvites.length" class="text-lg">
        {{ i18n.t("notifications.empty.invites") }}
      </div>
    </Card>
  </div>
</template>
