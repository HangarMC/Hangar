export interface Tab {
  value: string;
  header: string;
  show?: () => boolean;
  disable?: () => boolean;
}
