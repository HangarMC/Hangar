declare module 'hangar-api' {
    import { Model, Named, ProjectNamespace, TagColor } from 'hangar-api';
    import { Platform, ReviewState, Visibility } from '~/types/enums';

    interface PluginDependency extends Named {
        required: boolean;
        namespace: ProjectNamespace | null;
        externalUrl: string | null;
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

    interface Version extends Model, Named, DependencyVersion {
        platformDependencies: Record<Platform, string[]>;
        visibility: Visibility;
        description: string;
        stats: VersionStats;
        fileInfo: FileInfo;
        externalUrl: string | null;
        author: String;
        reviewState: ReviewState;
        tags: Tag[];
        recommended: boolean;
    }
}
