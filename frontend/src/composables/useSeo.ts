import type { Script } from "@unhead/vue";
import type { TranslateResult } from "vue-i18n";
import type { RouteLocationNormalized } from "vue-router/auto";

export function useSeo(
  input: ComputedRef<{
    title: string | TranslateResult | null | undefined;
    description?: string | TranslateResult | null;
    route: RouteLocationNormalized;
    image?: string | null;
    additionalScripts?: { type: string; textContent: string; key: string }[];
    manualTitle?: boolean;
  }>
) {
  const config = useRuntimeConfig();
  const i18n = useNuxtApp().$i18n;

  const description = computed(() => input.value.description || "Plugin repository for Paper, Velocity, Waterfall and Folia.");
  const canonical = computed(
    () =>
      config.public.host +
      (input.value.route.path.endsWith("/") ? input.value.route.path.slice(0, Math.max(0, input.value.route.path.length - 1)) : input.value.route.path)
  );
  const image = computed(() => {
    let img = input.value.image || "https://docs.papermc.io/img/paper.png";
    img = img.startsWith("http") ? img : config.public.host + img;
    return img;
  });
  const title = computed(() => {
    let title = input.value.title;
    if (!input.value.manualTitle) {
      title = title ? title + " | Hangar" : "Hangar";
    }
    return title!;
  });

  useSeoMeta({
    title: () => title.value,
    description: () => description.value,
    ogUrl: () => canonical.value,
    ogTitle: () => title.value,
    twitterTitle: () => title.value,
    ogDescription: () => description.value,
    twitterDescription: () => description.value,
    ogType: "website",
    ogSiteName: "Hangar",
    ogImage: () => image.value,
    twitterImage: () => image.value,
    msapplicationTileImage: () => image.value,
    themeColor: "#ffffff",
    msapplicationTileColor: "#ffffff",
    msapplicationConfig: "/browserconfig.xml",
  });

  const script = computed(() => {
    const result = [
      {
        type: "application/ld+json",
        textContent: JSON.stringify({
          "@context": "https://schema.org",
          "@type": "BreadcrumbList",
          itemListElement: generateBreadcrumbs(input.value.route, config.public.host),
        }),
        key: "breadcrumb",
      },
    ] as Script[];

    if (input.value.additionalScripts) {
      result.push(...input.value.additionalScripts);
    }

    if (i18n.locale.value === "dum") {
      console.log("found crowdin language activated, lets inject the script");
      result.push(
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
          defer: true,
          src: "https://cdn.crowdin.com/jipt/jipt.js",
        }
      );
    }
    return result;
  });

  useHead({
    title: () => title.value,
    link: [
      { rel: "canonical", href: () => canonical.value },
      { rel: "apple-touch-icon", sizes: "180x180", href: "/favicon/apple-touch-icon.png" },
      { rel: "icon", type: "image/png", sizes: "32x32", href: "/favicon/favicon-32x32.png" },
      { rel: "icon", type: "image/png", sizes: "16x16", href: "/favicon/favicon-16x16.png" },
      { rel: "manifest", href: "/site.webmanifest" },
      { rel: "mask-icon", href: "/favicon/safari-pinned-tab.svg", color: "#686868" },
      { rel: "shortcut icon", href: "/favicon/favicon.ico" },
    ],
    script,
  });
}

function generateBreadcrumbs(route: RouteLocationNormalized, host: string) {
  const arr = [];
  const split = route.path.split("/");
  let curr = "";
  for (const [i, element] of split.entries()) {
    // skip trailing slash
    if ((element === "" || element === "/") && curr !== "") continue;
    curr = `${curr + element}/`;
    arr.push({
      "@type": "ListItem",
      position: i,
      name: guessTitle(element),
      item: host + curr,
    });
  }

  return arr;
}

function guessTitle(segment: string): string {
  return segment === "/" || segment === "" ? "Hangar" : segment;
}
