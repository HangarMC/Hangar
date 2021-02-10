declare module 'hangar-api' {
    import { Model, Named, TagColor } from 'hangar-api';
    import { ReviewState, Visibility } from '~/types/enums';

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
        name: string;
        urlPath: string;
        dependencies: object; // TODO
        visibility: Visibility;
        description: string;
        stats: VersionStats;
        fileInfo: FileInfo;
        author: String;
        reviewState: ReviewState;
        tags: Tag[];
    }
}
