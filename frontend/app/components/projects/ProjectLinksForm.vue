<script lang="ts" setup>
import Draggable from "vuedraggable";
import type { LinkSection } from "#shared/types/backend";

const props = defineProps<{ modelValue: LinkSection[] }>();
const emit = defineEmits(["update:modelValue"]);
const sections = useVModel(props, "modelValue", emit);
const i18n = useI18n();

// mirrors LinkSectionType on the backend
const MAX_TOP_LINKS = 5;
const MAX_SIDEBAR_LINKS = 10;

const topSection = computed(() => sections.value.find((section) => section.type === "top"));
const sidebarSections = computed({
  get: () => sections.value.filter((section) => section.type !== "top"),
  set: (value) => (sections.value = topSection.value ? [topSection.value, ...value] : value),
});

function nextId(items: { id: number }[]) {
  return items.length === 0 ? 0 : Math.max(...items.map((item) => item.id)) + 1;
}

function addTopLink() {
  sections.value.unshift({ id: nextId(sections.value), type: "top", title: "", links: [{ id: 0, name: "", url: "" }] });
}

function addSection() {
  sections.value.push({ id: nextId(sections.value), type: "sidebar", title: "", links: [] });
}

function removeSection(section: LinkSection) {
  sections.value.splice(sections.value.indexOf(section), 1);
}

// the top section carries no title of its own, so an emptied one is dropped rather than saved as a stub
watch(
  () => topSection.value?.links.length,
  (count) => {
    if (count === 0 && topSection.value) removeSection(topSection.value);
  }
);
</script>

<template>
  <div class="flex flex-col gap-7">
    <section>
      <div class="border-b border-gray-300 pb-2 dark:border-gray-700">
        <div class="flex items-center gap-2">
          <h3 class="flex-grow font-semibold">{{ i18n.t("project.settings.links.topTitle") }}</h3>
          <span class="text-sm text-gray-secondary tabular-nums">{{ topSection?.links?.length ?? 0 }}/{{ MAX_TOP_LINKS }}</span>
        </div>
        <p class="mt-0.5 text-sm text-gray-secondary">{{ i18n.t("project.settings.links.topSub") }}</p>
      </div>

      <ProjectLinksFormInner v-if="topSection" v-model="topSection.links" :max="MAX_TOP_LINKS" class="mt-3" />
      <Button v-else variant="outline" tone="neutral" size="sm" class="mt-3" @click="addTopLink">
        <IconMdiPlus />
        {{ i18n.t("project.settings.links.addLink") }}
      </Button>
    </section>

    <section>
      <div class="border-b border-gray-300 pb-2 dark:border-gray-700">
        <div class="flex flex-wrap items-center gap-2">
          <h3 class="flex-grow font-semibold">{{ i18n.t("project.settings.links.sidebarTitle") }}</h3>
          <Button variant="outline" tone="neutral" size="sm" @click="addSection">
            <IconMdiPlus />
            {{ i18n.t("project.settings.links.addSection") }}
          </Button>
        </div>
        <p class="mt-0.5 text-sm text-gray-secondary">{{ i18n.t("project.settings.links.sidebarSub") }}</p>
      </div>

      <p v-if="sidebarSections.length === 0" class="mt-3 text-sm text-gray-secondary">No sections</p>
      <Draggable
        v-else
        v-model="sidebarSections"
        tag="ul"
        :animation="200"
        group="sections"
        handle=".section-handle"
        item-key="id"
        class="mt-1 divide-y divide-gray-300 dark:divide-gray-700"
      >
        <template #item="{ element: section }">
          <li class="py-3">
            <div class="flex items-center gap-2">
              <IconMdiDragVertical class="section-handle shrink-0 cursor-grab text-xl text-gray-secondary active:cursor-grabbing hover:color-primary" />
              <div class="min-w-50 max-w-100 flex-1">
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
              <span class="ml-auto shrink-0 text-sm text-gray-secondary tabular-nums">{{ section.links.length }}/{{ MAX_SIDEBAR_LINKS }}</span>
              <Button
                variant="ghost"
                tone="danger"
                size="sm"
                icon-only
                class="shrink-0"
                :title="i18n.t('project.settings.links.removeSection')"
                :aria-label="i18n.t('project.settings.links.removeSection')"
                @click="removeSection(section)"
              >
                <IconMdiDelete />
              </Button>
            </div>

            <ProjectLinksFormInner v-model="section.links" :max="MAX_SIDEBAR_LINKS" class="mt-2 pl-7" />
          </li>
        </template>
      </Draggable>
    </section>
  </div>
</template>

<style>
.sortable-ghost {
  filter: opacity(0.7);
}
</style>
