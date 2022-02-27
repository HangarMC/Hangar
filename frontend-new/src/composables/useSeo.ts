import type { HeadObject } from "@vueuse/head";
import type { TranslateResult } from "vue-i18n";
import type { RouteLocationNormalizedLoaded } from "vue-router";

export function useSeo(
  title: string | TranslateResult,
  description: string | TranslateResult | null,
  route: RouteLocationNormalizedLoaded,
  image: string | null
): HeadObject {
  description = description || "Plugin repository for Paper plugins and more!";
  const canonical = baseUrl() + (route.fullPath.endsWith("/") ? route.fullPath : `${route.fullPath}/`);
  image = image || "https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png";
  image = image.startsWith("http") ? image : baseUrl() + image;
  const seo = {
    title,
    link: [{ rel: "canonical", href: canonical }],
    meta: [
      { hid: "description", name: "description", content: description },
      {
        property: "og:description",
        name: "og:description",
        vmid: "og:description",
        hid: "og:description",
        content: description,
      },
      {
        property: "twitter:description",
        name: "twitter:description",
        vmid: "twitter:description",
        hid: "twitter:description",
        content: description,
      },
      {
        property: "og:title",
        name: "og:title",
        hid: "og:title",
        template: (chunk: string) => (chunk ? `${chunk} | Hangar` : "Hangar"),
        content: title,
      },
      {
        property: "twitter:title",
        name: "twitter:title",
        hid: "twitter:title",
        template: (chunk: string) => (chunk ? `${chunk} | Hangar` : "Hangar"),
        content: title,
      },
      {
        property: "og:url",
        name: "og:url",
        hid: "og:url",
        content: canonical,
      },
      {
        property: "twitter:url",
        name: "twitter:url",
        hid: "twitter:url",
        content: canonical,
      },
      {
        property: "og:image",
        name: "og:image",
        hid: "og:image",
        content: image,
      },
    ],
    script: [
      {
        type: "application/ld+json",
        json: {
          "@context": "https://schema.org",
          "@type": "BreadcrumbList",
          itemListElement: generateBreadcrumbs(route),
        },
      },
    ] as any[],
  } as HeadObject;

  // todo renenable crowdin integration
  // if (context.app.i18n.locale === 'dum') {
  //     console.log('found crowdin language activated, lets inject the script');
  //     seo.script = seo.script ? seo.script : [];
  //     seo.script.push({
  //         type: 'text/javascript',
  //         innerHTML: 'var _jipt = []; _jipt.push([\'project\', \'0cbf58a3d76226e92659632533015495\']); _jipt.push([\'domain\', \'hangar\']);'
  //     });
  //     seo.script.push({
  //         type: 'text/javascript',
  //         src: 'https://cdn.crowdin.com/jipt/jipt.js'
  //     });
  //     seo.__dangerouslyDisableSanitizers = ['script'];
  // }

  return seo;
}

function generateBreadcrumbs(route: RouteLocationNormalizedLoaded) {
  const arr = [];
  const split = route.fullPath.split("/");
  let curr = "";
  for (let i = 0; i < split.length; i++) {
    curr = `${curr + split[i]}/`;
    arr.push({
      "@type": "ListItem",
      position: i,
      name: guessTitle(split[i]),
      item: baseUrl() + curr,
    });
  }

  return arr;
}

function guessTitle(segment: string): string {
  return segment === "/" || segment === "" ? "Hangar" : segment;
}

function baseUrl(): string {
  // todo get this from somewhere
  return "https://hangar.benndorf.dev";
}
