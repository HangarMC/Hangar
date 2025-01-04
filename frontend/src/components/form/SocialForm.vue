<script lang="ts" setup>
const { t } = useI18n();
const notification = useNotificationStore();

const socials = defineModel<Record<string, string>>({ required: true });

const linkType = ref<string>();
const linkTypes = [
  { value: "discord", text: "Discord" },
  { value: "github", text: "GitHub" },
  { value: "twitter", text: "Twitter" },
  { value: "youtube", text: "YouTube" },
  { value: "website", text: "Website" },
];

function addLink() {
  if (!linkType.value) {
    return notification.error("You have to select a type");
  }
  if (Object.keys(socials.value).includes(linkType.value)) {
    return notification.error("You already have a link of that type added");
  }
  socials.value[linkType.value] = "";
}

function removeLink(type: string) {
  delete socials.value[type];
}
</script>

<template>
  <div v-for="(_, type) in socials" :key="type" class="flex items-center mt-2">
    <span class="w-25">{{ linkTypes.find((e) => e.value === type)?.text }}</span>
    <div class="w-75">
      <InputText v-if="type === 'website'" v-model="socials[type]" label="URL" :rules="[required(), validUrl()]" />
      <InputText v-else v-model="socials[type]" :label="t('auth.settings.account.username')" :rules="[required()]" />
    </div>
    <IconMdiBin class="ml-2 w-6 h-6 cursor-pointer hover:color-red" @click="removeLink(type)" />
  </div>
  <div class="flex items-center mt-2">
    <div class="w-25">
      <Button button-type="secondary" @click.prevent="addLink">Add link</Button>
    </div>
    <div class="w-75">
      <InputSelect v-model="linkType" :values="linkTypes" :label="t('project.settings.links.typeField')" />
    </div>
  </div>
</template>
