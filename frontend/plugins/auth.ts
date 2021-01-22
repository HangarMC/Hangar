import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { User } from 'hangar-api';

const createAuth = ({ app: { $cookies }, store, $api }: Context) => {
    class Auth {
        login(redirect: string): void {
            $cookies.set('returnRoute', redirect, {
                path: '/',
                maxAge: 120,
                secure: true,
            });
            location.replace(`/login?returnUrl=http://localhost:3000/${redirect}`);
        }

        processLogin(): Promise<void> {
            store.commit('auth/SET_AUTHED', true);
            return this.updateUser();
        }

        logout(): void {
            $api.invalidateSession();
            // location.replace('/logout'); // TODO uncomment
        }

        updateUser(): Promise<void> {
            return $api.requestInternal<User>('users/@me').then((user) => {
                store.commit('auth/SET_USER', user);
            });
        }
    }

    return new Auth();
};

type authType = ReturnType<typeof createAuth>;

declare module 'vue/types/vue' {
    interface Vue {
        $auth: authType;
    }
}

declare module '@nuxt/types' {
    interface NuxtAppOptions {
        $auth: authType;
    }

    interface Context {
        $auth: authType;
    }
}

declare module 'vuex/types/index' {
    interface Store<S> {
        $auth: authType;
    }
}

export default (ctx: Context, inject: Inject) => {
    inject('auth', createAuth(ctx));
};
