<script lang="ts" setup>
import type { Tab } from "#shared/types/components/design/Tabs";
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

const tabs = [
  { value: "file", header: "Hangar" },
  { value: "url", header: "URL" },
] as const satisfies Tab<string>[];

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
    <td class="flex flex-wrap gap-2">
      <Tabs v-model="dep.mode" :tabs="tabs" class="items-center -ml-2" compact>
        <template #file>
          <InputAutocomplete
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
        </template>
        <template #url>
          <InputText
            v-model.trim="dep.externalUrl"
            :placeholder="t('version.new.form.externalUrl')"
            :disabled="noEditing"
            :rules="[required(t('version.new.form.externalUrl')), validUrl()]"
            clearable
            :name="'externalurl-' + idx"
          />
        </template>
      </Tabs>
    </td>
    <td v-if="dep.mode === 'url'">
      <InputText
        v-model.trim="dep.name"
        dense
        hide-details
        flat
        :label="t('general.name')"
        :rules="[required(t('general.name'))]"
        :disabled="noEditing"
        :name="'name-' + idx"
      />
    </td>
    <td v-else />
    <td>
      <InputCheckbox v-model="dep.required" :disabled="noEditing" />
    </td>
    <td v-if="!noEditing">
      <Button button-type="red" @click="emit('delete')"><IconMdiDelete /></Button>
    </td>
  </tr>
</template>
