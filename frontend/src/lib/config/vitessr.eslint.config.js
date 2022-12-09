module.exports = {
  extends: ["./base.eslint.config.js"],
  settings: {
    "import/core-modules": ["windi.css", "virtual:generated-layouts", "virtual:generated-pages"],
  },
};
