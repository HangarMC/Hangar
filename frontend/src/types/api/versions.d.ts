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

  interface DependencyVersion {
    pluginDependencies: Record<Platform, PluginDependency[]>;
  }

  interface Version extends Model, Named, DependencyVersion, Visible {
    platformDependencies: Record<Platform, string[]>;
    description: string;
    stats: VersionStats;
    fileInfo: FileInfo;
    externalUrl: string | null;
    author: string;
    reviewState: ReviewState;
    tags: Tag[];
    recommended: Platform[];
  }
}
