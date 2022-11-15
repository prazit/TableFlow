'use strict';

var tflow = {
        ready: false,
        lastEvent: 'load',
        postUpdate: [],

        allowChangeActiveDuration: 1000,
        lastChangeActive: Date.now()
    }, lines = []
;

function blockScreen(text) {
    window.parent.blockScreen(text);
}

function unblockScreen() {
    window.parent.unblockScreen();
}

function noti() {
    window.parent.noti();
}

/*Notice: IMPORTANT: this is temporary function, remove this function when server-side functions are updated to refresh a room or a floor already.*/
function refreshFlowChart() {
    window.parent.refreshFlowChart();
}

function refreshStepList() {
    /*forwarding only*/
    window.parent.refreshStepList();
}

function updateEm(selectableId) {
    /*forwarding only*/
    window.parent.updateEm(selectableId);
}

function updateEmByClass(className) {
    var $property = $('.' + className);
    var id = $property.attr('id');
    tflow.postRefreshElement = function () {};
    refreshElement([
        {name: 'componentId', value: id}
    ]);
}


function getSelectableId($selectable) {
    return $selectable.find('input[name=selectableId]').attr('value');
}

function getSelectable($child) {
    if ($child.hasClass('selectable')) return $child;

    var $parent = $child.parent('.selectable').not('.step');
    if ($parent.length > 0) return $parent;

    var $parents = $child.parents('.selectable').not('.step');
    return $parents.first();
}

function getSelectableById(selectableId) {
    return $('input[name=selectableId][value=' + selectableId + ']').parents('.selectable');
}


/**
 * @param selectable = jQuery-Element or Selectable-ID
 */
function selectableHandle(selectable) {
    if (!tflow.ready) return;

    if (selectable.jquery === undefined) {
        /*selectable parameter is selectableId*/
        var isTable = selectable.startsWith('dt');

        /*call updateLines only when come from update function of selectable object*/
        if (window['updateLines'] != undefined) {
            updateLines([
                {name: 'selectableId', value: selectable}
            ]);
        }

        /*change selectable-id to selectable-object*/
        selectable = getSelectableById(selectable);

        if (isTable) {
            selectableHandle(selectable.find('.selectable'));
        }
    }

    $(selectable).off('click').on('click', function (ev) {
        if ($(ev.currentTarget).hasClass('step')) {
            /*to avoid invalid event after dragEnd (PlainDraggable)*/
            if (tflow.lastEvent.type === 'mouseover') {
                tflow.lastEvent = ev;
                return;
            }
        }
        tflow.lastEvent = ev;

        selectObject($(ev.currentTarget));

        ev.stopPropagation();
        return false;
    });

    /*reset TAB-Index to force recreate tab-index list in the editor.js.propertyCreated()*/
    window.parent.refreshTabIndex();
}

function setActiveObj($e) {
    /*this function take effect to client side only*/
    $e = $e.first();

    if ($e.hasClass('step')) {
        var thisActive = Date.now(),
            diff = thisActive - tflow.lastChangeActive;
        if (tflow.lastChangeActive != null && diff < tflow.allowChangeActiveDuration) {
            console.log('detected multiple set active at once (duration:' + diff + ', allowDuration:' + tflow.allowChangeActiveDuration + ', cancelledObject:' + $e.attr('class') + ')');
            return;
        }
    }

    /*set active*/
    /*console.log('setActiveObj:before(object:' + $e.attr('class') + ')');*/
    $('.active').removeClass('active');
    $e.addClass('active');
    window.parent.scrollToObj($e);
    tflow.lastChangeActive = Date.now();
    /*console.log('setActiveObj:after(object:' + $e.attr('class') + ')');*/
}

function selectObject($e) {
    var selectableId;
    if ($e.jquery === undefined) {
        selectableId = $e;
        $e = getSelectableById(selectableId);
    } else {
        selectableId = getSelectableId($e);
    }

    setActiveObj($e);

    window.parent.setActiveObj([
        {name: 'selectableId', value: selectableId}
    ]);
}

function hideLines() {
    tflow.needShowLines = true;
    /*for (var i = 0; i < lines.length; i++) {
        // lines[i] hide();
    }*/
}

function showLines() {
    if (!tflow.needShowLines) return;
    tflow.needShowLines = false;

    $(lines).each(function (i, line) {
        try {
            line.position();
            line.show();
        } catch (err) {
            /*nothing*/
        }
    });
}

function lineStart() {
    /*start of line creation*/
    lines.lineScroll = {
        enabled: tflow.ready,
        left: window.scrollX,
        top: window.scrollY
    };
    window.scrollTo(0, 0);
    hideLines();
}

function lineEnd() {
    /*end of line creation*/
    showLines();
    if (lines.lineScroll.enabled) window.scrollTo(lines.lineScroll);
}
