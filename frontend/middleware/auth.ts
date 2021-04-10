import { Context } from '@nuxt/types';

export default ({ app: { $cookies }, $auth, redirect }: Context) => {
    let shouldRefresh = $cookies.get('HangarAuth_REFRESH', { parseJSON: false });
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
        // only refresh when fake user is enabled
        shouldRefresh = process.env.fakeUser || false;
    }

    if (shouldRefresh) {
        return $auth.refreshUser();
    }
};
