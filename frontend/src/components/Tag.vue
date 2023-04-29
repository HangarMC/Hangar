<script lang="ts" setup>
import { computed } from "vue";

interface Color {
  foreground?: string;
  background?: string;
}

const props = defineProps<{
  name?: string;
  color?: Color;
  tooltip?: string;
}>();

const ccColor = computed(() => {
  if (props.color?.foreground) {
    return props.color;
  } else {
    // https://stackoverflow.com/a/3943023
    const background = props.color?.background;
    let colors: number[] = [];
    if (background?.startsWith("rgb")) {
      colors = background
        ?.replace("rgb(", "")
        .replace(")", "")
        .split(",")
        .map((c) => parseInt(c));
    } else if (background?.startsWith("#")) {
      const bg = background?.substring(1, 7);
      colors = [parseInt(bg?.substring(0, 2), 16), parseInt(bg?.substring(2, 4), 16), parseInt(bg?.substring(4, 6), 16)];
    } else {
      console.error("Can't figure out color value for", background);
      return props.color;
    }
    colors = colors
      .map((col) => col / 255)
      .map((col) => {
        if (col <= 0.03928) {
          return col / 12.92;
        }
        return Math.pow((col + 0.055) / 1.055, 2.4);
      });
    const L = 0.2126 * colors[0] + 0.7152 * colors[1] + 0.0722 * colors[2];
    return {
      foreground: L > 0.179 ? "black" : "white",
      background: props.color?.background,
    } as Color;
  }
});
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
