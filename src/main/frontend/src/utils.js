export function clearFromEmpty(object) {
    return Object.entries(object)
        .filter(([key, value]) => value != null && value !== "")
        .reduce((acc, [key, value]) => ({...acc, [key]: value}), {})
}

export function clearFromDefaults(object, defaults) {
    return Object.entries(object)
        .filter(([key, value]) => {
            if(value instanceof Array && defaults[key] instanceof Array) {
                return value.length !== defaults[key].length;
            }

            return value !== defaults[key];
        })
        .reduce((acc, [key, value]) => ({...acc, [key]: value}), {})
}

export function parseJsonOrNull(jsonString) {
    try {
        return JSON.parse(jsonString);
    } catch (e) {
        return null;
    }
}
