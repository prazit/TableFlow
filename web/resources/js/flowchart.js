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
        selectObject($(ev.currentTarget));
        return false;
    });
}

function selectObject($e) {
    $('.active').removeClass('active');
    $e.addClass('active');

    var id = $e.find('input[name=selectableId]').attr('value');
    var parentWindow = window.parent;
    parentWindow.setActiveObj([
        {name: 'selectableId', value: id}
    ]);
    parentWindow.scrollToObj($e);
}

function dragableEnter($dragTarget, dropTargetSelector) {
    var offset = $dragTarget.first().offset();

    $draggableJQ.first().show();

    draggable = new PlainDraggable($draggableJQ[0], {
        snap: true,
        autoScroll: true,
        width: 9000,
        left: (offset.left - (($draggableJQ.outerWidth() - $dragTarget.outerWidth()) / 2)),
        top: (offset.top - (($draggableJQ.outerHeight() - $dragTarget.outerHeight()) / 2)),
        /*-- custom --*/
        startLog: false,
        startPlug: $dragTarget.hasClass('start-plug') ? $dragTarget : null,
        endPlug: $dragTarget.hasClass('end-plug') ? $dragTarget : null,
        onDragStart: function (pos) {
            this.startLog = true;
            dragging = true;
            $(dropTargetSelector).addClass('droppable');
        },
        onDrag: function (pos) {
            var offset = $draggableJQ.offset();

            $draggableJQ.hide();
            var x = offset.left - window.scrollX,
                y = offset.top - window.scrollY,
                e = document.elementFromPoint(x, y);
            $draggableJQ.show();

            if (this.startLog) {
                console.log('onDragStart(left:' + x + ', top:' + y + ')');
                this.startLog = false;
            }

            if ($dropTarget !== undefined) {
                $dropTarget.removeClass('drop-target');
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
                $dropTarget = $e;
                $dropTarget.addClass('drop-target');
            } else {
                $dropTarget = undefined;
            }
        },
        onDragEnd: function (pos) {
            dragging = false;
            if (this.startPlug == null) {
                this.startPlug = $dropTarget;
            } else {
                this.endPlug = $dropTarget;
            }

            $draggableJQ.hide();
            $('.droppable').removeClass('droppable');

            console.log('onDragEnd: \'' + $draggableJQ.attr('id') + '\' dropped at pos(x:' + pos.left + ', y:' + pos.top + ') on target(' + ($dropTarget !== undefined ? $dropTarget.attr('id') : 'none') + ')');

            addLink(this.startPlug, this.endPlug);
        }
    });
}

function addLink($startPlug, $endPlug) {
    /*TODO: need to find selectableId of $startPlug*/
    /*TODO: need to find selectableId of $endPlug*/
    /*TODO: send them to server addLine function*/
}

function draggableStartup() {
    var draggableElement = document.getElementById('android');
    $draggableJQ = $(draggableElement).hide();

    /*TODO: flow-chart element need to resize to cover all 3 sections.*/
    $('.flow-chart').css('width', '2000px');

    /*Mapping between draggable selector and droppable selector.*/
    var draggableList = [
        {draggable: '.start-plug', droppable: '.column'},
        {draggable: '.end-plug', droppable: '.data-source'}
    ]

    /*move draggableElement to cover a plug at the mouse position*/
    $(draggableList).each(function (i, e) {
        $(e.draggable).on('mouseenter', function (ev) {
            if (dragging) return;
            dragableEnter($(ev.currentTarget), $(e.droppable));
        });
    });
    /*$('.end-plug').on('mouseenter', function (ev) {
        if (dragging) return;
        mouseenter(ev, '.data-source');
    });*/
    $draggableJQ.on('mouseleave', function (ev) {
        if (dragging) return;
        $draggableJQ.hide();
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
    dtLine = Object.assign({color: 'yellow', path: 'fluid'}, pLine);
var $draggableJQ, draggable,
    dragging = false,
    $dropTarget;

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
