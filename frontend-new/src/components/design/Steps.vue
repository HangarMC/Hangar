<script lang="ts" setup>
import { computed, ref } from "vue";
import Link from "~/components/design/Link.vue";
import Card from "~/components/design/Card.vue";
import { useSettingsStore } from "~/store/settings";
import Button from "~/components/design/Button.vue";
import { useI18n } from "vue-i18n";

const settings = useSettingsStore();
const i18n = useI18n();

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
}>();
const internalValue = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

const props = defineProps<{
  modelValue: string;
  steps: Step[];
  buttonLangKey: string;
}>();

const activeStep = computed(() => props.steps.find((s) => s.value === internalValue.value));
const activeStepIndex = computed(() => props.steps.indexOf(activeStep.value as Step) + 1);

const canBack = ref(true);
const canNext = ref(true);
const showBack = ref(true);
const showNext = ref(true);

function back() {
  if (canBack.value) {
    internalValue.value = props.steps[activeStepIndex.value - 2].value;
  }
}

function next() {
  if (canNext.value) {
    internalValue.value = props.steps[activeStepIndex.value].value;
  }
}

function goto(step: Step) {
  const idx = props.steps.indexOf(step);
  if (idx >= activeStepIndex.value && canNext.value) {
    next();
  } else if (idx < activeStepIndex.value && canBack.value) {
    back();
  }
}

export interface Step {
  value: string;
  header: string;
}
</script>

<template>
  <!-- todo style active, next and past step headers differently -->
  <div>
    <div class="w-full">
      <ul class="flex flex-row justify-around items-center">
        <template v-for="(step, count) in steps" :key="step.value">
          <div>
            <div class="bg-primary-100 text-white rounded-full w-[24px] h-[24px] inline-flex justify-center align-center" m="r-0 md:r-2">
              {{ count + 1 }}
            </div>
            <Link
              :class="internalValue === step.value ? 'underline' : '!font-semibold'"
              :href="'#' + step.value"
              display="hidden ml-0 md:inline"
              @click.prevent="goto(step)"
            >
              {{ step.header }}
            </Link>
          </div>
          <hr class="flex-grow flex-shrink mx-1 md:mx-2" />
        </template>
      </ul>
    </div>
    <div class="mt-4">
      <Card accent>
        <template v-if="settings.mobile" #header>
          {{ activeStep.header }}
        </template>
        <div v-for="step in steps" :key="step.value">
          <slot v-if="internalValue === step.value" :name="step.value" />
        </div>
        <Button v-if="showBack" :disable="canBack" size="medium" class="mt-6 mr-2" @click="back">{{
          i18n.t(buttonLangKey + activeStepIndex + ".back")
        }}</Button>
        <Button v-if="showNext" :disable="canNext" size="medium" class="mt-6" @click="next">{{ i18n.t(buttonLangKey + activeStepIndex + ".continue") }}</Button>
      </Card>
    </div>
  </div>
</template>
