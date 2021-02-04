import { Context } from '@nuxt/types';

export default ({ store, error }: Context) => {
    if (!store.state.auth.authenticated) {
        error({
            message: 'You must be logged in to perform this action',
            statusCode: 401,
        });
    }
};
