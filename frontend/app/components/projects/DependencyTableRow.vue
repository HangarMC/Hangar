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

const modeOptions: { value: "file" | "url"; label: string }[] = [
  { value: "file", label: "Hangar" },
  { value: "url", label: "URL" },
];

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
  <tr>
    <td class="align-middle">
      <SegmentedControl v-if="!noEditing" v-model="dep.mode" :options="modeOptions" :aria-label="t('version.new.form.linkOrProject')" />
      <span v-else class="text-sm text-gray-secondary">{{ dep.mode === "url" ? "URL" : "Hangar" }}</span>
    </td>
    <td class="align-middle">
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
    <td class="align-middle">
      <InputText
        v-if="dep.mode === 'url'"
        v-model.trim="dep.name"
        :placeholder="t('general.name')"
        :rules="[required(t('general.name'))]"
        :disabled="noEditing"
        :name="'name-' + idx"
      />
      <span v-else class="text-gray-secondary">&mdash;</span>
    </td>
    <td class="align-middle">
      <div class="flex justify-center">
        <InputCheckbox v-model="dep.required" :disabled="noEditing" :aria-label="t('general.required')" />
      </div>
    </td>
    <td v-if="!noEditing" class="align-middle">
      <Button variant="ghost" tone="danger" size="sm" icon-only :title="t('general.delete')" :aria-label="t('general.delete')" @click="emit('delete')">
        <IconMdiDelete />
      </Button>
    </td>
  </tr>
</template>
