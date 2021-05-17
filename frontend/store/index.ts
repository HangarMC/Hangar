import { ActionTree, GetterTree, MutationTree } from 'vuex';
import { Context } from '@nuxt/types';
import { IPlatform, IProjectCategory, IPrompt } from 'hangar-internal';
import { IPermission } from 'hangar-api';
import { NamedPermission, Platform, ProjectCategory, Prompt } from '~/types/enums';

interface Validation {
    regex?: string;
    max?: number;
    min?: number;
}

export interface RootState {
    projectCategories: Map<ProjectCategory, IProjectCategory>;
    permissions: Map<NamedPermission, IPermission>;
    platforms: Map<Platform, IPlatform>;
    validations: {
        project: {
            name: Validation;
            desc: Validation;
            keywords: Validation;
            channels: Validation;
            pageName: Validation;
            pageContent: Validation;
            maxPageCount: number;
            maxChannelCount: number;
        };
        org: Validation;
        userTagline: Validation;
        version: Validation;
        maxOrgCount: number;
        urlRegex: string;
    };
    prompts: Map<Prompt, IPrompt>;
}

export const state: () => RootState = () => ({
    projectCategories: null as unknown as Map<ProjectCategory, IProjectCategory>,
    permissions: null as unknown as Map<NamedPermission, IPermission>,
    platforms: null as unknown as Map<Platform, IPlatform>,
    validations: null as unknown as RootState['validations'],
    prompts: null as unknown as RootState['prompts'],
});

export const mutations: MutationTree<RootState> = {
    SET_PROJECT_CATEGORIES: (state, payload: Map<ProjectCategory, IProjectCategory>) => {
        state.projectCategories = payload;
    },
    SET_PERMISSIONS: (state, payload: Map<NamedPermission, IPermission>) => {
        state.permissions = payload;
    },
    SET_PLATFORMS: (state, payload: Map<Platform, IPlatform>) => {
        state.platforms = payload;
    },
    SET_VALIDATIONS: (state, payload: RootState['validations']) => {
        state.validations = payload;
    },
    SET_PROMPTS: (state, payload: RootState['prompts']) => {
        state.prompts = payload;
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
            const platformResult: IPlatform[] = await $api.requestInternal<IPlatform[]>('data/platforms', false);
            commit(
                'SET_PLATFORMS',
                convertToMap<Platform, IPlatform>(platformResult, (value) => value.name.toUpperCase())
            );
            commit('SET_VALIDATIONS', await $api.requestInternal('data/validations', false));
            const promptsResult: IPrompt[] = await $api.requestInternal<IPrompt[]>('data/prompts', false);
            commit(
                'SET_PROMPTS',
                convertToMap<Prompt, IPrompt>(promptsResult, (value) => value.name)
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
        const key: E = toStringFunc(value) as unknown as E;
        if (key == null) {
            throw new Error('Could not find an enum for ' + value);
        }
        map.set(key, value);
    }
    return map;
}
