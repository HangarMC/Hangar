<script lang="ts" setup>
import Draggable from "vuedraggable";
import { useI18n } from "vue-i18n";
import type { LinkSection } from "hangar-api";
import { useVModel } from "@vueuse/core";
import { computed } from "vue";
import InputText from "~/components/ui/InputText.vue";
import InputSelect from "~/components/ui/InputSelect.vue";
import ProjectLinksFormInner from "~/components/projects/ProjectLinksFormInner.vue";
import Button from "~/components/design/Button.vue";
import Card from "~/components/design/Card.vue";
import { maxLength, minLength, noDuplicated, required } from "~/composables/useValidationHelpers";
import { useBackendData } from "~/store/backendData";

const props = defineProps<{ modelValue: LinkSection[] }>();
const emit = defineEmits(["update:modelValue"]);
const sections = useVModel(props, "modelValue", emit);
const i18n = useI18n();

const types = computed(() => sections.value.map((l) => l.type));

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
  <Draggable v-model="sections" tag="ul" :animation="200" group="sections" handle=".handle" item-key="id">
    <template #item="{ element: section, index }">
      <li>
        <Card alternate-background class="mb-2">
          <div class="flex items-center gap-2 mb-2">
            <IconMdiMenu class="handle flex-shrink-0 cursor-grab" />
            <InputSelect
              v-model="section.type"
              :values="[
                { value: 'top', text: i18n.t('project.settings.links.top') },
                { value: 'sidebar', text: i18n.t('project.settings.links.sidebar') },
              ]"
              :label="i18n.t('project.settings.links.typeField')"
              :rules="[required(), noDuplicated()(() => types)]"
            />
            <InputText
              v-if="section.type !== 'top'"
              v-model="section.title"
              :label="i18n.t('project.settings.links.titleField')"
              :rules="[required(), maxLength()(useBackendData.validations.project.pageName.max), minLength()(useBackendData.validations.project.pageName.min)]"
            />

            <IconMdiClose class="flex-shrink-0 cursor-pointer" @click="removeSection(index)" />
          </div>

          <hr class="mb-2" />

          <ProjectLinksFormInner v-model="section.links" />
        </Card>
      </li>
    </template>
    <template #footer>
      <Button @click="addSection">{{ i18n.t("project.settings.links.addSection") }}</Button>
    </template>
  </Draggable>
</template>

<style>
.sortable-ghost {
  filter: opacity(0.7);
}
</style>
