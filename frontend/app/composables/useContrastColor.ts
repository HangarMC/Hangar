/** https://stackoverflow.com/a/3943023 — pick black or white text for an arbitrary background. */
export function contrastForeground(background?: string): "black" | "white" | undefined {
  if (!background) return;

  let colors: number[];
  if (background.startsWith("rgb")) {
    colors = background
      .replace("rgb(", "")
      .replace(")", "")
      .split(",")
      .map((c) => Number.parseInt(c));
  } else if (background.startsWith("#")) {
    const bg = background.slice(1, 7);
    colors = [Number.parseInt(bg.slice(0, 2), 16), Number.parseInt(bg.slice(2, 4), 16), Number.parseInt(bg.slice(4, 6), 16)];
  } else {
    return;
  }

  const [r, g, b] = colors.map((col) => col / 255).map((col) => (col <= 0.039_28 ? col / 12.92 : Math.pow((col + 0.055) / 1.055, 2.4)));
  return 0.2126 * r! + 0.7152 * g! + 0.0722 * b! > 0.179 ? "black" : "white";
}
