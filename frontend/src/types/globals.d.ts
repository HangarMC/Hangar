declare global {
  interface Window {
    hangarLoaded?: boolean;
    hangarDebug?: Record<string, Function<void, any>>;
  }
}
