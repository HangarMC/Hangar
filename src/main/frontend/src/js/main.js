import $ from 'jquery';
import ClipboardJS from 'clipboard';
import hljs from 'highlight.js';
import { initTooltips, scrollToAnchor, toggleSpinner } from '@/utils';
import 'bootstrap/js/dist/tooltip';

//=====> CONSTANTS

//=====> SETUP

const clipboardManager = new ClipboardJS('.copy-url');
clipboardManager.on('success', function () {
    const element = $('.btn-download').tooltip({ title: 'Copied!', placement: 'bottom', trigger: 'manual' }).tooltip('show');
    setTimeout(function () {
        element.tooltip('dispose');
    }, 2200);
});

//=====> HELPER FUNCTIONS

//=====> DOCUMENT READY

// Initialize highlighting
hljs.initHighlightingOnLoad();

$(function () {
    if (window.navigator.userAgent.indexOf('MSIE ') > 0 || window.navigator.userAgent.indexOf('Trident/') > 0) {
        alert("Hangar doesn't support Internet Explorer! Please use a modern browser.");
    }

    $('.alert-fade').fadeIn('slow');

    initTooltips();

    $('.btn-spinner').click(function () {
        const iconClass = $(this).data('icon');
        toggleSpinner($(this).find('[data-fa-i2svg]').toggle(iconClass));
    });

    $('.link-go-back').click(function () {
        window.history.back();
    });

    // we loaded now I guess
    $('.loader').hide();
});

// Fix page anchors which were broken by the fixed top navigation

$(window).on('load', function () {
    return scrollToAnchor(window.location.hash);
});

$("a[href^='#']").click(function () {
    window.location.replace(window.location.toString().split('#')[0] + $(this).attr('href'));

    return scrollToAnchor(this.hash);
});

//=====> SERVICE WORKER

// The service worker has been removed in commit 9ab90b5f4a5728587fc08176e316edbe88dfce9e.
// This code ensures that the service worker is removed from the browser.

// Hangar doesn't need this since it was never added.
/*if (window.navigator && navigator.serviceWorker) {
  if ("getRegistrations" in navigator.serviceWorker) {
    navigator.serviceWorker.getRegistrations().then(function(registrations) {
      registrations.forEach(function(registration) {
        registration.unregister();
      });
    });
  } else if ("getRegistration" in navigator.serviceWorker) {
    navigator.serviceWorker.getRegistration().then(function(registration) {
      registration.unregister();
    });
  }
}*/
