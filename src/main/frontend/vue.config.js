const path = require("path");
const fs = require("fs");

const sourceDir = path.resolve(__dirname, "src");
const entryDir = path.resolve(sourceDir, "entrypoints");
const outputDir = path.resolve(__dirname, '..', '..', '..', 'target', 'classes', 'public', 'build');

module.exports = {
  chainWebpack: config => {
    // clear default
    config.entry("app").clear();
    config.entry("app").add(path.resolve(entryDir, "dummy.js"));

    // iterate thru entry points and add them to webpack
    for (const file of fs.readdirSync(entryDir)) {
      config.entry(file.replace(".js", "")).add(path.resolve(entryDir, file));
    }
  },

  outputDir: outputDir,
  filenameHashing: false
};
