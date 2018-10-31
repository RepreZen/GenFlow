/**
 * Created with JetBrains WebStorm.
 * User: vipul-jain
 * Date: 3/1/14
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
  /**
   *  _obstacles - in layout the nodes are obstacles. link should avoid the overlapping of obstacles
   *  _cornerRound - is corner rounded.
   *  _layoutData - hold the all information of layout
   * */
d3.custom.layout.routing = function(){
    var _obstacles = [];
    var _cornerRound = false;
    var _layoutData = [];

    function routing(){
        return nodes;
    };

    /**
     * Set the obstacles for layout and set layout data .
     * */
    routing.setObstacles = function(value) {
        if (!arguments.length) return nodes;
        _obstacles = [];
        if(value.length > 0){
            for(n in value){
                _obstacles.push(value[n]);

                var _lObj = {};
                _lObj.id = value[n].id;
                _lObj.objecttype = value[n].objecttype;
                _lObj.start = {};
                _lObj.start.x = value[n].x;
                _lObj.start.y = value[n].y - 10;

                _lObj.end = {};
                _lObj.end.x = value[n].x + value[n].width + 30;
                _lObj.end.y = value[n].y + value[n].height ;

                _lObj.extra = {};
                _lObj.extra.top = 0;
                _lObj.extra.right = 0;
                _lObj.extra.bottom = 0;
                _lObj.extra.left = 0;

                _layoutData.push(_lObj);
            }
        }

        return this;
    };

    routing.isRoundCorner = function(value){
        if (!arguments.length) return _cornerRound;
        _cornerRound = value;
        return this;
    }

    routing.getLayoutData = function(){
        return _layoutData;
    }

    routing.drawPath = function(points){
        var path = "";
        function trun(_p1,p1,p2){
            console.log(p1,p2);
            if(p2){
                var y1 = parseInt(p1.y);
                var y2 = parseInt(p2.y);
                var x1 = parseInt(p1.x);
                var x2 = parseInt(p2.x);
                var a,b,c,d;
                a = b = c = d = 3;
                if(y1 != y2){
                  if(y1 <= y2){
                      console.log("turn down");
                      c = ( c * -1)
                      if(x1 < x2){
//                          d = ( d * -1)
                      }else{

                      }
                  }else{
                      console.log("turn up");
                      d = ( d * -1);
                      if(x1 < x2){

                      }else{

                      }
                  }
                    var string = "a"+ a +","+b +" 0 0 0 "+ c +","+d;
                    console.log(string);
                    path += "a"+ a +","+b +" 0 0 0 "+ c +","+d;
                }else{
                    if(_p1){
                        var _oldY = parseInt(_p1.y);
                        console.log(_oldY,y1);
                        if(_oldY != y1){
                            if(_oldY < y1){
                                d = ( d * -1);
                                if(x1 < x2){

                                }else{

                                }
                            }else{

                                c = ( c * -1);
                                if(x1 < x2){
//                                    console.log("123");
                                }else{
                                    d = ( d * -1);
                                }
                            }
                            path += "a"+ a +","+b +" 0 0 0 "+ c +","+d;
                        }
                    }

                }
            }
        }
        if(points.length > 0){
            var last = points.length-1;
            var _c;
            console.log(points);
            for(n in points){
              var _cPoint = parseInt(n);
              var p1 = points[_cPoint];
              var p2 = points[_cPoint+1];

              if(_cPoint == "0" ){
                  path += "M"+p1.x +","+p1.y +"H"+p2.x;

              }else{
                  if(p1.x == _c.x && p1.y != _c.y){
                      path +="V"+p1.y;
                  }else if(p1.x != _c.x && p1.y == _c.y){
                      path +="H"+p1.x;
                  }else{
                     //console.log("same case")
                  }
              }
              if(p2) trun(_c,p1,p2);
//              console.log(_c,p1);
              _c = p1;
            }
        }

//        console.log(path);
        return path;
    }

    /**
     * get path between source and target node
     * source - source node
     * target - target node
     * source_resource -  resource node or source
     * points - array of points on path
     * cx cy current point on layout
     * */
    routing.getPath = function(source,source_resource,target){
        var _source = source;
        var _target = target;
        var _points = [];
        var _currentNode = _source;
        var cx,cy;

        function getLayoutInfo(node){
            var _filterInfo = _layoutData.filter(function(d){
                return d.id == node.id;
            });
            if(_filterInfo.length > 0) return _filterInfo[0];
            return [];
        }

        function addPoint(x,y){
            var _obj = {};
            _obj.x = x+"";
            _obj.y = y+"";
            _points.push(_obj);
        }

        /**
         * find nearest node from current point
         * */
        function findNearNode(){
            var _nearNode = [];
            var _resourceNode = _layoutData.filter(function(d){
                return (d.objecttype == "ObjectResource" || d.objecttype == "CollectionResource")
                    && (d.id != source_resource.id)
                    && (d.id != _currentNode.id);
            });

            function addToNearNode(_Pnode,dis){
                var __flag = _nearNode.filter(function(d){
                    return d.node == _Pnode;
                });
                if(__flag.length > 0){
                    var _oldNode = __flag[0];
                    if(_oldNode.dis > dis){
                        _oldNode.dis = dis;
                    }
                }else{
                    var _obj = {};
                    _obj.node = _Pnode;
                    _obj.dis = dis;
                    _nearNode.push(_obj);
                }
            }

            var shortdis = 300;
            var p;

            //top left
            for(var intIndex=0; intIndex < _resourceNode.length; intIndex++){
                var dis = Math.sqrt( Math.pow( (_resourceNode[intIndex].start.x - cx),2) + Math.pow( (_resourceNode[intIndex].start.y - cy),2) );
                if (dis < shortdis){
                    addToNearNode(_resourceNode[intIndex],dis);
                }
            }

            //top -right
            for(var intIndex=0; intIndex < _resourceNode.length; intIndex++){
                var dis = Math.sqrt( Math.pow( (_resourceNode[intIndex].end.x - cx),2) + Math.pow( (_resourceNode[intIndex].start.y - cy),2) );
                if (dis < shortdis){
                    addToNearNode(_resourceNode[intIndex],dis);
                }
            }

            //bottom - left
            for(var intIndex=0; intIndex < _resourceNode.length; intIndex++){
                var dis = Math.sqrt( Math.pow( (_resourceNode[intIndex].start.x - cx),2) + Math.pow( (_resourceNode[intIndex].end.y - cy),2) );
                if (dis < shortdis){
                    addToNearNode(_resourceNode[intIndex],dis);
                }
            }
            //bottom - right
            for(var intIndex=0; intIndex < _resourceNode.length; intIndex++){
                var dis = Math.sqrt( Math.pow( (_resourceNode[intIndex].end.x - cx),2) + Math.pow( (_resourceNode[intIndex].end.y - cy),2) );
                if (dis < shortdis){
                    addToNearNode(_resourceNode[intIndex],dis);
                }
            }

            _nearNode.sort(function(a,b){
                if (a.dis < b.dis) return -1;
                if (a.dis > b.dis) return 1;
                return 0;
            });

            var _possibleTarget = _nearNode.filter(function(d){
                return d.node.id == _target.id;
            });

//          console.log(_nearNode);
            if(_possibleTarget.length > 0 && Math.abs(_possibleTarget[0].dis - _nearNode[0].dis ) < 10 ){
//                console.log("find near target");
                wrapNode(_possibleTarget[0].node);
            }else{
                wrapNode(_nearNode[0].node);
            }

        }

        /**
         * wrap node
         * function find each point around the node and decide the path
         * */
        function wrapNode(_node){

           var _nodeInfo = getLayoutInfo(_node);
           var _nodeResourceInfo = getLayoutInfo(source_resource);
           _currentNode = source_resource;

           if(_node.id == _source.id){
               cx = _source.x + _source.width;
               cy = _source.y + (_source.height/2);
               addPoint(cx,cy);

               cx = _nodeResourceInfo.end.x + ( parseInt(_nodeResourceInfo.extra.right+"") * 3);
               addPoint(cx,cy);

               _nodeResourceInfo.extra.right ++;
               if(source_resource.y >= _target.y){
                   //go up
                   if( (_target.y == source_resource.y) && _target.x > source_resource.x && Math.abs(_target.x - cx) < 30 ){
                         //beside node
                   }else{
                       cy = _nodeResourceInfo.start.y - ( parseInt(_nodeResourceInfo.extra.top+"") * 3);
                       addPoint(cx,cy);
                   }

               }else{
                   // go down
                   cy = _nodeResourceInfo.end.y + ( parseInt(_nodeResourceInfo.extra.top+"") * 2);
                   addPoint(cx,cy);
               }

               if(source_resource.x > _target.x){
                   //go left
//                   console.log("go left");
                   cx = _nodeResourceInfo.start.x;
                   _nodeResourceInfo.extra.top++;
                   addPoint(cx,cy);
               }

               //wrapNode(_target);
//               console.log("source")
              findNearNode();
           }else if(_node.id == _target.id){
               //wrap target node
               _currentNode = _node;
               //console.log("I am near node");

               if(_target.y == source_resource.y && Math.abs(_target.x - (source_resource.x + source_resource.width + 30) ) < 40 ){
                   cy = _target.y + ( _target.height / 2 ) + ( parseInt(_nodeInfo.extra.bottom+"") * 3);
                   addPoint(cx,cy);
                   cx = _nodeInfo.start.x ;
                   addPoint(cx,cy);
               }else{
                   if(Math.abs(cy - _nodeInfo.start.y ) > Math.abs(cy - _nodeInfo.end.y )){
                       cy = _nodeInfo.end.y + ( parseInt(_nodeInfo.extra.bottom+"") * 3);
                       _nodeInfo.extra.bottom++;
                       addPoint(cx,cy);
                   }else{
                       cy = _nodeInfo.start.y - ( parseInt(_nodeInfo.extra.top+"") * 3);
                       _nodeInfo.extra.top++;
                       addPoint(cx,cy);
                   }

                   if(Math.abs(cx - _nodeInfo.start.x ) > Math.abs(cx - _nodeInfo.end.x )){
//                    console.log("near end")
                       if(cx > _nodeInfo.end.x - ( parseInt(_nodeInfo.extra.right+"") * 2)){
                           cx = _nodeInfo.end.x - ( parseInt(_nodeInfo.extra.right+"") * 2);
                       }
                       cx = _target.x + _target.width - 10 - ( parseInt(_nodeInfo.extra.right+"") * 2);
                       addPoint(cx,cy);
                   }else{
//                   console.log("near start")
                       if(cx < _nodeInfo.start.x + ( parseInt(_nodeInfo.extra.left+"") * 2)){
                           cx = _nodeInfo.start.x + 10 + ( parseInt(_nodeInfo.extra.left+"") * 2);
                       }
                       //_nodeInfo.extra.top++;
                       addPoint(cx,cy);
                   }

                   if(Math.abs(cy - _target.y ) > Math.abs(cy - (_target.y + _target.height) )){
//                   console.log("touch end")
                       cy = _target.y + _target.height;
                       addPoint(cx,cy);
                   }else{
//                   console.log("touch top")
                       cy = _target.y;
                       addPoint(cx,cy);
                   }
               }
//               console.log("at end")

           }else{
               //wrap in between nodes
               //findNearNode();
               _currentNode = _node;
               //console.log("I am near node");
               if(Math.abs(cy - _nodeInfo.start.y ) > Math.abs(cy - _nodeInfo.end.y )){
//                   console.log("near down");
                   cy = _nodeInfo.end.y + ( parseInt(_nodeInfo.extra.bottom+"") * 3);
                   _nodeInfo.extra.bottom++;
                   addPoint(cx,cy);
               }else{
//                   console.log("near up");
                   cy = _nodeInfo.start.y - ( parseInt(_nodeInfo.extra.top+"") * 3);
                   _nodeInfo.extra.top++;
                   addPoint(cx,cy);
               }

               if(cx >= _target.x){
                   if( _node.start.x == _target.x ){
                       cx = _nodeInfo.end.x + ( parseInt(_nodeInfo.extra.right+"") * 3);
                       _nodeInfo.extra.right++;
                       addPoint(cx,cy);
                   }else{
                       console.log(Math.abs(_node.end.x - _target.x));
                       if( _node.start.x < _target.x ){
                           cx = _nodeInfo.end.x + ( parseInt(_nodeInfo.extra.right+"") * 3);
                           _nodeInfo.extra.right++;
                           addPoint(cx,cy);
                       }else{
                           cx = _nodeInfo.start.x - ( parseInt(_nodeInfo.extra.left+"") * 3);
                           _nodeInfo.extra.left++;
                           addPoint(cx,cy);
                       }

                   }

               }else{
                   cx = _nodeInfo.end.x + ( parseInt(_nodeInfo.extra.right+"") * 2);
                   addPoint(cx,cy);
               }

               if( (_node.start.y + 10) >= _target.y){
                   //go up
                   if((_target.y == _node.y)){
                       //beside node
                   }else{
                       cy = _node.start.y - ( parseInt(_node.extra.top+"") * 3);
                       addPoint(cx,cy);
                   }

               }else{
                   // go down
                   cy = _node.end.y + ( parseInt(_node.extra.top+"") * 3);
                   addPoint(cx,cy);
               }

               findNearNode();
           }

        }

        wrapNode(_source);
        return this.drawPath(_points);
    }

   return routing;
}


