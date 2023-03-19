<script lang="ts" setup>
import Draggable from "vuedraggable";
import { ref } from "vue";
import { LinkSection } from "hangar-api";
import { useVModel } from "@vueuse/core";
import InputText from "~/lib/components/ui/InputText.vue";
import InputSelect from "~/lib/components/ui/InputSelect.vue";
import ProjectLinksFormInner from "~/components/projects/ProjectLinksFormInner.vue";
import Button from "~/lib/components/design/Button.vue";
import Card from "~/lib/components/design/Card.vue";
import { maxLength, minLength, required } from "~/lib/composables/useValidationHelpers";
import { useBackendData } from "~/store/backendData";

const props = defineProps<{ modelValue: LinkSection[] }>();
const emit = defineEmits(["update:modelValue"]);
const sections = useVModel(props, "modelValue", emit);

function addSection() {
  let nextId = Math.max(...sections.value.map((l) => l.id)) + 1;
  if (nextId === -Infinity) {
    nextId = 0;
  }
  sections.value.push({ id: nextId, type: "sidebar", title: "", links: [] });
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

            <InputSelect v-model="section.type" :values="['top', 'sidebar']" label="Type" :rules="[required()]" />
            <InputText
              v-model="section.title"
              label="Title"
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
      <Button @click="addSection">Add section</Button>
    </template>
  </Draggable>
</template>

<style>
.sortable-ghost {
  filter: opacity(0.7);
}
</style>
