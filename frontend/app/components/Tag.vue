<script lang="ts" setup>
interface Color {
  foreground?: string;
  background?: string;
}

const props = defineProps<{
  name?: string;
  color?: Color;
  tooltip?: string;
}>();

const ccColor = computed<Color>(() => ({
  foreground: props.color?.foreground ?? contrastForeground(props.color?.background),
  background: props.color?.background,
}));
</script>

<template>
  <div class="tags inline-flex flex-wrap items-center justify-start" :title="tooltip">
    <span
      :style="{
        color: ccColor?.foreground,
        background: ccColor?.background,
        'border-color': ccColor?.background,
      }"
      class="flex rounded px-2 py-0.5 text-0.8em"
    >
      {{ name }}
    </span>
  </div>
</template>

<style lang="scss" scoped>
.tags {
  &.has-addons {
    .tag:first-child {
      border-bottom-right-radius: 0;
      border-top-right-radius: 0;
    }

    .tag:nth-child(2) {
      border-bottom-left-radius: 0;
      border-top-left-radius: 0;
      border-left: none;
    }
  }

  .tag {
    border: 1px solid #dcdcdc;
  }
}
</style>
