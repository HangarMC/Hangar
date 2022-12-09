module.exports = {
  extends: ["@nuxtjs/eslint-config-typescript", "./base.eslint.config.js"],
  settings: {
    "import/core-modules": ["virtual:windi.css"],
  },
};
