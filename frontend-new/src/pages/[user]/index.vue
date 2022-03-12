<script setup lang="ts">
import { PropType } from "vue";
import { User } from "hangar-api";

defineProps({
  user: {
    type: Object as PropType<User>,
    required: true,
  },
});
</script>

<template>
  <div>
    user child
    {{ user }}
  </div>
</template>
