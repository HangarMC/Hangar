<script lang="ts" setup>
import { computed, ref, unref } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { useVuelidate } from "@vuelidate/core";
import Link from "~/components/design/Link.vue";
import Card from "~/components/design/Card.vue";
import { useSettingsStore } from "~/store/useSettingsStore";
import Button from "~/components/design/Button.vue";
import type { Step } from "~/types/components/design/Steps";

const router = useRouter();
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

const loading = ref(false);
const disableBack = computed(() => loading.value || (activeStep.value?.disableBack !== undefined ? unref(activeStep.value?.disableBack) : false));
const disableNext = computed(() => loading.value || (activeStep.value?.disableNext !== undefined ? unref(activeStep.value?.disableNext) : v.value.$invalid));
const showBack = computed(() => (activeStep.value?.showBack !== undefined ? unref(activeStep.value?.showBack) : true));
const showNext = computed(() => (activeStep.value?.showNext !== undefined ? unref(activeStep.value?.showNext) : true));

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

    internalValue.value = props.steps[activeStepIndex.value - 2].value;
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

    internalValue.value = props.steps[activeStepIndex.value].value;
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
            <div class="lt-sm:hidden bg-primary-400 text-white rounded-full w-[24px] h-[24px] inline-flex justify-center align-center" m="r-2">
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
          <span v-show="settings.mobile">{{ activeStep?.header }}</span>
        </template>
        <div v-for="step in steps" :key="step.value">
          <slot v-if="internalValue === step.value" :name="step.value" />
        </div>
        <Button v-if="showBack && activeStepIndex === 1" button-type="red" :disabled="disableBack" size="medium" class="mt-3 mr-2" @click="back">
          {{ i18n.t(buttonLangKey + activeStepIndex + ".back") }}
        </Button>
        <Button v-else-if="showBack" :disabled="disableBack" size="medium" class="mt-3 mr-2" @click="back">
          {{ i18n.t(buttonLangKey + activeStepIndex + ".back") }}
        </Button>
        <Button v-if="showNext" :disabled="disableNext" size="medium" class="mt-3" @click="next">
          {{ i18n.t(buttonLangKey + activeStepIndex + ".continue") }}
        </Button>
      </Card>
    </div>
  </div>
</template>
