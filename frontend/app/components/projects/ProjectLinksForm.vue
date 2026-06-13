<script lang="ts" setup>
import Draggable from "vuedraggable";
import type { LinkSection } from "#shared/types/backend";

const props = defineProps<{ modelValue: LinkSection[] }>();
const emit = defineEmits(["update:modelValue"]);
const sections = useVModel(props, "modelValue", emit);
const i18n = useI18n();
const typeOptions = computed(() => [
  { value: "top", text: i18n.t("project.settings.links.top") },
  { value: "sidebar", text: i18n.t("project.settings.links.sidebar") },
]);

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

function selectType(section: LinkSection, type: LinkSection["type"]) {
  section.type = type;
  if (type === "top") section.title = "";
}
</script>

<template>
  <div v-if="sections.length === 0" class="flex min-h-48 flex-col items-center justify-center px-4 py-10 text-center">
    <p class="font-semibold">No link sections yet</p>
    <p class="mt-1 max-w-md text-sm text-gray">Add a section to organize the links displayed on your project.</p>
    <Button size="small" button-type="secondary" class="mt-4 !h-9 !px-3 text-sm" @click="addSection">
      <IconMdiPlus class="mr-1" />
      {{ i18n.t("project.settings.links.addSection") }}
    </Button>
  </div>

  <Draggable v-model="sections" tag="ul" :animation="200" group="sections" handle=".handle" item-key="id">
    <template #item="{ element: section, index }">
      <li class="mb-3 overflow-hidden rounded-xl border border-gray-200 bg-gray-50/40 dark:border-gray-800 dark:bg-charcoal-700/30">
        <div class="flex flex-col gap-3 bg-gray-100/60 p-2.5 dark:bg-charcoal-600 sm:flex-row sm:items-center">
          <button
            type="button"
            class="handle inline-flex h-11 w-7 flex-shrink-0 cursor-grab items-center justify-center text-gray"
            aria-label="Reorder link section"
          >
            <IconMdiDrag />
          </button>

          <div class="background-default inline-flex w-fit items-center gap-1 rounded-xl border border-gray-200 p-1 dark:border-gray-800">
            <button
              v-for="type in typeOptions"
              :key="type.value"
              type="button"
              class="inline-flex h-9 items-center justify-center rounded-lg border px-2.5 text-sm font-semibold leading-normal transition-all duration-250 hover:border-gray-300 hover:bg-gray-200 dark:hover:border-gray-700 dark:hover:bg-gray-800"
              :class="section.type === type.value ? 'border-primary-500' : 'border-transparent'"
              :style="
                section.type === type.value
                  ? {
                      backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                      borderColor: 'var(--primary-500)',
                    }
                  : {}
              "
              @click="selectType(section, type.value)"
            >
              {{ type.text }}
            </button>
          </div>

          <div v-if="section.type !== 'top'" class="h-11 min-w-0 flex-grow [&>div>label]:h-11">
            <InputText
              v-model="section.title"
              :placeholder="i18n.t('project.settings.links.titleField')"
              :rules="[
                required(),
                maxLength()(useBackendData.validations.project.pageName.max!),
                minLength()(useBackendData.validations.project.pageName.min!),
              ]"
            />
          </div>

          <button
            type="button"
            class="ml-auto inline-flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-lg border border-gray-200 text-gray transition-colors hover:border-red-600 hover:bg-red-900/30 hover:text-red-300 dark:border-gray-800"
            aria-label="Remove link section"
            @click="removeSection(index)"
          >
            <IconMdiBin />
          </button>
        </div>

        <div class="border-t border-gray-200 bg-white/30 dark:border-gray-800 dark:bg-charcoal-700/35">
          <ProjectLinksFormInner v-model="section.links" />
        </div>
      </li>
    </template>
    <template #footer>
      <Button v-if="sections.length > 0" size="small" button-type="secondary" class="!h-9 !px-3 text-sm" @click="addSection">
        <IconMdiPlus class="mr-1" />
        {{ i18n.t("project.settings.links.addSection") }}
      </Button>
    </template>
  </Draggable>
</template>

<style>
.sortable-ghost {
  filter: opacity(0.7);
}
</style>
