function hideLines() {
    /*for (var i = 0; i < lines.length; i++) {
        lines[i].hide();
    }*/
}

function showLines() {
    for (var i = 0; i < lines.length; i++) {
        lines[i].position();
        lines[i].show();
    }
}

/**
 * @param selectable = jQuery-Element or Selectable-ID
 */
function register(selectable) {
    if (selectable.jquery === undefined) {
        var isTable = selectable.startsWith('dt');

        /*call register only come from update function of selectable object*/
        updateLines([
            {name: 'selectableId', value: selectable}
        ]);

        /*change selectable-id to selectable-object*/
        selectable = $('input[name=selectableId][value=' + selectable + ']').parents('.selectable');

        if (isTable) {
            register(selectable.find('.selectable'));
        }
    }

    $(selectable).on('click', function (ev) {
        console.log('click on selectable object');
        selectObject($(ev.currentTarget));
        return false;
    });
}

function selectObject($e) {
    $('.active').removeClass('active');
    $e.addClass('active');

    var parentWindow = window.parent;
    parentWindow.setActiveObj([
        {name: 'selectableId', value: getSelectableId($e)}
    ]);
    parentWindow.scrollToObj($e);
}

var draggableCount = 0;

function dragableEnter($dragTarget, $dropTargets) {
    console.log('dragableEnter');

    var bgColor = $dragTarget.css('background-color'),
        isDraggable = $dragTarget.hasClass('draggable'),
        offset = $dragTarget.first().offset();

    $draggable.buttons.removeLine.toggle($dragTarget.hasClass('remove-line'));
    $draggable.buttons.extractData.toggle($dragTarget.hasClass('extract-data'));
    $draggable.buttons.draggable.toggle(isDraggable)
        .css('background-color', bgColor)
        .css('color', bgColor)
        .on('click', function (ev) {
            /* cancel the event 'click on selectable object' */
            return false;
        });

    /*position need to calculate after the size of buttons is not change*/
    var position = {
        /*--spot at center of draggable stack--*/
        left: (offset.left - (($draggable.outerWidth() - $dragTarget.outerWidth()) / 2)),
        top: (offset.top - (($draggable.outerHeight() - $dragTarget.outerHeight()) / 2))
        /*--spot at top of draggable stack--*/
        /*left: (offset.left - (($draggable.outerWidth() - $dragTarget.outerWidth()) / 2)),
        top: (offset.top - 10)*/
    };

    if (isDraggable) {
        $draggable.show().dragging = Object.assign(new PlainDraggable($draggable[0], Object.assign({
                snap: true,
                autoScroll: true,
                width: 9000,
                onMove: function () {
                    if (this.line != null) {
                        var x = window.scrollX,
                            y = window.scrollY;
                        window.scrollTo(0, 0);
                        this.line.position();
                        window.scrollTo(x, y);
                    }
                },
                onDragStart: function (pos) {
                    console.log('onDragStart: draggableId=' + this.draggableId);
                    this.startLog = true;

                    $draggable.buttons.removeLine.hide();
                    $draggable.buttons.extractData.hide();
                    $dropTargets.addClass('droppable');

                    this.line = new LeaderLine(this.dragTarget[0], this.element, this.lineOptions);
                    this.onMove();
                },
                onDrag: function (pos) {
                    var offset = $draggable.offset();

                    $draggable.hide();
                    var x = offset.left - window.scrollX,
                        y = offset.top - window.scrollY,
                        e = document.elementFromPoint(x, y);
                    $draggable.show();

                    if (this.startLog) {
                        console.log('onDragStart(left:' + x + ', top:' + y + ')');
                        this.startLog = false;
                    }

                    if (this.dropTarget != null) {
                        this.dropTarget.removeClass('drop-target');
                    }

                    var $e = $(e);
                    if (!$e.hasClass('droppable')) {
                        var $parent = $e.parents('.droppable');
                        if ($parent.length > 0) {
                            $e = $parent;
                        } else {
                            $e = $(undefined);
                        }
                    }
                    if ($e.length > 0) {
                        this.dropTarget = $e;
                        this.dropTarget.addClass('drop-target');
                    } else {
                        this.dropTarget = null;
                    }
                },
                onDragEnd: function (pos) {
                    console.log('onDragEnd: draggableId=' + this.draggableId);
                    this.line.remove();
                    this.line = null;

                    $draggable.dragging.remove();
                    $draggable.dragging = undefined;
                    $draggable.hide();
                    $('.droppable').removeClass('droppable');

                    console.log('onDragEnd: \'' + $draggable.attr('id') + '\' dropped at pos(x:' + pos.left + ', y:' + pos.top + ') on target(' + (this.dropTarget !== null ? this.dropTarget.attr('id') : 'none') + ')');

                    if (this.startPlug != null && this.endPlug != null) {
                        if (this.startPlug == null) {
                            this.startPlug = this.dropTarget;
                        } else {
                            this.endPlug = this.dropTarget;
                        }
                        addLink(this.startPlug, this.endPlug);
                    }

                    /*TODO: need to find from this point, why the flowchart has refreshed!*/
                }
            }, position)),
            {
                draggableId: ++draggableCount,
                startLog: false,
                startPlug: $dragTarget.hasClass('start-plug') ? $dragTarget : null,
                endPlug: $dragTarget.hasClass('end-plug') ? $dragTarget : null,
                line: null,
                lineOptions: dgLine,
                dragTarget: $dragTarget,
                dropTarget: null
            });
        $draggable.dragging.lineOptions.color = bgColor;
    } else {
        $draggable.show().offset(position);
    }
}

function addLink($startPlug, $endPlug) {
    console.log('--:addLink():--');
    console.log('startPlug:' + $startPlug);
    console.log('endPlug:' + $endPlug);
}

function addLinkReal($startPlug, $endPlug) {
    window.parent.addLine([
        {name: "startSelectableId", value: getSelectableId($startPlug)},
        {name: "endSelectableId", value: getSelectableId($endPlug)}
    ]);
}

function getSelectableId($e) {
    return $e.find('input[name=selectableId]').attr('value');
}

function draggableStartup() {

    /*TODO: need more type of Draggable-button for any type of plug*/
    /*use styleClass to knows any plug already connected or not.
     * no-connection
     * connected
     * selector for data-source without connection = .data-source .start-plug.no-connection
     **/

    var draggableElement = document.getElementById('plugButtons');
    $draggable = $(draggableElement).hide();
    $draggable.buttons = {
        removeLine: $draggable.find('#removeLine'),
        extractData: $draggable.find('#extractData'),
        draggable: $draggable.find('#draggable')
    };

    /*TODO: flow-chart element need to resize to cover all 3 sections.*/
    $('.flow-chart').css('width', '2000px');

    /*Mapping between draggable selector and droppable selector.*/
    var draggableList = [
        /*data-source to data-file*/
        {draggable: '.data-source.local .start-plug', droppable: '.data-file.local:has(.end-plug.no-connection)'},
        {draggable: '.data-file.local .end-plug.no-connection', droppable: '.data-source.local'},
        {draggable: '.data-source.sftp .start-plug', droppable: '.data-file.sftp:has(.end-plug.no-connection)'},
        {draggable: '.data-file.sftp .end-plug.no-connection', droppable: '.data-source.sftp'},
        {draggable: '.data-source.database .start-plug', droppable: '.data-file.database:has(.end-plug.no-connection)'},
        {draggable: '.data-file.database .end-plug.no-connection', droppable: '.data-source.database'},

        /*TODO: data-file to data-table*/

        /*TODO: data-table to data-table*/

        /*column to column*/
        {draggable: '.string.column .start-plug.no-connection', droppable: '.string.column:has(.end-plug.no-connection)'},
        {draggable: '.integer.column .start-plug.no-connection', droppable: '.integer.column:has(.end-plug.no-connection)'},
        {draggable: '.decimal.column .start-plug.no-connection', droppable: '.decimal.column:has(.end-plug.no-connection)'},
        {draggable: '.date.column .start-plug.no-connection', droppable: '.date.column:has(.end-plug.no-connection)'}

        /*TODO: column to column-fx*/

    ];

    /*move draggableElement to cover a plug at the mouse position*/
    $(draggableList).each(function (i, e) {
        $(e.draggable).on('mouseenter', function (ev) {
            if ($draggable.dragging === undefined) {
                dragableEnter($(ev.currentTarget), $(e.droppable));
            }
        });
    });
    $draggable.on('mouseleave', function (ev) {
        if (/*avoid error when drag with high speed move*/$draggable.dragging !== undefined && $draggable.dragging.line == null) {
            $draggable.dragging.remove();
            $draggable.dragging = undefined;
            $draggable.hide();
        }
    });

}

function startup() {
    LeaderLine.positionByWindowResize = false;

    /*make more height*/
    var parentWindow = window.parent;
    $('.section').each(function (i, e) {
        $(e).height(parentWindow.outerHeight * 10);
    })

    /*zoom to current value*/
    parentWindow.zoomStart();
    parentWindow.zoomEnd();

    /*make selectable objects*/
    register($('.selectable'));

    /*need to show after all works*/
    $('.flow-chart').css('visibility', 'visible');

    draggableStartup();
}

var lines = [];
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
    dgLine = Object.assign({color: 'silver', path: 'fluid'}, pLine);
var $draggable;

/*
// PlainDraggable SAMPLE
window.addEventListener('load', function () {
    'use strict';

    var ELEMENT_SIZE = 60,
        startElement = document.getElementById('ex-191-0'),
        line = new LeaderLine(startElement, document.getElementById('ex-191-1'), {
            startPlug: 'disc',
            endPlug: 'disc'
        }),
        islandBBox = document.getElementById('ex-191-island').getBoundingClientRect();

    islandBBox = {
        left: islandBBox.left + window.pageXOffset - ELEMENT_SIZE,
        top: islandBBox.top + window.pageYOffset - ELEMENT_SIZE,
        right: islandBBox.right + window.pageXOffset,
        bottom: islandBBox.bottom + window.pageYOffset
    };

    new PlainDraggable(startElement, {
        onDrag: function (newPosition) {
            if (newPosition.left > islandBBox.left && newPosition.left < islandBBox.right &&
                newPosition.top > islandBBox.top && newPosition.top < islandBBox.bottom) {
                if (this.left <= islandBBox.left) {
                    newPosition.left = islandBBox.left;
                } else if (this.left >= islandBBox.right) {
                    newPosition.left = islandBBox.right;
                }
                if (this.top <= islandBBox.top) {
                    newPosition.top = islandBBox.top;
                } else if (this.top >= islandBBox.bottom) {
                    newPosition.top = islandBBox.bottom;
                }
            }
        },
        onMove: function () {
            line.position();
        },
        zIndex: false
    });

});
*/
