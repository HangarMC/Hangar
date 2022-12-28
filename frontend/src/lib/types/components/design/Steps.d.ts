import { Ref } from "vue";

export interface Step {
  value: string;
  header: string;
  beforeBack?: () => Promise<boolean> | boolean;
  beforeNext?: () => Promise<boolean> | boolean;
  disableBack?: Ref<boolean> | boolean;
  disableNext?: Ref<boolean> | boolean;
  showBack?: Ref<boolean> | boolean;
  showNext?: Ref<boolean> | boolean;
}
