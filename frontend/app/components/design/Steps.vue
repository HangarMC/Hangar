<script lang="ts" setup>
import type { Step } from "#shared/types/components/design/Steps";
import { track } from "~/composables/useTracking";

const router = useRouter();
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
  trackingName: string;
}>();

const v = useVuelidate();

const activeStep = computed(() => props.steps.find((s) => s.value === internalValue.value));
const activeStepIndex = computed(() => props.steps.indexOf(activeStep.value as Step) + 1);

watch(
  () => activeStep.value,
  () => {
    track("funnel", props.trackingName + "-step-" + activeStepIndex.value + "-" + activeStep.value?.value);
  },
  {
    immediate: true,
  }
);

const loading = ref(false);
const disableBack = computed(() => loading.value || (activeStep.value?.disableBack === undefined ? false : unref(activeStep.value?.disableBack)));
const disableNext = computed(() => loading.value || (activeStep.value?.disableNext === undefined ? v.value.$invalid : unref(activeStep.value?.disableNext)));
const showBack = computed(() => (activeStep.value?.showBack === undefined ? true : unref(activeStep.value?.showBack)));
const showNext = computed(() => (activeStep.value?.showNext === undefined ? true : unref(activeStep.value?.showNext)));

async function back() {
  if (disableBack.value) return;

  loading.value = true;
  try {
    if (activeStep.value?.beforeBack && !(await activeStep.value?.beforeBack())) {
      return;
    }

    if (activeStepIndex.value === 1) {
      router.back();
      return;
    }

    internalValue.value = props.steps[activeStepIndex.value - 2]!.value;
  } finally {
    loading.value = false;
  }
}

async function next() {
  if (disableNext.value) return;
  if (!(await v.value.$validate())) return;

  loading.value = true;
  try {
    if (activeStep.value?.beforeNext && !(await activeStep.value?.beforeNext())) {
      return;
    }

    internalValue.value = props.steps[activeStepIndex.value]!.value;
  } finally {
    loading.value = false;
  }
}

async function goto(step: Step) {
  const idx = props.steps.indexOf(step);
  if (idx >= activeStepIndex.value) {
    await next();
  } else if (idx < activeStepIndex.value) {
    await back();
  }
}
</script>

<template>
  <div>
    <div class="w-full">
      <ul class="flex flex-row justify-around items-center">
        <template v-for="(step, count) in steps" :key="step.value">
          <div>
            <div class="lt-sm:hidden bg-primary-500 text-white rounded-full w-[24px] h-[24px] inline-flex justify-center align-center" m="r-2">
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
          <hr class="flex-grow flex-shrink mx-2" />
        </template>
      </ul>
    </div>
    <div class="mt-4">
      <Card accent>
        <template #header>
          <span class="hidden md:block">{{ activeStep?.header }}</span>
        </template>
        <div v-for="step in steps" :key="step.value">
          <slot v-if="internalValue === step.value" :name="step.value" />
        </div>
        <Button
          v-if="showBack && activeStepIndex === 1"
          button-type="red"
          :disabled="disableBack"
          size="medium"
          class="mt-3 mr-2"
          @click="back"
          v-on="useTracking(trackingName + '-back', { step: internalValue })"
        >
          {{ i18n.t(buttonLangKey + activeStepIndex + ".back") }}
        </Button>
        <Button
          v-else-if="showBack"
          :disabled="disableBack"
          size="medium"
          class="mt-3 mr-2"
          @click="back"
          v-on="useTracking(trackingName + '-back', { step: internalValue })"
        >
          {{ i18n.t(buttonLangKey + activeStepIndex + ".back") }}
        </Button>
        <Button
          v-if="showNext"
          :disabled="disableNext"
          size="medium"
          class="mt-3"
          @click="next"
          v-on="useTracking(trackingName + '-next', { step: internalValue })"
        >
          {{ i18n.t(buttonLangKey + activeStepIndex + ".continue") }}
        </Button>
      </Card>
    </div>
  </div>
</template>
