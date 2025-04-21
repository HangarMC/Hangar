module.exports = {
  hooks: {
    readPackage(pkg) {
      if (pkg?.dependencies?.["@netlify/functions"]) {
        delete pkg.dependencies["@netlify/functions"];
      }
      return pkg;
    },
  },
};
