const VueLoaderPlugin = require('vue-loader/lib/plugin');
const ProvidePlugin = require('webpack').ProvidePlugin;
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CopyPlugin = require('copy-webpack-plugin');
//const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

const Path = require('path');
const fs = require('fs');
const sourceDir = Path.resolve(__dirname, 'src');
const entryDir = Path.resolve(sourceDir, 'entries');
const pluginsDir = Path.resolve(sourceDir, 'plugins');
const modulesDir = Path.resolve(__dirname, 'node_modules');
const outputDir = Path.resolve(__dirname, '..', '..', '..', 'target', 'classes', 'public', 'build');
const javascriptDir = Path.resolve(__dirname, '..', '..', '..', 'target', 'classes', 'public', 'javascripts');
// const javascriptDir = Path.resolve(sourceDir, 'javascripts');
const entries = {}
for (const file of fs.readdirSync(entryDir)) {
    entries[file.replace('.js', "")] = Path.resolve(entryDir, file);
}

for (const file of fs.readdirSync(pluginsDir)) {
    entries[file.replace('.js', '')] = Path.resolve(pluginsDir, file);
}

// for (const file of fs.readdirSync(javascriptDir)) { // These can't really be entries. Cause they bork out when included in pages as scripts. Doesn't really matter. they'll be replaced with vue stuff soon enough.
//     entries[file.replace(".js", "")] = Path.resolve(javascriptDir, file);
// }
module.exports = {
    entry: {
        main: Path.resolve(sourceDir, 'scss', 'main.scss'),
        ...entries
    },
    output: {
        path: outputDir,
        filename: '[name].js',
        publicPath: '/dist/',
        libraryTarget: 'umd'
    },
    plugins: [
        new VueLoaderPlugin(),
        new MiniCssExtractPlugin(),
        new CopyPlugin({
                patterns: [
                    {
                        from: Path.resolve(modulesDir, '@fortawesome', 'fontawesome-svg-core', 'styles.css'),
                        to: Path.resolve(outputDir, 'font-awesome.css')
                    },
                    {
                        from: Path.resolve(sourceDir, "javascripts"),
                        to: javascriptDir
                    }
                ]
            }
        ),
        new ProvidePlugin({
            $: 'jquery',
            jQuery: 'jquery',
            'window.jQuery': 'jquery',
            'window.$': 'jquery'
        })
        //new BundleAnalyzerPlugin()
    ],
    module: {
        rules: [
            {
                test: /\.vue$/,
                loader: 'vue-loader'
            },
            {
                test: /\.js$/,
                loader: 'babel-loader',
                include: sourceDir,
                options: {
                    presets: ['@babel/preset-env']
                }
            },
            {
                test: /\.css$/,
                use: [MiniCssExtractPlugin.loader, 'css-loader', 'postcss-loader'],
            },
            {
                test: /\.scss$/,
                use: [MiniCssExtractPlugin.loader, 'css-loader', 'postcss-loader', 'sass-loader'],
            },
        ]
    },
    resolve: {
        extensions: ['.js', '.vue', '.css'],
        alias: {
            'vue$': 'vue/dist/vue.esm.js'
        },
        modules: [
            modulesDir
        ]
    },
    optimization: {
        splitChunks: {
            cacheGroups: {
                vendors: {
                    name: "vendors",
                    chunks: "initial",
                    test: /[\\/]node_modules[\\/]/,
                    priority: 10,
                    enforce: true
                },
                commons: {
                    name: "commons",
                    chunks: "initial",
                    minChunks: 2,
                },
            }
        },
    }
};
