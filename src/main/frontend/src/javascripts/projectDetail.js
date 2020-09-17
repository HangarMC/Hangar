//=====> CONSTANTS

var KEY_PLUS = 61;
var KEY_MINUS = 173;


//=====> EXTERNAL CONSTANTS

var projectOwner = null;
var projectSlug = null;
var projectId = null;
var alreadyStarred = false;


//=====> HELPER FUNCTIONS

function getActiveTab() {
    return $('.project-navbar').find('li.active');
}

function switchTabTo(tab, def) {
    var id = tab.attr('id');
    if (tab.is('li') && id !== 'issues' && id !== 'source') {
        window.location = tab.find('a').attr('href');
    } else {
        window.location = def.find('a').attr('href');
    }
}

function initFlagList() {
    var flagList = $('.list-flags');
    if (!flagList.length) return;
    flagList.find('li').click(function() {
        flagList.find(':checked').removeAttr('checked');
        $(this).find('input').prop('checked', true);
    });
}

function animateEditBtn(e, marginLeft, andThen) {
    e.animate({ marginLeft: marginLeft }, 100, function() {
        if (andThen) andThen();
    });
}

function showEditBtn(e, andThen) {
    animateEditBtn(e, '-34px', function() {
        e.css('z-index', '1000');
        if (andThen) andThen();
    });
}

function hideEditBtn(e, andThen) {
    animateEditBtn(e, '0', andThen);
}

var editing = false;
var previewing = false;

function initBtnEdit() {
    var btnEdit = $('.btn-edit');
    if (!btnEdit.length) return;

    var pageBtns = $('.btn-page');
    var otherBtns = $('.btn-edit-container');

    // highlight with textarea
    var editText = $('.page-edit').find('textarea');
    editText.focus(function() {
        btnEdit
            .css('border-color', '#66afe9')
            .css('border-right', '1px solid white')
            .css('box-shadow', 'inset 0 1px 1px rgba(0,0,0,.075), -3px 0 8px rgba(102, 175, 233, 0.6)');
        otherBtns.find('.btn').css('border-right-color', '#66afe9')
    }).blur(function() {
        $('.btn-page').css('border', '1px solid #ccc').css('box-shadow', 'none');
        $('button.open').css('border-right', 'white');
    });

    // handle button clicks
    pageBtns.click(function() {
        if ($(this).hasClass('open')) return;

        // toggle button
        $('button.open').removeClass('open').css('border', '1px solid #ccc');
        $(this).addClass('open').css('border-right-color', 'white');

        var editor = $('.page-edit');
        if ($(this).hasClass('btn-edit')) {
            editing = true;
            previewing = false;
            $(this).css('position', 'absolute').css('top', '');
            $(otherBtns).css('position', 'absolute').css('top', '');

            // open editor
            var content = $('.page-rendered');
            editor.find('textarea').css('height', content.css('height'));
            content.hide();
            editor.show();

            // show buttons
            showEditBtn($('.btn-preview-container'), function() {
                showEditBtn($('.btn-save-container'), function() {
                    showEditBtn($('.btn-cancel-container'), function() {
                        showEditBtn($('.btn-delete-container'));
                    });
                });
            });
        }

        else if ($(this).hasClass('btn-preview')) {
            // render markdown
            var preview = $('.page-preview');
            var raw = editor.find('textarea').val();
            editor.hide();
            preview.show();
            var icon = toggleSpinner($(this).find('[data-fa-i2svg]').toggleClass('fa-eye'));

            $.ajax({
                type: 'post',
                url: '/pages/preview',
                data: JSON.stringify({ raw: raw }),
                contentType: 'application/json',
                dataType: 'html',
                complete: function() {toggleSpinner($('.btn-preview').find('[data-fa-i2svg]').toggleClass('fa-eye')) },
                success: function(cooked) { preview.html(cooked); }
            });

            editing = false;
            previewing = true;
        }

        else if ($(this).hasClass('btn-save')) {
            // add spinner
            toggleSpinner($(this).find('[data-fa-i2svg]').toggleClass('fa-save'));
        }
    });

    $('.btn-cancel').click(function() {
        editing = false;
        previewing = false;

        // hide editor; show content
        $('.page-edit').hide();
        $('.page-preview').hide();
        $('.page-content').show();

        // move buttons behind
        $('.btn-edit-container').css('z-index', '-1000');

        // hide buttons
        var fromSave = function() {
            hideEditBtn($('.btn-save-container'), function() {
                hideEditBtn($('.btn-preview-container'));
            });
        };

        var btnDelete = $('.btn-delete-container');
        var btnCancel = $('.btn-cancel-container');
        if (btnDelete.length) {
            hideEditBtn(btnDelete, function() { hideEditBtn(btnCancel, fromSave) });
        } else {
            hideEditBtn(btnCancel, fromSave);
        }
    });

    // move with scroll
    $(window).scroll(function() {
        var scrollTop = $(this).scrollTop();
        var editHeight = btnEdit.height();
        var page = previewing ? $('.page-preview') : $('.page-content');
        var pageTop = page.position().top;
        var pto = page.offset().top;
        var pos = btnEdit.css('position');
        var bound = pto - editHeight - 30;

        if (scrollTop > bound && pos === 'absolute' && !editing) {
            var newTop = pageTop + editHeight + 20;
            btnEdit.css('position', 'fixed').css('top', newTop);
            otherBtns.each(function() {
                newTop += 0.5;
                $(this).css('position', 'fixed').css('top', newTop);
            });
        } else if (scrollTop < bound && pos === 'fixed') {
            btnEdit.css('position', 'absolute').css('top', '');
            otherBtns.css('position', 'absolute').css('top', '');
        }
    });
}


//=====> DOCUMENT READY

$(function() {
    initFlagList();
    initBtnEdit();

    // flag button alert
    var flagMsg = $('.flag-msg');
    if (flagMsg.length) {
        flagMsg.hide().fadeIn(1000).delay(2000).fadeOut(1000);
    }

    // watch button
    $('.btn-watch').click(function() {
        var status = $(this).find('.watch-status');
        var watching = $(this).hasClass('watching');
        if (watching) {
            status.text('Watch');
            $(this).removeClass('watching');
        } else {
            status.text('Unwatch');
            $(this).addClass('watching');
        }

        $(this)
            .find('[data-fa-i2svg]')
            .toggleClass('fa-eye')
            .toggleClass('fa-eye-slash');

        $.ajax({
            type: 'post',
            url: decodeHtml('/' + projectOwner + '/' + projectSlug) + '/watchers/' + !watching
        });
    });

    // setup star button
    var increment = alreadyStarred ? -1 : 1;
    $('.btn-star').click(function() {
        var starred = $(this).find('.starred');
        starred.html(' ' + (parseInt(starred.text()) + increment).toString());
        $.ajax({
            type: 'post',
            url: decodeHtml('/' + projectOwner + '/' + projectSlug) + '/stars/toggle'
        });

        if (increment > 0) {
            $(this).find('[data-fa-i2svg]').attr('data-prefix', 'fas');
        } else {
            $(this).find('[data-fa-i2svg]').attr('data-prefix', 'far');
        }

        increment *= -1;
    });

    if(projectId) {
        apiV2Request("projects/" + projectId).then((response) => {
            if(response.promoted_versions) {
                let html = "";
                response.promoted_versions.forEach((version) => {
                    const href = jsRoutes.controllers.project.Versions.show(projectOwner, projectSlug, version.version).absoluteURL();
                    html = html + "<li class='list-group-item'><a href='" + href + "'>" + version.version +  "</a></li>";
                });
                $(".promoted-list").html(html);
            }
            $(".stats #view-count").html(numberWithCommas(response.stats.views));
            $(".stats #star-count").html(numberWithCommas(response.stats.stars));
            $(".stats #watcher-count").html(numberWithCommas(response.stats.watchers));
            $(".stats #download-count").html(numberWithCommas(response.stats.downloads));
        });
    }
});
