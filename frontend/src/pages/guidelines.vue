<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";
import Markdown from "~/components/Markdown.vue";

const i18n = useI18n();
const route = useRoute();

// TODO Discuss and write proper guideliness, move to docs site
const text = `
Remember that these are just guidelines and that the Hangar team may choose to allow or disallow an action that is not
explicitly listed here at our own discretion.

In general, our [community guidelines](https://papermc.io/community-guidelines) also apply to resources submitted on
Hangar. The same applies to (international) law.

## Project Name

Your project's name should not include a platform version, tagline or other description. The name should be unique and
original and must not have a name implying it is an official PaperMC project. Large projects may receive a verified
status, but this is decided on a case-by-case basis and will not be given on request.

Examples of project names that are **not allowed**:

- *Home--The-Best-Plugin-Out-There*
- *PaperDocs*
- *Chairs-1.19*

Examples of names that are **allowed**:

- *Warps-Reborn*
- *Warps2*
- *CoolWarps-Paper* (although per-platform releases can be done in a single project)

## Project Category

The category you choose should be accurate. Your project should use the most fitting category rather than any category
that only slightly applies. If no category appears accurate, the Miscellaneous category should be used.

## Resource Updates

While the ratelimit for uploading new versions is very lenient to allow sometimes necessary hotfixes, you should not
unnecessarily push a large number updates to the release channel, solely to bump the update date of your project or to
flood watching users with notifications.

## Monetization / Advertising

Additional features or addons may not be hidden behind a paywall; this includes directly linking to off-site addons or
"full" versions of a "lite" plugin version. Advertisements and other revenue generating links (e.g. adfly) are not
permitted.

All functionality present in your plugin should be usable without restriction and cannot require a license key to
operate. Plugins may not be used to display advertisements.

On the resource page, you may link to the following:

* Related resources, such as projects you took inspiration from, dependencies, and *free* versions of the same resource
  on other websites
* Related documents, such as documentation, tutorials, source code, and support Discords, including paid support
* Donation links - you can also go to your project's donation settings to display them more prominently
* Paid subscription services (such as Patreon) to receive early previews of upcoming, free releases

### Sponsors

In project settings, you are given a text area to write sponsors into. Listed sponsors may only be donators (for example
one-time donators or Patreon subscribers) or OSS license sponsors (for example when being provided IntelliJ, YourKit, or
JProfiler license keys). Use this field in moderation and keep it secondary to your project's documentation. Only
information about sponsors may be put into the sponsors field.

## Obfuscation
To circumvent license issues and the effort involved in reviewing obfuscated code, we do not allow submitted plugins to be obfuscated in any capacity.

## EULA

We aim to comply with the [Minecraft EULA](https://account.mojang.com/documents/minecraft_eula). Any plugins, services,
posts, and/or links suspected of violating the EULA may be removed at the discretion of the Hangar staff.

### Offline-mode Support

Projects that explicitly state they are designed for such uses are not allowed. Some projects, such as authentication
systems, may have functionality that can be useful for servers regardless of the server's use of Mojang authentication,
but they may not promote this additional usage or be specifically designed for servers avoiding Mojang auth. Projects
designed for proxies requiring \`offline-mode=false\` are allowed, provided they are not written to facilitate
circumvention of Minecraft account ownership.

## Forks of Exisiting Projects

You are not allowed to re-upload resources of other authors as-is. Forks are permitted as long as they meet the
requirements as stated below. Staff have the final say in what constitutes an accepted fork. You must also follow the
license of the parent project appropriately.

Your project must either:

- Contain significant changes warranting the creation of a new project. This is to avoid "I changed the message colors
  in Plugin X and now I claim credit!", or
- Continue a plugin that has been abandoned, with the author no longer answering messages or having stated that the
  project will no longer be updated.

You have to acknowledge or credit the past plugin and its developers. Essentially, do not claim it is a new plugin and
exclusively your creation.

## Malicious Code

Plugins must not grant or revoke feature access to any particular user or group of users determined by the plugin
developer. This includes the author granting themselves a special display name or letting themselves use a special
command. Commands for granting specific, pre-programmed users OP or permissions are not acceptable.

Other malicious actions, such as deleting a server's data or banning random players, are also prohibited.

---
These guidelines are loosely based on [Ore's plugin submission guidelines](https://docs.spongepowered.org/stable/en/ore/guidelines.html),
licensed under the [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/).
`;

useHead(useSeo(i18n.t("pages.guidelinesTitle"), null, route, null));
</script>

<template>
  <PageTitle>{{ i18n.t("guidelines.title") }}</PageTitle>
  <h2 class="text-2xl font-bold uppercase">!!! NONE OF THIS IS FINAL !!!</h2>
  <Markdown :raw="text"></Markdown>
</template>
