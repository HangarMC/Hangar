import { useBackendData } from "~/store/backendData";

export function forumUrl(topicId: number) {
  return `https://forums.papermc.io/threads/` + topicId;
}

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
  if (urlString.startsWith("#") || urlString.startsWith("/")) {
    return true;
  }
  try {
    const url = new URL(urlString);
    const host = url.hostname;
    if (url.protocol?.startsWith("mailto")) {
      return true;
    } else if (!host || isSafeHost(host)) {
      return true;
    }
  } catch {}

  return false;
};

export const linkout = (urlString: string) => (isSafe(urlString) ? urlString : "/linkout?remoteUrl=" + encodeURIComponent(urlString));
export const proxyImage = (urlString: string) => (isSafe(urlString) ? urlString : "/api/internal/image/" + urlString);
export const sanitizeUrl = (urlString: string) => (urlString?.startsWith("javascript") ? "#invalid" : urlString);
