<script lang="ts" setup>
import { computed, ComputedRef, Ref, ref } from "vue";
import Link from "~/components/design/Link.vue";
import Card from "~/components/design/Card.vue";
import { useSettingsStore } from "~/store/settings";
import Button from "~/components/design/Button.vue";
import { useI18n } from "vue-i18n";
import { useVuelidate } from "@vuelidate/core";

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

const v = useVuelidate();

const activeStep = computed(() => props.steps.find((s) => s.value === internalValue.value));
const activeStepIndex = computed(() => props.steps.indexOf(activeStep.value as Step) + 1);

const disableBack = computed(() => (activeStep.value?.disableBack ? activeStep.value?.disableBack.value : false));
const disableNext = computed(() => (activeStep.value?.disableNext ? activeStep.value?.disableNext.value : v.value.$invalid));
const showBack = computed(() => (activeStep.value?.showBack ? activeStep.value?.showBack.value : true));
const showNext = computed(() => (activeStep.value?.showNext ? activeStep.value?.showNext.value : true));

async function back() {
  if (disableBack.value) return;
  if (activeStep.value?.beforeBack && !(await activeStep.value?.beforeBack())) return;

  internalValue.value = props.steps[activeStepIndex.value - 2].value;
}

async function next() {
  if (disableNext.value) return;
  if (!(await v.value.$validate())) return;
  if (activeStep.value?.beforeNext && !(await activeStep.value?.beforeNext())) return;

  internalValue.value = props.steps[activeStepIndex.value].value;
}

async function goto(step: Step) {
  const idx = props.steps.indexOf(step);
  if (idx >= activeStepIndex.value) {
    await next();
  } else if (idx < activeStepIndex.value) {
    await back();
  }
}

export interface Step {
  value: string;
  header: string;
  beforeBack?: () => Promise<boolean>;
  beforeNext?: () => Promise<boolean>;
  disableBack?: Ref<boolean>;
  disableNext?: Ref<boolean>;
  showBack?: Ref<boolean>;
  showNext?: Ref<boolean>;
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
        <template #header>
          <span v-show="settings.mobile">{{ activeStep.header }}</span>
        </template>
        <div v-for="step in steps" :key="step.value">
          <slot v-if="internalValue === step.value" :name="step.value" />
        </div>
        <Button v-if="showBack" :disabled="disableBack" size="medium" class="mt-6 mr-2" @click="back">{{
          i18n.t(buttonLangKey + activeStepIndex + ".back")
        }}</Button>
        <Button v-if="showNext" :disabled="disableNext" size="medium" class="mt-6" @click="next">{{
          i18n.t(buttonLangKey + activeStepIndex + ".continue")
        }}</Button>
      </Card>
    </div>
  </div>
</template>
