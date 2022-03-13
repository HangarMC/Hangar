<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useInvites, useNotifications } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { HangarNotification, Invites } from "hangar-internal";
import { computed, Ref } from "vue";

const ctx = useContext();
const i18n = useI18n();
const { params } = useRoute();
const notifications = (await useNotifications().catch((e) => handleRequestError(e, ctx, i18n))) as Ref<HangarNotification[]>;
const invites = (await useInvites().catch((e) => handleRequestError(e, ctx, i18n))) as Ref<Invites>;

const filteredInvites = computed(() => {
  return invites ? [...invites.value.project, invites.value.organization] : [];
});
</script>

<template>
  <h1>Notifications</h1>
  <div class="flex">
    <div>
      <span v-for="notification in notifications" :key="notification.id">
        Type: {{ notification.type }}<br />
        Message: {{ notification.message }}
      </span>
    </div>
    <div>
      <span v-for="(invite, index) in filteredInvites" :key="index">
        {{ invite }}
      </span>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  requireLoggedIn: true
</route>
