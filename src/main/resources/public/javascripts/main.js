//=====> CONSTANTS

const KEY_ENTER = 13;

//=====> SETUP

const clipboardManager = new ClipboardJS('.copy-url');
clipboardManager.on('success', function(e) {
    const element = $('.btn-download').tooltip({title: 'Copied!', placement: 'bottom', trigger: 'manual'}).tooltip('show');
    setTimeout(function () {
        element.tooltip('destroy');
    }, 2200);
});


//=====> HELPER FUNCTIONS

function sanitize(html) {
    return $('<textarea>').html(html).text();
}

function decodeHtml(html) {
    // lol
    return $('<textarea>').html(html).val();
}

function go(str) {
    window.location = decodeHtml(str);
}

function clearUnread(e) {
    e.find('.unread').remove();
    if (!$('.user-dropdown .unread').length) $('.unread').remove();
}

function initTooltips() {
    $('[data-toggle="tooltip"]').tooltip({
        container: "body",
        delay: { "show": 500 }
    });
}

function slugify(name) {
    return name.trim().replace(/ +/g, ' ').replace(/ /g, '-');
}

function toggleSpinner(e) {
    return e.toggleClass('fa-spinner').toggleClass('fa-spin');
}

function numberWithCommas(x) {
    const parts = x.toString().split(".");
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    return parts.join(".");
}


//=====> DOCUMENT READY

// Initialize highlighting
hljs.initHighlightingOnLoad();

$(function() {
    if(window.navigator.userAgent.indexOf('MSIE ') > 0 || window.navigator.userAgent.indexOf('Trident/') > 0) {
        alert("Hangar doesn't support Internet Explorer! Please use a modern browser.");
    }

    $('.alert-fade').fadeIn('slow');

    initTooltips();

    $('.btn-spinner').click(function() {
        const iconClass = $(this).data('icon');
        toggleSpinner($(this).find('[data-fa-i2svg]').toggle(iconClass));
    });

    $(".link-go-back").click(function () {
        window.history.back();
    });
});

// Fix page anchors which were broken by the fixed top navigation

const scrollToAnchor = function (anchor) {
    if (anchor) {
        let target = $("a" + anchor);

        if (target.length) {
            $('html,body').animate({
                scrollTop: target.offset().top - ($("#topbar").height() + 10)
            }, 1);

            return false;
        }
    }

    return true;
};

$(window).load(function () {
    return scrollToAnchor(window.location.hash);
});

$("a[href^='#']").click(function () {
    window.location.replace(window.location.toString().split("#")[0] + $(this).attr("href"));

    return scrollToAnchor(this.hash);
});


//=====> SERVICE WORKER

// The service worker has been removed in commit 9ab90b5f4a5728587fc08176e316edbe88dfce9e.
// This code ensures that the service worker is removed from the browser.

if (window.navigator && navigator.serviceWorker) {
    if ('getRegistrations' in navigator.serviceWorker) {
        navigator.serviceWorker.getRegistrations().then(function (registrations) {
            registrations.forEach(function (registration) {
                registration.unregister();
            })
        })
    } else if ('getRegistration' in navigator.serviceWorker) {
        navigator.serviceWorker.getRegistration().then(function (registration) {
            registration.unregister();
        })
    }
}
