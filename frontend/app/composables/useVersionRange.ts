function compareVersions(a: string, b: string): number {
  const pa = a.split(".");
  const pb = b.split(".");
  for (let i = 0; i < Math.max(pa.length, pb.length); i++) {
    const na = Number.parseInt(pa[i] ?? "0", 10);
    const nb = Number.parseInt(pb[i] ?? "0", 10);
    if (Number.isNaN(na) || Number.isNaN(nb)) {
      const cmp = (pa[i] ?? "").localeCompare(pb[i] ?? "");
      if (cmp !== 0) return cmp;
      continue;
    }
    if (na !== nb) return na - nb;
  }
  return 0;
}

/** "1.19", "1.19.1", … "1.21.4"  ->  "1.19–1.21.4". Sorts itself; don't trust incoming order. */
export function versionRange(versions?: string[] | readonly string[]): string {
  if (!versions?.length) return "";
  const sorted = versions.toSorted(compareVersions);
  const first = sorted[0]!;
  const last = sorted.at(-1)!;
  return first === last ? first : `${first}–${last}`;
}
