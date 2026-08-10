<script lang="ts" setup>
import type { Tab } from "#shared/types/components/design/Tabs";
import IconMdiAccount from "~icons/mdi/account";
import IconMdiCog from "~icons/mdi/cog";
import IconMdiKey from "~icons/mdi/key";
import IconMdiLock from "~icons/mdi/lock";
import IconMdiPalette from "~icons/mdi/palette";

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
  { value: "profile", header: t("auth.settings.profile.header"), icon: IconMdiAccount },
  { value: "account", header: t("auth.settings.account.header"), icon: IconMdiCog },
  { value: "security", header: t("auth.settings.security.header"), icon: IconMdiLock },
  { value: "api-keys", header: t("auth.settings.apiKeys.header"), icon: IconMdiKey },
  { value: "other", header: t("auth.settings.misc.header"), icon: IconMdiPalette },
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
      <div class="flex flex-1 flex-wrap items-center justify-between gap-x-4 gap-y-3">
        <div>
          <div class="font-semibold">Check your inbox</div>
          <div class="text-sm opacity-90">We sent a verification code to {{ auth.user.email }}. Enter it to confirm your address.</div>
        </div>
        <Button size="sm" :disabled="loading" @click="emailConfirmModal!.isOpen = true">Enter code</Button>
      </div>
    </Alert>
    <Alert v-else-if="!authSettings?.emailConfirmed" class="col-span-1 md:col-span-2">
      <div class="flex flex-1 flex-wrap items-center justify-between gap-x-4 gap-y-3">
        <div>
          <div class="font-semibold">Email not verified</div>
          <div class="text-sm opacity-90">Confirm {{ auth.user.email }} so we can reach you about your account.</div>
        </div>
        <Button size="sm" :disabled="loading" @click="emailConfirmModal!.isOpen = true">Verify email</Button>
      </div>
    </Alert>

    <Card>
      <Tabs :tabs="tabs" router highlight-selected>
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
        <p class="mb-2">Your code has expired. Request a new one to verify your email.</p>
        <Button :disabled="loading || hasPendingMail" @click="sendEmailCode">Send new code</Button>
      </template>
      <div v-else class="flex flex-col gap-2">
        <p>Enter the 6-digit code we emailed to {{ auth.user.email }}.</p>
        <InputText v-model="emailCode" label="Code" />
        <Button class="w-max" :disabled="loading" @click="verifyEmail(emailCode)">Verify code</Button>
      </div>
    </Modal>
  </div>
</template>
