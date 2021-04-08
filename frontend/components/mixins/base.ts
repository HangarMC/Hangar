import { Component, State, Vue } from 'nuxt-property-decorator';
import { HangarUser } from 'hangar-internal';
import { AuthState } from '~/store/auth';
import { RootState } from '~/store';

@Component
export class HangarComponent extends Vue {
    @State((state: AuthState) => state.user, { namespace: 'auth' })
    currentUser!: HangarUser | null;

    @State((state: RootState) => state.validations)
    validations!: RootState['validations'];

    @State((state: AuthState) => state.authenticated, { namespace: 'auth' })
    isLoggedIn!: boolean;
}

export class Authed extends HangarComponent {
    @State((state: AuthState) => state.user, { namespace: 'auth' })
    currentUser!: HangarUser;
}
