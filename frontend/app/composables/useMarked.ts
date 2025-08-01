import GithubSlugger from "github-slugger";
import type { Hooks, Renderer } from "marked";
import { marked } from "marked";
import markedLinkifyIt from "marked-linkify-it";
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-expect-error
import markedExtendedTables from "marked-extended-tables";
import markedAlert from "marked-alert";

const youtubeRegex = /(?:youtube\.com\/(?:[^\s/]+\/\S+\/|(?:v|e(?:mbed)?)\/|.*[&?]v=)|youtu\.be\/)([\w-]{11})(?:==(\d+))?/;
const imageSizeParts = /(.*)==\s*(\d*)\s*x?\s*(\d*)\s*$/;

let headings: { id: string; text?: string; level: number }[] = [];
let slugger: GithubSlugger;

const hooks = {
  preprocess(src) {
    headings = [];
    slugger = new GithubSlugger();
    return src;
  },
} satisfies Partial<Hooks>;

const renderer = {
  heading({ tokens, depth, raw }) {
    // https://github.com/markedjs/marked-gfm-heading-id/blob/main/src/index.js#L17
    // Licenced as MIT, copied because we want an anchor
    raw = raw
      .toLowerCase()
      .trim()
      .replaceAll(/<[!/a-z].*?>/gi, "");
    const id = `${slugger.slug(raw)}`;
    const text = this.parser?.parseInline(tokens);
    const heading = { level: depth, text, id };
    headings.push(heading);
    const level = depth + 1;

    return `<h${level} id="${id}">
              ${text}
              <a class="headeranchor" href="#${id}">
                <span class="header-link"></span>
                <svg class="text-xl inline-flex mb-1" preserveAspectRatio="xMidYMid meet" viewBox="0 0 24 24" width="1.2em" height="1em"><path fill="currentColor" d="M10.59 13.41c.41.39.41 1.03 0 1.42c-.39.39-1.03.39-1.42 0a5.003 5.003 0 0 1 0-7.07l3.54-3.54a5.003 5.003 0 0 1 7.07 0a5.003 5.003 0 0 1 0 7.07l-1.49 1.49c.01-.82-.12-1.64-.4-2.42l.47-.48a2.982 2.982 0 0 0 0-4.24a2.982 2.982 0 0 0-4.24 0l-3.53 3.53a2.982 2.982 0 0 0 0 4.24m2.82-4.24c.39-.39 1.03-.39 1.42 0a5.003 5.003 0 0 1 0 7.07l-3.54 3.54a5.003 5.003 0 0 1-7.07 0a5.003 5.003 0 0 1 0-7.07l1.49-1.49c-.01.82.12 1.64.4 2.43l-.47.47a2.982 2.982 0 0 0 0 4.24a2.982 2.982 0 0 0 4.24 0l3.53-3.53a2.982 2.982 0 0 0 0-4.24a.973.973 0 0 1 0-1.42Z"></path></svg>
              </a>
            </h${level}>`;
  },
  image({ href, title, text }) {
    const parts = imageSizeParts.exec(href);
    let url = href;
    let height;
    let width;
    if (parts) {
      url = parts[1]!;
      height = parts[2];
      width = parts[3];
    }

    const youtubeMatch = url.match(youtubeRegex);
    if (youtubeMatch && youtubeMatch[1]?.length === 11) {
      let res = '<iframe src="https://www.youtube.com/embed/' + youtubeMatch[1] + '"';

      if (!height && !width) {
        res += ' height="315" width="560"';
      } else {
        if (height) res += ' height="' + height + '" absolute top-0 left-0 w-full h-full max-w-full max-h-full';
        if (width) res += ' width="' + width + '"';
        if (title) res += ' title="' + title + '"';
      }

      res += ">" + text + "</iframe>";
      return res;
    } else {
      let res = '<img src="' + proxyImage(url) + '" alt="' + text + '"';
      if (height) res += ' height="' + height + '"';
      if (width) res += ' width="' + width + '"';
      if (title) res += ' title="' + title + '"';
      res += ">";
      return res;
    }
  },
  link({ href, title, tokens }) {
    return `<a href="${linkout(href)}"` + (title ? ` title="${title}">` : ">") + this.parser?.parseInline(tokens) + "</a>";
  },
} satisfies Partial<Renderer>;
marked.use({ hooks, renderer });
marked.use(markedExtendedTables());
marked.use(markedLinkifyIt());
marked.use(markedAlert());

export function parseMarkdown(text?: string | null) {
  if (!text) return {};
  return {
    html: marked.parse(text) as string,
    headings: [...headings],
  };
}
