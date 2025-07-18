import { Category } from "#shared/types/backend";
import type { NewProjectForm } from "#shared/types/backend";

export type ImportedProject = NewProjectForm & { externalId: string; util: { isCustomLicense?: ComputedRef<boolean>; licenseUnset?: ComputedRef<boolean> } };

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
  "5": Category.Gameplay, // Transportation
  "6": Category.Chat, // Chat
  "7": Category.AdminTools, // Tools and Utilities
  "8": Category.Misc, // Misc
  "9": Category.DevTools, // Libraries / APIs
  "10": Category.Gameplay, // Transportation again?!
  "11": Category.Chat, // Chat again?!
  "12": Category.AdminTools, // Tools and Utilites again?!
  "13": Category.Misc, // Misc again?!
  "14": Category.Chat, // CHat again again??!!
  "15": Category.AdminTools, // Tools and Utilities again again??!!
  "16": Category.Misc, // Misc again again??!!
  "17": Category.Gameplay, // Fun
  "18": Category.WorldManagement, // World Management
  "22": Category.Gameplay, // Mechanics
  "23": Category.Economy, // Economy
  "24": Category.Games, // Game Mode,
  "26": Category.DevTools, // Libraries / APIs again?!
} as Record<string, Category>;
const unspecifiedLicenseName = "Unspecified";

export async function convertSpigotProjects(spigotResources: SpigotResource[], ownerId: number) {
  const hangarResources = [] as ImportedProject[];
  for (const spigotResource of spigotResources) {
    const hangarResource = {
      ownerId,
      settings: {
        license: { type: "Unspecified" } as ImportedProject["settings"]["license"],
        donation: {} as ImportedProject["settings"]["donation"],
        keywords: [],
        links: [],
        tags: [],
      } as unknown as ImportedProject["settings"],
      externalId: spigotResource.id,
      name: spigotResource.title,
      description: spigotResource.tag,
      avatarUrl: spigotResource.icon_link,
      util: {},
    } as ImportedProject;
    try {
      hangarResource.pageContent = await useInternalApi<string>("pages/convert-bbcode", "post", {
        content: spigotResource.description,
      });
    } catch (err) {
      console.log("failed to convert", hangarResource, err);
    }

    hangarResource.category = categoryMapping[spigotResource.category.id] || Category.Undefined;

    hangarResource.settings.license.type = unspecifiedLicenseName;
    hangarResource.util.isCustomLicense = computed(() => hangarResource.settings.license.type === "Other");
    hangarResource.util.licenseUnset = computed(() => hangarResource.settings.license.type === unspecifiedLicenseName);

    hangarResources.push(hangarResource);
  }

  return hangarResources;
}
