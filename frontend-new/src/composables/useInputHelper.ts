import { defineComponent, h } from "vue";

export const inputClasses =
  "w-full outline-none p-2 border-bottom-1px rounded " +
  "bg-primary-light-200 border-gray-400 dark:(bg-primary-300/8 border-gray-500) " +
  "focus:(bg-primary-200/5 dark:bg-primary-200/10 border-primary-400) " +
  "error:(border-red-400) " +
  "disabled:(bg-black-15 text-black-50) " +
  "transition duration-200 ease";

export const labelClasses = [
  "absolute origin-top-left left-2 italic",
  "input-focused:(transform scale-62 opacity-100 not-italic) filled:(transform scale-62 text-black-50 not-italic)",
  "opacity-80 error:(!text-red-400) input-focused:(text-primary-300)",
  "top-10px input-focused:(top-0) filled:(top-0)",
  "transition duration-250 ease",
];

export const FloatingLabel = defineComponent({
  name: "FloatingLabel",
  props: {
    label: {
      type: String,
      required: false,
    },
  },
  render() {
    return this.label ? h("p", { class: labelClasses }, this.label) : [];
  },
});
