import { ChannelFlag, PinnedStatus } from "~/types/enums";

declare module "hangar-api" {
  import type { Model, Named, ProjectNamespace, Visible } from "hangar-api";
  import type { Platform, ReviewState } from "~/types/enums";

  interface PluginDependency extends Named {
    required: boolean;
    namespace: ProjectNamespace | null;
    externalUrl: string | null;
  }

  interface LastDependencies {
    pluginDependencies: PluginDependency[];
    platformDependencies: string[];
  }

  interface VersionStats {
    downloads: number;
  }

  interface FileInfo {
    name: string;
    sizeBytes: number;
    md5Hash: string;
  }

  interface ProjectChannel extends Model, Named {
    color: string;
    flags: ChannelFlag[];
  }

  interface DependencyVersion {
    pluginDependencies: Record<Platform, PluginDependency[]>;
  }

  interface VersionCompact extends Model, Named, Visible {
    description: string;
    stats: VersionStats;
    fileInfo: FileInfo | null;
    externalUrl: string | null;
    author: string;
    reviewState: ReviewState;
    channel: ProjectChannel;
    pinned: boolean;
    pinnedStatus: PinnedStatus;
  }

  interface Version extends VersionCompact, DependencyVersion {
    platformDependencies: Record<Platform, string[]>;
  }
}
