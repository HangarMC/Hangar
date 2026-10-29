import type { FileInfo, JarScanResult, Platform, Version } from "#shared/types/backend";

export interface VersionArtifact {
  key: string;
  platforms: Platform[];
  externalUrl?: string;
  downloadUrl?: string;
  fileInfo?: FileInfo;
  scan?: JarScanResult;
}

/**
One upload can cover several platforms, so platforms sharing a jar (or an external url) collapse into a single artifact.
*/
export function versionArtifacts(version: Version | undefined, versionPlatforms: Set<Platform>, scans?: JarScanResult[]): VersionArtifact[] {
  const artifacts = new Map<string, VersionArtifact>();
  for (const platform of versionPlatforms) {
    const download = version?.downloads?.[platform];
    const key = download?.fileInfo?.sha256Hash ?? download?.externalUrl ?? platform;
    const artifact = artifacts.get(key);
    if (artifact) {
      artifact.platforms.push(platform);
      continue;
    }

    artifacts.set(key, { key, platforms: [platform], externalUrl: download?.externalUrl, downloadUrl: download?.downloadUrl, fileInfo: download?.fileInfo });
  }

  for (const artifact of artifacts.values()) {
    artifact.scan = scans?.find((scan) => artifact.platforms.includes(scan.platform));
  }
  return artifacts.values().toArray();
}
