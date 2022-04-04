<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { ProjectChannel } from "hangar-internal";
import { computed, reactive, watch } from "vue";
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

const loading = reactive({
  name: false,
  color: false,
});
const errorMessages = reactive({
  name: [] as string[],
  color: [] as string[],
});
const form = reactive<ProjectChannel>({
  name: "",
  color: "",
  nonReviewed: false,
  editable: true,
  versionCount: 0,
});
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
  return errorMessages.name?.length === 0 && errorMessages.color?.length === 0;
});

watch(
  form,
  (newVal) => {
    checkName(newVal.name);
    checkColor(newVal.color);
  },
  { deep: true }
);

function checkName(name: string) {
  if (!name) return;
  loading.name = true;
  errorMessages.name = [];
  useInternalApi("channels/checkName", true, "get", {
    projectId: props.projectId,
    name: name,
    existingName: props.channel?.name,
  })
    .catch((err: AxiosError) => {
      if (!err.response?.data.isHangarApiException) {
        return handleRequestError(err, ctx, i18n);
      }
      errorMessages.name.push(i18n.t(err.response.data.message));
    })
    .finally(() => {
      loading.name = false;
    });
}

function checkColor(color: string) {
  if (!color) return;
  loading.color = true;
  errorMessages.color = [];
  useInternalApi("channels/checkColor", true, "get", {
    projectId: props.projectId,
    color: color,
    existingColor: props.channel?.color,
  })
    .catch((err: AxiosError) => {
      if (!err.response?.data.isHangarApiException) {
        return handleRequestError(err, ctx, i18n);
      }
      errorMessages.color.push(i18n.t(err.response.data.message));
    })
    .finally(() => {
      loading.color = false;
    });
}

function create(close: () => void) {
  close();
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
      <InputText v-model.trim="form.name" :label="i18n.t('channel.modal.name')" :disabled="loading.name" :error-messages="errorMessages.name" />
      <span class="text-lg">{{ i18n.t("channel.modal.color") }}</span>
      <div v-for="(arr, arrIndex) in swatches" :key="arrIndex" class="flex justify-center">
        <div v-for="(color, n) in arr" :key="n" class="flex-grow-0 flex-shrink-1 pa-2 px-1 mb-2">
          <div
            :style="`background-color: ${color}`"
            class="w-40px h-25px cursor-pointer inline-flex justify-center items-center rounded-lg border-black border-1"
            @click="form.color = color"
          >
            <IconMdiCheckboxMarkedCircle
              class="ma-auto transition-all ease-in-out duration-300"
              :class="form.color === color ? 'visible opacity-100' : 'invisible opacity-0'"
            />
          </div>
        </div>
      </div>
      <InputText v-model="form.color" :label="i18n.t('channel.modal.color')" :error-messages="errorMessages.color" :loading="loading.color" readonly />
      <InputCheckbox v-model="form.nonReviewed" :label="i18n.t('channel.modal.reviewQueue')" />

      <Button type="gray" class="mt-2" v-on="on">{{ i18n.t("general.close") }}</Button>
      <Button type="primary" class="mt-2 ml-2" :disabled="!isValid" @click="create(on.click)">{{
        edit ? i18n.t("general.save") : i18n.t("general.create")
      }}</Button>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="on"></slot>
    </template>
  </Modal>
</template>
