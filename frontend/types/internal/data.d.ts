declare module 'hangar-internal' {
    import { Model, TagColor } from 'hangar-api';
    import { ProjectCategory } from '~/types/enums';

    interface Table extends Model {
        id: number;
    }

    interface IProjectCategory {
        title: string;
        icon: string;
        apiName: ProjectCategory;
        visible: boolean;
    }

    interface FlagReason {
        type: string;
        title: string;
    }

    interface Color {
        name: string;
        hex: string;
    }

    interface IPlatform {
        name: string;
        category: 'Server' | 'Proxy';
        url: string;
        tagColor: TagColor;
        possibleVersions: string[];
    }
}
