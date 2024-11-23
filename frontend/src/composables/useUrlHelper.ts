const isSafeHost = (host: string) => {
  for (const safeHost of useBackendData.security.safeDownloadHosts) {
    // Make sure it's the full host or a subdomain
    if (host === safeHost || host.endsWith("." + safeHost)) {
      return true;
    }
  }
  return false;
};

const isSafe = (urlString: string) => {
  console.log("safe?", urlString, useBackendData.security.safeDownloadHosts);
  if (!urlString) {
    console.log("1");
    return false;
  }
  if (urlString.startsWith("#") || urlString.startsWith("/")) {
    console.log("2");
    return true;
  }
  try {
    const url = new URL(urlString);
    const host = url.hostname;
    if (url.protocol?.startsWith("mailto")) {
      console.log("3");
      return true;
    } else if (!host || isSafeHost(host)) {
      console.log("4");
      return true;
    }
  } catch {
    console.log("5");
    return false;
  }

  console.log("6");
  return false;
};

export const linkout = (urlString: string) => (isSafe(urlString) ? urlString : "/linkout?remoteUrl=" + encodeURIComponent(urlString));
export const proxyImage = (urlString: string) => (isSafe(urlString) ? urlString : "/api/internal/image/" + urlString);
export const sanitizeUrl = (urlString: string) => (urlString?.startsWith("javascript") ? "#invalid" : urlString);
