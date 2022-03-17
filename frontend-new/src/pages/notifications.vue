<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useInvites, useNotifications } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { HangarNotification, Invite, Invites } from "hangar-internal";
import { computed, ref, Ref } from "vue";
import { useApi, useInternalApi } from "~/composables/useApi";
import Vue from "@vitejs/plugin-vue";

const ctx = useContext();
const i18n = useI18n();
const { params } = useRoute();

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

function markAllAsRead() {
  for (const notification of notifications.value.filter((n) => !n.read)) {
    markNotificationRead(notification, false);
  }
}

async function markNotificationRead(notification: HangarNotification, router = true) {
  const result = await useInternalApi(`notifications/${notification.id}`, true, "post").catch((e) => handleRequestError(e, ctx, i18n));
  if (!result) return;
  delete notifications.value[notifications.value.findIndex((n) => n.id === notification.id)];
  if (notification.action && router) {
    await useRouter().push(notification.action);
  }
}

async function updateInvite(invite: Invite, status: "accept" | "decline" | "unaccept") {
  const result = await useInternalApi(`invites/${invite.type}/${invite.roleTableId}/${status}`, true, "post").catch((e) => handleRequestError(e, ctx, i18n));
  if (!result) return;
  if (status === "accept") {
    invite.accepted = true;
  } else if (status === "unaccept") {
    invite.accepted = false;
  } else {
    delete invites.value[invite.type][invites.value[invite.type].indexOf(invite)];
  }
  // this.$util.success(this.$t(`notifications.invite.msgs.${status}`, [invite.name])); // TODO success notification
}
</script>

<template>
  <div class="flex">
    <div>
      <h1>Notifications</h1>
      <select v-model="filters.notification">
        <option v-for="filter in notificationFilter" :key="filter.value" :value="filter.value">{{ filter.text }}</option>
      </select>
      <button v-if="filteredNotifications && filters && filters.notification === 'unread'" @click="markAllAsRead">
        {{ i18n.t("notifications.readAll") }}
      </button>
      <div v-for="notification in filteredNotifications" :key="notification.id">
        Type: {{ notification.type }}<br />
        Message: {{ i18n.t(notification.message[0], notification.message.slice(1)) }}<br />
        <button @click="markNotificationRead(notification)">Mark read</button>
      </div>
      <div v-if="!filteredNotifications.length">
        {{ i18n.t(`notifications.empty.${filters.notification}`) }}
      </div>
    </div>
    <div>
      <h1>Invites</h1>
      <select v-model="filters.invite">
        <option v-for="filter in inviteFilter" :key="filter.value" :value="filter.value">{{ filter.text }}</option>
      </select>
      <div v-for="(invite, index) in filteredInvites" :key="index">
        {{ i18n.t(!invite.accepted ? "notifications.invited" : "notifications.inviteAccepted", [invite.type]) }}:
        <router-link :to="invite.url" exact>{{ invite.name }}</router-link>
        <template v-if="invite.accepted">
          <button @click="updateInvite(invite, 'unaccept')">{{ i18n.t("notifications.invite.btns.unaccept") }}</button>
        </template>
        <template v-else>
          <button @click="updateInvite(invite, 'accept')">{{ i18n.t("notifications.invite.btns.accept") }}</button>
          <button @click="updateInvite(invite, 'decline')">{{ i18n.t("notifications.invite.btns.decline") }}</button>
        </template>
      </div>
      <div v-if="!filteredInvites.length">
        {{ i18n.t("notifications.empty.invites") }}
      </div>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  requireLoggedIn: true
</route>
