<script lang="ts" setup>
import { computed } from "vue";
import Link from "~/components/design/Link.vue";

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
}>();
const internalValue = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

interface Tab {
  value: string;
  header: string;
}

const props = defineProps<{
  modelValue: string;
  tabs: Tab[];
}>();
</script>

<template>
  <div class="flex gap-2">
    <div>
      <ul>
        <li v-for="tab in tabs" :key="tab.value">
          <Link :href="'#' + tab.value" @click.prevent="internalValue = tab.value">{{ tab.header }}</Link>
        </li>
      </ul>
    </div>
    <div>
      <template v-for="tab in tabs" :key="tab.value">
        <slot v-if="internalValue === tab.value" :name="tab.value" />
      </template>
    </div>
  </div>
</template>
