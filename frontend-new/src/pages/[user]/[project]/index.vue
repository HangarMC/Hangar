<script lang="ts" setup>
import { PropType } from "vue";
import { Project, User } from "hangar-api";

defineProps({
  user: {
    type: Object as PropType<User>,
    required: true,
  },
  project: {
    type: Object as PropType<Project>,
    required: true,
  },
});
</script>

<template>
  <div>
    Project child
    {{ project }}
  </div>
</template>
