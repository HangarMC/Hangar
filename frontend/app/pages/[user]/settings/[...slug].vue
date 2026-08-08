<script lang="ts" setup>
import { NamedPermission } from "#shared/types/backend";
import type { HangarOrganization, User } from "#shared/types/backend";
import type { Tab } from "#shared/types/components/design/Tabs";
import SocialForm from "~/components/form/SocialForm.vue";
import IconMdiTune from "~icons/mdi/tune";
import IconMdiAccountGroup from "~icons/mdi/account-group";
import IconMdiShieldAlert from "~icons/mdi/shield-alert";

definePageMeta({
  loginRequired: true,
});

const props = defineProps<{
  user?: User;
  organization?: HangarOrganization;
}>();

const route = useRoute("user-settings-slug");
const router = useRouter();
const i18n = useI18n();
const v = useVuelidate();
const notification = useNotificationStore();

const orgName = computed(() => props.user?.name || (route.params.user as string));
const isOwner = computed(() => hasPerms(NamedPermission.IsSubjectOwner));

const selectedTab = ref(route.params.slug?.[0] || "general");
const tabs = ref<Tab<string>[]>([
  { value: "general", header: i18n.t("organization.settings.tabs.general"), icon: IconMdiTune },
  { value: "members", header: i18n.t("organization.settings.tabs.members"), icon: IconMdiAccountGroup },
]);
if (isOwner.value) {
  tabs.value.push({ value: "management", header: i18n.t("organization.settings.tabs.management"), icon: IconMdiShieldAlert });
}

watch(route, (val) => (selectedTab.value = val.params.slug?.[0] || "general"), { deep: true });
watch(selectedTab, (val) => router.replace(`/${orgName.value}/settings/${val}`));

const form = reactive<{ tagline: string; socials: Record<string, string> }>({ tagline: "", socials: {} });
watch(
  () => props.user,
  (user) => {
    form.tagline = user?.tagline || "";
    form.socials = { ...(user?.socials || {}) };
  },
  { immediate: true }
);

const loading = reactive({ save: false, transfer: false });

async function save() {
  if (!(await v.value.$validate())) return;
  loading.save = true;
  try {
    await useInternalApi(`organizations/org/${orgName.value}/settings/tagline`, "post", { content: form.tagline });
    await useInternalApi(`organizations/org/${orgName.value}/settings/socials`, "post", form.socials);
    await notification.success(i18n.t("organization.settings.success.saved"));
    router.go(0);
  } catch (err) {
    handleRequestError(err);
  }
  loading.save = false;
}

const search = ref("");
const searchResult = ref<string[]>([]);
async function doSearch(val?: string) {
  searchResult.value = [];
  const users = await useApi<{ result: User[] }>("users", "get", { query: val, limit: 25, offset: 0 });
  searchResult.value = users.result?.filter((u) => !u.isOrganization).map((u) => u.name) || [];
}

async function transfer() {
  loading.transfer = true;
  try {
    await useInternalApi(`organizations/org/${orgName.value}/transfer`, "post", { content: search.value });
    await notification.success(i18n.t("organization.settings.success.transferRequest", [search.value]));
  } catch (err) {
    handleRequestError(err);
  }
  loading.transfer = false;
}

async function deleteOrg(comment: string) {
  try {
    await useInternalApi(`organizations/org/${orgName.value}/delete`, "post", { content: comment });
    await notification.success(i18n.t("organization.settings.success.deleted", [orgName.value]));
    await router.push("/");
  } catch (err) {
    handleRequestError(err);
  }
}

useSeo(computed(() => ({ title: i18n.t("organization.settings.title") + " | " + orgName.value, route })));
</script>

<template>
  <div>
    <UserHeader :viewing-user="user" :organization="organization" />

    <Card class="mt-4">
      <Tabs v-model="selectedTab" :tabs="tabs" highlight-selected divided>
        <template #general>
          <ProjectSettingsSection title="organization.settings.icon" description="organization.settings.iconSub">
            <EditableAvatar
              :username="orgName"
              :avatar-url="user?.avatarUrl"
              :action="`organizations/org/${orgName}/settings/avatar`"
              :label="i18n.t('organization.settings.changeAvatar')"
              size="xl"
            />
          </ProjectSettingsSection>

          <ProjectSettingsSection title="organization.settings.tagline" description="organization.settings.taglineSub">
            <InputText
              v-model="form.tagline"
              :label="i18n.t('organization.settings.tagline')"
              counter
              :maxlength="useBackendData.validations.userTagline.max"
              :rules="[maxLength()(useBackendData.validations.userTagline.max!)]"
            />
          </ProjectSettingsSection>

          <ProjectSettingsSection>
            <SocialForm v-model="form.socials" compact />
          </ProjectSettingsSection>
        </template>

        <template #members>
          <ProjectSettingsSection>
            <MemberList
              v-if="organization"
              bare
              title="organization.settings.tabs.members"
              description="organization.settings.membersSub"
              :members="organization.members"
              organization
              :author="orgName"
            />
          </ProjectSettingsSection>
        </template>

        <template #management>
          <ProjectSettingsSection title="organization.settings.transfer" description="organization.settings.transferSub">
            <div class="flex flex-wrap items-end gap-2">
              <div class="min-w-60 flex-1">
                <InputAutocomplete
                  id="org-transfer"
                  v-model="search"
                  :values="searchResult"
                  :label="i18n.t('organization.settings.transferModal.transferTo')"
                  @search="doSearch"
                />
              </div>
              <Button :disabled="search.length === 0" :loading="loading.transfer" class="mb-0.5" @click="transfer">
                <IconMdiCogTransfer />
                {{ i18n.t("project.settings.transfer") }}
              </Button>
            </div>
          </ProjectSettingsSection>

          <div class="overflow-hidden rounded-md border border-red-500/50">
            <h2 class="border-b border-red-500/50 bg-red-500/10 px-4 py-2 font-semibold text-red-700 dark:text-red-300">
              {{ i18n.t("project.settings.dangerZone") }}
            </h2>
            <div class="flex flex-wrap items-center gap-3 px-4 py-3">
              <div class="min-w-0 flex-1">
                <div class="font-semibold">{{ i18n.t("organization.settings.delete") }}</div>
                <p class="text-sm text-gray-secondary">{{ i18n.t("organization.settings.deleteModal.description", [orgName]) }}</p>
              </div>
              <TextAreaModal
                :title="i18n.t('organization.settings.deleteModal.title', [orgName])"
                :label="i18n.t('general.comment')"
                :submit="deleteOrg"
                :submit-label="i18n.t('general.delete')"
                submit-tone="danger"
                require-input
              >
                <template #activator="{ on }">
                  <Button tone="danger" class="flex-shrink-0" v-on="on">{{ i18n.t("organization.settings.delete") }}</Button>
                </template>
              </TextAreaModal>
            </div>
          </div>
        </template>
      </Tabs>

      <div v-if="selectedTab === 'general'" class="mt-6 flex justify-end border-t border-gray-300 pt-4 dark:border-gray-700">
        <Button :disabled="v.$error" :loading="loading.save" @click="save">
          <IconMdiCheck />
          {{ i18n.t("project.settings.save") }}
        </Button>
      </div>
    </Card>
  </div>
</template>
