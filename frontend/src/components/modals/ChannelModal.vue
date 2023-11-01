<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import type { ProjectChannel } from "hangar-internal";
import { computed, reactive, ref } from "vue";
import { useVuelidate } from "@vuelidate/core";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { useBackendData } from "~/store/backendData";
import InputText from "~/components/ui/InputText.vue";
import { isSame, maxLength, minLength, pattern, required } from "~/composables/useValidationHelpers";
import { validChannelName, validChannelColor } from "~/composables/useHangarValidations";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import { ChannelFlag } from "~/types/enums";

const props = defineProps<{
  projectId: number;
  edit?: boolean;
  channel?: ProjectChannel;
}>();
const emit = defineEmits<{
  (e: "create", channel: ProjectChannel): void;
}>();

const i18n = useI18n();
const v = useVuelidate();

const frozen = props.channel && props.channel.flags.includes(ChannelFlag.FROZEN);
const possibleFlags = frozen ? [ChannelFlag.PINNED] : [ChannelFlag.UNSTABLE, ChannelFlag.PINNED, ChannelFlag.SENDS_NOTIFICATIONS];

const form = reactive<ProjectChannel>({
  name: "",
  color: "",
  flags: [], // TODO only do automated name validation
  versionCount: 0,
});
const name = ref<string>(props.channel ? props.channel.name : "");
const description = ref<string>(props.channel?.description || "");
const color = ref<string>(props.channel ? props.channel.color : "");
const flags = ref<ChannelFlag[]>(props.channel ? props.channel.flags : []);
const swatches = computed<string[][]>(() => {
  const result: string[][] = [];
  const backendColors = useBackendData.channelColors;
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

const noChange = computed(() => {
  return (
    props.channel?.name === name.value &&
    props.channel?.description === description.value &&
    props.channel.color === color.value &&
    isSame(props.channel.flags, flags.value)
  );
});

async function create(close: () => void) {
  if (!(await v.value.$validate())) return;
  close();
  form.name = name.value;
  form.description = description.value;
  form.color = color.value;
  form.flags = flags.value;
  emit("create", form);
}

function open({ click }: { click: () => void }) {
  return {
    click: () => {
      reset();
      click();
    },
  };
}

function reset() {
  if (props.channel) {
    Object.assign(form, props.channel);
  } else {
    name.value = "";
    description.value = "";
    color.value = "";
    flags.value = [];
  }
}
reset();
</script>

<template>
  <Modal :title="edit ? i18n.t('channel.modal.titleEdit') : i18n.t('channel.modal.titleNew')" window-classes="w-150">
    <template #default="{ on }">
      <div v-if="!frozen">
        <InputText
          v-model.trim="name"
          :label="i18n.t('channel.modal.name')"
          :rules="[
            required(),
            maxLength()(useBackendData.validations.project.channels.max),
            pattern()(useBackendData.validations.project.channels.regex),
            validChannelName()(props.projectId, props.channel?.name),
          ]"
          counter
        />
        <div class="mt-3">
          <InputText v-model.trim="description" :label="i18n.t('channel.modal.description')" :maxlength="50" counter />
        </div>
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
        <InputText
          v-model="color"
          :label="i18n.t('channel.modal.pickedColor')"
          :rules="[required(), validChannelColor()(props.projectId, props.channel?.color)]"
          readonly
          disabled
        />
        <div class="mb-4" />
      </div>
      <InputCheckbox v-for="f in possibleFlags" :key="f" v-model="flags" :value="f">
        <template #label>
          <span class="mr-1">
            <IconMdiAlertOutline v-if="f === ChannelFlag.UNSTABLE" />
            <IconMdiPinOutline v-else-if="f === ChannelFlag.PINNED" />
            <IconMdiBellOutline v-else-if="f === ChannelFlag.SENDS_NOTIFICATIONS" />
          </span>
          {{ i18n.t("channel.modal.flags." + f.toLowerCase()) }}
        </template>
      </InputCheckbox>

      <Button class="mt-3" :disabled="noChange || v.$invalid" @click="create(on.click)">{{ edit ? i18n.t("general.save") : i18n.t("general.create") }}</Button>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="open(on)"></slot>
    </template>
  </Modal>
</template>
