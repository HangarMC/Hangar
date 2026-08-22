<script setup lang="ts">
const props = defineProps<{
  href: string;
  label: string;
}>();

const target = computed(() => {
  try {
    const url = new URL(props.href);
    if (url.protocol !== "http:" && url.protocol !== "https:") return;
    return { host: url.hostname.toLowerCase(), path: url.pathname.toLowerCase() };
  } catch {
    return;
  }
});

function isHost(...hosts: string[]): boolean {
  const host = target.value?.host;
  return host !== undefined && hosts.some((safe) => host === safe || host.endsWith("." + safe));
}

// the name is free-form and may not be English, so the url decides wherever it can
const icon = computed(() => {
  const path = target.value?.path ?? "";
  const label = props.label.toLowerCase();
  if (path.includes("/issues") || /issue|bug|tracker/.test(label)) return "issues";
  if (path.includes("/wiki") || /wiki|doc/.test(label)) return "wiki";
  if (isHost("github.com")) return "github";
  if (isHost("gitlab.com")) return "gitlab";
  if (isHost("discord.com", "discord.gg", "discordapp.com")) return "discord";
  if (isHost("youtube.com", "youtu.be")) return "youtube";
  if (/source|code/.test(label)) return "source";
  if (/support|help|faq/.test(label)) return "support";
  if (/donat|sponsor|fund/.test(label)) return "donate";
  return "generic";
});
</script>

<template>
  <a
    :href="linkout(href)"
    target="_blank"
    rel="noopener noreferrer"
    class="inline-flex items-center gap-1.5 px-2 py-1 text-sm text-gray-secondary hover:text-gray"
  >
    <IconMdiBugOutline v-if="icon === 'issues'" class="flex-shrink-0" />
    <IconMdiBookOpenOutline v-else-if="icon === 'wiki'" class="flex-shrink-0" />
    <IconMdiGithub v-else-if="icon === 'github'" class="flex-shrink-0" />
    <IconMdiGitlab v-else-if="icon === 'gitlab'" class="flex-shrink-0" />
    <IconMdiDiscord v-else-if="icon === 'discord'" class="flex-shrink-0" />
    <IconMdiYoutube v-else-if="icon === 'youtube'" class="flex-shrink-0" />
    <IconMdiCodeTags v-else-if="icon === 'source'" class="flex-shrink-0" />
    <IconMdiLifebuoy v-else-if="icon === 'support'" class="flex-shrink-0" />
    <IconMdiHeartOutline v-else-if="icon === 'donate'" class="flex-shrink-0" />
    <IconMdiWeb v-else class="flex-shrink-0" />
    {{ label }}
    <IconMdiOpenInNew class="flex-shrink-0 text-2.5 opacity-70" />
  </a>
</template>
