import { ActionTree, GetterTree, MutationTree } from 'vuex';
import { Context } from '@nuxt/types';
import { IProjectCategory } from 'hangar-internal';
import { IPermission } from 'hangar-api';
import { NamedPermission, ProjectCategory } from '~/types/enums';

export interface RootState {
    projectCategories: Map<ProjectCategory, IProjectCategory>;
    permissions: Map<NamedPermission, IPermission>;
}

export const state: () => RootState = () => ({
    projectCategories: (null as unknown) as Map<ProjectCategory, IProjectCategory>,
    permissions: (null as unknown) as Map<NamedPermission, IPermission>,
});

export const mutations: MutationTree<RootState> = {
    SET_PROJECT_CATEGORIES: (state, payload: Map<ProjectCategory, IProjectCategory>) => {
        state.projectCategories = payload;
    },
    SET_PERMISSIONS: (state, payload: Map<NamedPermission, IPermission>) => {
        state.permissions = payload;
    },
};

export const actions: ActionTree<RootState, RootState> = {
    async nuxtServerInit({ commit }, { $api }: Context) {
        try {
            const categoryResult = await $api.requestInternal<IProjectCategory[]>('data/categories', false);
            commit(
                'SET_PROJECT_CATEGORIES',
                convertToMap<ProjectCategory, IProjectCategory>(categoryResult, (value) => value.apiName)
            );
            const permissionResultTemp = await $api.requestInternal<{ value: NamedPermission; frontendName: string; permission: string }[]>(
                'data/permissions',
                false
            );
            const permissionResult: IPermission[] = permissionResultTemp.map(({ value, frontendName, permission }) => ({
                value,
                frontendName,
                permission: BigInt('0b' + permission),
            }));
            commit(
                'SET_PERMISSIONS',
                convertToMap<NamedPermission, IPermission>(permissionResult, (value) => value.value)
            );
            // others
        } catch (e) {
            console.error('ERROR FETCHING BACKEND DATA');
            console.error(e);
        }
    },
};

export const getters: GetterTree<RootState, RootState> = {
    visibleCategories: (state: RootState) => Array.from(state.projectCategories.values()).filter((value) => value.visible),
};

function convertToMap<E, T>(values: T[], toStringFunc: (value: T) => string): Map<E, T> {
    const map = new Map<E, T>();
    for (const value of values) {
        const key: E = (toStringFunc(value) as unknown) as E;
        if (key == null) {
            throw new Error('Could not find an enum for ' + value);
        }
        map.set(key, value);
    }
    return map;
}
