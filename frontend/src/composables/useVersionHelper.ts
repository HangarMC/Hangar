const lastWholeVersion = new RegExp("((?<=,\\s)|^)[0-9.]{2,}(?=-[0-9.]+$)");
const prevHasHyphen = new RegExp("(?<=\\d-)[0-9.]+$");
const prevHasCommaOrFirst = new RegExp("((?<=,\\s)|^)[0-9.]+$");

export function formatVersionNumbers(versionNumbers: string[]): string {
  if (!versionNumbers) {
    return "";
  }

  versionNumbers.sort((version1, version2) => {
    let vnum1 = 0;
    let vnum2 = 0;
    for (let i = 0, j = 0; i < version1.length || j < version2.length; ) {
      while (i < version1.length && version1.charAt(i) != ".") {
        vnum1 = vnum1 * 10 + (parseInt(version1.charAt(i)) - parseInt("0"));
        i++;
      }
      while (j < version2.length && version2.charAt(j) != ".") {
        vnum2 = vnum2 * 10 + (parseInt(version2.charAt(j)) - parseInt("0"));
        j++;
      }
      if (vnum1 > vnum2) {
        return 1;
      }
      if (vnum2 > vnum1) {
        return -1;
      }
      vnum1 = vnum2 = 0;
      i++;
      j++;
    }
    return 0;
  });

  let verString = "";
  for (const version of versionNumbers) {
    if (verString.length === 0) {
      verString = version;
      continue;
    }

    const versionArr = splitVersionNumber(version);
    const hyphen = prevHasHyphen.exec(verString);
    const comma = prevHasCommaOrFirst.exec(verString);
    if (hyphen && hyphen.length !== 0) {
      const group: string[] = hyphen[0].split(".");
      const prevVersion: number = +group.at(-1)!;
      const prevVersionMatcher = lastWholeVersion.exec(verString);
      if (!prevVersionMatcher || prevVersionMatcher.length === 0) {
        console.error("Bad versions " + versionNumbers);
        continue;
      }

      const previousWholeVersion = splitVersionNumber(prevVersionMatcher[0]);
      if (previousWholeVersion.length === versionArr.length) {
        if (versionArr.at(-1)! - 1 === prevVersion) {
          verString = verString.replace(new RegExp("-[0-9.]+$"), "-" + version);
        } else {
          verString += ", " + version;
        }
      } else {
        verString += ", " + version;
      }
    } else if (comma && comma.length !== 0) {
      const prevVersion = splitVersionNumber(comma[0]);
      if (prevVersion.length === versionArr.length) {
        verString += versionArr.at(-1)! - 1 === prevVersion.at(-1) ? "-" + version : ", " + version;
      } else {
        verString += ", " + version;
      }
    } else {
      console.error("Bad versions " + versionNumbers);
    }
  }
  return verString;
}

function splitVersionNumber(str: string): number[] {
  const list: number[] = [];
  for (const s of str.split(".")) {
    const parseInt: number = +s;
    list.push(parseInt);
  }
  return list;
}
