//=====> HELPER FUNCTIONS

function markRead(notification) {
    var btn = notification.find('.btn-mark-read');
    toggleSpinner(btn);
    $.ajax({
        type: 'post',
        url: '/notifications/read/' + notification.data('id'),
        complete: function() {
            toggleSpinner(notification.find('.btn-mark-read').addClass('btn-mark-read fa-check'));
        },
        success: function() {
            notification.fadeOut('slow', function() {
                notification.remove();
                if ($('.notification').length === 0)
                    $('.no-notifications').fadeIn('slow');
            })
        }
    })
}

function replyToInvite(invite, reply, success, error) {
    var url = invite.data('type') === 'project' ? '' : 'organizations';
    url += '/invite/' + invite.data('id') + '/' + reply;
    $.ajax({
        type: 'post',
        url: url,
        success: success,
        error: error
    });
}

function setupNotificationButtons() {
    $('.btn-mark-all-read').click(function() {
        $('.btn-mark-read').click();
    });

    $('.btn-mark-read').click(function() {
        markRead($(this).closest('.notification'));
    });

    $('.notification').click(function(e) {
        if (e.target !== this)
            return;

        var action = $(this).data('action');
        $(this).find('.btn-mark-read').click();
        if (action !== 'none')
            window.location.href = action;
    });
}

function setupFilters() {
    $('.select-notifications').on('change', function() {
        window.location = '/notifications?notificationFilter=' + $(this).val();
    });

    $('.select-invites').on('change', function() {
        window.location = '/notifications?inviteFilter=' + $(this).val();
    });
}

function setupInvites() {

    function fadeOutLoading(loading, after) {
        loading.fadeOut('fast', after);
    }

    $('.btn-invite').click(function() {
        var btn = $(this);
        var invite = btn.closest('.invite-content');
        var choice = invite.find('.invite-choice');
        choice.animate({
            right: "+=200"
        }, 200, function() {
            choice.hide();

            var loading = invite.find('.invite-loading').fadeIn('fast');
            var accepted = btn.hasClass('btn-accept');
            var result = invite.find(accepted ? '.invite-accepted' : '.invite-declined');

            replyToInvite(invite, accepted ? 'accept' : 'decline', function() {
                fadeOutLoading(loading, function() {
                    result.fadeIn('slow');
                    invite.find('.invite-dismiss').fadeIn('slow');
                })
            }, function() {
                fadeOutLoading(loading, function() {
                    choice.show().animate({
                        right: '5%'
                    }, 200);
                });
            });
        });
    });

    $('.btn-undo').click(function() {
        var invite = $(this).closest('.invite-content');
        var accepted = invite.find('.invite-accepted');
        invite.find('.invite-dismiss').fadeOut('slow');
        accepted.fadeOut('slow', function() {
            var loading = invite.find('.invite-loading').fadeIn('fast');
            replyToInvite(invite, 'unaccept', function() {
                fadeOutLoading(loading, function() {
                    var choice = invite.find('.invite-choice');
                    choice.css('right', '+=200').show().animate({
                        right: "5%"
                    }, 200);
                });
            }, function() {
                accepted.fadeIn('slow');
            });
        });
    });

    $('.dismiss').click(function() {
        var invite = $(this).closest('.invite-content');
        invite.fadeOut('slow', function() {
            invite.remove();
        })
    });
}


//=====> DOCUMENT READY

$(function() {
    var invites = $('.invite-content');
    invites.css('height', invites.width());
    setupNotificationButtons();
    setupFilters();
    setupInvites();
});
