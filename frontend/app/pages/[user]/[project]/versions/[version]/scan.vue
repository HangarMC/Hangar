<script lang="ts" setup>
import type { Platform, HangarProject, Version, JarScanEntry } from "#shared/types/backend";
import type { VersionArtifact } from "~/composables/useVersionArtifacts";
import { useJarScans } from "~/composables/useData";

definePageMeta({
  globalPermsRequired: ["Reviewer"],
});

const props = defineProps<{
  version?: Version;
  project?: HangarProject;
  versionPlatforms: Set<Platform>;
}>();

const route = useRoute("user-project-versions-version-scan");
const t = useI18n().t;

const { jarScans, refreshJarScans } = useJarScans(() => props.version?.id as unknown as string);

const SEVERITIES = ["HIGHEST", "HIGH", "MEDIUM", "LOW", "LOWEST", "UNKNOWN"] as const;

const severityFilter = ref<string>("ALL");
const search = ref<string>("");
const scanning = reactive<Record<string, boolean>>({});
const expanded = ref<Record<string, boolean>>({});

interface Occurrence {
  location: string;
  count: number;
  owner: string;
  member: string;
  href?: string;
}

interface Finding {
  key: string;
  severity: string;
  checkName?: string;
  message: string;
  total: number;
  occurrences: Occurrence[];
}

const sections = computed(() =>
  versionArtifacts(props.version, props.versionPlatforms, jarScans.value).map((artifact) => {
    const findings = artifact.scan ? groupEntries(artifact.scan.entries, artifact) : [];
    return {
      ...artifact,
      findings,
      total: findings.reduce((sum, finding) => sum + finding.total, 0),
      visible: filterFindings(findings),
    };
  })
);

const scannableSections = computed(() => sections.value.filter((s) => !s.externalUrl && s.fileInfo));
const anyScanning = computed<boolean>(() => Object.values(scanning).some(Boolean));
const scannedSections = computed(() => sections.value.filter((s) => s.scan));

const severityCounts = computed<Record<string, number>>(() => {
  const counts: Record<string, number> = {};
  for (const section of sections.value) {
    for (const finding of section.findings) {
      counts[finding.severity] = (counts[finding.severity] ?? 0) + finding.total;
    }
  }
  return counts;
});

const totalFindings = computed<number>(() => Object.values(severityCounts.value).reduce((sum, count) => sum + count, 0));

const highestSeverity = computed<string | undefined>(() =>
  SEVERITIES.find((severity) => scannedSections.value.some((s) => s.scan?.highestSeverity === severity))
);

const lastScannedAt = computed<string | undefined>(
  () => scannedSections.value.map((s) => s.scan!.createdAt).sort((a, b) => new Date(b).getTime() - new Date(a).getTime())[0]
);

const severityOptions = computed(() => [
  { value: "ALL", label: t("scan.filter.all"), count: totalFindings.value },
  ...SEVERITIES.filter((severity) => severityCounts.value[severity]).map((severity) => ({
    value: severity as string,
    label: t(`scan.severity.${severity}`),
    count: severityCounts.value[severity],
  })),
]);

function groupEntries(entries: JarScanEntry[], artifact: VersionArtifact): Finding[] {
  const grouped = new Map<string, { severity: string; checkName?: string; message: string; locations: Map<string, number> }>();
  for (const entry of entries) {
    const key = `${entry.severity} ${entry.checkName ?? ""} ${entry.message}`;
    let finding = grouped.get(key);
    if (!finding) {
      finding = { severity: entry.severity, checkName: entry.checkName, message: entry.message, locations: new Map() };
      grouped.set(key, finding);
    }
    finding.locations.set(entry.location, (finding.locations.get(entry.location) ?? 0) + 1);
  }

  return [...grouped]
    .map(([key, finding]) => {
      const occurrences = finding.locations
        .entries()
        .map(([location, count]) => ({ location, count, ...parseLocation(location, artifact) }))
        .toArray();
      return {
        key,
        severity: finding.severity,
        checkName: finding.checkName,
        message: finding.message,
        total: occurrences.reduce((sum, occurrence) => sum + occurrence.count, 0),
        occurrences,
      };
    })
    .sort((a, b) => severityRank(a.severity) - severityRank(b.severity) || b.total - a.total || a.message.localeCompare(b.message));
}

function filterFindings(findings: Finding[]): Finding[] {
  const query = search.value.trim().toLowerCase();
  return findings.filter((finding) => {
    if (severityFilter.value !== "ALL" && finding.severity !== severityFilter.value) return false;
    if (!query) return true;
    return (
      finding.message.toLowerCase().includes(query) ||
      (finding.checkName?.toLowerCase().includes(query) ?? false) ||
      finding.occurrences.some((o) => o.location.toLowerCase().includes(query))
    );
  });
}

function severityRank(severity: string): number {
  const index = SEVERITIES.indexOf(severity as (typeof SEVERITIES)[number]);
  return index === -1 ? SEVERITIES.length : index;
}

function severityTone(severity?: string): "neutral" | "amber" | "green" | "red" {
  switch (severity) {
    case "HIGHEST":
    case "HIGH":
      return "red";
    case "MEDIUM":
      return "amber";
    case "LOW":
    case "LOWEST":
      return "green";
    default:
      return "neutral";
  }
}

function severityPuckClasses(severity: string): string {
  switch (severityTone(severity)) {
    case "red":
      return "bg-red-500/15 text-red-500";
    case "amber":
      return "bg-amber-500/15 text-amber-500";
    case "green":
      return "bg-lime-500/15 text-lime-500";
    default:
      return "background-card text-gray-secondary";
  }
}

function severityLabel(severity?: string): string {
  return t(`scan.severity.${severity && SEVERITIES.includes(severity as (typeof SEVERITIES)[number]) ? severity : "UNKNOWN"}`);
}

function parseLocation(location: string, artifact: VersionArtifact): { owner: string; member: string; href?: string } {
  const separator = location.lastIndexOf(" @ ");
  const owner = separator === -1 ? location : location.slice(separator + 3);
  const member = separator === -1 ? "" : location.slice(0, separator);
  // method checks report the jvm internal name, the scanner's own checks report the jar entry as-is
  const named = /\.[a-z0-9]+$/i.test(owner);
  return { owner: named ? owner : owner.replaceAll("/", "."), member, href: slicerLink(artifact, named ? owner : owner + ".class") };
}

// slicer decompiles entirely in the browser, so it only needs the jar url and the entry to open.
// it names a remote archive after the last url segment - or input.jar if that has no extension - and prefixes the entries it unpacks with it
function slicerLink(artifact: VersionArtifact, entry: string): string | undefined {
  if (!artifact.downloadUrl || /\s/.test(entry)) return;
  const segment = new URL(artifact.downloadUrl).pathname.split("/").pop() ?? "";
  const file = (segment.includes(".") ? segment : "input.jar") + "/" + entry;
  return `https://slicer.run/?url=${encodeURIComponent(artifact.downloadUrl)}&file=${encodeURIComponent(file)}`;
}

function externalHost(url: string): string {
  try {
    return new URL(url).hostname;
  } catch {
    return url;
  }
}

function platformNames(artifact: VersionArtifact): string {
  return artifact.platforms.map((platform) => usePlatformName(platform)).join(", ");
}

function isExpanded(artifactKey: string, finding: Finding): boolean {
  return expanded.value[`${artifactKey} ${finding.key}`] ?? false;
}

function toggleExpanded(artifactKey: string, finding: Finding) {
  const key = `${artifactKey} ${finding.key}`;
  expanded.value[key] = !expanded.value[key];
}

function visibleOccurrences(artifactKey: string, finding: Finding): Occurrence[] {
  return isExpanded(artifactKey, finding) ? finding.occurrences : finding.occurrences.slice(0, 3);
}

async function scanArtifact(artifact: VersionArtifact) {
  scanning[artifact.key] = true;
  try {
    // Every platform of an artifact resolves to the same jar, so scanning one of them covers all
    await useInternalApi(`jarscanning/scan/${props.version?.id}/${artifact.platforms[0]}`, "POST");
    await refreshJarScans();
    useNotificationStore().success(t("scan.notify.done", [platformNames(artifact)]));
  } catch (err) {
    handleRequestError(err);
  } finally {
    scanning[artifact.key] = false;
  }
}

async function scanAll() {
  await Promise.all(scannableSections.value.map((section) => scanArtifact(section)));
}

useSeo(
  computed(() => ({
    title: "Scan | " + props.project?.name,
    route,
    description: props.project?.description,
    image: props.project?.avatarUrl,
  }))
);
</script>

<template>
  <div v-if="version" class="mt-4 flex flex-col gap-4">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div class="min-w-0">
        <h1 class="text-3xl font-bold">{{ t("scan.title") }}</h1>
        <p class="mt-1 flex flex-wrap items-center gap-x-2 gap-y-1 text-gray-secondary">
          <span>{{ t("scan.headline", [version.name]) }}</span>
          <Chip v-if="highestSeverity" :tone="severityTone(highestSeverity)">
            <IconMdiShieldAlertOutline v-if="severityTone(highestSeverity) === 'red'" />
            <IconMdiShieldCheckOutline v-else />
            {{ t("scan.highestSeverity", [severityLabel(highestSeverity)]) }}
          </Chip>
          <span v-if="lastScannedAt" class="text-sm"> {{ t("scan.lastScanned") }} <PrettyTime :time="lastScannedAt" long /> </span>
        </p>
      </div>
      <div class="flex flex-shrink-0 flex-wrap gap-2">
        <Button variant="outline" tone="neutral" :to="{ name: 'user-project-versions-version-reviews', params: route.params }">
          <IconMdiClipboardTextOutline />
          {{ t("scan.reviewLogs") }}
        </Button>
        <Button :loading="anyScanning" :disabled="scannableSections.length === 0" @click="scanAll">
          <IconMdiRadar />
          {{ scannedSections.length > 0 ? t("scan.rescanAll") : t("scan.scanAll") }}
        </Button>
      </div>
    </div>

    <div v-if="totalFindings > 0" class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
      <SegmentedControl v-model="severityFilter" :options="severityOptions" :aria-label="t('scan.filter.label')" class="max-w-full overflow-x-auto" />
      <InputText v-model.trim="search" :label="t('scan.search')" class="w-full lg:w-72">
        <template #append>
          <IconMdiMagnify class="text-gray-secondary" />
        </template>
      </InputText>
    </div>

    <Card v-for="section in sections" :key="section.key" flat padding="none">
      <div class="flex flex-wrap items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <span class="flex flex-shrink-0 items-center gap-1">
          <PlatformLogo v-for="platform in section.platforms" :key="platform" :platform="platform" :size="20" />
        </span>
        <div class="min-w-0 flex flex-grow flex-wrap items-baseline gap-x-2">
          <h2 class="text-lg font-bold">{{ platformNames(section) }}</h2>
          <span v-if="section.fileInfo" class="min-w-0 truncate text-sm text-gray-secondary" :title="section.fileInfo.name">
            {{ section.fileInfo.name }} · {{ formatSize(section.fileInfo.sizeBytes) }}
          </span>
        </div>

        <template v-if="section.scan">
          <Chip :tone="severityTone(section.scan.highestSeverity)">
            <IconMdiShieldAlertOutline v-if="severityTone(section.scan.highestSeverity) === 'red'" />
            <IconMdiShieldCheckOutline v-else />
            {{ severityLabel(section.scan.highestSeverity) }}
          </Chip>
          <Chip>{{ t("scan.findings", [section.total], section.total) }}</Chip>
          <span class="text-xs text-gray-secondary"><PrettyTime :time="section.scan.createdAt" short-relative /></span>
        </template>
        <Chip v-else-if="section.externalUrl" tone="amber">
          <IconMdiOpenInNew />
          {{ t("scan.external") }}
        </Chip>
        <Chip v-else tone="amber">
          <IconMdiShieldOffOutline />
          {{ t("scan.notScanned") }}
        </Chip>

        <Button
          v-if="!section.externalUrl && section.fileInfo"
          size="sm"
          variant="outline"
          tone="neutral"
          :loading="scanning[section.key]"
          @click="scanArtifact(section)"
        >
          <IconMdiRadar />
          {{ section.scan ? t("scan.rescan") : t("scan.scanNow") }}
        </Button>
      </div>

      <div v-if="section.externalUrl" class="flex flex-col items-center px-4 py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiOpenInNew />
        </div>
        <p class="text-gray-secondary">{{ t("scan.externalHint") }}</p>
        <a :href="linkout(section.externalUrl)" target="_blank" rel="noopener noreferrer" class="mt-1 text-sm color-primary hover:underline">
          {{ externalHost(section.externalUrl) }}
        </a>
      </div>

      <div v-else-if="!section.fileInfo" class="flex flex-col items-center px-4 py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiFileHidden />
        </div>
        <p class="text-gray-secondary">{{ t("scan.noFile") }}</p>
      </div>

      <div v-else-if="!section.scan" class="flex flex-col items-center px-4 py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiShieldOffOutline />
        </div>
        <p class="text-gray-secondary">{{ t("scan.notScannedHint") }}</p>
      </div>

      <div v-else-if="section.findings.length === 0" class="flex flex-col items-center px-4 py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full bg-lime-500/15 text-xl text-lime-500">
          <IconMdiShieldCheckOutline />
        </div>
        <p class="text-gray-secondary">{{ t("scan.clean") }}</p>
      </div>

      <div v-else-if="section.visible.length === 0" class="flex flex-col items-center px-4 py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiFilterRemoveOutline />
        </div>
        <p class="text-gray-secondary">{{ t("scan.noResults") }}</p>
      </div>

      <ul v-else class="divide-y divide-gray-300 dark:divide-gray-700">
        <li v-for="finding in section.visible" :key="finding.key" class="flex items-start gap-3 px-4 py-3">
          <div class="mt-0.5 h-7 w-7 flex flex-shrink-0 items-center justify-center rounded-lg" :class="severityPuckClasses(finding.severity)">
            <IconMdiShieldAlertOutline v-if="severityTone(finding.severity) === 'red'" />
            <IconMdiAlertOutline v-else-if="severityTone(finding.severity) === 'amber'" />
            <IconMdiInformationOutline v-else />
          </div>

          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-center gap-2">
              <span class="font-semibold">{{ finding.checkName || finding.message }}</span>
              <Chip :tone="severityTone(finding.severity)">{{ severityLabel(finding.severity) }}</Chip>
              <Chip v-if="finding.total > 1">{{ t("scan.occurrences", [finding.total]) }}</Chip>
            </div>
            <p v-if="finding.checkName" class="mt-0.5 text-sm text-gray-secondary">{{ finding.message }}</p>

            <ul class="mt-1.5 flex flex-col gap-1">
              <li v-for="occurrence in visibleOccurrences(section.key, finding)" :key="occurrence.location" class="min-w-0 flex items-baseline gap-2">
                <span class="min-w-0 flex flex-wrap items-baseline gap-x-2 font-mono text-xs">
                  <a
                    v-if="occurrence.href"
                    :href="occurrence.href"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="break-all hover:(color-primary underline)"
                    :title="t('scan.decompile')"
                  >
                    {{ occurrence.owner }}
                    <IconMdiOpenInNew class="inline-block text-2.5 opacity-70" />
                  </a>
                  <span v-else class="break-all">{{ occurrence.owner }}</span>
                  <span v-if="occurrence.member" class="break-all text-gray-secondary">{{ occurrence.member }}</span>
                </span>
                <span v-if="occurrence.count > 1" class="flex-shrink-0 text-xs text-gray-secondary tabular-nums">
                  {{ t("scan.occurrences", [occurrence.count]) }}
                </span>
              </li>
            </ul>

            <Button v-if="finding.occurrences.length > 3" variant="ghost" tone="neutral" size="sm" class="mt-1" @click="toggleExpanded(section.key, finding)">
              <IconMdiChevronDown class="transition-transform" :class="isExpanded(section.key, finding) ? 'rotate-180' : ''" />
              {{ isExpanded(section.key, finding) ? t("scan.showLess") : t("scan.showMore", [finding.occurrences.length - 3], finding.occurrences.length - 3) }}
            </Button>
          </div>
        </li>
      </ul>
    </Card>

    <Card v-if="sections.length === 0" flat>
      <div class="flex flex-col items-center py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiPackageVariantClosed />
        </div>
        <p class="text-gray-secondary">{{ t("scan.noPlatforms") }}</p>
      </div>
    </Card>
  </div>
</template>
