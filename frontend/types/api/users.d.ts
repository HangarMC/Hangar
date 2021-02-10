declare module 'hangar-api' {
    import { Color, Model, Named } from 'hangar-api';
    import { RoleCategory } from '~/types/enums';

    interface Role {
        value: string;
        roleId: number;
        category: RoleCategory;
        permission: string;
        title: string;
        color: Color;
    }

    interface User extends Model, Named {
        tagline: string | null;
        joinDate: string;
        roles: Role[];
        projectCount: number;
    }
}
