export interface Tab<T extends string> {
  value: T;
  header: string;
  show?: () => boolean;
  disable?: () => boolean;
}
