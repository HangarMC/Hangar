<script lang="ts" setup>
import Draggable from "vuedraggable";
import { useVModel } from "@vueuse/core";
import type { LinkSection } from "hangar-api";
import { useI18n } from "vue-i18n";
import InputText from "~/components/ui/InputText.vue";
import Button from "~/components/design/Button.vue";
import { maxLength, minLength, required, url } from "~/composables/useValidationHelpers";
import { useBackendData } from "~/store/backendData";

const props = defineProps<{ modelValue: LinkSection["links"] }>();
const emit = defineEmits(["update:modelValue"]);
const model = useVModel(props, "modelValue", emit);
const i18n = useI18n();

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
          :label="i18n.t('project.settings.links.nameField')"
          :rules="[required(), maxLength()(useBackendData.validations.project.pageName.max), minLength()(useBackendData.validations.project.pageName.min)]"
        />
        <InputText v-model="element.url" :label="i18n.t('project.settings.links.urlField')" :rules="[url(), required()]" />

        <IconMdiClose class="flex-shrink-0 cursor-pointer" @click="remove(index)" />
      </li>
    </template>
    <template #footer>
      <Button @click="add">{{ i18n.t("project.settings.links.addLink") }}</Button>
    </template>
  </Draggable>
</template>
