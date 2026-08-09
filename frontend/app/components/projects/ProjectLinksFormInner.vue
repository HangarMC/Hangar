<script lang="ts" setup>
import Draggable from "vuedraggable";
import type { LinkSection } from "#shared/types/backend";

const props = defineProps<{ modelValue: LinkSection["links"]; max: number }>();
const emit = defineEmits(["update:modelValue"]);
const model = useVModel(props, "modelValue", emit);
const i18n = useI18n();

function remove(index: number) {
  model.value.splice(index, 1);
}

function add() {
  const links = model.value ?? [];
  links.push({ id: links.length === 0 ? 0 : Math.max(...links.map((link) => link.id)) + 1, name: "", url: "" });
  model.value = links;
}
</script>

<template>
  <div>
    <Draggable v-model="model" tag="ul" :animation="200" group="link-sections" handle=".handle" item-key="id" class="flex flex-col gap-2">
      <template #item="{ element, index }">
        <li class="flex items-center gap-2">
          <IconMdiDragVertical class="handle shrink-0 cursor-grab text-xl text-gray-secondary active:cursor-grabbing hover:color-primary" />

          <div class="min-w-40 flex-1">
            <InputText
              v-model="element.name"
              :label="i18n.t('project.settings.links.nameField')"
              :rules="[
                required(),
                maxLength()(useBackendData.validations.project.pageName.max!),
                minLength()(useBackendData.validations.project.pageName.min!),
              ]"
            />
          </div>
          <div class="min-w-60 flex-[2]">
            <InputText v-model="element.url" :label="i18n.t('project.settings.links.urlField')" :rules="[validUrl(), required()]" />
          </div>

          <Button
            variant="ghost"
            tone="danger"
            size="sm"
            icon-only
            class="shrink-0"
            :title="i18n.t('general.delete')"
            :aria-label="i18n.t('general.delete')"
            @click="remove(index)"
          >
            <IconMdiDelete />
          </Button>
        </li>
      </template>
    </Draggable>

    <Button variant="ghost" tone="neutral" size="sm" class="mt-2" :disabled="(model?.length ?? 0) >= max" @click="add">
      <IconMdiPlus />
      {{ i18n.t("project.settings.links.addLink") }}
    </Button>
  </div>
</template>
