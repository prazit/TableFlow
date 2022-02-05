'use strict';

function refreshFlowChart() {
    console.log(document.location);
    document.location += '?refresh=2';
    console.log(document.location);
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

function hideLines() {
    /*for (var i = 0; i < lines.length; i++) {
        lines[i].hide();
    }*/
}

function showLines() {
    for (var i = 0; i < lines.length; i++) {
        if (lines[i] == null) continue;
        lines[i].position();
        lines[i].show();
    }
}

function addLink($startSelectable, $endSelectable, $activeObject) {
    $('.active').removeClass('active');

    addLine([
        {name: "startSelectableId", value: getSelectableId($startSelectable)},
        {name: "endSelectableId", value: getSelectableId($endSelectable)}
    ]);

    noActiveScrollTo($activeObject);
}

function buttonPerform(remoteFunction) {
    var dragTarget = $draggable.dragging.dragTarget,
        selectable = getSelectable(dragTarget),
        id = getSelectableId(selectable),
        isStartPlug = dragTarget.hasClass('start-plug');

    dragTarget.off('mouseenter');
    $draggable.trigger('mouseleave');

    $('.active').removeClass('active');

    window[remoteFunction]([
        {name: "selectableId", value: id},
        {name: "startPlug", value: isStartPlug}
    ]);

    /*TODO: issue: remove button still shown on the plug*/

    draggableHandle();

    noActiveScrollTo(selectable);
}

function buttonHandle(buttonName) {
    $draggable.buttons[buttonName] = $draggable.find('#' + buttonName).on('click', function (ev) {
        tflow.lastEvent = ev;
        buttonPerform(buttonName);
        ev.stopPropagation();
        return false; /* cancel the event 'click on selectable object' */
    })
}

function updateComplete(selectableId) {
    selectableHandle(selectableId);
    draggableHandle();
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
        updateLines([
            {name: 'selectableId', value: selectable}
        ]);

        /*change selectable-id to selectable-object*/
        selectable = $('input[name=selectableId][value=' + selectable + ']').parents('.selectable');

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
}

function setActiveObj($e) {
    /*this function take effect to client side only*/

    if ($e.hasClass('step')) {
        var thisActive = Date.now(),
            diff = thisActive - tflow.lastChangeActive;
        if (tflow.lastChangeActive != null && diff < tflow.allowChangeActiveDuration) {
            console.log('detected multiple set active at once (duration:' + diff + ', allowDuration:' + tflow.allowChangeActiveDuration + ') cancel setActiveObj(object:' + $e.attr('class') + ')');
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
    setActiveObj($e);

    window.parent.setActiveObj([
        {name: 'selectableId', value: getSelectableId($e)}
    ]);
}

function noActiveScrollTo($activeObject) {
    var $active = $('.active');
    if ($active.length === 0) {
        selectObject($activeObject);
    } else {
        scrollToObj($active);
    }
}

function lineStart() {
    /*start of line creation*/
    lines.lineScroll = {
        left: window.scrollX,
        top: window.scrollY
    };
    window.scrollTo(0, 0);
}

function lineEnd() {
    /*end of line creation*/
    window.scrollTo(lines.lineScroll);
}

function draggableEnter($dragTarget, $droppable) {
    console.log('draggableEnter(dragTarget:' + getSelectableId(getSelectable($dragTarget)) + '.' + $dragTarget.attr('id') + ')');

    var bgColor = $dragTarget.css('background-color'),
        isDraggable = $dragTarget.hasClass('draggable'),
        offset = $dragTarget.first().offset();

    $draggable.buttons.removeLine.toggle($dragTarget.hasClass('remove-line')).css('background-color', bgColor);
    $draggable.buttons.extractData.toggle($dragTarget.hasClass('extract-data')).css('background-color', bgColor);
    $draggable.buttons.transferData.toggle($dragTarget.hasClass('transfer-data')).css('background-color', bgColor);
    $draggable.buttons.draggable.toggle(isDraggable).css('background-color', bgColor).css('color', bgColor);
    $dragTarget.hasClass('locked') ? $draggable.addClass('locked') : $draggable.removeClass('locked');

    /*position need to calculate after the size of buttons is not change*/
    var position = {
            /*--spot at center of draggable stack--*/
            left: (offset.left - (($draggable.outerWidth() - $dragTarget.outerWidth()) / 2)),
            top: (offset.top - (($draggable.outerHeight() - $dragTarget.outerHeight()) / 2))
            /*--spot at top of draggable stack--*/
            /*left: (offset.left - (($draggable.outerWidth() - $dragTarget.outerWidth()) / 2)),
            top: (offset.top - 10)*/
        },
        dragging = {
            draggableId: ++$draggable.count,
            startPlug: $dragTarget.hasClass('start-plug') ? getSelectable($dragTarget) : null,
            endPlug: $dragTarget.hasClass('end-plug') ? getSelectable($dragTarget) : null,
            line: null,
            lineOptions: dgLine,
            droppable: $droppable,
            dragTarget: $dragTarget,
            dropTarget: null
        };

    console.log(dragging);

    if (isDraggable) {
        $draggable.show().dragging = Object.assign(new PlainDraggable($draggable[0], Object.assign({
            snap: true,
            autoScroll: true,
            width: 9000,
            onMove: function () {
                if (Boolean(this.line)) {
                    var x = window.scrollX,
                        y = window.scrollY;
                    window.scrollTo(0, 0);
                    this.line.position();
                    window.scrollTo(x, y);
                }
            },
            onDragStart: function (pos) {
                $draggable.buttons.draggable.hide();
                $draggable.buttons.removeLine.hide();
                $draggable.buttons.extractData.hide();

                $draggable.addClass('dragging');
                $(this.droppable).addClass('droppable');

                this.line = new LeaderLine(this.dragTarget[0], this.element, this.lineOptions);
                this.onMove();
            },
            onDrag: function (pos) {
                var offset = $draggable.offset();

                $draggable.hide();
                var x = offset.left - window.scrollX,
                    y = offset.top - window.scrollY,
                    $e = $(document.elementFromPoint(x, y));
                $draggable.show();

                if (this.dropTarget != null) {
                    this.dropTarget.removeClass('drop-target');
                }

                if (!$e.hasClass('droppable')) {
                    $e = $e.parent('.droppable');
                }

                if ($e.length > 0) {
                    this.dropTarget = getSelectable($e);
                    this.dropTarget.addClass('drop-target');
                    $draggable.removeClass('dragging').addClass('dropping');
                } else {
                    this.dropTarget = null;
                    $draggable.removeClass('dropping').addClass('dragging');
                }

                /*TODO: need to scope the draggable area here*/
            },
            onDragEnd: function (pos) {
                this.line.remove();
                this.line = null;

                $draggable.removeClass('dragging').removeClass('dropping');
                $('.droppable').removeClass('droppable');

                //console.log('onDragEnd: dropped on target(' + (this.dropTarget !== null ? getSelectableId(this.dropTarget) : 'null') + ')');
                if (this.dropTarget != null) {
                    this.dropTarget.removeClass('drop-target');

                    if (this.startPlug == null) {
                        this.startPlug = this.dropTarget;
                    } else {
                        this.endPlug = this.dropTarget;
                    }

                    //console.log('startPlug=' + (this.startPlug !== null ? getSelectableId(this.startPlug) : 'null'));
                    //console.log('endPlug=' + (this.endPlug !== null ? getSelectableId(this.endPlug) : 'null'));

                    if (this.startPlug != null && this.endPlug != null) {
                        addLink(this.startPlug, this.endPlug, this.dropTarget);
                    }
                }

                $draggable.dragging.remove();
                $draggable.dragging = undefined;
                $draggable.hide();
            }
        }, position)), dragging);
        $draggable.dragging.lineOptions.color = bgColor;
    } else {
        $draggable.show().offset(position).dragging = dragging;
    }
}

function draggableHandle() {
    if (!tflow.ready) return;

    $draggable.draggableList.each(function (i, e) {
        var $plug = $('body').find(e.draggable);
        if ($plug.length === 0) return;

        $plug.off('mouseenter').on('mouseenter', function (ev) {
            if ($draggable.dragging === undefined) {
                tflow.lastEvent = ev;
                draggableEnter($(ev.currentTarget), $(e.droppable));
            }
        });
    });
}

function draggableStartup() {
    var draggableElement = document.getElementById('plugButtons');
    $draggable = $(draggableElement).hide();
    $draggable.count = 0;
    $draggable.buttons = {
        draggable: $draggable.find('#draggable').on('click', function (ev) {
            tflow.lastEvent = ev;
            ev.stopPropagation();
            return false; /* cancel the event 'click on selectable object' */
        })
    };
    buttonHandle('removeLine');
    buttonHandle('extractData');
    buttonHandle('transferData');

    /*TODO: flow-chart element need to resize to cover all 3 sections.*/
    $('.flow-chart').css('width', '2000px');

    /*Mapping between draggable selector and droppable selector used in draggableRefreshed().*/
    $draggable.draggableList = $([

        /*-- data-source to data-file --*/
        {draggable: '.data-source.local .start-plug', droppable: '.data-file.local:has(.end-plug.no-connection)'},
        {draggable: '.data-file.local .end-plug', droppable: '.data-source.local'},

        {draggable: '.data-source.sftp .start-plug', droppable: '.data-file.sftp:has(.end-plug.no-connection)'},
        {draggable: '.data-file.sftp .end-plug', droppable: '.data-source.sftp'},

        {draggable: '.data-source.database .start-plug', droppable: '.data-file.database:has(.end-plug.no-connection)'},
        {draggable: '.data-file.database .end-plug', droppable: '.data-source.database'},

        /*-- data-file to data-table, TODO: need action AddDataTable after ExtractData --*/
        {draggable: '.data-file .start-plug', droppable: '.data-table .ui-panel-titlebar:has(.end-plug.no-connection)'},
        {draggable: '.data-table .ui-panel-titlebar .end-plug', droppable: '.data-file:has(.start-plug.no-connection)'},

        /*TODO: data-table to data-table, need action AddTransformTable*/

        /*column to column*/
        {draggable: '.string.column .start-plug', droppable: '.string.column:has(.end-plug.no-connection)'},
        {draggable: '.integer.column .start-plug', droppable: '.integer.column:has(.end-plug.no-connection)'},

        {draggable: '.decimal.column .start-plug', droppable: '.decimal.column:has(.end-plug.no-connection)'},
        {draggable: '.date.column .start-plug', droppable: '.date.column:has(.end-plug.no-connection)'}

        /*TODO: column to column-fx, need action AddTransformTable*/

    ]);

    $draggable.on('mouseleave', function (ev) {
        //console.log('$draggable.mouseleave triggered');
        if (/*avoid error when drag with high speed move*/$draggable.dragging !== undefined && $draggable.dragging.line == null) {
            tflow.lastEvent = ev;
            if ($draggable.dragging.element !== undefined) {
                $draggable.dragging.remove();
            }
            $draggable.dragging = undefined;
            $draggable.hide();
        }
    });
}

function startup() {
    /*make more height*/
    var parentWindow = window.parent;
    $('.section').each(function (i, e) {
        $(e).height(parentWindow.outerHeight * 10);
    })

    /*zoom to current value*/
    parentWindow.zoomStart();
    parentWindow.zoomEnd();

    draggableStartup();

    /*need to show after all works*/
    $('.flow-chart').css('visibility', 'visible');

    /*-------*/

    tflow.ready = true;

    draggableHandle();

    /*make selectable objects*/
    selectableHandle($('.selectable'));
}

var pLine = {
        outline: false,
        size: 1,

        startSocket: 'right',
        startPlug: 'behind',

        endSocket: 'left',
        /*endPlug: 'behind'*/
        endPlugSize: 2
    },
    tLine = Object.assign({color: 'gray', path: 'fluid'}, pLine),
    sLine = Object.assign({color: 'red', path: 'fluid'}, pLine),
    iLine = Object.assign({color: 'green', path: 'fluid'}, pLine),
    dLine = Object.assign({color: 'blue', path: 'fluid'}, pLine),
    dtLine = Object.assign({color: 'yellow', path: 'fluid'}, pLine),
    dgLine = Object.assign(pLine, {
        color: 'silver', path: 'fluid', startSocket: 'auto', endSocket: 'auto', size: 2
    }),
    lines = [],
    $draggable,
    tflow = {
        ready: false,
        lastEvent: 'load',

        allowChangeActiveDuration: 1000,
        lastChangeActive: Date.now()
    };