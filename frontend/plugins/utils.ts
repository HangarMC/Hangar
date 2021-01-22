import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';

const createUtil = (_: Context) => {
    class Util {
        avatarUrl(name: String): String {
            return 'https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png';
            // TODO avatar url
            // return '/avatar/' + name + '?size=120x120';
        }

        forumUrl(name: String): String {
            return 'https://papermc.io/forums/u/' + name;
        }

        prettyDate(date: Date): String {
            // TODO format date
            return 'Oct 8, 2020';
        }
    }

    return new Util();
};

type utilType = ReturnType<typeof createUtil>;

declare module 'vue/types/vue' {
    interface Vue {
        $util: utilType;
    }
}

declare module '@nuxt/types' {
    interface NuxtAppOptions {
        $util: utilType;
    }

    interface Context {
        $util: utilType;
    }
}

declare module 'vuex/types/index' {
    interface Store<S> {
        $util: utilType;
    }
}

export default (ctx: Context, inject: Inject) => {
    inject('util', createUtil(ctx));
};
