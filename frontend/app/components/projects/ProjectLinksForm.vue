<script lang="ts" setup>
import Draggable from "vuedraggable";
import type { LinkSection } from "#shared/types/backend";

const props = defineProps<{ modelValue: LinkSection[] }>();
const emit = defineEmits(["update:modelValue"]);
const sections = useVModel(props, "modelValue", emit);
const i18n = useI18n();

// only top is not allowed to be duplicated... (#1342)
const types = computed(() => sections.value.map((l) => (l.type === "top" ? l.type : l.type + "-" + Math.random())));

function addSection() {
  let nextId = Math.max(...sections.value.map((l) => l.id)) + 1;
  if (nextId === -Infinity) {
    nextId = 0;
  }
  sections.value.push({ id: nextId, type: "top", title: "", links: [] });
}

function removeSection(index: number) {
  sections.value.splice(index, 1);
}
</script>

<template>
  <div>
    <Draggable v-model="sections" tag="ul" :animation="200" group="sections" handle=".handle" item-key="id" class="flex flex-col gap-3">
      <template #item="{ element: section, index }">
        <li class="overflow-hidden rounded-md border border-gray-300 dark:border-gray-700">
          <div class="flex flex-wrap items-end gap-2 border-b border-gray-300 px-3 py-2.5 dark:border-gray-700">
            <IconMdiMenu class="handle mb-2 flex-shrink-0 cursor-grab text-gray-secondary" />

            <div class="flex-shrink-0">
              <InputDropdown
                v-model="section.type"
                :values="[
                  { value: 'top', text: i18n.t('project.settings.links.top') },
                  { value: 'sidebar', text: i18n.t('project.settings.links.sidebar') },
                ]"
                :label="i18n.t('project.settings.links.typeField')"
                :rules="[required(), noDuplicated('Can only have one top section')(() => types)]"
              />
            </div>
            <div v-if="section.type !== 'top'" class="min-w-50 flex-1">
              <InputText
                v-model="section.title"
                :label="i18n.t('project.settings.links.titleField')"
                :rules="[
                  required(),
                  maxLength()(useBackendData.validations.project.pageName.max!),
                  minLength()(useBackendData.validations.project.pageName.min!),
                ]"
              />
            </div>

            <Button
              variant="ghost"
              tone="danger"
              size="sm"
              icon-only
              class="mb-0.5 ml-auto flex-shrink-0"
              :title="i18n.t('general.delete')"
              :aria-label="i18n.t('general.delete')"
              @click="removeSection(index)"
            >
              <IconMdiDelete />
            </Button>
          </div>

          <div class="p-3">
            <ProjectLinksFormInner v-model="section.links" />
          </div>
        </li>
      </template>
    </Draggable>

    <Button variant="outline" tone="neutral" size="sm" class="mt-3" @click="addSection">
      <IconMdiPlus />
      {{ i18n.t("project.settings.links.addSection") }}
    </Button>
  </div>
</template>

<style>
.sortable-ghost {
  filter: opacity(0.7);
}
</style>
