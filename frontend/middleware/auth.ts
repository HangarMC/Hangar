import { Context } from '@nuxt/types';

export default ({ app: { $cookies }, $auth, redirect }: Context) => {
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
        // TODO if not running hangarauth locally, this needs to just be a regular if not an else-if (idk what a good fix for that is)
    } else if ($cookies.get('HangarAuth_REFRESH', { parseJSON: false })) {
        return $auth.refreshUser();
    }
};
