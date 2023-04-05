import { filesize } from "filesize";

export function formatSize(input: any) {
  return filesize(input);
}
