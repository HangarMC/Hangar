declare module 'hangar-api' {
    import { Model, Named, TagColor } from 'hangar-api';
    import { Platform, ReviewState, Visibility } from '~/types/enums';

    interface PluginDependency extends Named {
        required: boolean;
        projectId: number | null;
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

    interface Version extends Model, Named {
        pluginDependencies: Record<Platform, PluginDependency[]>;
        platformDependencies: Record<Platform, string[]>;
        visibility: Visibility;
        description: string;
        stats: VersionStats;
        fileInfo: FileInfo;
        author: String;
        reviewState: ReviewState;
        tags: Tag[];
    }
}
