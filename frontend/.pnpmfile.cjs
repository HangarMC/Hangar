module.exports = {
  hooks: {
    readPackage(pkg) {
      // unused and brings deprecated transitive dependencies
      if (pkg?.dependencies?.["@netlify/functions"]) {
        delete pkg.dependencies["@netlify/functions"];
      }
      return pkg;
    },
  },
};
