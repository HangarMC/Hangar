<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import PageTitle from "~/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";
import Markdown from "~/components/Markdown.vue";

const i18n = useI18n();
const route = useRoute();

// TODO Discuss and write proper guideliness, move to docs site
const text = `
Welcome to the Hanagr resource guidelines. This document provides an outline of our expectations for both project and
file submissions.

Remember that these are just guidelines and that the Hangar team may choose to allow or disallow an action that is not
explicitly listed here at our own discretion.

In general, our [community guidelines](https://papermc.io/community-guidelines) also apply to resources submitted on
Hangar. The same applies to (international) law.

## Project Name

Your project's name should not include a tagline or other description. The name should be unique and original and must
not have a name implying it is an official PaperMC project.

Examples of project names that are **not acceptable**:

- *Warps-On-Sale*
- *Home--The-Best-Plugin-Out-There*
- *PaperDocs*

Examples of names that are **acceptable**:

- *CoolWarps-Paper*
- *Calendar-for-Sponge*

## Main Documentation Page (Home)

If your project sends information (for example a plugin list or player data) to you or another service and these metrics
cannot be turned off, the information collected must be listed on the main page. This also applies to plugins
downloading executable files (\`.jar\` files, \`.exe\` files, etc.).

Examples of systems that require documentation if they do not have an opt-out:

- Plugin auto-updaters
- Statistics or usage information collection (metrics)
- IRC/Discord/Telegram/WhatsApp bot or relay

Examples of systems that do **not** require documentation:

- Local databases or database connections specified by the end-user
- Metrics with an option to disable them (like bStats)
- File downloads that are very intentionally initiated by the user, for example via a command

## Project Category

The category you choose should be accurate. Your project should use the narrowest category possible rather than any
category that slightly applies. If no category appears accurate, the Miscellaneous category should be used.

## Monetization / Advertising

Resources may not be sold, nor may additional features be unlockable with payment. Advertisements and other revenue
generating links (e.g. adfly) are not permitted. You can, however, set donation links in the project's settings and use
that as a basis for exclusively paid support, for example.

All functionality present in your plugin should be usable without restriction and cannot require a license key to
operate. Plugins may not be used to display advertisements.

## Offline-mode Support

Projects that explicitly state they are designed for such uses are not allowed. Some projects, such as authentication
systems, may have functionality that can be useful for servers regardless of the server’s use of Mojang authentication,
but they may not promote this additional usage or be specifically designed for servers avoiding Mojang auth. Projects
designed for proxies requiring online-mode=false are allowed, provided they are not written to facilitate circumvention
of Minecraft account ownership.

## EULA

We aim to comply with the [Minecraft EULA](https://account.mojang.com/documents/minecraft_eula). Any plugins, services,
posts, and/or links suspected of violating the EULA may be removed at the discretion of the Hangar staff.

## Forks of Exisiting Projects

Forks are permitted, provided they meet all items in the below list. Staff have the final say in what constitutes an
accepted fork. You must also follow the license of the parent project appropriately.

Either:

- Contain significant changes warranting the creation of a new project. This is to avoid "I changed the message colors
  in Plugin X and now I claim credit!", or
- Continue a plugin that has been abandoned, with proof the author has not been answering messages or has stated the
  project will no longer be updated.

Acknowledge or credit the past plugin and developers. Essentially, do not claim it is a new plugin and exclusively your
creation.

## Privilege Granting and Malicious Actions

Plugins must not grant or revoke feature access to any particular user or group of users determined by the plugin
developer. This includes the author granting themselves a special display name or letting themselves use a special
command. Commands for granting specific, pre-programmed users OP or permissions are not acceptable.

Other malicious actions, such as deleting a server's data or banning random players, are also prohibited.

---
Currently being a modified version of Ore guidelines, this document is licensed under a
[Creative Commons Attribution-ShareAlike 4.0 International License](https://creativecommons.org/licenses/by-sa/4.0/).
`;

useHead(useSeo(i18n.t("pages.guidelinesTitle"), null, route, null));
</script>

<template>
  <PageTitle>{{ i18n.t("guidelines.title") }}</PageTitle>
  <h2 class="text-2xl font-bold uppercase">!!! NONE OF THIS IS FINAL, THIS IS JUST A QUICK MOCKUP !!!</h2>
  <Markdown :raw="text"></Markdown>
</template>
