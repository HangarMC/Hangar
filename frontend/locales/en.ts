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
        new: {
            step1: {
                title: 'Agree to guidelines',
                text:
                    'A project contains your downloads and the documentation for your plugin.<br>Before continuing, please review the <a href="#">Hangar Submission Guidelines.</a>',
                continue: 'I agree',
                back: 'Abort',
            },
            step2: {
                title: 'Basic Settings',
                continue: 'Continue',
                back: 'Back',
                userselect: 'Create as...',
                projectname: 'Project name',
                projectsummary: 'Project Summary',
                projectcategory: 'Project Category',
            },
            step3: {
                title: 'Additional Settings',
                continue: 'Continue',
                back: 'Back',
                optional: 'Optional',
                links: 'Links',
                homepage: 'Homepage',
                issues: 'Issue Tracker',
                source: 'Source Code',
                support: 'External Support',
                licence: 'Licence',
                type: 'Type',
                url: 'URL',
                seo: 'SEO',
                keywords: 'Keywords',
            },
            step4: {
                title: 'Import from Spigot',
                continue: 'Continue',
                back: 'Back',
                optional: 'Optional',
                convert: 'Convert',
                preview: 'Preview',
                tutorial: 'How to get the BBCode',
            },
            step5: {
                title: 'Finishing',
                text: 'Creating...',
            },
        },
        sendForApproval: 'Send for approval',
    },
    organization: {
        new: {
            title: 'Create a new Organization',
            text: 'Organizations allow you group users provide closer collaboration between them within your projects on Hangar.',
            name: 'Organization Name',
        },
    },
    form: {
        userSelection: {
            addUser: 'Add User...',
            create: 'Create',
        },
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
