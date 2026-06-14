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
  const currentIdx = activeStepIndex.value - 1;
  if (idx === currentIdx) {
    return;
  }
  if (idx > currentIdx) {
    await next();
  } else {
    internalValue.value = step.value;
  }
}
</script>

<template>
  <div class="space-y-4">
    <nav class="background-default overflow-x-auto rounded-xl border border-gray-200 p-1 dark:border-gray-800">
      <div class="flex min-w-max items-center gap-1">
        <div v-for="(step, index) in steps" :key="step.value">
          <button
            type="button"
            class="inline-flex h-10 items-center gap-2 rounded-lg border px-3 text-sm font-semibold transition-colors"
            :class="internalValue === step.value ? 'border-primary-500 color-primary' : 'border-transparent text-gray hover:bg-gray-100 dark:hover:bg-gray-800'"
            :style="
              internalValue === step.value
                ? {
                    backgroundColor: 'color-mix(in srgb, var(--primary-500) 15%, transparent)',
                  }
                : {}
            "
            @click="goto(step)"
          >
            <span
              class="inline-flex h-6 w-6 items-center justify-center rounded-md text-xs"
              :class="index + 1 < activeStepIndex ? 'bg-primary-500 text-white' : 'bg-gray-200 dark:bg-gray-800'"
            >
              <IconMdiCheck v-if="index + 1 < activeStepIndex" />
              <span v-else>{{ index + 1 }}</span>
            </span>
            {{ step.header }}
          </button>
        </div>
      </div>
    </nav>

    <Card class="!p-0 overflow-hidden">
      <div class="border-b border-gray-200 px-5 py-4 dark:border-gray-800">
        <p class="text-xs font-semibold uppercase tracking-wide color-primary">Step {{ activeStepIndex }} of {{ steps.length }}</p>
        <h1 class="mt-1 text-2xl font-bold">{{ activeStep?.header }}</h1>
      </div>

      <div class="p-5">
        <div v-for="step in steps" :key="step.value">
          <slot v-if="internalValue === step.value" :name="step.value" />
        </div>
      </div>

      <div v-if="showBack || showNext" class="flex items-center justify-between gap-3 px-5 pb-5">
        <Button
          v-if="showBack"
          :button-type="activeStepIndex === 1 ? 'red' : 'secondary'"
          :disabled="disableBack"
          size="medium"
          @click="back"
          v-on="useTracking(trackingName + '-back', { step: internalValue })"
        >
          <IconMdiArrowLeft class="mr-1" />
          {{ i18n.t(buttonLangKey + activeStepIndex + ".back") }}
        </Button>
        <span v-else />

        <Button v-if="showNext" :disabled="disableNext" size="medium" @click="next" v-on="useTracking(trackingName + '-next', { step: internalValue })">
          {{ i18n.t(buttonLangKey + activeStepIndex + ".continue") }}
          <IconMdiArrowRight class="ml-1" />
        </Button>
      </div>
    </Card>
  </div>
</template>
