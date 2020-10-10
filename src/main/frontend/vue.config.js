const path = require('path');
const fs = require('fs');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const StatsPlugin = require('stats-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');

const sourceDir = path.resolve(__dirname, 'src');
const entryDir = path.resolve(sourceDir, 'entrypoints');
const jsDir = path.resolve(sourceDir, 'js');
const outputDir = path.resolve(__dirname, '..', '..', '..', 'target', 'classes', 'public');

module.exports = {
    pluginOptions: {
        webpackBundleAnalyzer: {
            openAnalyzer: false,
            // analyzerMode: "json"
        },
    },
    configureWebpack: {
        plugins: [new StatsPlugin('stats.json')],
        optimization: {
            minimize: true,
            minimizer: [
                new TerserPlugin({
                    extractComments: true,
                    cache: true,
                    parallel: true,
                    sourceMap: true, // Must be set to true if using source-maps in production
                    terserOptions: {
                        // https://github.com/webpack-contrib/terser-webpack-plugin#terseroptions
                        extractComments: 'all',
                        compress: {
                            drop_console: true,
                            pure_funcs: ['console.log', 'console.info', 'console.debug', 'console.warn'],
                        },
                    },
                }),
            ],
            splitChunks: {
                cacheGroups: {
                    // TODO remove vendor chunk, make everything depend on main chunk, stuff like chart.js should only be used in its own file
                    // vendors: {
                    //     name: 'chunk-vendors',
                    //     test: /disabledCauseWeDontNeedItAndIDontKnowHowElseICanDisableThis/,
                    // },
                    common: {
                        name: 'chunk-common',
                        minChunks: 2,
                        priority: -10,
                        chunks: 'initial',
                        reuseExistingChunk: true,
                    },
                },
            },
        },
    },
    devServer: {
        port: 8081,
    },
    chainWebpack: (config) => {
        // clear default
        config.entryPoints.delete('app');

        config.entry('main').add(path.resolve(sourceDir, 'scss', 'main.scss'));
        // iterate thru entry points and add them to webpack
        for (const file of fs.readdirSync(entryDir)) {
            config.entry(file.replace('.js', '')).add(path.resolve(entryDir, file));
        }

        for (const file of fs.readdirSync(jsDir)) {
            config.entry(file.replace('.js', '')).add(path.resolve(jsDir, file));
        }

        config.module.rules.delete('css');
        config.module.rules.delete('scss');

        config.plugin('mini-css-extract').use(MiniCssExtractPlugin, [{ filename: 'css/[name].css' }]);

        config.module
            .rule('css')
            .test(/\.(s?)css$/i)
            .use('mini-css-extract')
            .loader(MiniCssExtractPlugin.loader)
            .options({
                hmr: process.env.NODE_ENV === 'development',
                reloadAll: true,
                publicPath: '/css/',
            })
            .end()
            .use('css-loader')
            .loader('css-loader')
            .end()
            .use('postcss-loader')
            .loader('postcss-loader')
            .end()
            .use('sass-loader')
            .loader('sass-loader')
            .end();
    },

    outputDir: outputDir,
    filenameHashing: false,
};
