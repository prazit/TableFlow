'use strict'

function updatePanelComplete(panel) {
    console.log('updatePanelComplete("' + panel + '")');
    selectableHandle($(panel + ' .selectable'));
}

function updateComplete(selectableId) {
    selectableHandle(selectableId);
}

$(function () {
    window.parent.refreshProperties();

    tflow.ready = true;

    selectableHandle($('.selectable'));
});