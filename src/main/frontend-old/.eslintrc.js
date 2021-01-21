module.exports = {
    root: true,
    env: {
        node: true,
    },
    extends: ['plugin:vue/vue3-essential', 'eslint:recommended', '@vue/prettier'],
    parserOptions: {
        parser: 'babel-eslint',
    },
    rules: {
        'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'no-unused-vars': 'warn',
        'vue/no-unused-vars': process.env.NODE_ENV === 'production' ? 'error' : 'warn',
        'vue/no-unused-components': process.env.NODE_ENV === 'production' ? 'error' : 'warn',
        'vue/no-mutating-props': 'off',
    },
};
