declare module 'hangar-internal' {
    import { Model, TagColor } from 'hangar-api';
    import { Platform, ProjectCategory, Visibility } from '~/types/enums';

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
        enumName: Platform;
        category: 'Server' | 'Proxy';
        url: string;
        tagColor: TagColor;
        possibleVersions: string[];
    }

    interface IVisibility {
        name: Visibility;
        showModal: boolean;
        cssClass: string;
        title: string;
    }
}
