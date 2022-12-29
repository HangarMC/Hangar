<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useHead } from "@vueuse/head";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import Markdown from "~/components/Markdown.vue";

const route = useRoute();

// TODO Finish up and move to docs site
const text = `
The documentation of resources on hangar uses Markdown, which is a markup language that allows you to format text in a simple manner.
You can learn how to use Markdown here: https://www.markdownguide.org/basic-syntax/

The most basic formatting tools are:
* ### Header: \`### Header\`, one # being the largest, five # being the smallest
* **Bold text**: \`**Bold**\`
* Italics text*: \`*Italics*\`
* Add line breaks by adding two or more spaces at the end of a line
* And more, like block quotes (\`> Text\`), lists (\`1. First item\`), unordered lists (\`* Item\`), and code blocks (\\\`Code\\\`)
* Embedding an image: \`![Alt text](image link)\` ![Snowy](https://i.imgur.com/o1zcXsM.png =32x32)

Additionally, Hangar provides the following features on top of basic Markdown syntax:
* ~~Strikethrough text~~: \`~~This will be striked through~~\`
* Links and e-mails are converted to clickable links automatically
* Footnotes[^footnote]: \`text [^footnote]\`, and later \`[^footnote]: footnote text\`
* Emojis \`:smile:\` :smile:
* [Tables](https://github.com/vsch/flexmark-java/tree/master/flexmark-ext-tables/src/test/resources)

Resizing images: \`![Alt text](Image URL =8x8)\`, in this case to size an image 8x8 pixels
![Snowy](https://i.imgur.com/o1zcXsM.png =8x8)

Embedding YouTube videos: \`@[YouTube](https://youtu.be/video-id)\`
* @[YouTube](https://youtu.be/AG8VFyW61e0?t=3)

Task lists: \`* [ ] First task\`, \`* [x] Completed task\`
* [ ] Unfinished task
* [x] Finished task

[^footnote]: May contain traces of soy
`;

useHead(useSeo("Hangar Markdown", null, route, null));
</script>

<template>
  <div>
    <PageTitle>{{ "Hangar Markdown" }}</PageTitle>
    <Markdown :raw="text"></Markdown>
  </div>
</template>
