import { Context } from '@nuxt/types';

export default async ({ store, app: { $cookies }, $auth, $api, redirect, route }: Context) => {
    if ($cookies.get('returnRoute') && route.query.token) {
        // is returning from login
        console.log('returning from login');
        const returnRoute = $cookies.get<string>('returnRoute');
        store.commit('auth/SET_TOKEN', route.query.token);
        return $auth.processLogin(<string>route.query.token).then(() => {
            $cookies.remove('returnRoute', {
                path: '/',
            });
            console.log('before redirect');
            redirect(returnRoute);
        });
    } else {
        const token = await $api.refreshToken();
        if (token != null) {
            return $auth.processLogin(token);
        }
        // const session = $cookies.get<ApiSession>('api_session');
        // if (!session || session.type !== ApiSessionType.USER) return;
        // if (!store.state.auth.authenticated) {
        //     return $auth.processLogin(store.state.auth.token);
        // } else {
        //     return $auth.updateUser(store.state.auth.token);
        // }
    }
};
