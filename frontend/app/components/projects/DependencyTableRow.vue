<script lang="ts" setup>
import type { PaginatedResultProject, PluginDependency } from "#shared/types/backend";
import { validUrl } from "~/composables/useValidationHelpers";

defineProps<{
  noEditing?: boolean;
  idx?: number;
}>();

const emit = defineEmits<{
  (e: "delete"): void;
}>();
const dep = defineModel<PluginDependency & { mode: "url" | "file" }>({ required: true });
const id = useId();

const { t } = useI18n();
const route = useRoute("user-project");

const completionResult = ref<string[]>([]);

async function onSearch(val: string | undefined) {
  if (val) {
    const projects = await useApi<PaginatedResultProject>(`projects?limit=25&offset=0&q=${val.replace("/", " ")}`);
    completionResult.value = projects.result
      .filter((p) => p.namespace.owner !== route.params.user || p.namespace.slug !== route.params.project)
      .map((p) => p.name);
  }
}
</script>

<template>
  <tr class="border-b last:border-b-0 dark:border-gray-800">
    <td class="px-2 py-3 align-middle">
      <div class="inline-flex items-center gap-1 rounded-lg border border-gray-300 p-1 dark:border-gray-700">
        <button
          v-for="mode in ['file', 'url'] as const"
          :key="mode"
          type="button"
          class="inline-flex h-8 items-center justify-center rounded-md border px-2 text-xs font-semibold transition-all duration-250 hover:border-gray-300 hover:bg-gray-200 dark:hover:border-gray-700 dark:hover:bg-gray-800"
          :class="dep.mode === mode ? 'border-primary-500' : 'border-transparent'"
          :style="
            dep.mode === mode
              ? {
                  backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                  borderColor: 'var(--primary-500)',
                }
              : {}
          "
          :disabled="noEditing"
          @click="dep.mode = mode"
        >
          {{ mode === "file" ? "Hangar" : "URL" }}
        </button>
      </div>
    </td>
    <td class="min-w-0 px-3 py-3 align-middle">
      <InputAutocomplete
        v-if="dep.mode === 'file'"
        :id
        v-model="dep.name"
        :placeholder="t('version.new.form.hangarProject')"
        :values="completionResult"
        :item-text="dep.name"
        :item-value="dep.name"
        :disabled="noEditing"
        :rules="[required(t('version.new.form.hangarProject'))]"
        :name="'hangarproject-' + idx"
        @search="onSearch($event)"
        @change="dep.externalUrl = undefined"
      />
      <InputText
        v-else
        v-model.trim="dep.externalUrl"
        :placeholder="t('version.new.form.externalUrl')"
        :disabled="noEditing"
        :rules="[required(t('version.new.form.externalUrl')), validUrl()]"
        clearable
        :name="'externalurl-' + idx"
      />
    </td>
    <td class="min-w-0 px-2 py-3 align-middle">
      <InputText
        v-if="dep.mode === 'url'"
        v-model.trim="dep.name"
        :placeholder="t('general.name')"
        :rules="[required(t('general.name'))]"
        :disabled="noEditing"
        :name="'name-' + idx"
      />
      <span v-else class="text-gray">Project name</span>
    </td>
    <td class="px-2 py-3 text-center align-middle">
      <label class="inline-flex cursor-pointer items-center justify-center">
        <input v-model="dep.required" type="checkbox" class="peer sr-only" :disabled="noEditing" />
        <span
          class="inline-flex h-5 w-5 items-center justify-center rounded border border-gray-400 bg-gray-200 text-white transition-colors peer-checked:border-primary-500 peer-checked:bg-primary-500 peer-focus-visible:ring-2 peer-focus-visible:ring-primary-500/50 peer-disabled:cursor-not-allowed peer-disabled:opacity-50 dark:border-gray-600 dark:bg-gray-700"
        >
          <IconMdiCheck v-if="dep.required" class="text-sm" />
        </span>
      </label>
    </td>
    <td v-if="!noEditing" class="px-2 py-3 text-right align-middle">
      <button
        type="button"
        class="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-transparent text-gray transition-colors hover:border-red-600 hover:bg-red-900/30 hover:text-red-300"
        aria-label="Remove dependency"
        @click="emit('delete')"
      >
        <IconMdiDeleteOutline />
      </button>
    </td>
  </tr>
</template>
