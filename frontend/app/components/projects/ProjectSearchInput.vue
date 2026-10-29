<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";
import type { PaginatedResultProject, Project } from "#shared/types/backend";

const props = defineProps<{
  label: string;
  name?: string;
  rules?: ValidationRule<string | undefined>[];
  excludeOwner?: string;
  excludeSlug?: string;
}>();

const model = defineModel<string | undefined>();
const { t } = useI18n();

async function search(query: string): Promise<Project[]> {
  const projects = await useApi<PaginatedResultProject>(`projects?limit=10&offset=0&q=${encodeURIComponent(query.replaceAll("/", " "))}`);
  return projects.result.filter((p) => p.namespace.owner !== props.excludeOwner || p.namespace.slug !== props.excludeSlug);
}
</script>

<template>
  <SearchSelect
    v-model="model"
    :label
    :name
    :rules
    :search
    :option-key="(project: Project) => project.id"
    :option-value="(project: Project) => project.namespace.slug"
    :empty-message="t('version.deps.noProjects')"
  >
    <template #option="{ item }">
      <UserAvatar size="xs" class="flex-shrink-0" :img-src="item.avatarUrl" :monogram-name="item.name" disable-link />
      <span class="min-w-0 flex-1">
        <span class="block truncate font-medium">{{ item.name }}</span>
        <span class="block truncate text-xs text-gray-secondary">{{ item.namespace.owner }}/{{ item.namespace.slug }}</span>
      </span>
    </template>
  </SearchSelect>
</template>
