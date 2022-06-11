const proxyHost = process.env.proxyHost || "http://localhost:8080";
const authHost = process.env.authHost || "http://localhost:3001";
const oauthHost = process.env.oauthHost || "http://localhost:4444";

exports["default"] = {
  // backend
  "/api/": proxyHost,
  "/signup": proxyHost,
  "/login": proxyHost,
  "/logout": proxyHost,
  "/handle-logout": proxyHost,
  "/refresh": proxyHost,
  "/invalidate": proxyHost,
  "/v2/api-docs/": proxyHost,
  "/robots.txt": proxyHost,
  "/sitemap.xml": proxyHost,
  "/global-sitemap.xml": proxyHost,
  "/*/sitemap.xml": proxyHost,
  "/statusz": proxyHost,
  // auth
  "/avatar": authHost,
  "/oauth/logout": authHost,
  "/oauth2": oauthHost,
};
