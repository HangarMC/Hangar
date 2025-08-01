import type { Platform, PlatformData, PlatformVersion } from "#shared/types/backend";

export function useGlobalData() {
  const { data: globalData } = useDataLoader("globalData");

  return globalData;
}

export function usePlatformData(platform: Platform): PlatformData | undefined {
  return useGlobalData().value?.platforms?.find((p) => p.enumName === platform);
}

export function usePlatformName(platform: Platform | string): string | undefined {
  return usePlatformData(platform as Platform)?.name;
}

export function usePlatformVersions(platform: Platform): PlatformVersion[] {
  const platformData = usePlatformData(platform);
  if (!platformData) {
    return [];
  }

  return platformData.platformVersions;
}

export const useVisiblePlatforms = computed(() =>
  useGlobalData().value?.platforms ? [...useGlobalData().value!.platforms].filter((value) => value.visible) : []
);
