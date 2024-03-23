<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    delay?: number;
  }>(),
  {
    delay: 300,
  }
);

function doDelay(time: number) {
  return new Promise((resolve) => setTimeout(resolve, time));
}

const show = ref(false);
doDelay(props.delay).then(() => {
  show.value = true;
});
</script>

<template>
  <Transitioned :show="show">
    <slot />
  </Transitioned>
</template>
