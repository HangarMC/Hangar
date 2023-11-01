<script lang="ts" setup>
import { useHead } from "@unhead/vue";
import { useRoute, useRouter } from "vue-router";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { useVuelidate } from "@vuelidate/core";
import type { AuthSettings } from "hangar-internal";
import { useSeo } from "~/composables/useSeo";
import { useAuthStore } from "~/store/auth";
import Button from "~/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import { useAuthSettings } from "~/composables/useApiHelper";
import Card from "~/components/design/Card.vue";
import InputText from "~/components/ui/InputText.vue";
import { definePageMeta } from "#imports";
import Alert from "~/components/design/Alert.vue";
import Modal from "~/components/modals/Modal.vue";
import { useNotificationStore } from "~/store/notification";
import Tabs from "~/components/design/Tabs.vue";
import type { Tab } from "~/types/components/design/Tabs";
import Delayed from "~/components/design/Delayed.vue";

definePageMeta({
  loginRequired: true,
});

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const notification = useNotificationStore();
const i18n = useI18n();
const { t } = useI18n();
const v = useVuelidate();

const settings = await useAuthSettings();

if (process.client && route.path.endsWith("settings")) {
  window.location.replace("/auth/settings/profile");
}

const tabs: Tab[] = [
  { value: "profile", header: t("auth.settings.profile.header") },
  { value: "account", header: t("auth.settings.account.header") },
  { value: "security", header: t("auth.settings.security.header") },
  { value: "api-keys", header: t("auth.settings.apiKeys.header") },
];

const emailConfirmModal = ref();
const hasPendingMail = ref(settings.value?.emailPending);
const emailCode = ref();

const loading = ref(false);

if (process.client && route.query.verify) {
  // no await, we dont need to block
  verifyEmail(route.query.verify as string);
}

async function sendEmailCode() {
  loading.value = true;
  try {
    await useInternalApi("auth/email/send", "POST");
    hasPendingMail.value = true;
    notification.success("Email sent!");
  } catch (e) {
    notification.fromError(i18n, e);
  }
  loading.value = false;
}

async function verifyEmail(emailCode: string) {
  if (!settings.value) return;
  loading.value = true;
  try {
    await useInternalApi("auth/email/verify", "POST", emailCode, { headers: { "content-type": "text/plain" } });
    settings.value!.emailConfirmed = true;
    settings.value!.emailPending = false;
    emailConfirmModal.value.isOpen = false;
    notification.success("Email verified!");
    await router.replace({ query: { verify: undefined } });
  } catch (e) {
    notification.fromError(i18n, e);
  }
  loading.value = false;
}

async function refreshSettings() {
  settings.value = await useInternalApi<AuthSettings>("auth/settings", "POST");
}

useHead(useSeo("Settings", null, route, null));
</script>

<template>
  <div v-if="auth.user" class="space-y-3">
    <Alert v-if="settings?.emailPending" class="col-span-1 md:col-span-2">
      Enter the email verification code
      <Button size="small" :disabled="loading" @click="emailConfirmModal.isOpen = true">here</Button>
    </Alert>
    <Alert v-else-if="!settings?.emailConfirmed" class="col-span-1 md:col-span-2">
      You haven't verified your email yet, click
      <Button size="small" :disabled="loading" @click="emailConfirmModal.isOpen = true">here</Button>
      to change that
    </Alert>

    <Card>
      <Tabs :tabs="tabs" router>
        <router-view v-slot="{ Component }">
          <Suspense>
            <component :is="Component" :settings="settings" @refresh-settings="refreshSettings" @open-email-confirm-modal="emailConfirmModal.isOpen = true" />
            <template #fallback><Delayed> Loading... </Delayed></template>
          </Suspense>
        </router-view>
      </Tabs>
    </Card>

    <Modal ref="emailConfirmModal" title="Confirm email" @close="emailConfirmModal.isOpen = false">
      <template v-if="!hasPendingMail">
        <p class="mb-2">Your previous code expired.</p>
        <Button :disabled="loading || hasPendingMail" @click="sendEmailCode">Resend verification code</Button>
      </template>
      <div v-else class="flex flex-col gap-2">
        <p>Enter the code you received via email here</p>
        <InputText v-model="emailCode" label="Code" />
        <Button class="w-max" :disabled="loading" @click="verifyEmail(emailCode)">Verify Code</Button>
      </div>
    </Modal>
  </div>
</template>
