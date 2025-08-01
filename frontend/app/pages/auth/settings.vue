<script lang="ts" setup>
import type { Tab } from "#shared/types/components/design/Tabs";

definePageMeta({
  loginRequired: true,
});

const route = useRoute("auth-settings");
const router = useRouter();
const auth = useAuthStore();
const notification = useNotificationStore();
const i18n = useI18n();
const { t } = useI18n();

const { authSettings, refreshAuthSettings } = useAuthSettings();

if (import.meta.client && route.path.endsWith("settings")) {
  window.location.replace("/auth/settings/profile");
}

const tabs = [
  { value: "profile", header: t("auth.settings.profile.header") },
  { value: "account", header: t("auth.settings.account.header") },
  { value: "security", header: t("auth.settings.security.header") },
  { value: "api-keys", header: t("auth.settings.apiKeys.header") },
  { value: "other", header: t("auth.settings.misc.header") },
] as const satisfies Tab<string>[];

const emailConfirmModal = useTemplateRef("emailConfirmModal");
const hasPendingMail = ref(authSettings.value?.emailPending);
const emailCode = ref();

const loading = ref(false);

if (import.meta.client && route.query.verify) {
  // no await, we dont need to block
  // eslint-disable-next-line unicorn/prefer-top-level-await
  verifyEmail(route.query.verify as string);
}

async function sendEmailCode() {
  loading.value = true;
  try {
    await useInternalApi("auth/email/send", "POST");
    hasPendingMail.value = true;
    notification.success("Email sent!");
  } catch (err) {
    notification.fromError(i18n, err);
  }
  loading.value = false;
}

async function verifyEmail(emailCode: string) {
  if (!authSettings.value) return;
  loading.value = true;
  try {
    await useInternalApi("auth/email/verify", "POST", emailCode, { headers: { "content-type": "text/plain" } });
    authSettings.value!.emailConfirmed = true;
    authSettings.value!.emailPending = false;
    emailConfirmModal.value!.isOpen = false;
    notification.success("Email verified!");
    await router.replace({ query: { verify: undefined } });
  } catch (err) {
    notification.fromError(i18n, err);
  }
  loading.value = false;
}

useSeo(computed(() => ({ title: "Settings", route })));
</script>

<template>
  <div v-if="auth.user" class="space-y-3">
    <Alert v-if="authSettings?.emailPending" class="col-span-1 md:col-span-2">
      Enter the email verification code
      <Button size="small" :disabled="loading" @click="emailConfirmModal!.isOpen = true">here</Button>
    </Alert>
    <Alert v-else-if="!authSettings?.emailConfirmed" class="col-span-1 md:col-span-2">
      You haven't verified your email yet, click
      <Button size="small" :disabled="loading" @click="emailConfirmModal!.isOpen = true">here</Button>
      to change that
    </Alert>

    <Card>
      <Tabs :tabs="tabs" router>
        <router-view v-slot="{ Component }">
          <Suspense>
            <div>
              <component
                :is="Component"
                :settings="authSettings"
                @refresh-settings="refreshAuthSettings"
                @open-email-confirm-modal="emailConfirmModal!.isOpen = true"
              />
            </div>
            <template #fallback><Delayed> Loading... </Delayed></template>
          </Suspense>
        </router-view>
      </Tabs>
    </Card>

    <Modal ref="emailConfirmModal" title="Confirm email" @close="emailConfirmModal!.isOpen = false">
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
