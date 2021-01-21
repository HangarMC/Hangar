export enum RoleCategory {
    GLOBAL = 'global',
    PROJECT = 'project',
    ORGANIZATION = 'organization',
}

export enum ApiSessionType {
    KEY = 'key',
    USER = 'user',
    PUBLIC = 'public',
    DEV = 'dev',
}

export enum ProjectCategory {
    ADMIN_TOOLS = 'admin_tools',
    CHAT = 'chat',
    DEV_TOOLS = 'dev_tools',
    ECONOMY = 'economy',
    GAMEPLAY = 'gameplay',
    GAMES = 'games',
    PROTECTION = 'protection',
    ROLE_PLAYING = 'role_playing',
    WORLD_MANAGEMENT = 'world_management',
    MISC = 'misc',
    UNDEFINED = 'undefined',
}

export enum Visibility {
    PUBLIC = 'public',
    NEW = 'new',
    NEEDS_CHANGES = 'needsChanges',
    NEEDS_APPROVAL = 'needsApproval',
    SOFT_DELETE = 'softDelete',
}

// export class ProjectCategory {
//     private static values: Map<string, ProjectCategory>;
//     public static getValues(): IterableIterator<ProjectCategory> {
//         return this.values.values();
//     }
//
//     public static getByName(name: string): ProjectCategory | undefined {
//         return this.values.get(name);
//     }
//
//     public static ADMIN_TOOLS = ProjectCategory.create('admin_tools', 'Admin Tools', 'fa-server');
//     public static CHAT = ProjectCategory.create('chat', 'Chat', 'fa-comment');
//     public static DEV_TOOLS = ProjectCategory.create('dev_tools', 'Developer Tools', 'fa-wrench');
//     public static ECONOMY = ProjectCategory.create('economy', 'Economy', 'fa-money-bill-alt');
//     public static GAMEPLAY = ProjectCategory.create('gameplay', 'Gameplay', 'fa-puzzle-piece');
//     public static GAMES = ProjectCategory.create('games', 'Games', 'fa-gamepad');
//
//     name: string;
//     title: string;
//     icon: string;
//
//     constructor(name: string, title: string, icon: string) {
//         this.name = name;
//         this.title = title;
//         this.icon = icon;
//     }
//
//     static create(name: string, title: string, icon: string): ProjectCategory {
//         const category = new ProjectCategory(name, title, icon);
//         this.values.set(name, category);
//         return category;
//     }
// }
