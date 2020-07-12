const merge = require("webpack-merge");
const commonConfig = require("./webpack.config.common.js");

module.exports = merge.merge(commonConfig, {
    mode: "development",
    watch: true
});
