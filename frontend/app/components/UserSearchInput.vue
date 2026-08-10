<script lang="ts" setup>
import type { PaginatedResultUser, User } from "#shared/types/backend";

const props = defineProps<{
  label: string;
  name?: string;
  errorMessages?: string[];
  exclude?: string[];
}>();

const model = defineModel<string | undefined>();
const { t } = useI18n();

// organization accounts can't hold a membership, so they never belong in the results
async function search(query: string): Promise<User[]> {
  const users = await useApi<PaginatedResultUser>("users", "get", { query, limit: 25, offset: 0 });
  return users.result.filter((u) => !u.isOrganization && !props.exclude?.includes(u.name)).slice(0, 10);
}
</script>

<template>
  <SearchSelect
    v-model="model"
    :label
    :name
    :search
    :error-messages
    :option-key="(user: User) => user.id"
    :option-value="(user: User) => user.name"
    :empty-message="t('form.memberList.noUsers')"
  >
    <template #option="{ item }">
      <UserAvatar size="xs" class="flex-shrink-0" :avatar-url="item.avatarUrl" :username="item.name" disable-link />
      <span class="min-w-0 flex-1">
        <span class="block truncate font-medium">{{ item.name }}</span>
        <span v-if="item.tagline" class="block truncate text-xs text-gray-secondary">{{ item.tagline }}</span>
      </span>
    </template>
  </SearchSelect>
</template>
