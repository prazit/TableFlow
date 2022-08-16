'use strict';

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
            window[$e.find('input[name=command]').attr('value')]();
        });
    });
}

$(function () {
    window.parent.refreshProperties();

    tflow.ready = true;

    updatePanelComplete('.database-panel');
    updatePanelComplete('.sftp-panel');
    updatePanelComplete('.local-panel');
});