import { Context } from '@nuxt/types';
import { UserPermissions } from 'hangar-api';

export default ({ store, route, $api, $util }: Context) => {
    if (route.params.author && route.params.slug) {
        return $api
            .request<UserPermissions>('permissions', true, 'get', {
                author: route.params.author,
                slug: route.params.slug,
            })
            .then((userPermissions) => {
                store.commit('auth/SET_ROUTE_PERMISSIONS', userPermissions.permissionBinString);
            })
            .catch($util.handleAxiosError);
    }
    // TODO other route permissions
};
