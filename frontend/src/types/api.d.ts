declare module "hangar-api" {
  import type { Visibility } from "~/types/enums";

  interface Model {
    createdAt: string;
  }

  interface Named {
    name: string;
  }

  interface Announcement {
    text: string;
    color: string;
  }

  interface Sponsor {
    image: string;
    name: string;
    link: string;
  }

  interface Visible {
    visibility: Visibility;
  }
}
