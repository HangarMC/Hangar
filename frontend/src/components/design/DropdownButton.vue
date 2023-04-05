<script setup lang="ts">
import { ref } from "vue";
import { OnClickOutside } from "@vueuse/components";
import IconMdiMenuDown from "~icons/mdi/menu-down";
import IconMdiMenuUp from "~icons/mdi/menu-up";
import Button from "~/components/design/Button.vue";
import Popper from "~/components/design/Popper.vue";

const props = withDefaults(
  defineProps<{
    name?: string;
    buttonSize?: "small" | "medium" | "large";
    buttonType?: "primary" | "red" | "transparent";
    buttonArrow?: boolean;
    placement?: "bottom" | "top" | "left" | "right" | "bottom-end" | "bottom-start";
  }>(),
  {
    name: "Dropdown",
    buttonSize: "medium",
    buttonType: "primary",
    buttonArrow: true,
    placement: "bottom-end",
  }
);

const open = ref(false);
const popper = ref();
function close() {
  open.value = false;
}

defineExpose({ close });
</script>

<template>
  <OnClickOutside @trigger="open = false">
    <Popper ref="popper" as="div" :show="open" :placement="placement">
      <Button :button-type="props.buttonType" :size="props.buttonSize" @click="open = !open">
        <slot name="button-label">
          <span class="mx-1">{{ props.name }}</span>
        </slot>
        <template v-if="props.buttonArrow">
          <IconMdiMenuUp v-if="open" class="text-lg"></IconMdiMenuUp>
          <IconMdiMenuDown v-else class="text-lg"></IconMdiMenuDown>
        </template>
      </Button>
      <template #content>
        <div class="flex flex-col z-10 -mt-2 py-1 rounded border-t-2 border-primary-400 background-default shadow-default">
          <slot :close="close" />
        </div>
      </template>
    </Popper>
  </OnClickOutside>
</template>
