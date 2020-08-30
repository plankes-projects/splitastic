module.exports = {
  pwa: {},
  css: {
    loaderOptions: {
      sass: {
        prependData: `@import "@/styles/scss/_variables.scss";`,
      },
    },
  },
  chainWebpack: (config) => {
    config.plugin("html").tap((args) => {
      args[0].title = "Splitastic";
      return args;
    });
  },
};
