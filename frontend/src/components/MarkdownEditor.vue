<script lang="ts" setup>
import { computed, reactive, ref, watch } from "vue";
import { ValidationRule } from "@vuelidate/core";
import InputTextarea from "~/lib/components/ui/InputTextarea.vue";
import Markdown from "~/components/Markdown.vue";
import Button from "~/lib/components/design/Button.vue";
import DeletePageModal from "~/components/modals/DeletePageModal.vue";
import { useValidation } from "~/lib/composables/useValidationHelpers";
import ErrorTooltip from "~/lib/components/design/ErrorTooltip.vue";

const props = withDefaults(
  defineProps<{
    raw: string;
    editing: boolean;
    deletable: boolean;
    cancellable: boolean;
    saveable: boolean;
    maxlength?: number;
    title?: string;
    errorMessages?: string[];
    rules?: ValidationRule<string | undefined>[];
  }>(),
  {
    maxlength: 30_000,
    title: undefined,
    errorMessages: undefined,
    rules: undefined,
  }
);

const emit = defineEmits<{
  (e: "save", edited: string): void;
  (e: "delete"): void;
  (e: "update:editing", editing: boolean): void;
}>();

const preview = ref(false);
const rawEdited = ref(props.raw || "");
const loading = reactive({
  save: false,
  delete: false,
});
const internalEditing = computed({
  get: () => props.editing,
  set: (value) => emit("update:editing", value),
});

const errorMessages = computed(() => props.errorMessages);
const { v, errors } = useValidation(props.title, props.rules, rawEdited, errorMessages);

defineExpose({ rawEdited });

watch(
  ref(props),
  (val) => {
    if (!val.editing) {
      preview.value = false;
      loading.save = false;
      loading.delete = false;
    }
  },
  { deep: true }
);

function savePage() {
  loading.save = true;
  emit("save", rawEdited.value);
}

function deletePage() {
  loading.delete = true;
  emit("delete");
}
</script>

<template>
  <div class="relative">
    <div class="flex">
      <h1 v-if="props.title" class="mt-3 ml-5 text-xl">{{ props.title }}</h1>
      <div class="absolute top-2 right-0 space-x-1">
        <Button v-if="!internalEditing" @click="internalEditing = true">
          <IconMdiPencil />
        </Button>
        <Button v-if="internalEditing && saveable" :disabled="loading.save" @click="savePage">
          <IconMdiContentSave />
        </Button>
        <Button v-if="internalEditing && !preview" @click="preview = true">
          <IconMdiEye />
        </Button>
        <Button v-if="internalEditing && preview" @click="preview = false">
          <IconMdiEyeOff />
        </Button>
        <DeletePageModal @delete="deletePage">
          <template #activator="{ on }">
            <Button v-if="internalEditing && deletable" :disabled="loading.delete" v-on="on">
              <IconMdiDelete />
            </Button>
          </template>
        </DeletePageModal>
        <Button
          v-if="internalEditing && cancellable"
          @click="
            internalEditing = false;
            preview = false;
          "
        >
          <IconMdiClose />
        </Button>
      </div>
    </div>
    <div v-if="!props.title && internalEditing && !preview" class="mt-11"></div>
    <div v-if="internalEditing && !preview" class="pl-4 mt-1">
      <InputTextarea v-model="rawEdited" :extra-rows="3" :maxlength="maxlength" counter></InputTextarea>
    </div>
    <div v-if="props.title && (!internalEditing || preview)" class="-mt-5"></div>
    <Markdown v-if="!internalEditing" :raw="raw" class="pl-4" />
    <Markdown v-if="preview" :raw="rawEdited" class="pl-4" />
    <ErrorTooltip :error-messages="errors" class="w-full">
      <slot />
    </ErrorTooltip>
  </div>
</template>
