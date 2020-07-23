//=====> EXTERNAL CONSTANTS

var CURRENT_PAGE = 0;


//=====> DOCUMENT READY

$(function() {
    $('.table-users').find('thead').find('td:not(:first-child)').click(function() {
        var sort = $(this).text().toLowerCase().trim();
        var direction = '';
        var thisObj = $(this);
        if (thisObj.hasClass('user-sort')) {
            // Change direction
            direction = $(this).find('svg').hasClass('fa-chevron-up') ? '-' : '';
        }
        var start = thisObj.data("list");
        var url = '/' + start + '?sort=' + direction + sort;
        if (CURRENT_PAGE > 1) url += '&page=' + CURRENT_PAGE;
        go(url);
    });
});
