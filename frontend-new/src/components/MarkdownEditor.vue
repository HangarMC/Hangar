<script lang="ts" setup>
import { ref } from "vue";
import InputTextarea from "~/components/ui/InputTextarea.vue";
import Markdown from "~/components/Markdown.vue";

const props = defineProps<{
  raw: string;
  isEditing: boolean;
  deletable: boolean;
  cancellable: boolean;
  saveable: boolean;
}>();

const preview = ref(false);
const rawEdited = ref(props.raw || "");
</script>

<template>
  <div v-show="isEditing && !preview">
    <InputTextarea v-model="rawEdited" :rows="rawEdited.split(/\r\n|\r|\n/g).length + 3"></InputTextarea>
  </div>
  <Markdown v-show="!isEditing" :raw="raw" />
  <Markdown v-if="preview" :raw="rawEdited" class="pl-5" />
</template>
