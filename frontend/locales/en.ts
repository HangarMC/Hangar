import { LocaleMessageObject } from 'vue-i18n';

const msgs: LocaleMessageObject = {
    hangar: {
        projectSearch: {
            query: 'Search in {0} projects, proudly made by the community...',
            relevanceSort: 'Sort with relevance',
        },
        subtitle: 'A Minecraft package repository',
        sponsoredBy: 'Sponsored by',
    },
    nav: {
        login: 'Login',
        signup: 'Signup',
        user: {
            notifications: 'Notifications',
            flags: 'Flags',
            projectApprovals: 'Project approvals',
            versionApprovals: 'Version approvals',
            stats: 'Stats',
            health: 'Hangar Health',
            log: 'User Action Log',
            platformVersions: 'Platform Versions',
            logout: 'Sign out',
        },
        new: {
            project: 'New Project',
            organization: 'New Organization',
        },
        hangar: {
            home: 'Homepage',
            forums: 'Forums',
            code: 'Code',
            docs: 'Docs',
            javadocs: 'JavaDocs',
            hangar: 'Hangar (Plugins)',
            downloads: 'Downloads',
            community: 'Community',
        },
    },
    project: {
        category: {
            admin_tools: 'Admin Tools',
            chat: 'Chat',
            dev_tools: 'Developer Tools',
            economy: 'Economy',
            gameplay: 'Gameplay',
            games: 'Games',
            protection: 'Protection',
            role_playing: 'Role Playing',
            world_management: 'World Management',
            misc: 'Miscellaneous',
        },
        actions: {
            unwatch: 'Unwatch',
            watch: 'Watch',
            flag: 'Flag',
            adminActions: 'Admin Actions',
            flagHistory: 'Flag history ({0})',
            staffNotes: 'Staff notes ({0})',
            userActionLogs: 'User Action Logs',
            forum: 'Forum',
        },
        tabs: {
            docs: 'Docs',
            versions: 'Versions',
            discuss: 'Discuss',
            settings: 'Settings',
            homepage: 'Homepage',
            issues: 'Issues',
            source: 'Source',
            support: 'Support',
        },
        sendForApproval: 'Send for approval',
    },
    visibility: {
        notice: {
            needsChanges: 'This project requires changes: {0}',
            needsApproval: 'You have sent the project for review',
            softDelete: 'Project deleted by {0}',
        },
    },
    message: 'Good morning!',
};

export default msgs;
