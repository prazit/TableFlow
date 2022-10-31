'use strict';

function showProgress(parentClass, className) {
    PF(className).start();
    var selector = '.' + parentClass + ' .' + className;
    $(selector).removeClass('hide');
}

function hideProgress(parentClass, className) {
    PF(className).cancel();
    var selector = '.' + parentClass + ' .' + className;
    $(selector).addClass('hide');
}

function updatePanelComplete(panel) {
    selectableHandle($(panel + ' .selectable'));
    buttonHandle($(panel + ' .button'));
}

function updateComplete(selectableId) {
    selectableHandle(selectableId);
}

function buttonHandle($button) {
    $button.each(function (i, e) {
        var $e = $(e);
        $e.on('click', function (ev) {
            $('.active').removeClass('active');
            window[$e.find('input[name=command]').attr('value')]();
            ev.stopPropagation();
        });
    });
}

$(function () {
    window.parent.refreshProperties();

    tflow.ready = true;
});