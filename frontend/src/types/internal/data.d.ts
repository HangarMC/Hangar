declare module "hangar-internal" {
  import type { Model } from "hangar-api";
  import type { Platform, ProjectCategory, Prompt, Visibility } from "~/types/enums";

  interface Table extends Model {
    id: number;
  }

  interface IProjectCategory {
    title: string;
    icon: string;
    apiName: ProjectCategory;
    visible: boolean;
  }

  interface FlagReason {
    type: string;
    title: string;
  }

  interface Color {
    name: string;
    hex: string;
  }

  interface IPlatform {
    name: string;
    enumName: Platform;
    category: "Server" | "Proxy";
    url: string;
    possibleVersions: string[];
    visible: boolean;
  }

  interface IVisibility {
    name: Visibility;
    showModal: boolean;
    canChangeTo: boolean;
    cssClass: string;
    title: string;
  }

  interface IPrompt {
    ordinal: number;
    name: Prompt;
    titleKey: string;
    messageKey: string;
  }
}
