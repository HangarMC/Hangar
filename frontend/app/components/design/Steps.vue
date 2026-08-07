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
    <ol class="list-none flex items-center gap-2">
      <template v-for="(step, count) in steps" :key="step.value">
        <li class="min-w-0 flex-shrink-0">
          <button
            type="button"
            class="flex items-center gap-2 rounded px-1 py-0.5 focus-visible:(outline-2 outline-offset-2 outline-primary-500)"
            :aria-current="count + 1 === activeStepIndex ? 'step' : undefined"
            @click.prevent="goto(step)"
          >
            <span
              class="h-7 w-7 flex flex-shrink-0 items-center justify-center rounded-full text-sm font-bold transition-colors"
              :class="
                count + 1 < activeStepIndex
                  ? 'background-card color-primary'
                  : count + 1 === activeStepIndex
                    ? 'bg-primary-500 text-white'
                    : 'background-card text-gray-secondary'
              "
            >
              <IconMdiCheck v-if="count + 1 < activeStepIndex" />
              <template v-else>{{ count + 1 }}</template>
            </span>
            <span
              class="truncate text-sm lt-md:hidden"
              :class="count + 1 === activeStepIndex ? 'font-bold color-primary' : count + 1 < activeStepIndex ? 'font-semibold' : 'text-gray-secondary'"
            >
              {{ step.header }}
            </span>
          </button>
        </li>
        <li
          v-if="count < steps.length - 1"
          class="h-0.5 min-w-4 flex-1 rounded-full transition-colors"
          :class="count + 1 < activeStepIndex ? 'bg-primary-500' : 'bg-gray-300 dark:bg-gray-700'"
        />
      </template>
    </ol>

    <div class="mt-4">
      <Card accent>
        <template #header>
          <span class="hidden md:block">{{ activeStep?.header }}</span>
        </template>
        <div v-for="step in steps" :key="step.value">
          <slot v-if="internalValue === step.value" :name="step.value" />
        </div>
        <div class="mt-5 flex justify-end gap-2 border-t border-gray-300 pt-4 dark:border-gray-700">
          <Button
            v-if="showBack"
            variant="ghost"
            tone="neutral"
            :disabled="disableBack"
            @click="back"
            v-on="useTracking(trackingName + '-back', { step: internalValue })"
          >
            {{ i18n.t(buttonLangKey + activeStepIndex + ".back") }}
          </Button>
          <Button v-if="showNext" :disabled="disableNext" :loading="loading" @click="next" v-on="useTracking(trackingName + '-next', { step: internalValue })">
            {{ i18n.t(buttonLangKey + activeStepIndex + ".continue") }}
          </Button>
        </div>
      </Card>
    </div>
  </div>
</template>
