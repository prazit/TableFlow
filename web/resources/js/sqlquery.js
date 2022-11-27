'use strict';

function hideTableButton(e) {
    var $e = $(e).parent('div');

    /*hide button pad*/
    Appanel.chains($e, "ani-hide-button:0.5,hidden");

    tflow.queryNeedRefresh = true;

    /*refresh query when close the select-table-panel*/
    var $uiGrid = $e.parent('.ui-g'),
        $buttons = $uiGrid.find('.ui-g-3'),
        $hiddens = $uiGrid.find('.ui-g-3.hidden'),
        remainButtons = $buttons.length - $hiddens.length;
    if (remainButtons === 0) {
        tflow.queryAddTableButtonText.innerText = tflow.queryOpenTableText;
        showTableList(false);
    }
}

function toggleTableList(e, openTableText, closeTableText) {
    var $tablePanel = $('.query').find('.select-table-panel'),
        isHidden = $tablePanel.hasClass('hidden');
    showTableList(isHidden);
    tflow.queryOpenTableText = openTableText;
    tflow.queryAddTableButtonText = $(e).find('.ui-menuitem-text')[0];
    tflow.queryAddTableButtonText.innerText = isHidden ? closeTableText : openTableText;
}

function showTableList(show) {
    if (show === undefined) show = true;

    var $tablePanel = $('.query').find('.select-table-panel'),
        isHidden = $tablePanel.hasClass('hidden');

    if (show && isHidden) {
        Appanel.chains($tablePanel, "-hidden,ani-open-query-table-list:0.5");
    } else if (!show && !isHidden) {
        Appanel.chains($tablePanel, "ani-close-query-table-list:0.5,hidden");
        if (tflow.queryNeedRefresh !== undefined && tflow.queryNeedRefresh) {
            PF('tabview').select(0);
        }
    }
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

function sortTableColumns(tableId, byName, maxFirst) {
    console.debug('sortTableColumns( tableId:', tableId, ', byName:', byName, ', maxFirst:', maxFirst, ' )');

    sortColumns([
        {name: 'byName', value: byName},
        {name: 'maxFirst', value: maxFirst},
        {name: 'tableId', value: tableId}
    ]);

    updateEmByClass('qt' + tableId, function () {
        selectableHandle($('.qt' + tableId).find('.selectable'));
    });
}

function selectTableColumns(tableId, selected) {
    console.debug('selectTableColumns(tableId:', tableId, ', selected:', selected, ')');

    selectQueryColumn([
        {name: 'columnId', value: tableId},
        {name: 'selected', value: selected}
    ]);

    updateEmByClass('qt' + tableId, function () {
        selectableHandle($('.qt' + tableId).find('.selectable'));
    });
}

function selectColumn($e) {
    if ($e.jquery !== undefined) {
        if ($e.hasClass('column')) {
            var selectedClass = 'selected',
                selected = !$e.hasClass(selectedClass),
                columnId = $e.find('.hidden').attr('id');

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