const path = require('path');

module.exports = {
  publicPath: '/',
  outputDir: path.resolve('src', 'main', 'resources', 'public', 'app'),
  assetsDir: '',
  devServer: {
      contentBase: path.resolve(__dirname, 'src', 'main', 'resources', 'assets'),
      compress: true,
      port: 9000
  },
  pages: {
    index: {
      // entry for the page
      entry: path.resolve('src', 'main','javascript', 'main.js'),
      // the source template
      template: path.resolve('src', 'main','resources', 'index.html'),
      // output as dist/index.html
      filename: 'index.html',
      // when using title option,
      // template title tag needs to be <title><%= htmlWebpackPlugin.options.title %></title>
      title: 'Kapenta',
      // chunks to include on this page, by default includes
      // extracted common chunks and vendor chunks.
      chunks: ['chunk-vendors', 'chunk-common', 'index']
    }
  }
}
