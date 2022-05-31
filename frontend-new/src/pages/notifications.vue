<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useInvites, useNotifications } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { HangarNotification, Invite, Invites } from "hangar-internal";
import { computed, ref, Ref } from "vue";
import { useInternalApi } from "~/composables/useApi";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";
import { useNotificationStore } from "~/store/notification";
import Card from "~/components/design/Card.vue";
import Button from "~/components/design/Button.vue";
import InputSelect from "~/components/ui/InputSelect.vue";
import IconMdiAlertOutline from "~icons/mdi/alert-outline";
import IconMdiInformationOutline from "~icons/mdi/information-outline";
import IconMdiMessageOutline from "~icons/mdi/message-outline";
import IconMdiCheck from "~icons/mdi/check";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const notificationStore = useNotificationStore();

const notifications = (await useNotifications().catch((e) => handleRequestError(e, ctx, i18n))) as Ref<HangarNotification[]>;
const invites = (await useInvites().catch((e) => handleRequestError(e, ctx, i18n))) as Ref<Invites>;

const filters = ref({
  notification: "unread" as "unread" | "read" | "all",
  invite: "all" as "organizations" | "projects" | "all",
});

const notificationFilter = [
  { text: i18n.t("notifications.unread"), value: "unread" },
  { text: i18n.t("notifications.read"), value: "read" },
  { text: i18n.t("notifications.all"), value: "all" },
];
const inviteFilter = [
  { text: i18n.t("notifications.invite.organizations"), value: "organizations" },
  { text: i18n.t("notifications.invite.projects"), value: "projects" },
  { text: i18n.t("notifications.invite.all"), value: "all" },
];

const filteredInvites = computed(() => {
  if (!invites || !invites.value) return [];
  switch (filters.value.invite) {
    case "projects":
      return invites.value.project;
    case "organizations":
      return invites.value.organization;
    default:
      return [...invites.value.project, ...invites.value.organization];
  }
});
const filteredNotifications = computed(() => {
  if (!notifications || !notifications.value) return [];
  switch (filters.value.notification) {
    case "unread":
      return notifications.value.filter((n) => !n.read);
    case "read":
      return notifications.value.filter((n) => n.read);
    default:
      return notifications.value;
  }
});

useHead(useSeo("Notifications", null, route, null));

function markAllAsRead() {
  for (const notification of notifications.value.filter((n) => !n.read)) {
    markNotificationRead(notification, false);
  }
}

async function markNotificationRead(notification: HangarNotification, router = true) {
  await useInternalApi(`notifications/${notification.id}`, true, "post").catch((e) => handleRequestError(e, ctx, i18n));
  notification.read = true;
  if (notification.action && router) {
    await useRouter().push(notification.action);
  }
}

async function updateInvite(invite: Invite, status: "accept" | "decline" | "unaccept") {
  await useInternalApi(`invites/${invite.type}/${invite.roleTableId}/${status}`, true, "post").catch((e) => handleRequestError(e, ctx, i18n));
  if (status === "accept") {
    invite.accepted = true;
  } else if (status === "unaccept") {
    invite.accepted = false;
  } else {
    invites.value[invite.type] = invites.value[invite.type].filter((i) => i.roleTableId !== invite.roleTableId);
  }
  notificationStore.success(i18n.t(`notifications.invite.msgs.${status}`, [invite.name]));
  await useRouter().go(0);
}
</script>

<template>
  <div class="flex gap-4 flex-col md:flex-row">
    <Card class="basis-full md:basis-6/12" accent>
      <template #header>
        <h1>{{ i18n.t("notifications.title") }}</h1>
      </template>
      <div class="flex mb-4">
        <span class="flex-grow">
          <InputSelect v-model="filters.notification" :values="notificationFilter" />
        </span>
        <Button v-if="filteredNotifications.length && filters && filters.notification === 'unread'" size="medium" @click="markAllAsRead">
          {{ i18n.t("notifications.readAll") }}
        </Button>
      </div>
      <Card v-for="notification in filteredNotifications" :key="notification.id" :class="'text-' + notification.type + ' flex shadow-0'">
        <div class="inline-flex items-center">
          <span class="text-lg mr-2">
            <IconMdiInformationOutline v-if="notification.type === 'info'" class="text-sky-600" />
            <IconMdiCheck v-else-if="notification.type === 'success'" class="text-lime-600" />
            <IconMdiAlertOutline v-else-if="notification.type === 'warning'" class="text-red-600" />
            <IconMdiMessageOutline v-else-if="notification.type === 'neutral'" />
          </span>
          <span class="flex-grow">
            {{ i18n.t(notification.message[0], notification.message.slice(1)) }}
          </span>
        </div>
        <Button v-if="!notification.read" class="ml-2" @click="markNotificationRead(notification)"><IconMdiCheck /></Button>
      </Card>
      <div v-if="!filteredNotifications.length" class="text-red-500 text-lg mt-4">
        {{ i18n.t(`notifications.empty.${filters.notification}`) }}
      </div>
    </Card>
    <Card class="basis-full md:basis-6/12" accent>
      <template #header>
        <h1>{{ i18n.t("notifications.invites") }}</h1>
      </template>
      <InputSelect v-model="filters.invite" :values="inviteFilter" class="mb-4" />
      <Card v-for="(invite, index) in filteredInvites" :key="index">
        {{ i18n.t(!invite.accepted ? "notifications.invited" : "notifications.inviteAccepted", [invite.type]) }}:
        <router-link :to="invite.url" exact>{{ invite.name }}</router-link>
        <template v-if="invite.accepted">
          <Button class="ml-2" @click="updateInvite(invite, 'unaccept')">{{ i18n.t("notifications.invite.btns.unaccept") }}</Button>
        </template>
        <template v-else>
          <Button class="mr-2 ml-2" @click="updateInvite(invite, 'accept')">{{ i18n.t("notifications.invite.btns.accept") }}</Button>
          <Button @click="updateInvite(invite, 'decline')">{{ i18n.t("notifications.invite.btns.decline") }}</Button>
        </template>
      </Card>
      <div v-if="!filteredInvites.length" class="text-red-500 text-lg mt-4">
        {{ i18n.t("notifications.empty.invites") }}
      </div>
    </Card>
  </div>
</template>

<route lang="yaml">
meta:
  requireLoggedIn: true
</route>
