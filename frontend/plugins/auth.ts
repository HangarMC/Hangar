import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { User } from 'hangar-api';

const createAuth = ({ app: { $cookies }, $axios, store, $api }: Context) => {
    class Auth {
        login(redirect: string): void {
            $cookies.set('returnRoute', redirect, {
                path: '/',
                maxAge: 120,
                secure: process.env.NODE_ENV === 'production',
            });
            location.replace(`/login?returnUrl=http://localhost:3000${redirect}`);
        }

        processLogin(token: string): Promise<void> {
            store.commit('auth/SET_AUTHED', true);
            return this.updateUser(token);
        }

        logout(reload = true): void {
            store.commit('auth/SET_USER', null);
            store.commit('auth/SET_TOKEN', null);
            store.commit('auth/SET_AUTHED', false);
            $axios.get('/invalidate');
            $cookies.remove('HangarAuth_REFRESH', {
                path: '/',
            });
            if (reload) {
                location.reload();
            }
        }

        updateUser(token: string): Promise<void> {
            return $api
                .requestInternalWithToken<User>('users/@me', token)
                .then((user) => {
                    store.commit('auth/SET_USER', user);
                })
                .catch((err) => {
                    console.log(err);
                    this.logout(process.client);
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
    // eslint-disable-next-line no-unused-vars,@typescript-eslint/no-unused-vars
    interface Store<S> {
        $auth: authType;
    }
}

export default (ctx: Context, inject: Inject) => {
    inject('auth', createAuth(ctx));
};
