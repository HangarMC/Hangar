<script lang="ts" setup>
import Draggable from "vuedraggable";
import { useVModel } from "@vueuse/core";
import { LinkSection } from "hangar-api";
import InputText from "~/lib/components/ui/InputText.vue";
import Button from "~/lib/components/design/Button.vue";
import { maxLength, minLength, required, url } from "~/lib/composables/useValidationHelpers";
import { useBackendData } from "~/store/backendData";

const props = defineProps<{ modelValue: LinkSection["links"] }>();
const emit = defineEmits(["update:modelValue"]);
const model = useVModel(props, "modelValue", emit);

function remove(index: number) {
  model.value.splice(index, 1);
}
function add() {
  let nextId = Math.max(...model.value.map((l) => l.id)) + 1;
  if (nextId === -Infinity) {
    nextId = 0;
  }
  model.value.push({ id: nextId, name: "", url: "" });
}
</script>

<template>
  <Draggable v-model="model" tag="ul" :animation="200" group="link-sections" handle=".handle" item-key="id">
    <template #item="{ element, index }">
      <li class="flex items-center gap-2 mb-2">
        <IconMdiMenu class="handle flex-shrink-0 cursor-grab" />

        <InputText
          v-model="element.name"
          label="Name"
          :rules="[required(), maxLength()(useBackendData.validations.project.pageName.max), minLength()(useBackendData.validations.project.pageName.min)]"
        />
        <InputText v-model="element.url" label="URL" :rules="[url(), required()]" />

        <IconMdiClose class="flex-shrink-0 cursor-pointer" @click="remove(index)" />
      </li>
    </template>
    <template #footer>
      <Button @click="add">Add</Button>
    </template>
  </Draggable>
</template>
