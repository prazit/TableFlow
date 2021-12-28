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

$(function () {

    var parentWindow = window.parent;
    parentWindow.zoomStart();
    parentWindow.zoomEnd();

    $('.selectable').on('click', function (ev) {
        /*TODO: remove clickEvent variable below, test only*/
        clickEvent = ev;
        var e = $(ev.currentTarget);
        var id = e.find('input[name=selectableId]').attr('value');
        $('.active').removeClass('active');
        e.addClass('active');
        setActiveObj([
            {name: 'selectableId', value: id}
        ]);

        if (e.hasClass('column')) {
            return false;
        }
    });

});

var lines = [];
var pLine = {
        outline: false,
        size: 1,

        startSocket: 'auto',
        startPlug: 'behind',

        endSocket: 'auto',
        /*endPlug: 'behind'*/
        endPlugSize: 2
    },
    tLine = Object.assign({color: 'gray', path: 'fluid'}, pLine),
    sLine = Object.assign({color: 'red', path: 'fluid'}, pLine),
    iLine = Object.assign({color: 'green', path: 'fluid'}, pLine),
    dLine = Object.assign({color: 'blue', path: 'fluid'}, pLine),
    dtLine = Object.assign({color: 'yellow', path: 'fluid'}, pLine);

var clickEvent;

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
