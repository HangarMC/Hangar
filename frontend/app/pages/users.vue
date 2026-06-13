<script lang="ts" setup>
import type { Header } from "#shared/types/components/SortableTable";

const i18n = useI18n();
const route = useRoute("users");

const page = ref(0);
const sort = ref<string[]>(["-projectCount"]);
const query = ref("");
const staffOnly = ref(false);
const headers = computed(
  () =>
    [
      { name: "pic", title: "", sortable: false },
      { name: "name", title: i18n.t("pages.headers.username"), sortable: true },
      { name: "roles", title: i18n.t("pages.headers.roles"), sortable: true },
      { name: "createdAt", title: i18n.t("pages.headers.joined"), sortable: true },
      { name: "projectCount", title: i18n.t("pages.headers.projects"), sortable: !staffOnly.value },
    ] satisfies Header<string>[]
);
const authorRequestParams = computed(() => {
  const limit = 25;
  return {
    query: query.value,
    limit,
    offset: page.value * limit,
    sort: staffOnly.value ? ["-projectCount"] : sort.value,
  };
});
const staffRequestParams = computed(() => {
  const limit = 25;
  return {
    query: query.value,
    limit,
    offset: page.value * limit,
    sort: staffOnly.value ? sort.value : ["roles"],
  };
});
const { authors } = useAuthors(() => authorRequestParams.value);
const { staff } = useStaff(() => staffRequestParams.value);
const users = computed(() => (staffOnly.value ? staff.value : authors.value));

watch(query, () => {
  page.value = 0;
});

watch(staffOnly, (enabled) => {
  page.value = 0;
  sort.value = enabled ? ["roles"] : ["-projectCount"];
});

function updateSort(col: string, sorter: Record<string, number>) {
  if (staffOnly.value && col === "projectCount") {
    return;
  }

  sort.value = Object.keys(sorter)
    .map((k) => {
      const val = sorter[k];
      if (val === -1) return "-" + k;
      if (val === 1) return k;
      return;
    })
    .filter((v) => v !== undefined) as string[];
}

useSeo(computed(() => ({ title: "Users", description: "Browse Hangar users and staff", route })));
</script>

<template>
  <div class="flex flex-col gap-4">
    <div>
      <PageTitle>Users</PageTitle>
      <p class="mt-1 text-gray">Browse project creators and Hangar staff.</p>
    </div>

    <Card class="flex flex-col gap-3 sm:flex-row sm:items-center">
      <div class="relative flex h-10.5 min-w-0 flex-grow rounded-md transition-all duration-200">
        <input
          v-model="query"
          class="min-w-0 flex-grow truncate rounded-lg border border-transparent bg-gray-100 py-2 pr-9 pl-9 outline-none transition-colors hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-600"
          placeholder="Search users..."
          type="text"
        />
        <IconMdiMagnify class="absolute top-3 left-3 text-gray-500" />
        <button
          v-if="query"
          type="button"
          class="absolute top-1.25 right-1.5 inline-flex h-8 w-8 items-center justify-center rounded-md border border-transparent transition-all duration-250 hover:border-red-600 hover:bg-red-900/50"
          aria-label="Clear search"
          @click="query = ''"
        >
          <IconMdiClose />
        </button>
      </div>

      <button
        type="button"
        class="inline-flex h-10.5 flex-shrink-0 items-center justify-center gap-2 rounded-lg border border-gray-800 px-3 font-semibold transition-all duration-250 hover:border-gray-700 hover:bg-gray-800"
        :style="
          staffOnly
            ? {
                backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                borderColor: 'var(--primary-500)',
              }
            : {}
        "
        :aria-pressed="staffOnly"
        @click="staffOnly = !staffOnly"
      >
        <IconMdiShieldAccountOutline />
        Staff only
      </button>
    </Card>

    <Card class="!p-0 overflow-hidden">
      <SortableTable
        :key="staffOnly ? 'staff' : 'users'"
        :headers="headers"
        :items="users?.result || []"
        :server-pagination="users?.pagination"
        :initial-sorter="staffOnly ? { roles: 1 } : { projectCount: -1 }"
        hide-pagination
        @update:sort="updateSort"
        @update:page="(p) => (page = p)"
      >
        <template #pic="{ item }">
          <UserAvatar :username="item.name" :avatar-url="item.avatarUrl" size="sm" />
        </template>
        <template #createdAt="{ item }">
          <span class="inline-flex items-center gap-1.5 text-gray">
            <IconMdiCalendarOutline />
            {{ i18n.d(item.createdAt, "date") }}
          </span>
        </template>
        <template #name="{ item }">
          <Link :to="'/' + item.name" class="font-semibold">{{ item.name }}</Link>
        </template>
        <template #roles="{ item }">
          <div class="flex flex-wrap gap-1">
            <Tag v-for="roleId in item.roles" :key="roleId" :color="{ background: getRole(roleId)?.color }" :name="getRole(roleId)?.title" />
            <span v-if="item.roles.length === 0" class="text-sm text-gray">User</span>
          </div>
        </template>
        <template #projectCount="{ item }">
          <span class="inline-flex items-center gap-1.5">
            <IconMdiPackageVariantClosed />
            {{ item.projectCount }}
          </span>
        </template>
        <template #empty>
          <div class="py-10 text-center text-gray">
            {{ staffOnly ? "No staff found." : "No users found." }}
          </div>
        </template>
      </SortableTable>
    </Card>
    <PaginationButtons
      v-if="users && Math.ceil(users.pagination.count / users.pagination.limit) > 1"
      :page="page"
      :pages="Math.ceil(users.pagination.count / users.pagination.limit)"
      @update:page="(p) => (page = p)"
    />
  </div>
</template>
