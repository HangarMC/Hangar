module.exports = {
  root: true,
  env: {
    node: true,
    browser: true,
    "vue/setup-compiler-macros": true,
  },
  parser: "vue-eslint-parser",
  parserOptions: {
    parser: "@typescript-eslint/parser",
    project: "./tsconfig.json",
  },
  extends: [
    "@nuxtjs/eslint-config-typescript",
    "plugin:vue/vue3-recommended",
    "eslint:recommended",
    "plugin:import/recommended",
    "plugin:import/typescript",
    "@vue/typescript/recommended",
    "plugin:eslint-comments/recommended",
    "prettier",
  ],
  plugins: ["unicorn"],
  settings: {
    "import/resolver": {
      node: {
        extensions: [".js", ".ts", ".d.ts"],
      },
      typescript: true,
      alias: {
        map: [["~", "./src/"]],
        extensions: [".js", ".ts", ".d.ts", ".vue"],
      },
    },
    "import/core-modules": ["virtual:windi.css"],
  },
  rules: {
    // temp disable
    // TODO reenable
    "vue/no-setup-props-destructure": "off",

    "eol-last": ["error", "always"],

    "vue/multi-word-component-names": "off",

    // TS
    "@typescript-eslint/no-empty-interface": "off",
    "@typescript-eslint/no-explicit-any": "off",
    "@typescript-eslint/no-unused-vars": "off",
    "no-unused-vars": "off",
    "@typescript-eslint/consistent-type-exports": "error",
    "@typescript-eslint/consistent-type-imports": "error",

    // unicorn
    "unicorn/better-regex": "error",
    "unicorn/empty-brace-spaces": "error",
    "unicorn/escape-case": "error",
    "unicorn/new-for-builtins": "error",
    "unicorn/no-array-for-each": "error",
    "unicorn/no-array-push-push": "error",
    "unicorn/no-console-spaces": "error",
    "unicorn/no-instanceof-array": "error",
    "unicorn/no-new-buffer": "error",
    "unicorn/no-unsafe-regex": "error",
    "unicorn/no-useless-promise-resolve-reject": "error",
    "unicorn/prefer-array-find": "error",
    "unicorn/prefer-array-flat": "error",
    "unicorn/prefer-array-flat-map": "error",
    "unicorn/prefer-array-index-of": "error",
    "unicorn/prefer-array-some": "error",
    "unicorn/prefer-at": "error",
    "unicorn/prefer-date-now": "error",
    "unicorn/prefer-default-parameters": "error",
    "unicorn/prefer-dom-node-append": "error",
    "unicorn/prefer-export-from": "error",
    "unicorn/prefer-includes": "error",
    "unicorn/prefer-modern-dom-apis": "error",
    "unicorn/prefer-set-has": "error",
    "unicorn/prefer-spread": "error",
    "unicorn/prefer-string-replace-all": "error",
    "unicorn/prefer-switch": "error",
    "unicorn/prefer-ternary": "error",
    "unicorn/relative-url-style": "error",
    "unicorn/throw-new-error": "error",
    "import/no-unresolved": [
      "error",
      {
        ignore: ["^~icons/mdi/.*", ".*\\.svg$", "hangar-internal", "hangar-api"],
      },
    ],
  },
  overrides: [
    {
      files: ".eslintrc.js",
      extends: ["plugin:@typescript-eslint/disable-type-checked"],
      rules: {
        "unicorn/prefer-module": "off",
      },
    },
    {
      files: ["*.html"],
      rules: {
        "vue/comment-directive": "off",
      },
    },
    {
      files: "src/module/backendData.ts",
      rules: {
        "import/namespace": "off",
      },
    },
  ],
};
