<script lang="ts" setup>
import { isAxiosError } from "axios";
import type { AxiosRequestConfig } from "axios";
import type { Authenticator, SettingsResponse } from "#shared/types/backend";

defineProps<{
  settings?: SettingsResponse;
}>();
const emit = defineEmits<{
  refreshSettings: [];
  openEmailConfirmModal: [];
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
const accountLoading = ref(false);

const accountForm = reactive({
  username: auth.user?.name,
  email: auth.user?.email,
  currentPassword: "",
  newPassword: "",
});

async function saveAccount() {
  accountLoading.value = true;
  try {
    await useInternalApi("auth/account", "POST", accountForm);
    notification.success("Saved!");
    accountForm.currentPassword = "";
    accountForm.newPassword = "";
    emit("refreshSettings");
    useAuth.updateUser(true);
  } catch (err) {
    notification.fromError(i18n, err);
  } finally {
    accountLoading.value = false;
  }
}

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
  <div v-if="auth.user" class="min-w-0">
    <div class="grid grid-cols-1 items-start gap-4 xl:grid-cols-2">
      <div class="space-y-4">
        <Card>
          <div class="mb-4">
            <h2 class="text-xl font-bold">Account details</h2>
            <p class="mt-1 text-sm text-gray">Update the identity and email address used to sign in.</p>
          </div>
          <form class="space-y-3">
            <div>
              <label class="mb-1.5 block text-sm font-semibold" for="security-email">Email</label>
              <div class="relative flex h-10.5 rounded-md transition-all duration-200">
                <input
                  id="security-email"
                  v-model="accountForm.email"
                  class="min-w-0 flex-grow truncate rounded-lg border border-transparent bg-gray-100 px-3 py-2 outline-none transition-colors hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-600"
                  autocomplete="username"
                  type="email"
                />
              </div>
              <Button
                v-if="!settings?.emailConfirmed"
                class="mt-2"
                button-type="secondary"
                size="small"
                :disabled="accountLoading"
                @click.prevent="$emit('openEmailConfirmModal')"
              >
                {{ t("auth.settings.account.verifyEmail") }}
              </Button>
            </div>
            <template v-if="settings?.hasPassword">
              <div>
                <label class="mb-1.5 block text-sm font-semibold" for="current-password">
                  {{ t("auth.settings.account.currentPassword") }}
                </label>
                <div class="relative flex h-10.5 rounded-md transition-all duration-200">
                  <input
                    id="current-password"
                    v-model="accountForm.currentPassword"
                    class="min-w-0 flex-grow rounded-lg border border-transparent bg-gray-100 px-3 py-2 outline-none transition-colors hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-600"
                    autocomplete="current-password"
                    type="password"
                  />
                </div>
              </div>
              <div>
                <label class="mb-1.5 block text-sm font-semibold" for="new-password">
                  {{ t("auth.settings.account.newPassword") }}
                </label>
                <div class="relative flex h-10.5 rounded-md transition-all duration-200">
                  <input
                    id="new-password"
                    v-model="accountForm.newPassword"
                    class="min-w-0 flex-grow rounded-lg border border-transparent bg-gray-100 px-3 py-2 outline-none transition-colors hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-600"
                    autocomplete="new-password"
                    type="password"
                  />
                </div>
              </div>
            </template>
            <div class="flex justify-end">
              <Button type="submit" size="medium" :disabled="accountLoading" :loading="accountLoading" @click.prevent="saveAccount">
                <IconMdiContentSaveOutline class="mr-1" />
                {{ t("general.save") }}
              </Button>
            </div>
          </form>
        </Card>

        <Card>
          <div class="mb-3 flex items-start gap-3">
            <span class="inline-flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-md bg-gray-100 text-xl dark:bg-charcoal-500">
              <IconMdiCellphoneKey />
            </span>
            <div>
              <h2 class="text-xl font-bold">{{ t("auth.settings.security.authApp.name") }}</h2>
              <p class="mt-1 text-sm text-gray">
                {{ settings?.hasTotp ? t("auth.settings.security.authApp.active") : t("auth.settings.security.authApp.none") }}
              </p>
            </div>
          </div>
          <Button v-if="settings?.hasTotp" button-type="secondary" :disabled="loading" @click="unlinkTotp">
            {{ t("auth.settings.security.button.unlinkTotp") }}
          </Button>
          <Button v-else-if="!totpData" :disabled="loading" @click="setupTotp">{{ t("auth.settings.security.button.setupAuthApp") }}</Button>
          <div v-else class="grid gap-4 md:grid-cols-[1fr_auto]">
            <div class="space-y-3">
              <p class="text-sm text-gray">{{ t("auth.settings.security.authAppSetup.scan") }}</p>
              <p class="text-sm text-gray">{{ t("auth.settings.security.authAppSetup.cantScan") }}</p>
              <InputText v-model="totpCode" label="TOTP Code" inputmode="numeric" :rules="[requiredIf()(() => totpData != undefined)]" />
              <Button :disabled="loading || v.$invalid" @click="addTotp">{{ t("auth.settings.security.authAppSetup.verifyTotp") }}</Button>
            </div>
            <div class="text-center">
              <img :src="totpData.qrCode" alt="QR code for TOTP setup" class="mx-auto w-48 rounded-lg bg-white p-2" />
              <code class="mt-2 block max-w-48 break-all text-xs">{{ totpData.secret }}</code>
            </div>
          </div>
        </Card>

        <Card v-if="settings?.hasBackupCodes">
          <h2 class="text-xl font-bold">{{ t("auth.settings.security.backupCodes.name") }}</h2>
          <p class="mt-1 text-sm text-gray">{{ t("auth.settings.security.backupCodes.info") }}</p>
          <div v-if="showCodes" class="mt-3 grid grid-cols-2 gap-2 sm:grid-cols-4">
            <code v-for="code in codes" :key="code.code" class="rounded-md bg-gray-100 p-2 text-center text-xs dark:bg-charcoal-500">
              {{ code["used_at"] ? t("general.used") : code.code }}
            </code>
          </div>
          <div class="mt-3 flex flex-wrap gap-2">
            <Button v-if="!showCodes" button-type="secondary" :disabled="loading" @click="revealCodes">{{ t("general.reveal") }}</Button>
            <Button :disabled="loading" @click="generateNewCodes">{{ t("auth.settings.security.backupCodes.generateNew") }}</Button>
          </div>
        </Card>
      </div>

      <div class="space-y-4">
        <Card>
          <h2 class="text-xl font-bold">{{ t("auth.settings.security.securityKeys.name") }}</h2>
          <p class="mt-1 text-sm text-gray">Use passkeys or hardware security keys for strong, phishing-resistant authentication.</p>
          <ul v-if="settings?.authenticators?.length" class="mt-3 space-y-2">
            <li
              v-for="authenticator in settings.authenticators"
              :key="authenticator.id"
              class="flex flex-wrap items-center gap-2 rounded-lg border border-gray-200 bg-gray-100/60 p-3 dark:border-gray-800 dark:bg-charcoal-500/60"
            >
              <IconMdiKeyVariant class="text-xl text-gray" />
              <div class="min-w-0 flex-grow">
                <p class="truncate font-semibold">{{ authenticator.displayName }}</p>
                <p class="text-xs text-gray">Added <PrettyTime :time="authenticator.addedAt" long /></p>
              </div>
              <Button button-type="borderless" size="small" :disabled="loading" @click.prevent="renameAuthenticatorModal(authenticator)">
                <IconMdiPencil />
              </Button>
              <button
                class="inline-flex h-8 w-8 items-center justify-center rounded-md border border-transparent hover:border-red-600 hover:bg-red-900/50"
                :disabled="loading"
                @click.prevent="unregisterAuthenticator(authenticator)"
              >
                <IconMdiBin />
              </button>
            </li>
          </ul>
          <p v-else class="mt-3 rounded-lg border border-dashed p-4 text-center text-sm text-gray dark:border-gray-700">
            {{ t("auth.settings.security.securityKeys.none") }}
          </p>
          <div class="mt-4 border-t pt-4 dark:border-gray-800">
            <h3 class="font-semibold">{{ t("auth.settings.security.securityKeys.registerTitle") }}</h3>
            <div class="mt-2 flex flex-col gap-2 sm:flex-row">
              <div class="relative flex h-10.5 min-w-0 flex-grow rounded-md transition-all duration-200">
                <input
                  v-model="authenticatorName"
                  class="min-w-0 flex-grow truncate rounded-lg border border-transparent bg-gray-100 px-3 py-2 outline-none transition-colors hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-600"
                  :placeholder="t('auth.settings.security.securityKeys.keyName')"
                  type="text"
                />
              </div>
              <Button class="sm:self-center" :disabled="loading" @click="addAuthenticator">
                {{ t("auth.settings.security.button.setupSecurityKey") }}
              </Button>
            </div>
          </div>
        </Card>

        <Card>
          <h2 class="text-xl font-bold">Connected accounts</h2>
          <p class="mt-1 text-sm text-gray">Link an identity provider for another secure way to sign in.</p>
          <div class="mt-3 flex flex-wrap gap-2">
            <Button
              v-for="provider in backendData.security.oauthProviders"
              :key="provider"
              button-type="secondary"
              :disabled="loading"
              @click="setupOAuth(provider)"
            >
              <IconMdiGithub v-if="provider === 'github'" class="mr-1" />
              <IconMdiGoogle v-else-if="provider === 'google'" class="mr-1" />
              <IconMdiMicrosoft v-else-if="provider === 'microsoft'" class="mr-1" />
              {{
                provider === "github"
                  ? t("auth.settings.security.button.linkGithub")
                  : provider === "google"
                    ? t("auth.settings.security.button.linkGoogle")
                    : provider === "microsoft"
                      ? t("auth.settings.security.button.linkMicrosoft")
                      : t("auth.settings.security.button.linkOther", [provider])
              }}
            </Button>
          </div>
          <div v-if="settings?.oauthConnections?.length" class="mt-4 space-y-2 border-t pt-4 dark:border-gray-800">
            <div
              v-for="credential in settings.oauthConnections"
              :key="credential.provider + credential.id"
              class="flex items-center gap-2 rounded-lg border border-gray-200 p-3 dark:border-gray-800"
            >
              <IconMdiGithub v-if="credential.provider === 'github'" class="text-xl" />
              <IconMdiLinkVariant v-else class="text-xl" />
              <div class="min-w-0 flex-grow">
                <p class="font-semibold capitalize">{{ credential.provider }}</p>
                <p class="truncate text-xs text-gray">{{ credential.name }}</p>
              </div>
              <button
                class="inline-flex h-8 w-8 items-center justify-center rounded-md border border-transparent hover:border-red-600 hover:bg-red-900/50"
                :disabled="!settings?.hasPassword && settings.oauthConnections.length === 1"
                :title="
                  !settings?.hasPassword && settings.oauthConnections.length === 1 ? t('auth.settings.security.unlinkOAuth.cantUnlink') : 'Unlink account'
                "
                @click="unlinkOAuth(credential.provider, credential.id)"
              >
                <IconMdiLinkOff />
              </button>
            </div>
          </div>
        </Card>
      </div>
    </div>

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
      <Button class="mt-3" :disabled="loading" @click.prevent="renameAuthenticator">
        {{ t("auth.settings.security.securityKeys.rename") }}
      </Button>
    </Modal>

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
  </div>
</template>
