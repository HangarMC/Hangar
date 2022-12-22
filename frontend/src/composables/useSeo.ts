import type { HeadObject } from "@vueuse/head";
import type { TranslateResult } from "vue-i18n";
import type { RouteLocationNormalizedLoaded } from "vue-router";
import { useConfig } from "~/lib/composables/useConfig";

export function useSeo(
  title: string | TranslateResult | null | undefined,
  description: string | TranslateResult | null | undefined,
  route: RouteLocationNormalizedLoaded,
  image: string | null
): HeadObject {
  description = description || "Plugin repository for Paper plugins and more!";
  const config = useConfig();
  const canonical = config.publicHost + (route.fullPath.endsWith("/") ? route.fullPath.substring(0, route.fullPath.length - 1) : route.fullPath);
  image = image || "https://docs.papermc.io/img/paper.png";
  image = image.startsWith("http") ? image : config.publicHost + image;
  title = title ? title + " | Hangar" : "Hangar";
  const seo = {
    title,
    link: [
      { rel: "canonical", href: canonical },
      { rel: "apple-touch-icon", sizes: "180x180", href: "/favicon/apple-touch-icon.png" },
      { rel: "icon", type: "image/png", sizes: "32x32", href: "/favicon/favicon-32x32.png" },
      { rel: "icon", type: "image/png", sizes: "16x16", href: "/favicon/favicon-16x16.png" },
      { rel: "manifest", href: "/favicon/site.webmanifest" },
      { rel: "mask-icon", href: "/favicon/safari-pinned-tab.svg", color: "#686868" },
      { rel: "shortcut icon", href: "/favicon/favicon.ico" },
    ],
    meta: [
      { property: "description", name: "description", content: description },
      {
        property: "og:description",
        name: "og:description",
        content: description,
      },
      {
        property: "twitter:description",
        name: "twitter:description",
        content: description,
      },
      {
        property: "og:title",
        name: "og:title",
        content: title,
      },
      {
        property: "twitter:title",
        name: "twitter:title",
        content: title,
      },
      {
        property: "og:url",
        name: "og:url",
        content: canonical,
      },
      {
        property: "twitter:url",
        name: "twitter:url",
        content: canonical,
      },
      {
        property: "og:image",
        name: "og:image",
        content: image,
      },
      { property: "msapplication-TileColor", name: "msapplication-TileColor", content: "#da532c" },
      { property: "msapplication-config", name: "msapplication-config", content: "/favicon/browserconfig.xml" },
      { property: "theme-color", name: "theme-color", content: "#ffffff" },
    ],
    script: [
      {
        type: "application/ld+json",
        children: JSON.stringify({
          "@context": "https://schema.org",
          "@type": "BreadcrumbList",
          itemListElement: generateBreadcrumbs(route),
        }),
        key: "breadcrumb",
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
  const config = useConfig();
  for (let i = 0; i < split.length; i++) {
    // skip trailing slash
    if ((split[i] === "" || split[i] === "/") && curr !== "") continue;
    curr = `${curr + split[i]}/`;
    arr.push({
      "@type": "ListItem",
      position: i,
      name: guessTitle(split[i]),
      item: config.publicHost + curr,
    });
  }

  return arr;
}

function guessTitle(segment: string): string {
  return segment === "/" || segment === "" ? "Hangar" : segment;
}
