<script lang="ts" setup>
import { computed, reactive, ref, watch } from "vue";
import InputTextarea from "~/components/ui/InputTextarea.vue";
import Markdown from "~/components/Markdown.vue";
import Button from "~/components/design/Button.vue";
import DeletePageModal from "~/components/modals/DeletePageModal.vue";

const props = withDefaults(
  defineProps<{
    raw: string;
    editing: boolean;
    deletable: boolean;
    cancellable: boolean;
    saveable: boolean;
    maxlength?: number;
  }>(),
  {
    maxlength: 30_000,
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
    <div v-show="internalEditing && !preview" class="pl-10">
      <InputTextarea v-model="rawEdited" :rows="rawEdited.split(/\r\n|\r|\n/g).length + 3" :maxlength="maxlength" counter></InputTextarea>
    </div>
    <Markdown v-show="!internalEditing" :raw="raw" class="pl-5" />
    <Markdown v-if="preview" :raw="rawEdited" class="pl-5" />
    <div class="absolute flex flex-col top-0 ml-2 mt-2 space-y-2">
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
</template>
