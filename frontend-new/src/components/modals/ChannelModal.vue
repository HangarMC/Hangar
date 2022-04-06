<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { ProjectChannel } from "hangar-internal";
import { computed, reactive, ref, watch } from "vue";
import { useBackendDataStore } from "~/store/backendData";
import { AxiosError } from "axios";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useInternalApi } from "~/composables/useApi";
import { useContext } from "vite-ssr/vue";
import InputText from "~/components/ui/InputText.vue";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";

const props = defineProps<{
  projectId: number;
  edit?: boolean;
  channel?: ProjectChannel;
}>();
const emit = defineEmits<{
  (e: "create", channel: ProjectChannel): void;
}>();

const i18n = useI18n();
const ctx = useContext();

const validation = reactive({
  nameErrors: [] as string[],
  colorErrors: [] as string[],
  nameValid: false,
  colorValid: false,
});
const form = reactive<ProjectChannel>({
  name: "",
  color: "",
  nonReviewed: true, //TODO only do automated name validation
  editable: true,
  versionCount: 0,
});
const name = ref<string>(props.channel ? props.channel.name : "");
const color = ref<string>(props.channel ? props.channel.color : "");
const swatches = computed<string[][]>(() => {
  const result: string[][] = [];
  const backendColors = useBackendDataStore().channelColors;
  const columns = Math.floor(Math.sqrt(backendColors.length));
  const rows = Math.ceil(Math.sqrt(backendColors.length));
  for (let i = 0; i < rows; i++) {
    result[i] = [];
    for (let j = 0; j < columns; j++) {
      if (backendColors[i * columns + j]) {
        result[i][j] = backendColors[i * columns + j].hex;
      }
    }
  }
  return result;
});

const isValid = computed(() => {
  return validation.colorValid && validation.nameValid;
});

watch(name, (newVal) => checkName(newVal));
watch(color, (newVal) => checkColor(newVal));

function checkName(name: string) {
  validation.nameValid = false;
  validation.nameErrors = [];
  if (name.length === 0) {
    return;
  }

  useInternalApi("channels/checkName", true, "get", {
    projectId: props.projectId,
    name: name,
    existingName: props.channel?.name,
  })
    .then(() => {
      validation.nameValid = true;
    })
    .catch((err: AxiosError) => {
      if (!err.response?.data.isHangarApiException) {
        return handleRequestError(err, ctx, i18n);
      }
      validation.nameErrors.push(i18n.t(err.response.data.message));
    });
}

function checkColor(color: string) {
  validation.colorValid = false;
  validation.colorErrors = [];
  if (color.length === 0) {
    return;
  }

  useInternalApi("channels/checkColor", true, "get", {
    projectId: props.projectId,
    color: color,
    existingColor: props.channel?.color,
  })
    .then(() => {
      validation.colorValid = true;
    })
    .catch((err: AxiosError) => {
      if (!err.response?.data.isHangarApiException) {
        return handleRequestError(err, ctx, i18n);
      }
      validation.colorErrors.push(i18n.t(err.response.data.message));
    });
}

function create(close: () => void) {
  close();
  form.name = name.value;
  form.color = color.value;
  emit("create", form);
}

function reset() {
  if (props.channel) {
    Object.assign(form, props.channel);
  }
}
reset();
</script>

<template>
  <Modal :title="edit ? i18n.t('channel.modal.titleEdit') : i18n.t('channel.modal.titleNew')">
    <template #default="{ on }">
      <InputText v-model.trim="name" :label="i18n.t('channel.modal.name')" :error-messages="validation.nameErrors" />
      <p class="text-lg font-bold mt-3 mb-1">{{ i18n.t("channel.modal.color") }}</p>
      <div v-for="(arr, arrIndex) in swatches" :key="arrIndex" class="flex">
        <div v-for="(c, n) in arr" :key="n" class="flex-grow-0 flex-shrink-1 pa-2 pr-1 mb-1">
          <div
            :style="`background-color: ${c}`"
            class="w-27px h-25px cursor-pointer inline-flex justify-center items-center rounded-lg border-black border-1"
            @click="color = c"
          >
            <IconMdiCheckboxMarkedCircle
              class="ma-auto transition-all ease-in-out duration-100"
              :class="color === c ? 'visible opacity-100' : 'invisible opacity-0'"
            />
          </div>
        </div>
      </div>
      <InputText v-model="color" :label="i18n.t('channel.modal.color')" :error-messages="validation.colorErrors" readonly />
      <br />

      <Button button-type="primary" class="mt-2" :disabled="!isValid" @click="create(on.click)">{{
        edit ? i18n.t("general.save") : i18n.t("general.create")
      }}</Button>
      <Button button-type="gray" class="mt-2 ml-2" v-on="on">{{ i18n.t("general.close") }}</Button>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="on"></slot>
    </template>
  </Modal>
</template>
