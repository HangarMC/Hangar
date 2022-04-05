<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import PageTitle from "~/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";
import Markdown from "~/components/Markdown.vue";

const i18n = useI18n();
const route = useRoute();

// TODO Finish up and move to docs site
const text = `
The documentation of resources on hangar uses Markdown, which is a markup language that allows you to format text in a simple manner.

## Basic Syntax
Hangar supports most of the basic formatting options provided by the Base-Markdown language.  
A full guide on the basics of Markdown can be found here: https://www.markdownguide.org/basic-syntax

## Additional Syntaxes
The following features are provided on top of basic Markdown syntax:

### Strikethrough
Use \`~~text~~\` to make the text appear with a horizontal line through it.

**Example:**  
\`~~Strikethrough~~\`: ~~Strikethrough~~

### URLs and E-Mails
URLs and E-Mails will automatically be transformed into clickable links.

**Example:**  
\`https://papermc.io\`: https://papermc.io

### Footnotes
Use \`Text[^footnote]\` to create a footnote.  
You also need to put \`[^footnote]: text\` somewhere in the document to make it work.

**Example:**  
\`Footnote[^footnote]\`: Footnote[^footnote]

### Emojis
You can use emojis by using the common Emoji-Syntax for it.

**Example:**  
\`:smile:\`: :smile:

### Image resizing
You can change the size of an image by appending \`=[with]x[height]\` after the URL in an embedded image (Make sure to replace \`[width]\` and \`[height]\` with actual numbers).

**Example:**
\`![image](https://i.imgur.com/o1zcXsM.png =16x16)\`:  
![image](https://i.imgur.com/o1zcXsM.png =16x16)

### Task lists
Use \`* [ ] Text\` for an unchecked and \`* [x] Text\` for a checked list entry.

**Examples:**  
\`* [ ] Unchecked Entry\`:
* [ ] Unchecked Entry

\`* [x] Checked Entry\`:
* [x] Checked Entry

### Videos
You can embed YouTube videos by using a slight variation of the embedded image syntax.  
Use \`@[text](https://youtu.be/\<video-id\>)\` to embed a video (Replace \`\<video-id\>` with a valid ID).

**Example:**  
\`@[video](https://youtu.be/AG8VFyW61e0)\`:  
@[video](https://youtu.be/AG8VFyW61e0)

[^footnote]: May contain traces of soy
`;

useHead(useSeo("Hangar Markdown", null, route, null));
</script>

<template>
  <PageTitle>{{ "Hangar Markdown" }}</PageTitle>
  <Markdown :raw="text"></Markdown>
</template>
