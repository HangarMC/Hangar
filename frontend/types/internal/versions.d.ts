declare module 'hangar-internal' {
    import { FileInfo, Named, Version } from 'hangar-api';
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
        channelNonReviewed: boolean;
        forumSync: boolean;
        unstable: boolean;
        recommended: boolean;
        isFile: boolean;
    }

    interface ProjectChannel extends Named {
        color: string;
        nonReviewed: boolean;
        temp?: boolean;
    }

    interface HangarVersion extends Version {
        id: number;
        approvedBy?: string;
    }
}
