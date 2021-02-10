import { Context } from '@nuxt/types';

export default async ({ app: { $cookies }, $auth, $api, store, redirect }: Context) => {
    if ($cookies.get('returnRoute')) {
        // is returning from login
        const returnRoute = $cookies.get<string>('returnRoute');
        $cookies.remove('returnRoute', {
            path: '/',
        });
        $cookies.remove('url', {
            path: '/',
        });
        redirect(returnRoute);
    } else if ($cookies.get('HangarAuth_REFRESH', { parseJSON: false })) {
        const token = await $api.getToken(true);
        if (token != null) {
            if (store.state.auth.authenticated) {
                return $auth.updateUser(token);
            } else {
                return $auth.processLogin(token);
            }
        } else {
            console.log('LOGGING OUT VIA MIDDLEWARE');
            return $auth.logout(process.client);
        }
    }
};
