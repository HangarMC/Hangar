<!--
The MIT License (MIT)

Copyright (c) 2016-2020 Marcos Moura, Creative Tim (https://www.creative-tim.com) & Community

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
-->
<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    value?: number;
    diameter?: number;
    stroke?: number;
    indeterminate?: boolean;
    unit?: string;
  }>(),
  {
    value: 0,
    diameter: 24,
    stroke: 2,
    indeterminate: true,
    unit: "px",
  }
);

const svg = ref();
const circle = ref();

const circleRadius = computed(() => (props.diameter - props.stroke) / 2);
const circleCircumference = computed(() => 2 * Math.PI * circleRadius.value);

watch(
  props,
  () => {
    attachSvgStyle();
    attachCircleStyle();
  },
  { deep: true }
);

onMounted(() => {
  attachSvgStyle();
  attachCircleStyle();
});

function attachSvgStyle() {
  const size = props.diameter + props.unit;
  svg.value.style.width = size;
  svg.value.style.height = size;
}

function attachCircleStyle() {
  if (!props.indeterminate) {
    circle.value.style.strokeDashoffset = (circleCircumference.value * (100 - props.value)) / 100 + props.unit;
  }
  circle.value.style.strokeDasharray = circleCircumference.value;
  circle.value.style.strokeWidth = props.stroke + props.unit;
  circle.value.style.setProperty("--md-progress-spinner-start-value", String(0.95 * circleCircumference.value));
  circle.value.style.setProperty("--md-progress-spinner-end-value", String(0.2 * circleCircumference.value));
}
</script>

<template>
  <transition name="md-progress-spinner" appear>
    <div
      class="md-progress-spinner"
      :class="{ 'md-progress-spinner-indeterminate': true, 'md-determinate': !indeterminate, 'md-indeterminate': indeterminate }"
    >
      <svg ref="svg" class="md-progress-spinner-draw" preserveAspectRatio="xMidYMid meet" focusable="false" :viewBox="`0 0 ${diameter} ${diameter}`">
        <circle ref="circle" class="md-progress-spinner-circle" cx="50%" cy="50%" :r="circleRadius"></circle>
      </svg>
    </div>
  </transition>
</template>

<style lang="scss">
:root {
  --md-transition-stand-timing: cubic-bezier(0.25, 0.8, 0.25, 1);
}

@keyframes md-progress-spinner-rotate {
  0% {
    transform: rotate(0);
  }
  100% {
    transform: rotate(360deg);
  }
}
@keyframes md-progress-spinner-initial-rotate {
  0% {
    opacity: 0;
    transform: rotate(-90deg) translateZ(0);
  }
  20% {
    opacity: 1;
  }
  100% {
    transform: rotate(270deg) translateZ(0);
  }
}

@keyframes md-progress-spinner-stroke-rotate {
  0% {
    stroke-dashoffset: var(--md-progress-spinner-start-value);
    transform: rotate(0);
  }
  12.5% {
    stroke-dashoffset: var(--md-progress-spinner-end-value);
    transform: rotate(0);
  }
  12.51% {
    stroke-dashoffset: var(--md-progress-spinner-end-value);
    transform: rotateX(180deg) rotate(72.5deg);
  }
  25% {
    stroke-dashoffset: var(--md-progress-spinner-start-value);
    transform: rotateX(180deg) rotate(72.5deg);
  }
  25.1% {
    stroke-dashoffset: var(--md-progress-spinner-start-value);
    transform: rotate(270deg);
  }
  37.5% {
    stroke-dashoffset: var(--md-progress-spinner-end-value);
    transform: rotate(270deg);
  }
  37.51% {
    stroke-dashoffset: var(--md-progress-spinner-end-value);
    transform: rotateX(180deg) rotate(161.5deg);
  }
  50% {
    stroke-dashoffset: var(--md-progress-spinner-start-value);
    transform: rotateX(180deg) rotate(161.5deg);
  }
  50.01% {
    stroke-dashoffset: var(--md-progress-spinner-start-value);
    transform: rotate(180deg);
  }
  62.5% {
    stroke-dashoffset: var(--md-progress-spinner-end-value);
    transform: rotate(180deg);
  }
  62.51% {
    stroke-dashoffset: var(--md-progress-spinner-end-value);
    transform: rotateX(180deg) rotate(251.5deg);
  }
  75% {
    stroke-dashoffset: var(--md-progress-spinner-start-value);
    transform: rotateX(180deg) rotate(251.5deg);
  }
  75.01% {
    stroke-dashoffset: var(--md-progress-spinner-start-value);
    transform: rotate(90deg);
  }
  87.5% {
    stroke-dashoffset: var(--md-progress-spinner-end-value);
    transform: rotate(90deg);
  }
  87.51% {
    stroke-dashoffset: var(--md-progress-spinner-end-value);
    transform: rotateX(180deg) rotate(341.5deg);
  }
  100% {
    stroke-dashoffset: var(--md-progress-spinner-start-value);
    transform: rotateX(180deg) rotate(341.5deg);
  }
}
.md-progress-spinner {
  display: inline-flex;
  position: relative;
  &.md-indeterminate {
    animation: md-progress-spinner-rotate 2s linear infinite;
    &.md-progress-spinner-enter,
    &.md-progress-spinner-leave-to {
      .md-progress-spinner-draw {
        opacity: 0;
        transform: scale(0.1);
      }
    }
    &.md-progress-spinner-enter-active,
    &.md-progress-spinner-leave-active {
      transition-duration: 0.4s;
      animation: none;
    }
    .md-progress-spinner-circle {
      animation: 4s infinite var(--md-transition-stand-timing);
      animation-name: md-progress-spinner-stroke-rotate;
    }
  }
  &.md-determinate {
    &.md-progress-spinner-enter-active {
      transition-duration: 2s;
      .md-progress-spinner-draw {
        animation: md-progress-spinner-initial-rotate 1.98s var(--md-transition-stand-timing) forwards;
      }
    }
    &.md-progress-spinner-leave-active {
      transition-duration: 2s;
      .md-progress-spinner-draw {
        animation: md-progress-spinner-initial-rotate reverse 1.98s var(--md-transition-stand-timing) forwards;
      }
    }
    .md-progress-spinner-draw {
      transition: none;
    }
  }
}
.md-progress-spinner-draw {
  overflow: visible;
  transform: scale(1) rotate(-90deg);
  transform-origin: center;
  transition: 0.4s var(--md-transition-stand-timing);
  will-change: opacity, transform;
}
.md-progress-spinner-circle {
  fill: none;
  transform-origin: center;
  transition: stroke-dashoffset 0.25s var(--md-transition-stand-timing);
  will-change: stroke-dashoffset, stroke-dasharray, stroke-width, animation-name, r;
}
</style>
