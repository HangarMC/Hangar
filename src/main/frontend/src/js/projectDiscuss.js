//=====> DOCUMENT READY

(function () {
    var d = document.createElement('script');
    d.type = 'text/javascript';
    d.async = true;
    d.src = window.DiscourseEmbed.discourseUrl + 'build/js/embed.js'; // TODO this was ... + "javascripts/embed.js"
    (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(d);
})();
