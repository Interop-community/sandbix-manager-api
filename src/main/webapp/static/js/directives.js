/* Directives */

angular.module('sandManApp.directives', []).directive('resize', function ($window) {
        return function (scope, element, attr) {

            var w = angular.element($window);
            scope.$parent.$watch(function () {
                return {
                    'h': w.height(),
                    'w': w.width()
                };
            }, function (newValue, oldValue) {
                scope.$parent.windowHeight = newValue.h;
                scope.$parent.windowWidth = newValue.w;

                scope.$parent.resizeWithOffset = function (offsetH) {

                    scope.$parent.$eval(attr.notifier);

                    return {
                        'height': (newValue.h - offsetH) + 'px'
                        //,'width': (newValue.w - 100) + 'px'
                    };
                };
            }, true);

            w.bind('resize', function () {
                scope.$parent.$apply();
            });
        }
}).directive('enterKey', function () {
        return function (scope, element, attrs) {
            element.bind("keydown keypress", function (event) {
                var key = typeof event.which === "undefined" ? event.keyCode : event.which;
                if(key === 13) {
                    scope.$apply(function (){
                        scope.$eval(attrs.enterKey);
                    });

                    event.preventDefault();
                }
            });
        };
    }).directive( 'tableHeaderInner', function() {
        return {
            link: function( scope, elem, attrs ) {
                scope.$watch(function () {
                        return {
                            width: elem.parent().width()
                        }
                    },
                    function( width ) {
                        elem.css({
                            width: elem.parent()[0].clientWidth + 'px'
                        });
                    }, //listener
                    true  //deep watch
                );
            }
        }
    }).directive('arrowSelector',['$document',function($document){
    return{
        restrict:'A',
        link:function(scope,elem,attrs,ctrl){
            $document.bind('keydown',function(e){
                    if(e.keyCode == 38){
                        scope.$parent.arrowUpDownResourceTable("up");
                        scope.$parent.$apply();
                        e.preventDefault();
                    }
                    if(e.keyCode == 40){
                        scope.$parent.arrowUpDownResourceTable("down");
                        scope.$parent.$apply();
                        e.preventDefault();
                    }
            });
        }
    };
}]).directive("scrollableTable", function () {
        return {
            restrict: 'E',
            templateUrl: 'static/js/templates/scrollableTable.html'
        };
    });
