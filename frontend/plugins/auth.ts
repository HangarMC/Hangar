import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { User } from 'hangar-api';

const createAuth = ({ app: { $cookies }, store, $api }: Context) => {
    class Auth {
        login(redirect: string): void {
            $cookies.set('returnRoute', redirect, {
                path: '/',
                maxAge: 120,
                sameSite: 'lax',
            });
            location.replace(`http://localhost:8080/login?returnUrl=http://localhost:3000/login`);
        }

        processLogin(): Promise<void> {
            store.commit('auth/SET_AUTHED', true);
            return $api.request<User>('users/@me').then((user) => {
                store.commit('auth/SET_USER', user);
            });
        }

        logout(): void {
            store.commit('auth/SET_USER', false);
            $api.invalidateSession();
            location.replace('http://localhost:8080/logout');
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
