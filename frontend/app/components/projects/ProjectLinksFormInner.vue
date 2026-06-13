<script lang="ts" setup>
import Draggable from "vuedraggable";
import type { LinkSection } from "#shared/types/backend";

const props = defineProps<{ modelValue: LinkSection["links"] }>();
const emit = defineEmits(["update:modelValue"]);
const model = useVModel(props, "modelValue", emit);
const i18n = useI18n();

function remove(index: number) {
  model.value.splice(index, 1);
}
function add() {
  if (!model.value) {
    model.value = [{ id: 0, name: "", url: "" }];
    return;
  }
  let nextId = Math.max(...model.value.map((l) => l.id)) + 1;
  if (nextId === -Infinity) {
    nextId = 0;
  }
  model.value.push({ id: nextId, name: "", url: "" });
}
</script>

<template>
  <div>
    <div
      class="hidden grid-cols-[2rem_minmax(10rem,1fr)_minmax(14rem,1.5fr)_2.75rem] items-center gap-2 border-b border-gray-200 px-2.5 py-2 text-xs font-semibold text-gray dark:border-gray-800 sm:grid"
    >
      <span aria-hidden="true" />
      <span>{{ i18n.t("project.settings.links.nameField") }}</span>
      <span>{{ i18n.t("project.settings.links.urlField") }}</span>
      <span aria-hidden="true" />
    </div>

    <Draggable v-model="model" tag="ul" :animation="200" group="link-sections" handle=".handle" item-key="id">
      <template #item="{ element, index }">
        <li
          class="grid grid-cols-[2rem_minmax(0,1fr)_2.75rem] items-end gap-2 border-b border-gray-200 p-2.5 sm:grid-cols-[2rem_minmax(10rem,1fr)_minmax(14rem,1.5fr)_2.75rem] sm:items-center dark:border-gray-800"
        >
          <button type="button" class="handle inline-flex h-10.5 w-8 cursor-grab items-center justify-center text-gray" aria-label="Reorder link">
            <IconMdiDrag />
          </button>

          <div class="min-w-0">
            <span class="mb-1 block text-xs font-semibold text-gray sm:hidden">{{ i18n.t("project.settings.links.nameField") }}</span>
            <InputText
              v-model="element.name"
              :placeholder="i18n.t('project.settings.links.nameField')"
              :rules="[
                required(),
                maxLength()(useBackendData.validations.project.pageName.max!),
                minLength()(useBackendData.validations.project.pageName.min!),
              ]"
            />
          </div>

          <div class="col-start-2 min-w-0 sm:col-start-auto">
            <span class="mb-1 block text-xs font-semibold text-gray sm:hidden">{{ i18n.t("project.settings.links.urlField") }}</span>
            <InputText v-model="element.url" :placeholder="i18n.t('project.settings.links.urlField')" :rules="[validUrl(), required()]" />
          </div>

          <button
            type="button"
            class="row-start-1 inline-flex h-11 w-11 items-center justify-center rounded-lg border border-gray-200 text-gray transition-colors hover:border-red-600 hover:bg-red-900/30 hover:text-red-300 dark:border-gray-800 sm:row-start-auto"
            aria-label="Remove link"
            @click="remove(index)"
          >
            <IconMdiBin />
          </button>
        </li>
      </template>
      <template #footer>
        <li class="p-2.5">
          <Button size="small" button-type="secondary" class="!h-9 !px-3 text-sm" @click="add">
            <IconMdiPlus class="mr-1" />
            {{ i18n.t("project.settings.links.addLink") }}
          </Button>
        </li>
      </template>
    </Draggable>
  </div>
</template>
