export default {
  site: "hangar.papermc.io",
  scanner: {
    sitemap: false,
    exclude: ["/admin/*"],
    device: "desktop",
  },
  discovery: {
    pagesDir: "./src/pages",
  },
  server: {
    showURL: true,
    open: false,
  },
  puppeteerClusterOptions: {
    maxConcurrency: 2,
  },
};
