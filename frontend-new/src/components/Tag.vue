<script lang="ts" setup>
import { Tag } from "hangar-api";
import { computed } from "vue";

const props = defineProps<{
  name?: string;
  data?: string;
  color?: { foreground?: string; background: string };
  tag?: Tag;
  shortForm?: boolean;
}>();

const cName = computed(() => (props.tag ? props.tag.name : props.name));
const cData = computed(() => (props.tag ? props.tag.data : props.data));
const cColor = computed(() => (props.tag ? props.tag.color : props.color));
</script>

<template>
  <div class="tags" :class="{ 'has-addons': cData && !shortForm }">
    <span
      :style="{
        color: cColor.foreground,
        background: cColor.background,
        'border-color': cColor.background,
      }"
      class="tag"
    >
      {{ shortForm && cData ? cData : cName }}
    </span>
    <span v-if="cData && !shortForm" class="tag">{{ cData }}</span>
  </div>
</template>

<style lang="scss" scoped>
// todo reimplement using windi, but I g2g now
.tags {
  display: inline-flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  align-items: center;

  &.has-addons {
    .tag:first-child {
      border-bottom-right-radius: 0;
      border-top-right-radius: 0;
      margin-right: 0;
    }

    .tag:nth-child(2) {
      border-bottom-left-radius: 0;
      border-top-left-radius: 0;
      border-left: none;
      margin-left: 0;
    }
  }

  .tag {
    border: 1px solid #dcdcdc;
    display: flex;
    background-color: #f5f5f5;
    color: #495057;
    border-radius: 3px;
    font-size: 0.75em;
    height: 2em;
    padding-left: 0.75em;
    padding-right: 0.75em;
    white-space: nowrap;
    margin: 5px;
  }
}
</style>
