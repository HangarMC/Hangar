<script setup lang="ts">
const props = defineProps({
  label: {
    type: String,
    required: true,
  },
});
</script>

<template>
  <label class="group relative cursor-pointer pl-30px customCheckboxContainer">
    {{ props.label }}
    <input type="checkbox" class="hidden" />
    <span
      class="absolute top-5px left-0 h-15px w-15px rounded bg-gray-300"
      after="absolute hidden content-DEFAULT top-1px left-5px border-solid w-6px h-12px border-r-3px border-b-3px border-white"
      group-hover="bg-gray-400"
    />
  </label>
</template>

<style>
/*This is needed, because you cannot have more than one parent group in tailwind/windi*/
.customCheckboxContainer input:checked ~ span {
  background-color: #004ee9 !important;
}

/*The tailwind/windi utility class rotate-45 is BROKEN*/
.customCheckboxContainer input:checked ~ span:after {
  display: block;
  transform: rotate(45deg);
}
</style>
