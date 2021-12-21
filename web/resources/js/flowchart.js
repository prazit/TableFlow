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

var lines = [];
var curveLine = {
        path: 'fluid',
        outline: false,
        size: 1,

        startSocket: 'auto',
        startPlug: 'behind',

        endSocket: 'auto',
        endPlug: 'behind'
    },
    tLine = Object.assign({color: 'gray'}, curveLine),
    sLine = Object.assign({color: 'red'}, curveLine),
    iLine = Object.assign({color: 'green'}, curveLine),
    dLine = Object.assign({color: 'blue'}, curveLine),
    dtLine = Object.assign({color: 'yellow'}, curveLine);

function addLine(lineList) {
    $(lineList).each(function(e){
        lines[lines.length] = new LeaderLine(document.getElementById(e.startPlug), document.getElementById(e.endPlug), e.type);
    });
}

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
