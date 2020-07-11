const VueLoaderPlugin = require('vue-loader/lib/plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CopyPlugin = require('copy-webpack-plugin');
//const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

const Path = require('path');
const sourceDir = Path.resolve(__dirname, 'src');
const entryDir = Path.resolve(sourceDir, 'entries');
const modulesDir = Path.resolve(__dirname, 'node_modules');
const outputDir = Path.resolve(__dirname, 'build');

module.exports = {
    entry: {
        main: Path.resolve(sourceDir, 'scss', 'main.scss'),
        home: Path.resolve(entryDir, 'home.js'),
        'font-awesome': Path.resolve(entryDir, 'font-awesome.js'),
        'user-profile': Path.resolve(entryDir, 'user-profile.js'),
        'version-list': Path.resolve(entryDir, 'version-list.js')
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
        // new CopyPlugin([
        //     {
        //         from: Path.resolve(modulesDir, '@fortawesome', 'fontawesome-svg-core', 'styles.css'),
        //         to: Path.resolve(outputDir, 'font-awesome.css')
        //     }
        // ]), TODO fix copy plugin
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
