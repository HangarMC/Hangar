module.exports = {
    plugins: {
        autoprefixer: true,
        require('cssnano')({
            preset: 'default',
        }),
    },
};
