<script lang="ts" setup>
import { refDebounced } from "@vueuse/core";
import type { Header } from "#shared/types/components/SortableTable";

const props = defineProps<{
  directory: "authors" | "staff";
}>();

const i18n = useI18n();
const router = useRouter();
const route = useRoute();

const selectedTab = computed({
  get: () => props.directory,
  set: (value) => router.push(value === "staff" ? "/staff" : "/authors"),
});

const tabs = computed(() => [
  { value: "authors" as const, label: i18n.t("pages.authorsTitle") },
  { value: "staff" as const, label: i18n.t("pages.staffTitle") },
]);

const isStaff = computed(() => props.directory === "staff");

const headers = computed(
  () =>
    [
      { name: "name", title: i18n.t("pages.headers.username"), sortable: true },
      isStaff.value
        ? { name: "roles", title: i18n.t("pages.headers.roles"), sortable: true }
        : { name: "projectCount", title: i18n.t("pages.headers.projects"), sortable: true },
      { name: "createdAt", title: i18n.t("pages.headers.joined"), sortable: true, width: "10rem" },
    ] satisfies Header<string>[]
);

const page = ref(0);
const sort = ref<string[]>(isStaff.value ? ["roles"] : ["-projectCount"]);
const query = ref<string>();
const debouncedQuery = refDebounced(query, 300);

watch(debouncedQuery, () => (page.value = 0));

const requestParams = computed(() => {
  const limit = 25;
  return {
    query: debouncedQuery.value,
    limit,
    offset: page.value * limit,
    sort: sort.value,
  };
});

const { users } = useUserDirectory(
  () => props.directory,
  () => requestParams.value
);

function updateSort(col: string, sorter: Record<string, number>) {
  sort.value = Object.entries(sorter)
    .map(([k, value]) => {
      const val = value;
      if (val === -1) return "-" + k;
      if (val === 1) return k;
      return;
    })
    .filter((v) => v !== undefined) as string[];
}

useSeo(
  computed(() => ({
    title: i18n.t(isStaff.value ? "pages.staffTitle" : "pages.authorsTitle"),
    description: isStaff.value ? "Hangar Staff" : "Hangar Project Authors",
    route,
  }))
);
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t(isStaff ? "pages.staffTitle" : "pages.authorsTitle") }}</PageTitle>

    <div class="mb-4 mt-3 flex flex-wrap items-center gap-3">
      <SegmentedControl v-model="selectedTab" :options="tabs" :aria-label="i18n.t('pages.authorsTitle')" />

      <div class="min-w-60 flex-1">
        <InputText v-model="query" :label="i18n.t('pages.headers.username')" />
      </div>

      <span v-if="users?.pagination" class="flex-shrink-0 text-sm text-gray-secondary tabular-nums">
        {{ users.pagination.count.toLocaleString("en-US") }} {{ i18n.t("pages.userCount", users.pagination.count) }}
      </span>
    </div>

    <SortableTable
      :headers="headers"
      :items="users?.result || []"
      :server-pagination="users?.pagination"
      :initial-sorter="isStaff ? { roles: 1 } : { projectCount: -1 }"
      @update:sort="updateSort"
      @update:page="(p) => (page = p)"
    >
      <template #name="{ item }">
        <NuxtLink :to="'/' + item.name" class="inline-flex min-w-0 items-center gap-2.5 hover:color-primary">
          <UserAvatar :username="item.name" :avatar-url="item.avatarUrl" size="xs" disable-link class="flex-shrink-0" />
          <span class="truncate font-semibold">{{ item.name }}</span>
        </NuxtLink>
      </template>

      <template #projectCount="{ item }">
        <span class="inline-flex items-center gap-1.5 tabular-nums">
          <IconMdiPackageVariantClosed class="flex-shrink-0 text-gray-secondary" />
          {{ item.projectCount?.toLocaleString("en-US") ?? 0 }}
        </span>
      </template>

      <template #roles="{ item }">
        <div class="flex flex-wrap gap-1">
          <Tag v-for="roleId in item.roles" :key="roleId" :color="{ background: getRole(roleId)?.color }" :name="getRole(roleId)?.title" />
        </div>
      </template>

      <template #createdAt="{ item }">
        <span class="whitespace-nowrap text-gray-secondary tabular-nums">{{ i18n.d(item.createdAt, "date") }}</span>
      </template>

      <template #empty>
        <div class="flex flex-col items-center gap-2 py-10 text-gray-secondary">
          <IconMdiAccountSearchOutline class="text-4xl" />
          <span>{{ i18n.t(isStaff ? "pages.noStaff" : "pages.noAuthors") }}</span>
        </div>
      </template>
    </SortableTable>
  </div>
</template>
