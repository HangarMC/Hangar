<script lang="ts" setup>
import { isAxiosError } from "axios";
import type { AxiosRequestConfig } from "axios";
import type { Authenticator, SettingsResponse } from "#shared/types/backend";

const props = defineProps<{
  settings?: SettingsResponse;
}>();
const emit = defineEmits<{
  refreshSettings: [];
}>();

const auth = useAuthStore();
const notification = useNotificationStore();
const i18n = useI18n();
const { t } = i18n;
const v = useVuelidate();
const router = useRouter();
const route = useRoute("auth-settings");
const backendData = useBackendData;

const loading = ref(false);

const oauthAccounts = computed(() =>
  [...new Set([...backendData.security.oauthProviders, ...(props.settings?.oauthConnections.map((credential) => credential.provider) ?? [])])].flatMap(
    (provider) => {
      const name = provider
        .split(/[-_]/)
        .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
        .join(" ");
      const credentials = props.settings?.oauthConnections.filter((credential) => credential.provider === provider) ?? [];
      if (credentials.length === 0) {
        return [{ key: provider, id: provider, name, credential: undefined, canConnectAnother: false }];
      }
      return credentials.map((credential, index) => ({
        key: provider + ":" + credential.id,
        id: provider,
        name,
        credential,
        canConnectAnother: index === 0 && backendData.security.oauthProviders.includes(provider),
      }));
    }
  )
);

const authenticatorName = ref<string>();

async function addAuthenticator() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  try {
    const credentialCreateOptions = await useInternalApi<string>("auth/webauthn/setup", "POST", authenticatorName.value, {
      headers: { "content-type": "text/plain" },
    });
    const publicKey = PublicKeyCredential.parseCreationOptionsFromJSON(JSON.parse(credentialCreateOptions).publicKey);
    const publicKeyCredential = await navigator.credentials.create({ publicKey });
    await useInternalApi("auth/webauthn/register", "POST", JSON.stringify(publicKeyCredential), { headers: { "content-type": "text/plain" } });
    authenticatorName.value = "";
    emit("refreshSettings");
    v.value.$reset();
  } catch (err) {
    if (isAxiosError(err) && err.response?.status === 499) {
      codes.value = err.response.data.body;
      backupCodeModal.value!.isOpen = true;
      savedRequest.value = err.config;
    } else if (isAxiosError(err) && err.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else if (err?.toString()?.startsWith("NotAllowedError")) {
      notification.error("Security Key Authentication failed!");
    } else {
      notification.fromError(i18n, err);
    }
  }
  loading.value = false;
}

async function unregisterAuthenticator(authenticator: Authenticator) {
  loading.value = true;
  try {
    await useInternalApi("auth/webauthn/unregister", "POST", authenticator.id, { headers: { "content-type": "text/plain" } });
    emit("refreshSettings");
  } catch (err) {
    if (isAxiosError(err) && err.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else if (err?.toString()?.startsWith("NotAllowedError")) {
      notification.error("Security Key Authentication failed!");
    } else {
      notification.fromError(i18n, err);
    }
  }
  loading.value = false;
}

const newAuthenticatorName = ref<string>();
const currentlyRenamingAuthenticator = ref<Authenticator>();
const authenticatorRenameModal = useTemplateRef("authenticatorRenameModal");

function renameAuthenticatorModal(authenticator: Authenticator) {
  newAuthenticatorName.value = authenticator.displayName;
  currentlyRenamingAuthenticator.value = authenticator;
  authenticatorRenameModal.value!.isOpen = true;
  v.value.$reset();
}

async function renameAuthenticator() {
  if (!(await v.value.$validate())) return;
  if (!currentlyRenamingAuthenticator.value) {
    notification.error("Something went wrong, please try again");
    return;
  }
  loading.value = true;
  try {
    await useInternalApi("auth/webauthn/rename", "POST", { id: currentlyRenamingAuthenticator.value.id, displayName: newAuthenticatorName.value });
    authenticatorRenameModal.value!.isOpen = false;
    emit("refreshSettings");
  } catch (err) {
    if (isAxiosError(err) && err.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else if (err?.toString()?.startsWith("NotAllowedError")) {
      notification.error("Security Key Authentication failed!");
    } else {
      notification.fromError(i18n, err);
    }
  }
  loading.value = false;
}

const totpData = ref<{ secret: string; qrCode: string } | undefined>();

async function setupTotp() {
  loading.value = true;
  try {
    totpData.value = await useInternalApi<{ secret: string; qrCode: string }>("auth/totp/setup", "POST");
  } catch (err) {
    if (isAxiosError(err) && err.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, err);
    }
  }
  loading.value = false;
}

function cancelTotpSetup() {
  totpData.value = undefined;
  totpCode.value = undefined;
  v.value.$reset();
}

async function handleTotpAction() {
  if (props.settings?.hasTotp) {
    await unlinkTotp();
  } else if (totpData.value) {
    cancelTotpSetup();
  } else {
    await setupTotp();
  }
}

function totpActionLabel() {
  if (props.settings?.hasTotp) return t("auth.settings.security.twoFactor.remove");
  if (totpData.value) return t("general.close");
  return t("auth.settings.security.twoFactor.setUp");
}

function securityKeysActionLabel() {
  if (showSecurityKeys.value) return t("general.close");
  if (props.settings?.authenticators.length) return t("auth.settings.security.twoFactor.manage");
  return t("auth.settings.security.twoFactor.setUp");
}

const totpCode = ref();

async function addTotp() {
  loading.value = true;
  try {
    await useInternalApi("auth/totp/register", "POST", { secret: totpData.value?.secret, code: totpCode.value });
    totpCode.value = undefined;
    emit("refreshSettings");
  } catch (err) {
    if (isAxiosError(err) && err.response?.status === 499) {
      codes.value = err.response.data.body;
      backupCodeModal.value!.isOpen = true;
      savedRequest.value = err.config;
      otp.value = err.response.headers["x-hangar-verify"];
    } else if (isAxiosError(err) && err.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, err);
    }
  }
  loading.value = false;
}

async function unlinkTotp() {
  loading.value = true;
  try {
    await useInternalApi("auth/totp/remove", "POST");
    emit("refreshSettings");
  } catch (err) {
    if (isAxiosError(err) && err.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, err);
    }
  }
  loading.value = false;
}

const showCodes = ref(false);
const showSecurityKeys = ref(false);
const codes = ref();

const savedRequest = ref<AxiosRequestConfig>();
const backupCodeModal = useTemplateRef("backupCodeModal");
const backupCodeConfirm = ref();
const otp = ref<string>();

async function confirmAndRepeat() {
  loading.value = true;
  try {
    const req = savedRequest.value;
    if (req) {
      // set header
      let headers = req.headers;
      if (!headers) {
        headers = {};
        req.headers = headers;
      }
      headers["X-Hangar-Verify"] = backupCodeConfirm.value + (otp.value ? ":" + otp.value : "");
      // repeat request
      await useAxios()(req);
      // close modal
      backupCodeConfirm.value = undefined;
      backupCodeModal.value!.isOpen = false;
      // reset stuff
      emit("refreshSettings");
      totpCode.value = undefined;
      totpData.value = undefined;
      authenticatorName.value = "";
      notification.success("Successfully enabled 2FA!");
      v.value.$reset();
    } else {
      notification.error("no saved request?");
    }
  } catch (err) {
    notification.fromError(i18n, err);
  }
  loading.value = false;
}

async function revealCodes() {
  loading.value = true;
  try {
    if (!codes.value) {
      codes.value = await useInternalApi("auth/codes/show", "POST");
    }
    showCodes.value = true;
  } catch (err) {
    if (isAxiosError(err) && err.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, err);
    }
  }
  loading.value = false;
}

async function toggleBackupCodes() {
  if (showCodes.value) {
    showCodes.value = false;
    return;
  }
  await revealCodes();
}

async function generateNewCodes() {
  loading.value = true;
  try {
    codes.value = await useInternalApi("auth/codes/regenerate", "POST");
    notification.success("Regenerated backup codes!");
    emit("refreshSettings");
  } catch (err) {
    if (isAxiosError(err) && err.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, err);
    }
  }
  loading.value = false;
}

const currentlyUnlinkingProvider = ref<string>();
const oauthModal = useTemplateRef("oauthModal");
const unlinkUrl = ref<string>();

async function setupOAuth(provider: string) {
  try {
    window.location.href = await useInternalApi<string>("oauth/" + provider + "/login?mode=settings&returnUrl=" + encodeURIComponent(route.fullPath), "GET");
  } catch (err) {
    if (isAxiosError(err) && err.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, err);
    }
  }
}

async function unlinkOAuth(provider: string, id: string) {
  try {
    unlinkUrl.value = await useInternalApi("oauth/" + provider + "/unlink/" + id, "POST");
    currentlyUnlinkingProvider.value = provider;
    oauthModal.value!.isOpen = true;
  } catch (err) {
    notification.fromError(i18n, err);
  }
}

function closeUnlinkModal() {
  oauthModal.value!.isOpen = false;
  emit("refreshSettings");
}
</script>

<template>
  <div v-if="auth.user">
    <PageTitle>{{ t("auth.settings.security.header") }}</PageTitle>
    <section>
      <h3 class="text-lg font-bold">{{ t("auth.settings.security.twoFactor.title") }}</h3>
      <p class="mt-1 text-sm text-gray-secondary">{{ t("auth.settings.security.twoFactor.description") }}</p>

      <div class="mt-2">
        <div class="flex items-center gap-3 py-4 lt-sm:flex-wrap">
          <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg background-card text-xl">
            <IconMdiCellphone />
          </div>
          <div class="min-w-0">
            <div class="font-semibold">{{ t("auth.settings.security.authApp.name") }}</div>
            <div class="text-sm text-gray-secondary">
              {{ settings?.hasTotp ? t("auth.settings.security.twoFactor.configured") : t("auth.settings.security.twoFactor.notConfigured") }}
            </div>
          </div>
          <div class="ml-auto flex shrink-0 items-center gap-3 lt-sm:w-full lt-sm:justify-end">
            <span v-if="settings?.hasTotp" class="rounded-full bg-green-500/20 px-2 py-0.5 text-xs font-semibold text-green-700 dark:text-green-300">
              {{ t("auth.settings.security.twoFactor.enabled") }}
            </span>
            <span v-else class="rounded-full background-card px-2 py-0.5 text-xs font-semibold text-gray-secondary">
              {{ t("auth.settings.security.twoFactor.off") }}
            </span>
            <Button variant="outline" tone="neutral" :disabled="loading" @click="handleTotpAction">
              {{ totpActionLabel() }}
            </Button>
          </div>
        </div>

        <div v-if="totpData && !settings?.hasTotp" class="mb-4 flex gap-8 rounded-lg background-card p-4 lt-sm:flex-col">
          <div class="flex basis-1/2 flex-col gap-2">
            <p>{{ t("auth.settings.security.authAppSetup.scan") }}</p>
            <p>{{ t("auth.settings.security.authAppSetup.cantScan") }}</p>
            <div class="mt-auto flex flex-col gap-2">
              <p>{{ t("auth.settings.security.authAppSetup.enterTotp") }}</p>
              <InputText v-model="totpCode" label="TOTP Code" inputmode="numeric" :rules="[requiredIf()(() => totpData != undefined)]" />
              <Button :disabled="loading || v.$invalid" @click="addTotp">{{ t("auth.settings.security.authAppSetup.verifyTotp") }}</Button>
            </div>
          </div>
          <div class="basis-1/2">
            <img :src="totpData.qrCode" alt="QR code for TOTP setup" class="w-60" />
            <small>{{ totpData.secret }}</small>
          </div>
        </div>

        <div class="flex items-center gap-3 border-t border-gray-300 py-4 dark:border-gray-600 lt-sm:flex-wrap">
          <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg background-card text-xl">
            <IconMdiKey />
          </div>
          <div class="min-w-0">
            <div class="font-semibold">{{ t("auth.settings.security.securityKeys.name") }}</div>
            <div class="text-sm text-gray-secondary">
              {{ t("auth.settings.security.twoFactor.securityKeyCount", [settings?.authenticators.length ?? 0], settings?.authenticators.length ?? 0) }}
            </div>
          </div>
          <div class="ml-auto flex shrink-0 items-center gap-3 lt-sm:w-full lt-sm:justify-end">
            <span
              v-if="settings?.authenticators.length"
              class="rounded-full bg-green-500/20 px-2 py-0.5 text-xs font-semibold text-green-700 dark:text-green-300"
            >
              {{ t("auth.settings.security.twoFactor.enabled") }}
            </span>
            <span v-else class="rounded-full background-card px-2 py-0.5 text-xs font-semibold text-gray-secondary">
              {{ t("auth.settings.security.twoFactor.off") }}
            </span>
            <Button variant="outline" tone="neutral" :disabled="loading" @click="showSecurityKeys = !showSecurityKeys">
              {{ securityKeysActionLabel() }}
            </Button>
          </div>
        </div>

        <div v-if="showSecurityKeys" class="mb-4 rounded-lg background-card p-4">
          <ul v-if="settings?.authenticators.length" class="mb-4 space-y-2">
            <li v-for="authenticator in settings.authenticators" :key="authenticator.id" class="flex items-center gap-2 lt-sm:flex-wrap">
              <div class="min-w-0 mr-auto">
                <div class="truncate font-semibold">{{ authenticator.displayName }}</div>
                <small class="text-gray-secondary"> {{ t("auth.settings.security.twoFactor.added") }} <PrettyTime :time="authenticator.addedAt" long /> </small>
              </div>
              <Button variant="outline" tone="neutral" :disabled="loading" @click.prevent="renameAuthenticatorModal(authenticator)">
                {{ t("auth.settings.security.securityKeys.rename") }}
              </Button>
              <Button variant="outline" tone="neutral" :disabled="loading" @click.prevent="unregisterAuthenticator(authenticator)">
                {{ t("auth.settings.security.securityKeys.unregister") }}
              </Button>
            </li>
          </ul>
          <h4 class="font-semibold mb-2">{{ t("auth.settings.security.securityKeys.registerTitle") }}</h4>
          <div class="flex items-start gap-2 lt-sm:flex-col">
            <InputText
              v-model="authenticatorName"
              class="flex-grow lt-sm:w-full"
              :label="t('auth.settings.security.securityKeys.keyName')"
              :rules="[requiredIf()(() => showSecurityKeys && totpData == undefined && !authenticatorRenameModal?.isOpen)]"
            />
            <Button class="mt-1" :disabled="loading" @click="addAuthenticator">{{ t("auth.settings.security.twoFactor.addKey") }}</Button>
          </div>
        </div>

        <div class="flex items-center gap-3 border-t border-gray-300 py-4 dark:border-gray-600 lt-sm:flex-wrap">
          <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg background-card text-xl">
            <IconMdiLock />
          </div>
          <div class="min-w-0">
            <div class="font-semibold">{{ t("auth.settings.security.backupCodes.name") }}</div>
            <div class="text-sm text-gray-secondary">
              {{
                settings?.hasBackupCodes ? t("auth.settings.security.twoFactor.backupCodesReady") : t("auth.settings.security.twoFactor.backupCodesUnavailable")
              }}
            </div>
          </div>
          <div class="ml-auto flex shrink-0 items-center gap-3 lt-sm:w-full lt-sm:justify-end">
            <span v-if="settings?.hasBackupCodes" class="rounded-full bg-green-500/20 px-2 py-0.5 text-xs font-semibold text-green-700 dark:text-green-300">
              {{ t("auth.settings.security.twoFactor.available") }}
            </span>
            <span v-else class="rounded-full background-card px-2 py-0.5 text-xs font-semibold text-gray-secondary">
              {{ t("auth.settings.security.twoFactor.off") }}
            </span>
            <Button variant="outline" tone="neutral" :disabled="loading || !settings?.hasBackupCodes" @click="toggleBackupCodes">
              {{ showCodes ? t("auth.settings.security.twoFactor.hide") : t("auth.settings.security.twoFactor.view") }}
            </Button>
          </div>
        </div>

        <div v-if="showCodes" class="mb-4 rounded-lg background-card p-4">
          <div class="flex flex-wrap gap-y-1 mb-3">
            <div v-for="code in codes" :key="code.code" class="basis-3/12 lt-sm:basis-1/2">
              <code>{{ code["used_at"] ? t("general.used") : code.code }}</code>
            </div>
          </div>
          <Button :disabled="loading" @click="generateNewCodes">{{ t("auth.settings.security.backupCodes.generateNew") }}</Button>
        </div>
      </div>
    </section>

    <Modal
      ref="authenticatorRenameModal"
      title="Rename authenticator"
      @close="
        authenticatorRenameModal!.isOpen = false;
        v.$reset();
      "
    >
      <InputText
        v-model="newAuthenticatorName"
        :label="t('auth.settings.security.securityKeys.keyName')"
        :rules="[requiredIf()(() => authenticatorRenameModal?.isOpen || false)]"
      />
      <Button class="mt-2" size="sm" :disabled="loading" @click.prevent="renameAuthenticator">
        {{ t("auth.settings.security.securityKeys.rename") }}
      </Button>
    </Modal>

    <section class="mt-6">
      <h3 class="text-lg font-bold">{{ t("auth.settings.security.connectedAccounts.title") }}</h3>
      <p class="mt-1 text-sm text-gray-secondary">{{ t("auth.settings.security.connectedAccounts.description") }}</p>

      <div class="mt-2">
        <div
          v-for="account in oauthAccounts"
          :key="account.key"
          class="flex items-center gap-3 border-t border-gray-300 py-4 first:border-t-0 dark:border-gray-600 lt-sm:flex-wrap"
        >
          <IconMdiGithub v-if="account.id === 'github'" class="shrink-0 text-2xl" />
          <IconMdiGoogle v-else-if="account.id === 'google'" class="shrink-0 text-2xl" />
          <IconMdiMicrosoft v-else-if="account.id === 'microsoft'" class="shrink-0 text-2xl" />
          <IconMdiLinkVariant v-else class="shrink-0 text-2xl" />

          <div class="min-w-0">
            <div class="font-semibold">{{ account.name }}</div>
            <div class="truncate text-sm text-gray-secondary">
              {{ account.credential?.name || t("auth.settings.security.connectedAccounts.notConnected") }}
            </div>
          </div>

          <div class="ml-auto flex shrink-0 items-center gap-3 lt-sm:w-full lt-sm:justify-end">
            <span v-if="account.credential" class="rounded-full bg-green-500/20 px-2 py-0.5 text-xs font-semibold text-green-700 dark:text-green-300">
              {{ t("auth.settings.security.connectedAccounts.connected") }}
            </span>
            <span v-else class="rounded-full background-card px-2 py-0.5 text-xs font-semibold text-gray-secondary">
              {{ t("auth.settings.security.connectedAccounts.off") }}
            </span>

            <Button
              v-if="account.canConnectAnother"
              variant="outline"
              tone="neutral"
              :disabled="loading"
              :title="t('auth.settings.security.connectedAccounts.connectAnother', [account.name])"
              :aria-label="t('auth.settings.security.connectedAccounts.connectAnother', [account.name])"
              @click="setupOAuth(account.id)"
            >
              <IconMdiPlus />
            </Button>
            <Button
              variant="outline"
              tone="neutral"
              :disabled="loading || (!!account.credential && !settings?.hasPassword && settings?.oauthConnections.length === 1)"
              :title="
                account.credential && !settings?.hasPassword && settings?.oauthConnections.length === 1
                  ? t('auth.settings.security.unlinkOAuth.cantUnlink')
                  : undefined
              "
              @click="account.credential ? unlinkOAuth(account.id, account.credential.id) : setupOAuth(account.id)"
            >
              {{ account.credential ? t("auth.settings.security.connectedAccounts.disconnect") : t("auth.settings.security.connectedAccounts.connect") }}
            </Button>
          </div>
        </div>
      </div>
    </section>

    <Modal ref="oauthModal" :title="t('auth.settings.security.unlinkOAuth.modal.title')" @close="closeUnlinkModal">
      <p>{{ t("auth.settings.security.unlinkOAuth.modal.message", [currentlyUnlinkingProvider]) }}</p>
      <Link :href="unlinkUrl" target="_blank"> {{ t("auth.settings.security.unlinkOAuth.modal.unlinkUrl", [currentlyUnlinkingProvider]) }} </Link>
    </Modal>

    <Modal ref="backupCodeModal" :title="t('auth.settings.security.backupCodes.modal.title')" @close="backupCodeModal!.isOpen = false">
      {{ t("auth.settings.security.backupCodes.modal.needConfigure") }}
      <div class="flex flex-wrap mt-2 mb-2">
        <div v-for="code in codes" :key="code.code" class="basis-3/12">
          <code>{{ code.code }}</code>
        </div>
      </div>
      <p class="mb-2">{{ t("auth.settings.security.backupCodes.modal.confirm") }}</p>
      <InputText
        v-model="backupCodeConfirm"
        :label="t('auth.settings.security.backupCodes.modal.backupCode')"
        :rules="[requiredIf()(backupCodeModal?.isOpen || false)]"
      />
      <Button class="mt-2" :disabled="v.$invalid" @click="confirmAndRepeat">{{ t("general.confirm") }}</Button>
    </Modal>

    <!-- TODO: implement session list
    <h3 class="text-lg font-bold mt-4 mb-2">{{ t("auth.settings.security.devices") }}</h3>
    <ComingSoon>
      last login<br />
      on revoke iphone<br />
      revoke all
    </ComingSoon>
    -->
  </div>
</template>
