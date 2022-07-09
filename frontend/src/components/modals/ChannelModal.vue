<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/lib/components/design/Button.vue";
import Modal from "~/lib/components/modals/Modal.vue";
import { ProjectChannel } from "hangar-internal";
import { computed, reactive, ref } from "vue";
import { useBackendDataStore } from "~/store/backendData";
import { useContext } from "vite-ssr/vue";
import InputText from "~/lib/components/ui/InputText.vue";
import { required } from "~/lib/composables/useValidationHelpers";
import { validChannelName, validChannelColor } from "~/composables/useHangarValidations";
import { useVuelidate } from "@vuelidate/core";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import { ChannelFlag } from "~/types/enums";

const possibleFlags = [ChannelFlag.UNSTABLE, ChannelFlag.PINNED]; // TODO maybe load from backend? unsure if needed

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
const v = useVuelidate();

const form = reactive<ProjectChannel>({
  name: "",
  color: "",
  flags: [], // TODO only do automated name validation
  versionCount: 0,
});
const name = ref<string>(props.channel ? props.channel.name : "");
const color = ref<string>(props.channel ? props.channel.color : "");
const flags = ref<ChannelFlag[]>(props.channel ? props.channel.flags : []);
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

async function create(close: () => void) {
  if (!(await v.value.$validate())) return;
  close();
  form.name = name.value;
  form.color = color.value;
  form.flags = flags.value;
  emit("create", form);
}

function open({ click }: { click: () => void }) {
  return {
    click: () => {
      reset();
      console.log("reset");
      click();
    },
  };
}

function reset() {
  if (props.channel) {
    Object.assign(form, props.channel);
  } else {
    name.value = "";
    color.value = "";
    flags.value = [];
  }
}
reset();
</script>

<template>
  <Modal :title="edit ? i18n.t('channel.modal.titleEdit') : i18n.t('channel.modal.titleNew')">
    <template #default="{ on }">
      <InputText v-model.trim="name" :label="i18n.t('channel.modal.name')" :rules="[required(), validChannelName()(props.projectId, props.channel?.name)]" />
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
        :label="i18n.t('channel.modal.color')"
        :rules="[required(), validChannelColor()(props.projectId, props.channel?.color)]"
        readonly
        disabled
      />
      <div class="mb-4" />
      <InputCheckbox v-for="f in possibleFlags" :key="f" v-model="flags" :label="i18n.t(`channel.modal.flags.${f.toLowerCase()}`)" :value="f" />

      <Button class="mt-3" :disabled="v.$invalid" @click="create(on.click)">{{ edit ? i18n.t("general.save") : i18n.t("general.create") }}</Button>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="open(on)"></slot>
    </template>
  </Modal>
</template>
