/**
 * Requires yyyy-MM-DD format
 * @param dateString
 */
export function fromISOString(dateString: string): Date {
  const ds = dateString.split("-").map((s) => parseInt(s));
  return new Date(ds[0], ds[1] - 1, ds[2]);
}

export function toISODateString(date: Date): string {
  return `${date.getFullYear()}-${("0" + (date.getMonth() + 1)).slice(-2)}-${("0" + date.getDate()).slice(-2)}`;
}
