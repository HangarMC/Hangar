import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { NamedPermission } from '~/types/enums';
import { RootState } from '~/store';

const createUtil = ({ store }: Context) => {
    class Util {
        avatarUrl(_name: String): String {
            return 'https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png';
            // TODO avatar url
            // return '/avatar/' + name + '?size=120x120';
        }

        forumUrl(name: String): String {
            return 'https://papermc.io/forums/u/' + name;
        }

        prettyDate(date: Date): String {
            return date.toLocaleDateString(undefined, {
                day: 'numeric',
                month: 'long',
                year: 'numeric',
            });
        }

        has(perms: bigint | string, namedPermission: NamedPermission): boolean {
            let _perms: bigint;
            const perm = (store.state as RootState).permissions.get(namedPermission);
            if (!perm) {
                throw new Error(namedPermission + ' is not valid');
            }
            if (typeof perms === 'string') {
                _perms = BigInt('0b' + perm.value);
            } else {
                _perms = perms;
            }

            return (_perms & perm.permission) === perm.permission;
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
    // eslint-disable-next-line no-unused-vars,@typescript-eslint/no-unused-vars
    interface Store<S> {
        $util: utilType;
    }
}

export default (ctx: Context, inject: Inject) => {
    inject('util', createUtil(ctx));
};
