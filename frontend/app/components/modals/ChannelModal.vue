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

const frozen = props.channel && props.channel.flags.includes(ChannelFlag.FROZEN);
const possibleFlags = frozen ? [ChannelFlag.PINNED] : [ChannelFlag.UNSTABLE, ChannelFlag.PINNED, ChannelFlag.SENDS_NOTIFICATIONS, ChannelFlag.HIDE_BY_DEFAULT];
const channelValidation = useBackendData.validations.project.channels;

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
const submitted = ref(false);
const nameError = computed(() => {
  if (!submitted.value) return "";
  if (!name.value.trim()) return "Channel name is required.";
  if (name.value.length > channelValidation.max!) return `Channel name must be ${channelValidation.max} characters or fewer.`;
  if (channelValidation.regex && !new RegExp(channelValidation.regex).test(name.value)) return "Channel name contains invalid characters.";
  return "";
});
const canSubmit = computed(() => !!name.value.trim() && !!color.value && !nameError.value && !noChange.value);

const noChange = computed(() => {
  return (
    props.channel?.name === name.value &&
    props.channel?.description === description.value &&
    props.channel.color === color.value &&
    isSame(props.channel.flags, flags.value)
  );
});

async function create(close: () => void) {
  submitted.value = true;
  if (!canSubmit.value) return;
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
    name.value = props.channel.name;
    description.value = props.channel.description;
    color.value = props.channel.color;
    flags.value = [...props.channel.flags];
  } else {
    name.value = "";
    description.value = "";
    color.value = "";
    flags.value = [];
  }
  submitted.value = false;
}
reset();
</script>

<template>
  <Modal
    :title="edit ? i18n.t('channel.modal.titleEdit') : i18n.t('channel.modal.titleNew')"
    window-classes="w-full max-w-xl !rounded-xl border border-gray-200 dark:border-gray-800 shadow-lg !bg-white dark:!bg-charcoal-900"
    close-button-right
    @close="reset"
  >
    <template #default="{ on }">
      <div v-if="!frozen" class="space-y-4">
        <div>
          <div class="mb-1.5 flex items-center justify-between gap-2">
            <label class="text-sm font-semibold" for="channel-name">{{ i18n.t("channel.modal.name") }}</label>
            <span class="text-xs text-gray">{{ name.length }}/{{ useBackendData.validations.project.channels.max }}</span>
          </div>
          <input
            id="channel-name"
            v-model.trim="name"
            class="h-10.5 w-full rounded-lg border border-transparent bg-gray-100 px-3 py-2 outline-none transition-all duration-200 hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-600"
            :class="{ '!border-red-400': nameError }"
            name="name"
            :maxlength="useBackendData.validations.project.channels.max"
            type="text"
          />
          <p v-if="nameError" class="mt-1 text-xs text-red-400">{{ nameError }}</p>
        </div>
        <div>
          <div class="mb-1.5 flex items-center justify-between gap-2">
            <label class="text-sm font-semibold" for="channel-description">{{ i18n.t("channel.modal.description") }}</label>
            <span class="text-xs text-gray">{{ description.length }}/50</span>
          </div>
          <input
            id="channel-description"
            v-model.trim="description"
            class="h-10.5 w-full rounded-lg border border-transparent bg-gray-100 px-3 py-2 outline-none transition-all duration-200 hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-600"
            name="description"
            :maxlength="50"
            type="text"
          />
        </div>
        <div>
          <p class="mb-1 text-sm font-semibold">{{ i18n.t("channel.modal.color") }}</p>
          <p class="mb-3 text-sm text-gray">Choose the color used for this channel across versions and filters.</p>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="clr in useBackendData.channelColors"
              :key="clr.name"
              type="button"
              class="relative inline-flex h-9 w-9 items-center justify-center rounded-md border-2 transition-transform"
              :class="color === clr.hex ? 'border-white shadow-md dark:border-white' : 'border-transparent'"
              :style="{ backgroundColor: clr.hex }"
              :title="clr.name"
              :aria-label="`Use ${clr.name} channel color`"
              :aria-pressed="color === clr.hex"
              @click="color = clr.hex"
            >
              <IconMdiCheck v-if="color === clr.hex" class="text-lg text-white drop-shadow" />
            </button>
          </div>
          <div class="mt-3 flex items-center gap-2">
            <label
              class="relative inline-flex h-10.5 w-10.5 flex-shrink-0 cursor-pointer items-center justify-center overflow-hidden rounded-lg border border-gray-200 dark:border-gray-800"
              :style="{ backgroundColor: color || '#71717a' }"
              title="Choose a custom color"
            >
              <input v-model="color" type="color" class="absolute inset-0 h-full w-full cursor-pointer opacity-0" aria-label="Choose a custom channel color" />
              <IconMdiPaletteOutline class="pointer-events-none text-lg text-white drop-shadow" />
            </label>
            <input
              v-model.trim="color"
              type="text"
              class="h-10.5 min-w-0 flex-grow rounded-lg border border-transparent bg-gray-100 px-3 py-2 font-mono text-sm uppercase outline-none transition-all duration-200 hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-600"
              placeholder="#RRGGBB"
              maxlength="7"
              aria-label="Custom channel color hex value"
            />
          </div>
        </div>
      </div>

      <div class="mt-5">
        <h3 class="mb-2 text-sm font-semibold">Channel options</h3>
        <div class="grid gap-2 sm:grid-cols-2">
          <button
            v-for="f in possibleFlags"
            :key="f"
            type="button"
            class="flex min-h-12 items-center gap-2 rounded-lg border px-3 py-2 text-left text-sm transition-all duration-200 hover:border-gray-600 hover:bg-gray-800/60"
            :class="flags.includes(f) ? 'color-primary' : 'border-gray-200 bg-transparent dark:border-gray-800'"
            :style="
              flags.includes(f)
                ? {
                    backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                    borderColor: 'var(--primary-500)',
                  }
                : {}
            "
            :aria-pressed="flags.includes(f)"
            @click="flags = flags.includes(f) ? flags.filter((flag) => flag !== f) : [...flags, f]"
          >
            <span class="inline-flex flex-shrink-0 items-center text-lg">
              <IconMdiAlertOutline v-if="f === ChannelFlag.UNSTABLE" />
              <IconMdiPinOutline v-else-if="f === ChannelFlag.PINNED" />
              <IconMdiBellOutline v-else-if="f === ChannelFlag.SENDS_NOTIFICATIONS" />
              <IconMdiHideOutline v-else-if="f === ChannelFlag.HIDE_BY_DEFAULT" />
            </span>
            <span class="min-w-0 leading-snug">{{ i18n.t("channel.modal.flags." + f.toLowerCase()) }}</span>
            <IconMdiCheck v-if="flags.includes(f)" class="ml-auto flex-shrink-0 color-primary" />
          </button>
        </div>
      </div>

      <div class="mt-5 flex justify-end gap-2">
        <Button button-type="secondary" size="small" class="!h-9 !px-3 !py-1 text-sm" @click="on.click">
          {{ i18n.t("general.close") }}
        </Button>
        <Button size="small" class="!h-9 !px-3 !py-1 text-sm" :disabled="!name.trim() || !color || noChange" @click="create(on.click)">
          {{ edit ? i18n.t("general.save") : i18n.t("general.create") }}
        </Button>
      </div>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="open(on)" />
    </template>
  </Modal>
</template>
