import type { HeadObject } from "@unhead/vue";
import type { TranslateResult } from "vue-i18n";
import type { RouteLocationNormalizedLoaded } from "vue-router";
import { useSeoMeta } from "@unhead/vue";
import { useConfig } from "~/composables/useConfig";

export function useSeo(
  title: string | TranslateResult | null | undefined,
  description: string | TranslateResult | null | undefined,
  route: RouteLocationNormalizedLoaded,
  image: string | null
): HeadObject {
  description = description || "Plugin repository for the Paper, Waterfall and Folia platforms.";
  const config = useConfig();
  const canonical = config.publicHost + (route.fullPath.endsWith("/") ? route.fullPath.substring(0, route.fullPath.length - 1) : route.fullPath);
  image = image || "https://docs.papermc.io/img/paper.png";
  image = image.startsWith("http") ? image : config.publicHost + image;
  title = title ? title + " | Hangar" : "Hangar";

  useSeoMeta({
    title,
    description,
    ogUrl: canonical,
    ogTitle: title,
    twitterTitle: title,
    ogDescription: description,
    twitterDescription: description,
    ogType: "website",
    ogSiteName: "Hangar",
    ogImage: image,
    twitterImage: image,
    msapplicationTileImage: image,
    themeColor: "#ffffff",
    msapplicationTileColor: "#ffffff",
    msapplicationConfig: "/browserconfig.xml",
  });

  const seo = {
    title,
    link: [
      { rel: "canonical", href: canonical },
      { rel: "apple-touch-icon", sizes: "180x180", href: "/favicon/apple-touch-icon.png" },
      { rel: "icon", type: "image/png", sizes: "32x32", href: "/favicon/favicon-32x32.png" },
      { rel: "icon", type: "image/png", sizes: "16x16", href: "/favicon/favicon-16x16.png" },
      { rel: "manifest", href: "/site.webmanifest" },
      { rel: "mask-icon", href: "/favicon/safari-pinned-tab.svg", color: "#686868" },
      { rel: "shortcut icon", href: "/favicon/favicon.ico" },
    ],
    meta: [],
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
