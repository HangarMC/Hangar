declare module 'hangar-internal' {
    import { FileInfo } from 'hangar-api';
    import { Platform } from '~/types/enums';

    interface PlatformDependency {
        name: string;
        required: boolean;
        projectId: number | null;
        externalUrl: string | null;
    }

    interface PendingVersion {
        versionString: string | null;
        pluginDependencies: Record<Platform, PlatformDependency[]>;
        platformDependencies: Record<Platform, string[]>;
        description: string | null;
        fileInfo: FileInfo;
        externalUrl: string | null;
        channelName: string;
        channelColor: string;
        forumSync: boolean;
        isFile: boolean;
    }
}
