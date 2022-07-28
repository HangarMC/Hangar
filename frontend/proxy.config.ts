const backendHost = process.env.BACKEND_HOST || "http://localhost:8080";
const authHost = process.env.AUTH_HOST || "http://localhost:3001";

exports["default"] = {
  // backend
  "/api/": backendHost,
  "/signup": backendHost,
  "/login": backendHost,
  "/logout": backendHost,
  "/handle-logout": backendHost,
  "/refresh": backendHost,
  "/invalidate": backendHost,
  "/v2/api-docs/": backendHost,
  "/robots.txt": backendHost,
  "/sitemap.xml": backendHost,
  "/global-sitemap.xml": backendHost,
  "/*/sitemap.xml": backendHost,
  "/statusz": backendHost,
  // auth
  "/avatar": authHost,
  "/oauth/logout": authHost,
};
