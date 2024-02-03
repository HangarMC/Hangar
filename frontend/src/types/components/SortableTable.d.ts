export type Header<T extends string> = {
  name: T;
  title: string;
  sortable?: boolean;
  width?: string;
};
