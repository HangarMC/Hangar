<template>
  <button
    class="rounded-md bg-gradient-to-r from-[#004ee9] to-[#367aff] text-white p-3 h-min"
    hover="text-shadow-xl from-[#004ee9] to-[#6699ff]"
    v-bind="$attrs"
    @click="$emit('click')"
  >
    <slot></slot>
  </button>
</template>
