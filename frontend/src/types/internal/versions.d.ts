declare module "hangar-internal" {
  import type { DependencyVersion, FileInfo, Named, ProjectNamespace, Version } from "hangar-api";
  import type { Table } from "hangar-internal";
  import type { Platform } from "~/types/enums";

  interface PlatformDependency {
    name: string;
    required: boolean;
    namespace: ProjectNamespace | null;
    externalUrl: string | null;
  }

  interface PendingVersion extends DependencyVersion {
    platformDependencies: Record<Platform, string[]>;
    versionString: string | null;
    description: string | null;
    fileInfo: FileInfo;
    externalUrl: string | null;
    channelName: string;
    channelColor: string;
    channelNonReviewed: boolean;
    forumSync: boolean;
    unstable: boolean;
    recommended: boolean;
    isFile: boolean;
  }

  interface ProjectChannel extends Named, Partial<Table> {
    color: string;
    nonReviewed: boolean;
    editable: boolean;
    temp?: boolean;
    versionCount: number;
  }

  interface HangarVersion extends Version {
    id: number;
    approvedBy?: string;
  }
}
