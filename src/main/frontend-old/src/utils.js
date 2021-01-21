import $ from 'jquery';

export function clearFromEmpty(object) {
    return Object.entries(object)
        .filter(([key, value]) => value != null && value !== '') // eslint-disable-line no-unused-vars
        .reduce((acc, [key, value]) => ({ ...acc, [key]: value }), {});
}

export function clearFromDefaults(object, defaults) {
    return Object.entries(object)
        .filter(([key, value]) => {
            if (value instanceof Array && defaults[key] instanceof Array) {
                return value.length !== defaults[key].length;
            }

            return value !== defaults[key];
        })
        .reduce((acc, [key, value]) => ({ ...acc, [key]: value }), {});
}

export function parseJsonOrNull(jsonString) {
    try {
        return JSON.parse(jsonString);
    } catch (e) {
        return null;
    }
}

export function sanitize(html) {
    return $('<textarea>').html(html).text();
}

export function decodeHtml(html) {
    // lol
    return $('<textarea>').html(html).val();
}

export function go(str) {
    window.location = decodeHtml(str);
}

export function clearUnread(e) {
    e.find('.unread').remove();
    if (!$('.user-dropdown .unread').length) $('.unread').remove();
}

export function initTooltips() {
    $('[data-tooltip-toggle]').tooltip({
        container: 'body',
        delay: { show: 500 },
    });
}

export function slugify(name) {
    return name.trim().replace(/ +/g, ' ').replace(/ /g, '-');
}

export function toggleSpinner(e) {
    return e.toggleClass('fa-spinner').toggleClass('fa-spin');
}

export function toggleSpin(element) {
    element.classList.toggle('fa-spinner');
    element.classList.toggle('fa-spin');
    return element;
}

export function numberWithCommas(x) {
    const parts = x.toString().split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return parts.join('.');
}

export const scrollToAnchor = function (anchor) {
    if (anchor) {
        let target = $('a' + anchor);

        if (target.length) {
            $('html,body').animate(
                {
                    scrollTop: target.offset().top - ($('#topbar').height() + 10),
                },
                1
            );

            return false;
        }
    }

    return true;
};
export const KEY_ENTER = 13;
