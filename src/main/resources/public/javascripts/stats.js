//=====> DOCUMENT READY

$(function() {
    $('#dateGoButton').click(function() {
        var from = $('#fromDate').val();
        var to = $('#toDate').val();

        function removeTime(date) {
            date.setHours(0, 0, 0, 0)
        }

        if(!to) {
            to = new Date();
        }
        else {
            to = new Date(to);
        }
        removeTime(to);

        if(!from) {
            from = new Date();
            from.setDate(to.getDate() - 10);
        }
        else {
            from = new Date(from);
        }
        removeTime(from);

        var now = removeTime(new Date());
        if(to > now) {
            to.setDate(now.getDate());
        }

        if(from > to) {
            from = new Date();
            from.setDate(to.getDate() - 1)
        }

        var url = '/admin/stats?from=' + from.toISOString().substr(0, 10) + '&to=' + to.toISOString().substr(0, 10);
        go(url);
    });
});
