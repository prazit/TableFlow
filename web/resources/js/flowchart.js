'use strict';

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

    noActiveScrollTo(selectable);

    /*must use postUpdate to void issue of remove-button still shown on the plug.*/
    postUpdate(draggableHandle);
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
    doPostUpdate();
}

function postUpdate(func) {
    var i = tflow.postUpdate.length;
    tflow.postUpdate[i] = func;
}

function doPostUpdate() {
    $(tflow.postUpdate).each(function (i, e) {
        e();
    });
    tflow.postUpdate = [];
}

function tableAction(remoteFunction, selectableId) {
    window[remoteFunction]([
        {name: "selectableId", value: selectableId}
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

function draggableEnter($dragTarget, $droppable) {
    var bgColor = $dragTarget.css('background-color'),
        isDraggable = $dragTarget.hasClass('draggable'),
        offset = $dragTarget.first().offset(),
        removeButtonTip = $dragTarget.find('input[name=removeButtonTip]');

    $draggable.buttons.removeLine.toggle($dragTarget.hasClass('remove-line')).css('background-color', bgColor);
    $draggable.buttons.removeLineTip[0].innerText = (removeButtonTip.length > 0) ? removeButtonTip.attr('value') : $draggable.buttons.removeLineTipText;
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

                /*Notice: don't need to scope the draggable area but if you want, script here*/
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
    var draggableElement = document.getElementById('plugButtons'),
        removeButtonTip = $('#removeLineTip .ui-tooltip-text');

    $draggable = $(draggableElement).hide();
    $draggable.count = 0;
    $draggable.buttons = {
        draggable: $draggable.find('#draggable').on('click', function (ev) {
            tflow.lastEvent = ev;
            ev.stopPropagation();
            return false; /* cancel the event 'click on selectable object' */
        }),
        removeLineTip: removeButtonTip,
        removeLineTipText: removeButtonTip[0].innerText
    };
    buttonHandle('removeLine');
    buttonHandle('extractData');
    buttonHandle('transferData');

    /*flow-chart element need to resize to cover all 3 sections.*/
    /*var width = 0;
    $('.section').each(function (i, e) {
        // show it before get outerWidth
        width += $(e).outerWidth();
    });
    $('.flow-chart').css('width', width + 'px');*/
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

        /*-- data-file to data-table --*/
        {draggable: '.data-file .start-plug', droppable: '.data-table .ui-panel-titlebar:has(.end-plug.no-connection)'},
        {draggable: '.data-table .ui-panel-titlebar .end-plug', droppable: '.data-file:has(.start-plug.no-connection)'},

        /*-- data-table to data-table --*/
        {draggable: '.data-table .start-plug', droppable: '.data-table .ui-panel-titlebar:has(.end-plug.no-connection)'},

        /*column to column, columnfx to column*/
        {draggable: '.string.column .start-plug', droppable: '.string.column:has(.end-plug.no-connection)'},
        {draggable: '.string.column .end-plug', droppable: '.string.column:has(.start-plug)'},

        {draggable: '.integer.column .start-plug', droppable: '.integer.column:has(.end-plug.no-connection)'},
        {draggable: '.integer.column .end-plug', droppable: '.integer.column:has(.start-plug)'},

        {draggable: '.decimal.column .start-plug', droppable: '.decimal.column:has(.end-plug.no-connection)'},
        {draggable: '.decimal.column .end-plug', droppable: '.decimal.column:has(.start-plug)'},

        {draggable: '.date.column .start-plug', droppable: '.date.column:has(.end-plug.no-connection)'},
        {draggable: '.date.column .end-plug', droppable: '.date.column:has(.start-plug)'}
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
    });

    /*zoom to current value*/
    parentWindow.zoomStart();
    parentWindow.zoomEnd();

    /*Notice: support for first load with zooming factor to show lines again after 1 second*/
    /*if (parentWindow.tflow.isFirstFlow) {
        parentWindow.tflow.isFirstFlow = false;
        tflow.postStartUp = setTimeout(function () {
            clearTimeout(tflow.postStartUp);
            lineStart();
            lineEnd();
        }, 1000);
    }*/

    draggableStartup();

    /*need to show after all works*/
    $('.flow-chart').css('visibility', 'visible');

    /*-------*/

    tflow.ready = true;

    draggableHandle();

    /*make selectable objects*/
    selectableHandle($('.selectable'));

    /**/
    parentWindow.refreshProperties();
}

var pLine = {
        outline: false,

        startSocket: 'right',
        startPlug: 'behind',

        endSocket: 'left',
        /*endPlug: 'behind'*/
        endPlugSize: 2
    },
    tLine = Object.assign({color: 'gray', path: 'fluid', size: 2}, pLine),
    cLine = Object.assign({path: 'straight', size: 1, dash: {len: 7, gap: 7}}, pLine),
    sLine = Object.assign({color: 'red'}, cLine),
    iLine = Object.assign({color: 'green'}, cLine),
    dLine = Object.assign({color: 'blue'}, cLine),
    dtLine = Object.assign({color: 'yellow'}, cLine),
    dgLine = Object.assign(cLine, {
        color: 'silver', startSocket: 'auto', endSocket: 'auto', size: 2
    }),
    $draggable,
    word = {
        /*string constants*/
    };
