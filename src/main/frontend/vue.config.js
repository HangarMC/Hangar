const path = require('path');
const fs = require('fs');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

const sourceDir = path.resolve(__dirname, 'src');
const entryDir = path.resolve(sourceDir, 'entrypoints');
const jsDir = path.resolve(sourceDir, 'js');
const outputDir = path.resolve(__dirname, '..', '..', '..', 'target', 'classes', 'public', 'build');

module.exports = {
  chainWebpack: config => {
    // clear default
    config.entry('app').clear();
    config.entry('app').add(path.resolve(entryDir, 'dummy.js'));

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
        publicPath: '/css/'
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
  filenameHashing: false
};
