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
  <div class="flex flex-col <md:space-y-2 md:(flex-row space-x-2)">
    <div class="min-w-12ch">
      <ul class="flex flex-row <md:space-x-2 md:(flex-col space-y-2)">
        <li v-for="tab in tabs" :key="tab.value">
          <Link :class="internalValue == tab.value ? 'underline' : '!font-semibold'" :href="'#' + tab.value" @click.prevent="internalValue = tab.value">
            {{ tab.header }}
          </Link>
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
