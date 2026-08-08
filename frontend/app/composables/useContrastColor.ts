function hexToRgb(hex: string): [number, number, number] | undefined {
  const clean = hex.trim().replace("#", "");
  const full = clean.length === 3 ? [...clean].map((c) => c + c).join("") : clean;
  if (!/^[0-9a-f]{6}$/i.test(full)) return undefined;
  const num = Number.parseInt(full, 16);
  return [(num >> 16) & 255, (num >> 8) & 255, num & 255];
}

function relativeLuminance([r, g, b]: [number, number, number]): number {
  const [R, G, B] = [r, g, b].map((c) => {
    const s = c / 255;
    return s <= 0.039_28 ? s / 12.92 : ((s + 0.055) / 1.055) ** 2.4;
  }) as [number, number, number];
  return 0.2126 * R + 0.7152 * G + 0.0722 * B;
}

function contrastRatio(l1: number, l2: number): number {
  const [a, b] = l1 > l2 ? [l1, l2] : [l2, l1];
  return (a + 0.05) / (b + 0.05);
}

// Slightly above the 4.5 target: the final RGB channels get rounded to integers for the CSS string, which
// nudges the real rendered contrast down a hair from whatever the (unrounded) binary search converges to.
const MIN_CONTRAST = 4.6;
// Contrast against a light bg only gets *harder* as the bg darkens, and against a dark bg only gets harder
// as the bg lightens. "background-card" (slate-200 / slate-700, see color.css) is the least-contrasty
// surface a tag realistically sits on in each mode, so targeting it is the binding case in both directions.
const DARK_SURFACE_LUMINANCE = relativeLuminance([0x33, 0x41, 0x55]); // slate-700
const LIGHT_SURFACE_LUMINANCE = relativeLuminance([0xE2, 0xE8, 0xF0]); // slate-200

/**
 * Mixes an arbitrary backend colour toward black (light mode) or white (dark mode) until it clears
 * MIN_CONTRAST, preserving as much of the original hue as possible. Binary search because hues differ
 * wildly in how much mixing they need to reach the same contrast (yellow needs far more than indigo).
 */
export function readableAccent(background?: string, dark = false): string | undefined {
  const rgb = background && hexToRgb(background);
  if (!rgb) return undefined;

  const mixTarget: [number, number, number] = dark ? [255, 255, 255] : [0, 0, 0];
  const surfaceLuminance = dark ? DARK_SURFACE_LUMINANCE : LIGHT_SURFACE_LUMINANCE;
  const mixed = (pct: number): [number, number, number] => [0, 1, 2].map((i) => pct * rgb[i]! + (1 - pct) * mixTarget[i]!) as [number, number, number];

  let lo = 0;
  let hi = 1;
  for (let i = 0; i < 24; i++) {
    const mid = (lo + hi) / 2;
    if (contrastRatio(relativeLuminance(mixed(mid)), surfaceLuminance) >= MIN_CONTRAST) {
      lo = mid;
    } else {
      hi = mid;
    }
  }

  const [r, g, b] = mixed(lo).map((c) => Math.round(Math.min(255, Math.max(0, c))));
  return `rgb(${r}, ${g}, ${b})`;
}
