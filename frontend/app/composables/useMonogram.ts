// Both ends of every pair are contrast-checked against white text (>= 4.5:1).
const MONOGRAM_GRADIENTS: [string, string][] = [
  ["#075985", "#0369a1"],
  ["#065f46", "#047857"],
  ["#5b21b6", "#7c3aed"],
  ["#92400e", "#b45309"],
  ["#9f1239", "#be123c"],
  ["#115e59", "#0f766e"],
  ["#3730a3", "#4f46e5"],
  ["#9a3412", "#c2410c"],
  ["#86198f", "#a21caf"],
  ["#3f6212", "#4d7c0f"],
  ["#155e75", "#0e7490"],
  ["#9d174d", "#be185d"],
];

function tokenize(name: string): string[] {
  return name
    .replaceAll(/([a-z\d])([A-Z])/g, "$1 $2")
    .split(/[\s\-_.]+/)
    .filter(Boolean);
}

export function monogramInitials(name?: string): string {
  if (!name) return "?";
  const parts = tokenize(name);
  if (parts.length === 0) return name.slice(0, 2).toUpperCase();
  if (parts.length === 1) return parts[0]!.slice(0, 2).toUpperCase();
  return (parts[0]![0]! + parts[1]![0]!).toUpperCase();
}

function hashOf(name: string): number {
  let hash = 5381;
  for (let i = 0; i < name.length; i++) {
    hash = (hash * 33) ^ name.codePointAt(i)!;
  }
  return Math.abs(hash);
}

/**
Flat duotone: two adjacent shades of one hue, angle varied per name. No highlight -- that reads as gloss.
*/
export function monogramBackground(name?: string): string {
  const [from, to] = MONOGRAM_GRADIENTS[name ? hashOf(name) % MONOGRAM_GRADIENTS.length : 0]!;
  const angle = name ? 90 + (hashOf(name) % 5) * 30 : 135;
  return `linear-gradient(${angle}deg, ${from} 0%, ${to} 100%)`;
}

/**
Hangar serves this when a subject has no uploaded avatar.
*/
export function isDefaultAvatar(url?: string): boolean {
  return !url || url.includes("/avatar/default/");
}
