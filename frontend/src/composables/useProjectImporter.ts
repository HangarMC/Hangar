import type { NewProjectForm } from "hangar-internal";
import { computed } from "vue";
import { useInternalApi } from "~/composables/useApi";
import { ProjectCategory } from "~/types/enums";

export interface SpigotAuthor {
  avatar: string;
  id: string;
  resource_count: string;
  username: string;
}

export async function getSpigotAuthor(author: string) {
  return (await useInternalApi("cors/", "GET", {
    url: buildSpigotUrl({
      action: "findAuthor",
      name: author,
    }),
  })) as SpigotAuthor;
}

export interface SpigotResource {
  id: string;
  title: string;
  tag: string;
  current_version: string;
  category: { title: string; id: string };
  native_minecraft_version: string;
  supported_minecraft_versions: string[];
  icon_link: string;
  premium: { price: string };
  description: string;
  external_download_url?: string;
}

export async function getAllSpigotResourcesByAuthor(authorId: string) {
  const result = [] as SpigotResource[];

  let page = 1;
  while (true) {
    const curr = await getSpigotResourcesByAuthor(authorId, page);
    if (curr?.length > 0) {
      result.push(...curr);
      page++;
    } else {
      break;
    }
  }

  return result;
}

export async function getSpigotResourcesByAuthor(authorId: string, page = 1) {
  return (await useInternalApi("cors/", "GET", {
    url: buildSpigotUrl({
      action: "getResourcesByAuthor",
      id: authorId,
      page,
    }),
  })) as SpigotResource[];
}

function buildSpigotUrl(params: Record<string, string | number>) {
  const url = new URL("https://api.spigotmc.org/simple/0.2/index.php");
  for (const key in params) {
    url.searchParams.append(key, params[key] + "");
  }
  return url.href;
}

const categoryMapping = {
  "5": ProjectCategory.GAMEPLAY, // Transportation
  "6": ProjectCategory.CHAT, // Chat
  "7": ProjectCategory.ADMIN_TOOLS, // Tools and Utilities
  "8": ProjectCategory.MISC, // Misc
  "9": ProjectCategory.DEV_TOOLS, // Libraries / APIs
  "10": ProjectCategory.GAMEPLAY, // Transportation again?!
  "11": ProjectCategory.CHAT, // Chat again?!
  "12": ProjectCategory.ADMIN_TOOLS, // Tools and Utilites again?!
  "13": ProjectCategory.MISC, // Misc again?!
  "14": ProjectCategory.CHAT, // CHat again again??!!
  "15": ProjectCategory.ADMIN_TOOLS, // Tools and Utilities again again??!!
  "16": ProjectCategory.MISC, // Misc again again??!!
  "17": ProjectCategory.GAMEPLAY, // Fun
  "18": ProjectCategory.WORLD_MANAGEMENT, // World Management
  "22": ProjectCategory.GAMEPLAY, // Mechanics
  "23": ProjectCategory.ECONOMY, // Economy
  "24": ProjectCategory.GAMES, // Game Mode,
  "26": ProjectCategory.DEV_TOOLS, // Libraries / APIs again?!
} as Record<string, ProjectCategory>;
const unspecifiedLicenseName = "Unspecified";

export async function convertSpigotProjects(spigotResources: SpigotResource[], ownerId: number) {
  const hangarResources = [] as NewProjectForm[];
  for (const spigotResource of spigotResources) {
    const hangarResource = {
      ownerId,
      settings: {
        license: { type: "Unspecified" } as NewProjectForm["settings"]["license"],
        donation: {} as NewProjectForm["settings"]["donation"],
        keywords: [],
        links: [],
        tags: [],
      } as unknown as NewProjectForm["settings"],
      externalId: spigotResource.id,
      name: spigotResource.title,
      description: spigotResource.tag,
      avatarUrl: spigotResource.icon_link,
      util: {},
    } as NewProjectForm;
    try {
      hangarResource.pageContent = await useInternalApi<string>("pages/convert-bbcode", "post", {
        content: spigotResource.description,
      });
    } catch (e) {
      console.log("failed to convert", hangarResource, e);
    }

    hangarResource.category = categoryMapping[spigotResource.category.id] || ProjectCategory.UNDEFINED;

    hangarResource.settings.license.type = unspecifiedLicenseName;
    hangarResource.util.isCustomLicense = computed(() => hangarResource.settings.license.type === "Other");
    hangarResource.util.licenseUnset = computed(() => hangarResource.settings.license.type === unspecifiedLicenseName);

    hangarResources.push(hangarResource);
  }

  return hangarResources;
}
