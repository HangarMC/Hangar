<script lang="ts" setup>
import type { ApiKey, NamedPermission, SettingsResponse } from "#shared/types/backend";
import { useApiKeys, usePossiblePerms, useScopableProjects } from "~/composables/useData";

defineProps<{
  settings?: SettingsResponse;
}>();

const i18n = useI18n();
const notification = useNotificationStore();
const v = useVuelidate();
const auth = useAuthStore();

const { apiKeys } = useApiKeys(() => auth.user!.name);
const { possiblePerms } = usePossiblePerms(() => auth.user!.name);
const { scopableProjects } = useScopableProjects(() => auth.user!.name);

const name = ref("");
const loadingCreate = ref(false);
const loadingDelete = reactive<Record<string, boolean>>({});
const selectedPerms = ref<NamedPermission[]>([]);
const createdKey = ref<string | undefined>();
const keyRevealed = ref(false);

const scope = ref<"all" | "selected">("all");
const selectedProjects = ref<string[]>([]);
const projectFilter = ref("");

const expiration = ref<"never" | "30" | "90" | "365" | "custom">("never");
const customExpiration = ref("");

const permLabel = (perm: string) => perm.replaceAll("_", " ").replace(/^./, (c) => c.toUpperCase());
const allSelected = computed(() => (possiblePerms.value?.length ?? 0) > 0 && selectedPerms.value.length === possiblePerms.value?.length);

function toggleAll() {
  selectedPerms.value = allSelected.value ? [] : [...(possiblePerms.value ?? [])];
}

const filteredProjects = computed(() => {
  const filter = projectFilter.value.trim().toLowerCase();
  const projects = scopableProjects.value ?? [];
  if (!filter) return projects;
  return projects.filter((p) => p.name.toLowerCase().includes(filter) || p.namespace.owner.toLowerCase().includes(filter));
});

function toggleProject(slug: string) {
  selectedProjects.value = selectedProjects.value.includes(slug) ? selectedProjects.value.filter((s) => s !== slug) : [...selectedProjects.value, slug];
}

// end of the picked day in the user's own timezone, so the key lives through the date they see
const expiresAt = computed(() => {
  if (expiration.value === "never") return;
  if (expiration.value === "custom") {
    if (!customExpiration.value) return;
    const end = new Date(customExpiration.value + "T23:59:59");
    return Number.isNaN(end.getTime()) ? undefined : end.toISOString();
  }
  return new Date(Date.now() + Number(expiration.value) * 86_400_000).toISOString();
});

const expirationOptions = computed(() => [
  { value: "never", text: i18n.t("apiKeys.expiration.never") },
  { value: "30", text: i18n.t("apiKeys.expiration.days", [30]) },
  { value: "90", text: i18n.t("apiKeys.expiration.days", [90]) },
  { value: "365", text: i18n.t("apiKeys.expiration.year") },
  { value: "custom", text: i18n.t("apiKeys.expiration.custom") },
]);

const today = new Date().toISOString().slice(0, 10);
const invalidScope = computed(() => scope.value === "selected" && selectedProjects.value.length === 0);
const invalidExpiration = computed(() => expiration.value === "custom" && (!expiresAt.value || expiresAt.value <= new Date().toISOString()));

function isExpired(key: ApiKey) {
  return !!key.expiresAt && new Date(key.expiresAt).getTime() <= Date.now();
}

const expiryLabel = (key: ApiKey) => i18n.t(isExpired(key) ? "apiKeys.expiration.expiredOn" : "apiKeys.expiration.expiresOn");

async function create() {
  if (!(await v.value.$validate())) return;
  loadingCreate.value = true;
  const projects = scope.value === "selected" ? [...selectedProjects.value] : undefined;
  const key = await useInternalApi<string>(`api-keys/create-key/${auth.user?.name}`, "post", {
    name: name.value,
    permissions: selectedPerms.value,
    projects,
    expiresAt: expiresAt.value,
  }).catch((err) => handleRequestError(err));
  if (key) {
    createdKey.value = key;
    keyRevealed.value = false;
    if (!apiKeys.value) {
      apiKeys.value = [];
    }
    apiKeys.value.unshift({
      tokenIdentifier: key.slice(0, Math.max(0, key.indexOf("."))),
      name: name.value,
      permissions: selectedPerms.value,
      createdAt: new Date().toISOString(),
      expiresAt: expiresAt.value,
      projectScoped: !!projects?.length,
      projects: (scopableProjects.value ?? []).filter((p) => projects?.includes(p.namespace.slug)).map((p) => p.namespace),
    });
    const val = name.value;
    name.value = "";
    selectedPerms.value = [];
    scope.value = "all";
    selectedProjects.value = [];
    projectFilter.value = "";
    expiration.value = "never";
    customExpiration.value = "";
    v.value.$reset();
    notification.success(i18n.t("apiKeys.success.create", [val]));
  }
  loadingCreate.value = false;
}

async function deleteKey(key: ApiKey) {
  loadingDelete[key.name] = true;
  await useInternalApi(`api-keys/delete-key/${auth.user?.name}`, "post", {
    content: key.name,
  }).catch((err) => handleRequestError(err));
  apiKeys.value = apiKeys.value?.filter((k) => k.name !== key.name);
  notification.success(i18n.t("apiKeys.success.delete", [key.name]));
  loadingDelete[key.name] = false;
}

const copied = ref(false);
function copy(event: any) {
  const clipboardData = event.clipboardData || event.originalEvent?.clipboardData || navigator.clipboard;
  clipboardData.writeText(createdKey.value as string);
  copied.value = true;
  setTimeout(() => (copied.value = false), 2000);
}
</script>

<template>
  <div v-if="auth.user">
    <PageTitle>{{ i18n.t("auth.settings.apiKeys.header") }}</PageTitle>

    <div v-if="createdKey" class="mb-5 rounded-lg background-card p-4">
      <div class="flex items-start gap-3">
        <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-lime-500/15 text-lg text-lime-500">
          <IconMdiKeyVariant />
        </div>
        <div class="min-w-0 flex-1">
          <h2 class="font-bold">{{ i18n.t("apiKeys.createdTitle") }}</h2>
          <p class="mt-0.5 text-sm text-gray-secondary">{{ i18n.t("apiKeys.created") }}</p>
          <div class="mt-2 flex flex-wrap items-center gap-2">
            <code class="min-w-0 flex-1 truncate rounded background-card px-2 py-1.5 text-sm">
              {{ keyRevealed ? createdKey : "•".repeat(48) }}
            </code>
            <Button variant="outline" tone="neutral" size="sm" @click="keyRevealed = !keyRevealed">
              <IconMdiEyeOff v-if="keyRevealed" />
              <IconMdiEye v-else />
              {{ i18n.t(keyRevealed ? "apiKeys.hide" : "apiKeys.reveal") }}
            </Button>
            <Tooltip :hover="false" :show="copied">
              <template #content>{{ i18n.t("apiKeys.copied") }}</template>
              <Button variant="outline" tone="neutral" size="sm" @click="copy">
                <IconMdiContentCopy />
                {{ i18n.t("apiKeys.copy") }}
              </Button>
            </Tooltip>
          </div>
        </div>
        <Button
          variant="ghost"
          tone="neutral"
          size="sm"
          icon-only
          :title="i18n.t('general.close')"
          :aria-label="i18n.t('general.close')"
          @click="createdKey = undefined"
        >
          <IconMdiClose />
        </Button>
      </div>
    </div>

    <section class="mt-5">
      <h3 class="text-lg font-bold">{{ i18n.t("apiKeys.createNew") }}</h3>

      <div class="mt-3 max-w-md">
        <InputText v-model="name" :label="i18n.t('apiKeys.name')" :rules="[required(), minLength()(5), maxLength()(36), validApiKeyName()(auth.user.name)]" />
      </div>

      <div class="mt-4 flex flex-wrap items-center gap-2">
        <h3 class="flex-grow font-semibold">{{ i18n.t("apiKeys.permissions") }}</h3>
        <span class="text-sm text-gray-secondary tabular-nums">{{ selectedPerms.length }}/{{ possiblePerms?.length ?? 0 }}</span>
        <Button variant="ghost" tone="neutral" size="sm" @click="toggleAll">
          {{ i18n.t(allSelected ? "apiKeys.clearAll" : "apiKeys.selectAll") }}
        </Button>
      </div>

      <InputGroup v-model="selectedPerms" full-width>
        <div class="mt-2 grid gap-x-4 gap-y-1 sm:grid-cols-2 lg:grid-cols-3">
          <InputCheckbox v-for="perm in possiblePerms" :key="perm" v-model="selectedPerms" :label="permLabel(perm)" :value="perm" />
        </div>
      </InputGroup>

      <div class="mt-5 flex flex-wrap items-center gap-2">
        <h3 class="flex-grow font-semibold">{{ i18n.t("apiKeys.scope.title") }}</h3>
        <Button v-if="scope === 'selected' && selectedProjects.length > 0" variant="ghost" tone="neutral" size="sm" @click="selectedProjects = []">
          {{ i18n.t("apiKeys.clearAll") }}
        </Button>
      </div>
      <p class="mt-0.5 text-sm text-gray-secondary">{{ i18n.t("apiKeys.scope.sub") }}</p>

      <SegmentedControl
        v-model="scope"
        class="mt-2"
        :aria-label="i18n.t('apiKeys.scope.title')"
        :options="[
          { value: 'all', label: i18n.t('apiKeys.scope.all') },
          { value: 'selected', label: i18n.t('apiKeys.scope.selected'), count: selectedProjects.length },
        ]"
      />

      <div v-if="scope === 'selected'" class="mt-3">
        <div v-if="scopableProjects?.length" class="max-w-2xl">
          <InputText
            v-if="scopableProjects.length > 8"
            v-model="projectFilter"
            class="mb-2 max-w-xs"
            :label="i18n.t('apiKeys.scope.filter')"
            autocomplete="off"
          />
          <div class="max-h-72 flex flex-col gap-0.5 overflow-y-auto rounded-md border border-gray-300 p-1 dark:border-gray-700">
            <button
              v-for="project in filteredProjects"
              :key="project.namespace.owner + '/' + project.namespace.slug"
              type="button"
              class="w-full flex flex-shrink-0 items-center gap-2 rounded px-2 py-1.5 text-left transition-colors"
              :class="selectedProjects.includes(project.namespace.slug) ? 'background-card' : 'hover:background-card'"
              :aria-pressed="selectedProjects.includes(project.namespace.slug)"
              @click="toggleProject(project.namespace.slug)"
            >
              <span
                class="h-4 w-4 flex flex-shrink-0 items-center justify-center rounded-sm border transition-colors"
                :class="selectedProjects.includes(project.namespace.slug) ? 'ak-box-on' : 'border-gray-400 dark:border-gray-500'"
              >
                <IconMdiCheckBold v-if="selectedProjects.includes(project.namespace.slug)" class="text-[10px]" />
              </span>
              <UserAvatar size="xs" class="flex-shrink-0" :img-src="project.avatarUrl" :monogram-name="project.name" disable-link />
              <span class="min-w-0 flex-1">
                <span class="block truncate font-medium">{{ project.name }}</span>
                <span class="block truncate text-xs text-gray-secondary">{{ project.namespace.owner }}/{{ project.namespace.slug }}</span>
              </span>
            </button>
            <p v-if="filteredProjects.length === 0" class="px-2 py-1.5 text-sm text-gray-secondary">
              {{ i18n.t("apiKeys.scope.noMatches", [projectFilter]) }}
            </p>
          </div>
        </div>
        <p v-else class="text-sm text-gray-secondary">{{ i18n.t("apiKeys.scope.noProjects") }}</p>
      </div>

      <h3 class="mt-5 font-semibold">{{ i18n.t("apiKeys.expiration.title") }}</h3>
      <p class="mt-0.5 text-sm text-gray-secondary">{{ i18n.t("apiKeys.expiration.sub") }}</p>

      <div class="mt-2 max-w-md flex flex-wrap items-center gap-3">
        <InputDropdown v-model="expiration" :values="expirationOptions" />
        <!-- no floating label: a native date input already shows its own format placeholder, and the two overlap -->
        <div v-if="expiration === 'custom'" class="w-48">
          <InputDate v-model="customExpiration" :min="today" :aria-label="i18n.t('apiKeys.expiration.date')" />
        </div>
      </div>
      <p v-if="expiresAt && !invalidExpiration" class="mt-1.5 text-sm text-gray-secondary">
        {{ i18n.t("apiKeys.expiration.expiresOn") }} <PrettyTime :time="expiresAt" long />
      </p>

      <div class="mt-4 flex flex-wrap items-center gap-3 border-t border-gray-300 pt-3 dark:border-gray-700">
        <p class="flex-grow text-sm text-gray-secondary">
          <template v-if="selectedPerms.length === 0">{{ i18n.t("apiKeys.permissionRequired") }}</template>
          <template v-else-if="invalidScope">{{ i18n.t("apiKeys.scope.projectRequired") }}</template>
          <template v-else-if="invalidExpiration">{{ i18n.t("apiKeys.expiration.dateRequired") }}</template>
          <template v-else>{{ i18n.t("apiKeys.createHint") }}</template>
        </p>
        <Button
          :disabled="v.$invalid || loadingCreate || v.$pending || selectedPerms.length === 0 || invalidScope || invalidExpiration"
          :loading="loadingCreate"
          @click="create"
        >
          <IconMdiPlus />
          {{ i18n.t("apiKeys.createKey") }}
        </Button>
      </div>
    </section>

    <section class="mt-8">
      <div class="flex items-center gap-2 border-b border-gray-300 pb-2 dark:border-gray-700">
        <h3 class="flex-grow text-lg font-bold">{{ i18n.t("apiKeys.existing") }}</h3>
        <span class="text-sm text-gray-secondary tabular-nums">{{ apiKeys?.length ?? 0 }}</span>
      </div>

      <ul v-if="apiKeys?.length" class="divide-y divide-gray-300 dark:divide-gray-700">
        <li v-for="key in apiKeys" :key="key.name" class="flex flex-wrap items-start gap-3 py-3">
          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-baseline gap-x-2">
              <span class="font-semibold">{{ key.name }}</span>
              <code class="text-xs text-gray-secondary">{{ key.tokenIdentifier }}</code>
              <Chip v-if="isExpired(key)" tone="red">{{ i18n.t("apiKeys.expiration.expired") }}</Chip>
            </div>
            <div class="mt-1 flex flex-wrap gap-1">
              <Chip v-for="perm in key.permissions" :key="perm">{{ permLabel(perm) }}</Chip>
            </div>
            <div v-if="key.projectScoped" class="mt-1 flex flex-wrap items-center gap-1">
              <span class="text-xs text-gray-secondary">{{ i18n.t("apiKeys.scope.limitedTo") }}</span>
              <Chip v-for="project in key.projects" :key="project.slug" tone="primary">{{ project.slug }}</Chip>
              <span v-if="key.projects.length === 0" class="text-xs text-gray-secondary">{{ i18n.t("apiKeys.scope.noneLeft") }}</span>
            </div>
            <div class="mt-1.5 text-xs text-gray-secondary">
              <template v-if="key.lastUsed">{{ i18n.t("apiKeys.lastUsed") }}: <PrettyTime :time="key.lastUsed" long /></template>
              <template v-else>{{ i18n.t("apiKeys.neverUsed") }}</template>
              <template v-if="key.expiresAt"> &middot; {{ expiryLabel(key) }} <PrettyTime :time="key.expiresAt" long /> </template>
            </div>
          </div>
          <Button variant="outline" tone="danger" size="sm" :loading="loadingDelete[key.name]" :title="i18n.t('general.delete')" @click="deleteKey(key)">
            <IconMdiDelete />
            {{ i18n.t("general.delete") }}
          </Button>
        </li>
      </ul>
      <div v-else class="flex flex-col items-center py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiKeyVariant />
        </div>
        <p class="text-gray-secondary">{{ i18n.t("apiKeys.noKeys") }}</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.ak-box-on {
  border-color: var(--gray-500);
  background-color: var(--gray-500);
  color: var(--gray-50);
}

.dark .ak-box-on {
  border-color: var(--gray-400);
  background-color: var(--gray-400);
  color: var(--gray-900);
}
</style>
