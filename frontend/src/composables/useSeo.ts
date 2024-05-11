import type { UseHeadInput, Script } from "@unhead/vue";
import type { TranslateResult } from "vue-i18n";
import type { RouteLocationNormalized } from "vue-router/auto";

export function useSeo(
  title: string | TranslateResult | null | undefined,
  description: string | TranslateResult | null | undefined,
  route: RouteLocationNormalized,
  image: string | null,
  additionalScripts?: { type: string; children: string; key: string }[]
): UseHeadInput {
  description = description || "Plugin repository for Paper, Velocity, Waterfall and Folia.";
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

  const script = [
    {
      type: "application/ld+json",
      children: JSON.stringify({
        "@context": "https://schema.org",
        "@type": "BreadcrumbList",
        itemListElement: generateBreadcrumbs(route),
      }),
      key: "breadcrumb",
    },
  ] as Script[];

  if (additionalScripts) {
    script.push(...additionalScripts);
  }

  if (useNuxtApp().$i18n.locale.value === "dum") {
    console.log("found crowdin language activated, lets inject the script");
    script.push(
      {
        type: "text/javascript",
        innerHTML: `var _jipt = [];
                   _jipt.push(['project', '0cbf58a3d76226e92659632533015495']);
                   _jipt.push(['domain', 'hangar']);
                   _jipt.push([
                       "edit_strings_context",
                       (context) => {
                           const separator = "\\n";
                           const lineStart = "* ";
                           const contextParts = context.split(separator);
                           let linkCount = 0;

                           for (const i in contextParts) {
                               const line = contextParts[i];
                               if (lineStart + window.location.href === line) {
                                   return context;
                               }

                               if (line.indexOf(lineStart) === 0) {
                                   linkCount++;
                               }

                               if (line === "And more...") {
                                   return context;
                               }
                           }

                           if (linkCount < 5) {
                               return context + separator + lineStart + window.location.href;
                           }

                           return context + separator + "And more...";
                       },
                   ]);
              `,
      },
      {
        type: "text/javascript",
        src: "https://cdn.crowdin.com/jipt/jipt.js",
      }
    );
  }

  return {
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
    script,
  } as UseHeadInput;
}

function generateBreadcrumbs(route: RouteLocationNormalized) {
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
