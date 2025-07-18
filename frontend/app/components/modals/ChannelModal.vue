<script lang="ts" setup>
import { ChannelFlag, Color } from "#shared/types/backend";
import type { HangarChannel, ProjectChannel } from "#shared/types/backend";

const props = defineProps<{
  projectId: number;
  edit?: boolean;
  channel?: ProjectChannel;
}>();
const emit = defineEmits<{
  (e: "create", channel: HangarChannel | ProjectChannel): any;
}>();

const i18n = useI18n();
const v = useVuelidate({ $stopPropagation: true });

const frozen = props.channel && props.channel.flags.includes(ChannelFlag.FROZEN);
const possibleFlags = frozen ? [ChannelFlag.PINNED] : [ChannelFlag.UNSTABLE, ChannelFlag.PINNED, ChannelFlag.SENDS_NOTIFICATIONS, ChannelFlag.HIDE_BY_DEFAULT];

const form = reactive<ProjectChannel>({
  name: "",
  color: Color.Transparent,
  description: "",
  createdAt: "",
  flags: [] as ChannelFlag[], // TODO only do automated name validation
});
const name = ref<string>(props.channel ? props.channel.name : "");
const description = ref<string>(props.channel?.description || "");
const color = ref<string>(props.channel ? props.channel.color : "");
const flags = ref<ChannelFlag[]>(props.channel ? props.channel.flags : []);

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
  form.color = color.value as Color;
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
  v.value.$reset();
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
          name="name"
          :rules="[
            required(),
            maxLength()(useBackendData.validations.project.channels.max!),
            pattern()(useBackendData.validations.project.channels.regex!),
            validChannelName()(String(props.projectId), props.channel?.name),
          ]"
          counter
        />
        <div class="mt-3">
          <InputText v-model.trim="description" :label="i18n.t('channel.modal.description')" name="description" :maxlength="50" counter />
        </div>
        <p class="text-lg font-bold mt-3 mb-1">{{ i18n.t("channel.modal.color") }}</p>
        <div class="grid gap-10px mb-2" style="grid-template-columns: repeat(auto-fit, 30px)">
          <div
            v-for="clr in useBackendData.channelColors"
            :key="clr.name"
            :style="`background-color: ${clr.hex}`"
            class="h-30px w-30px cursor-pointer inline-flex justify-center items-center rounded-lg border-black border-1"
            :data-value="clr.hex"
            @click="color = clr.hex"
          >
            <IconMdiCheckboxMarkedCircle
              class="transition-all ease-in-out duration-100 w-full h-full"
              :class="color === clr.hex ? 'visible opacity-100' : 'invisible opacity-0'"
            />
          </div>
        </div>
        <InputText
          v-model="color"
          :label="i18n.t('channel.modal.pickedColor')"
          :rules="[required(), validChannelColor()(String(props.projectId), props.channel?.color)]"
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
            <IconMdiHideOutline v-else-if="f === ChannelFlag.HIDE_BY_DEFAULT" />
          </span>
          {{ i18n.t("channel.modal.flags." + f.toLowerCase()) }}
        </template>
      </InputCheckbox>

      <Button class="mt-3" :disabled="noChange || v.$errors.length > 0" @click="create(on.click)">
        {{ edit ? i18n.t("general.save") : i18n.t("general.create") }}
      </Button>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="open(on)" />
    </template>
  </Modal>
</template>
