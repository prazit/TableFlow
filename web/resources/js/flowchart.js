function hideLines() {
    for (var i = 0; i < lines.length; i++) {
        lines[i].hide();
    }
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
        console.log('register(selectableId:' + selectable + ') isTable=' + isTable);

        /*call register only come from update function of selectable object*/
        updateLines([
            {name: 'selectableId', value: selectable}
        ]);

        /*change selectable-id to selectable-object*/
        selectable = $('input[name=selectableId][value=' + selectable + ']').parent();

        if (isTable) {
            var child = $(selectable).find('.selectable');
            console.log('has '+ child.length +' child')
            register(child);
        }
    }

    selectable.on('click', function (ev) {
        $('.active').removeClass('active');

        var e = $(ev.currentTarget);
        e.addClass('active');

        var id = e.find('input[name=selectableId]').attr('value');
        var parentWindow = window.parent;
        parentWindow.setActiveObj([
            {name: 'selectableId', value: id}
        ]);
        parentWindow.scrollToActive(e);

        /*stopPropagation*/
        return false;
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

    /*make selectable objects*/
    register($('.selectable'));

    /*need to show after all works*/
    $('.flow-chart').css('visibility', 'visible');

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
