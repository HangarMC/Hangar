import withNuxt from "./.nuxt/eslint.config.mjs";
import oxlint from "eslint-plugin-oxlint";
import comments from "@eslint-community/eslint-plugin-eslint-comments/configs";
import unicorn from "eslint-plugin-unicorn";

export default withNuxt()
  .prepend({
    ignores: ["shared/types/backend/**", "app/types/generated/**"],
  })
  .append(comments.recommended, unicorn.configs["flat/recommended"], oxlint.configs["flat/recommended"])
  .append({
    name: "hangar",
    rules: {
      "import/namespace": "off", // its slow as fuck

      "vue/multi-word-component-names": "off",
      "vue/html-self-closing": "off",
      "no-console": "off",
      // TS
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/no-unused-vars": "off",
      "@typescript-eslint/unified-signatures": "off",
      "@typescript-eslint/no-dynamic-delete": "off",
      "no-unused-vars": "off",
      // "@typescript-eslint/consistent-type-exports": "error",
      // "@typescript-eslint/consistent-type-imports": "error",
      // unicorn
      "unicorn/filename-case": "off",
      "unicorn/prevent-abbreviations": "off",
      "unicorn/catch-error-name": [
        "error",
        {
          name: "err",
        },
      ],
      "unicorn/switch-case-braces": ["error", "avoid"],
      "unicorn/prefer-query-selector": "off",
      "unicorn/prefer-global-this": "off",
      "unicorn/name-replacements": "off",
      "unicorn/consistent-boolean-name": "off",
      "unicorn/no-top-level-assignment-in-function": "off",
      "unicorn/max-nested-calls": "off",
      "unicorn/no-unsafe-string-replacement": "off",
      "unicorn/consistent-class-member-order": "off",
      // we want this but this is wayy too much work rn
      "unicorn/prefer-await": "off",
      "unicorn/no-computed-property-existence-check": "off",
      "unicorn/prefer-simple-condition-first": "off",
      // toSorted is ES2023 and isn't polyfilled by the build, so it hard-crashes older browsers
      "unicorn/no-array-sort": "off",
    },
  });
