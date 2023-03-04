declare module "hangar-internal" {
  import type { DependencyVersion, FileInfo, Named, Version } from "hangar-api";
  import type { Table } from "hangar-internal";
  import type { Platform } from "~/types/enums";
  import { ChannelFlag } from "~/types/enums";

  interface PendingVersion extends DependencyVersion {
    platformDependencies: Record<Platform, string[]>;
    versionString: string | null;
    description: string | null;
    files: PendingVersionFile[];
    channelName: string;
    channelColor: string;
    channelFlags: ChannelFlag[];
    forumSync: boolean;
  }

  interface PendingVersionFile {
    platforms: Platform[];
    fileInfo: FileInfo | null;
    externalUrl: string | null;
  }

  interface ProjectChannel extends Named, Partial<Table> {
    color: string;
    flags: ChannelFlag[];
    temp?: boolean;
    versionCount: number;
  }

  interface HangarVersion extends Version {
    id: number;
    approvedBy?: string;
  }

  interface JarScanResult extends Table {
    versionId: number;
    platform: string;
    highestSeverity: string;
    data: string[][];
  }
}
