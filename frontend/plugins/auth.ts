import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { User } from 'hangar-api';
import { AuthState } from '~/store/auth';

const createAuth = ({ app: { $cookies }, $axios, store, $api, redirect }: Context) => {
    class Auth {
        login(redirect: string): void {
            location.replace(`/login?returnUrl=${process.env.publicHost}${redirect}`);
        }

        processLogin(token: string): Promise<void> {
            store.commit('auth/SET_AUTHED', true);
            return this.updateUser(token);
        }

        async logout() {
            const token = await $api.getToken(true);
            location.replace(`/logout?returnUrl=${process.env.publicHost}/logged-out&t=${token}`);
        }

        async invalidate(shouldRedirect = true) {
            store.commit('auth/SET_USER', null);
            store.commit('auth/SET_TOKEN', null);
            store.commit('auth/SET_AUTHED', false);
            await $axios.get('/invalidate').catch(() => console.log('invalidate failed'));
            $cookies.remove('HangarAuth_REFRESH', {
                path: '/',
            });
            if (shouldRedirect) {
                redirect('/logged-out');
            }
        }

        updateUser(token: string): Promise<void> {
            return $api
                .requestInternalWithToken<User>('users/@me', token)
                .then((user) => {
                    if (process.server) {
                        store.commit('auth/SET_USER', user);
                    } else if (process.env.NODE_ENV === 'production') {
                        store.commit('auth/SET_USER', user);
                    } else {
                        // TODO hella hacky fix but it fixes the issue reproducible here https://codesandbox.io/s/nuxt-dev-reload-issue-mk16j (also, this only plagues dev env so I guess it doesn't matter?)
                        setTimeout(() => {
                            store.commit('auth/SET_USER', user);
                        }, 1000);
                    }
                    return Promise.resolve();
                })
                .catch((err) => {
                    console.log(err);
                    console.log('LOGGING OUT ON updateUser');
                    return this.invalidate(process.client);
                });
        }

        refreshUser(): Promise<void> {
            return $api.getToken(true).then((token) => {
                if (token) {
                    if (store.state.auth.authenticated) {
                        return this.updateUser(token);
                    } else {
                        return this.processLogin(token);
                    }
                } else {
                    return this.invalidate(process.client);
                }
            });
        }

        isLoggedIn(): boolean {
            return (store.state.auth as AuthState).authenticated;
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
