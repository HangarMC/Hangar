import { ChannelFlag } from "~/types/enums";

declare module "hangar-api" {
  import type { Model, Named, ProjectNamespace, TagColor, Visible } from "hangar-api";
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

  interface Tag extends Named {
    data: string;
    color: TagColor;
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
    fileInfo: FileInfo;
    externalUrl: string | null;
    author: string;
    reviewState: ReviewState;
    tags: Tag[];
    channel: ProjectChannel;
    pinned: boolean;
    recommended: Platform[];
  }

  interface Version extends VersionCompact, DependencyVersion {
    platformDependencies: Record<Platform, string[]>;
  }
}
