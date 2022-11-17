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
            $('.active').removeClass('active');
            window[$e.find('input[name=command]').attr('value')]();
            ev.stopPropagation();
        });
    });
}

function selectColumn($e) {
    if ($e.jquery !== undefined) {
        if ($e.hasClass('column')) {
            var selectedClass = 'selected',
                selected = !$e.hasClass(selectedClass),
                columnId = $e.find('.hidden').attr('id');
            console.debug('column(' + columnId + ') clicked');

            if (selected) $e.addClass(selectedClass);
            else $e.removeClass(selectedClass);

            selectQueryColumn([
                {name: 'columnId', value: columnId},
                {name: 'selected', value: selected}
            ]);

            return;
        }
    }
    activeSelectObject($e);
}

function refreshProperties() {
    window.parent.refreshProperties();
}

$(function () {
    // redirect selectObject to selectColumn
    window['activeSelectObject'] = window['selectObject'];
    window['selectObject'] = selectColumn;

    tflow.ready = true;
    clientReady();
});