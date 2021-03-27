import { Component, Prop } from 'nuxt-property-decorator';
import { PaginatedResult, User } from 'hangar-api';
import { Organization } from 'hangar-internal';
import { PropType } from 'vue';
import { DataOptions } from 'vuetify';
import { HangarComponent } from './base';

@Component
export class UserPage extends HangarComponent {
    user!: User;
    organization!: Organization | null;

    get isCurrentUser() {
        return this.currentUser && this.currentUser.name === this.user.name;
    }
}

@Component
export class UserPropPage extends UserPage {
    @Prop({ type: Object as PropType<User>, required: true })
    user!: User;

    @Prop({ type: Object as PropType<Organization> })
    organization!: Organization | null;
}

@Component
export class UserListPage extends HangarComponent {
    loading = false;
    options = { page: 1, itemsPerPage: 25 } as DataOptions;
    users!: PaginatedResult<User>;

    mounted() {
        this.$watch('options', this.onOptionsChanged, { deep: true });
    }

    get url(): string {
        throw new Error('Must be overridden');
    }

    onOptionsChanged() {
        this.loading = true;
        this.$api
            .request<PaginatedResult<User>>(this.url, false, 'get', this.requestOptions)
            .then((users) => {
                this.users = users;
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading = false;
            });
    }

    get requestOptions() {
        const sort: string[] = [];
        for (let i = 0; i < this.options.sortBy.length; i++) {
            let sortStr = this.options.sortBy[i];
            if (this.options.sortDesc[i]) {
                sortStr = '-' + sortStr;
            }
            sort.push(sortStr);
        }
        return {
            limit: this.options.itemsPerPage,
            offset: (this.options.page - 1) * this.options.itemsPerPage,
            sort,
        };
    }
}
