var DiagramSpecifics = {

        // Vertical gap between horizontal parts of URISegment links used to separate them from each other.
        // This goes (for every bound URISegment) into the vertical space
        // between resource label and the top of its contents
        URI_LINK_VGAP: 5,

        // Horizontal gap between vertical parts of URISegment used to separate them from each other.
        // This goes (for every bound URISegment) into the left-side padding of the resource
        URI_LINK_HGAP: 5,

        // FIXME: this is used to locate response ports which should be placed
        // FIXME: at the middle of the repsonse's part of the method
        // FIXME: Actually this code is called from many places, which is bad
        // FIXME: Instead: introduce implicit child ResponsePort
        // FIXME: compute its positions (using this metho inlined) once and then reuse the value
        collectChildrenVerticalMinMax: function (parent, childFilter) {
            var result = {
                "minTop": Number.POSITIVE_INFINITY,
                "maxBottom": Number.NEGATIVE_INFINITY,
                getMiddle: function () {
                    return (this.minTop + this.maxBottom) / 2;
                }
            };
            (parent.children || []).forEach(function (item, i, arr) {
                if (childFilter(item) && item.y && item.height) {
                    result.minTop = Math.min(result.minTop, item.y);
                    result.maxBottom = Math.max(result.maxBottom, item.y + item.height);
                }
            });
            return (result.minTop < result.maxBottom) ? result : null;
        },

        getURISegmentBindings: function (node) {
            function getFeaturesToBind(resource) {
                var result = [];
                if (resource.children) {
                    resource.children.forEach(function (child) {
                        if (child.objecttype == "DataType" || child.objecttype == "BaseType") {
                            if (child.children) {
                                child.children.forEach(function (deep) {
                                    if (deep.objecttype == "PrimitiveProperty") {
                                        result.push(deep);
                                    }
                                });
                            }
                        }
                    });
                }
                return result;
            }

            var bindings = [];
            var segments = (node.children || []).filter(function (d) {
                return d.objecttype == "URISegment";
            });
            if (segments.length > 0 && node.URI && node.URI.parameters && node.URI.parameters.length > 0) {
                /*const*/var featuresToMatch = getFeaturesToBind(node);
                node.URI.parameters.forEach(function (param) {
                    var matchedFeature = CoreUtils.findFirst(featuresToMatch, function (f) {
                        return f.id == param.propertyId;
                    });
                    var matchedSegment = CoreUtils.findFirst(segments, function (seg) {
                        return seg.id == param.uriFragment;
                    });
                    if (matchedFeature && matchedSegment) {
                        var nextBinding = {};
                        nextBinding.matchedFeature = matchedFeature;
                        nextBinding.matchedSegment = matchedSegment;
                        nextBinding.matchingParameter = param;
                        bindings.push(nextBinding);
                    }
                });
            }
            return bindings;
        },

        /**
         * get which string going to be shown on container
         * */
        getVisibleNodeString: function (node) {
            switch (node.objecttype) {
                case "URISegment":
                    return node.label || "";

                case "Response":
                    //Responses themselves are empty now, status & resource type labels are ad hoc children
                    return "";

                case "Request":
                    return node.name || node.resource_type || "";

                case "ResourceType":
                    return node.resource_type || "(empty response)";

                case "StatusCode":
                    //FIXME: check swagger default responses and responses without status code in general
                    return node.statusCode || "Status";

                case "Method":
                    return node.name + " (" + node.type + ")";

                case "ObjectResource":
                case "CollectionResource":
                case "DataType":
                case "ReferenceLink":
                case "HeaderParameter":
                case "QueryParameter":
                // break through, next 3 are separated as they are questionable, see below
                case "TemplateParameter":
                case "MatrixParameter":
                case "CollectionParameter":
                    //FIXME: last 3 parameters are questionable: I don't think that they can be shown at any diagram
                    //FIXME: leaving here just in case
                    return node.name || "";

                case "ReferenceEmbed":
                    // note: for ReferenceEmbed the name itself contains ':'
                    return node.name || "";

                case "PrimitiveProperty":
                case "ReferenceProperty":
                    return node.name + ":" + node.type;

                default:
                    if (!node.name) {
                        return "Empty";
                    }
                    return node.name + (node.type ? " (" + node.type + ") " : "");
            }
        },

        computeChildren: function (data, depth) {
            function cloneData(any) {
                var result = JSON.parse(JSON.stringify(any));
                return result;
            }

            function isArray(any) {
                return Object.prototype.toString.call(any) == '[object Array]';
            }

            var children;
            if (data.children) {
                return data.children;
            } else {
                if (!data._children) {
                    if (data.objecttype == "Response") {
                        //FIXME: revisit known false/true checks here and below, simplify structure
                        if (!children) {
                            children = [];
                        }

                        // This label is shown when resourceType or *neither type nor status*,
                        // in the latter case, show '(empty request)'
                        // FIXME: actually '(empty request)' is passed as data.name,
                        // FIXME: would be better to solve this at the data generator level
                        if (data.resource_type || !data.statusCode) {
                            //NOTE: data.resource_type may be null here
                            var implicitResourceType = {
                                "objecttype": "ResourceType",
                                "resource_type": data.resource_type
                            };
                            children.push(implicitResourceType);
                        }

                        if (data.statusCode) {
                            var implicitStatusCode = {
                                "objecttype": "StatusCode",
                                "statusCode": data.statusCode,
                                "statusCodeGroup": data.statusCodeGroup
                            };
                            children.push(implicitStatusCode);
                        }
                    }

                    if (data.baseType) {
                        if (!children) {
                            children = [];
                        }
                        children.push(data.baseType);
                    }

                    if (data.objecttype == "Method") {
                        if (!children) {
                            children = [];
                        }
                        if (data.request) {
                            children.push(cloneData(data.request));
                        }
                        if (data.responses) {
                            if (isArray(data.responses)) {
                                for (_responseIndex in data.responses) {
                                    children.push(cloneData(data.responses[_responseIndex]));
                                }
                            }
                        }
                    } else {
                        for (_dataIndex in data) {
                            if (isArray(data[_dataIndex])) {
                                if (children) {
                                    Array.prototype.push.apply(children, cloneData(data[_dataIndex]));
                                } else {
                                    children = cloneData(data[_dataIndex]);
                                }
                                break;
                            }
                        }
                    }

                    if (data.objecttype == "ObjectResource" || data.objecttype == "CollectionResource") {

                        if (data.dataType) {
                            var _dataTypeObject = cloneData(data.dataType);
                            if (children) {
                                var _dataType = children.filter(function (d) {
                                    return d.objecttype == "DataType";
                                });
                                if (_dataType.length == 0) {
                                    children.splice(0, 0, _dataTypeObject);
                                }
                            }
                        }

                        if (data.URI && data.URI.name) {
                            if (!children) {
                                children = [];
                            }
                            if (isArray(data.URI.name)) {
                                Array.prototype.push.apply(children, cloneData(data.URI.name));
                            }
                        }
                    }

//                            if(data.objecttype == "DataType"){
                    if (data.referenceTreatments) {
                        var _referenceLinks = cloneData(data.referenceTreatments).reverse();
                        if (children) {
                            var _referenceLinkList = children.filter(function (d) {
                                return d.objecttype == "ReferenceLink";
                            });
                            if (_referenceLinkList.length == 0) {
                                for (_referenceIndex in _referenceLinks) {
                                    children.splice(0, 0, _referenceLinks[_referenceIndex]);
                                }
                            }
                        }
                    }
//                            }
                }
            }

            if (children) {
                children.forEach(DiagramSpecifics.setupViewmap);
            }
            return children;
        },

        setupViewmap: function (datum) {
            /*const*/var imageHrefFun = DiagramSpecifics.getImageHrefFun(url_prefix);
            /*const*/var toStringFun = DiagramSpecifics.getVisibleNodeString;
            /*const*/var EXPANDER_GROUP_CLASS = "expander-group";

            //FIXME: move to _Conditions?
            function isPrimitiveOrReferenceProperty(d) {
                return d.objecttype == "PrimitiveProperty" || d.objecttype == "ReferenceProperty";
            }

            function isNotProperty(d) {
                return !isPrimitiveOrReferenceProperty(d);
            }

            function isURISegment(d) {
                return d.objecttype == "URISegment";
            }

            function isNotURISegment(d) {
                return !isURISegment(d);
            }

            switch (datum.objecttype) {
                case "QueryParameter":
                case "HeaderParameter":
                case "RequestParameter":
                case "ResourceType":
                case "StatusCode":
                    Viewmaps.newLabeledRowViewMap(datum)
                        .withIcon(12, 12, imageHrefFun)
                        .withText(toStringFun)
                        .asSemanticRoot();
                    break;

                case "Request":
                    // requests without parameters show the name,
                    // requests with parameters only show parameters
                    if (datum.parameters == null || datum.parameters.length == 0) {
                        Viewmaps.newLabeledRowViewMap(datum)
                            .withInsets(0, 0, 0, 0)
                            .withIcon(12, 12, imageHrefFun)
                            .withText(toStringFun)
                            .asSemanticRoot();
                    } else {
                        Viewmaps.newVBoxViewMap(datum)
                            .withInsets(5, 5, 5, 5)
                            .asSemanticRoot();
                    }
                    break;

                case "Response":
                    Viewmaps.newVBoxViewMap(datum)
                        .withInsets(5, 5, 5, 5)
                        .asSemanticRoot();
                    break;

                case "Method":
                    Viewmaps.newVBoxViewMap(datum)
                        .withInsets(0, 0, 5, 0)
                        .withChild(Viewmaps.newLabeledRowViewMap(datum)
                            .withEmptyBox(12, 16, EXPANDER_GROUP_CLASS)
                            .withIcon(12, 12, imageHrefFun)
                            .withText(toStringFun))
                        .withChildAsSharedContentPane(
                            Viewmaps.newVBoxViewMap(datum)
                                .withSpacing(10)
                                .withInsets(5, 10, 5, 10)
                        )
                        .asSemanticRoot();
                    break;

                case "PrimitiveProperty":
                case "ReferenceProperty":
                    Viewmaps.newLabeledRowViewMap(datum)
                        .withText(toStringFun)
                        .asSemanticRoot();
                    break;

                case "DataType":
                case "ReferenceEmbed":
                    //FIXME: old code did NOT had expanders for empty containers
                    Viewmaps.newVBoxViewMap(datum)
                        .withSpacing(5)
                        .withInsets(0, 0, 5, 5)
                        .withChild(Viewmaps.newLabeledRowViewMap(datum)
                            .withEmptyBox(12, 16, EXPANDER_GROUP_CLASS)
                            .withIcon(12, 12, imageHrefFun)
                            .withText(toStringFun))
                        .withChildAsContentPaneFor(isNotProperty,
                            Viewmaps.newVBoxViewMap(datum)
                                .withSpacing(10)
                                .withInsets(0, 10, 0, 5)
                        )
                        .withChildAsContentPaneFor(isPrimitiveOrReferenceProperty,
                            Viewmaps.newVBoxViewMap(datum)
                                .withInsets(0, 10, 0, 5)
                        )
                        .asSemanticRoot();
                    break;

                case "ReferenceLink":
                    //ReferenceLink's are like DadaType's but also may have LinkRelation label
                    //FIXME: old code did NOT had expanders for empty containers
                    Viewmaps.newVBoxViewMap(datum)
                        .withSpacing(5)
                        .withInsets(0, 0, 5, 5)
                        .withChild(Viewmaps.newHBoxViewMap(datum)
                            .withMinorAlignment(Draw2D.Alignment.TOP_LEFT)
                            .withChild(Viewmaps.newLabeledRowViewMap(datum)
                                .withEmptyBox(12, 16, EXPANDER_GROUP_CLASS)
                                .withIcon(12, 12, imageHrefFun)
                            )
                            .withChild(Viewmaps.newVBoxViewMap(datum)
                                .withChild(Viewmaps.newLabeledRowViewMap(datum)
                                    .withText(toStringFun)
                                )
                                .withChild(Viewmaps.newLabeledRowViewMap(datum)
                                    .withText(function (d) {
                                        var result = d.linkRelation;
                                        return result && result.length > 0 ? result : ""
                                    })
                                )
                            )
                        )
                        .withChildAsContentPaneFor(isNotProperty,
                            Viewmaps.newVBoxViewMap(datum)
                                .withSpacing(10)
                                .withInsets(0, 10, 0, 5)
                        )
                        .withChildAsContentPaneFor(isPrimitiveOrReferenceProperty,
                            Viewmaps.newVBoxViewMap(datum)
                                .withInsets(0, 10, 0, 5)
                        )
                        .asSemanticRoot();
                    break;

                case "ObjectResource":
                case "CollectionResource":
                    var segmentsContentPane;
                    var restContentPane;
                    var rightColumn = Viewmaps.newVBoxViewMap(datum)
                        .withInsets(0, 0, 10, 0)
                        .withChild(// top row: expander + icon + vertical place for name and composite uri label
                            Viewmaps.newHBoxViewMap(datum)
                                .withMinorAlignment(Draw2D.Alignment.TOP_LEFT)
                                .withSpacing(5)
                                .withChild(
                                    Viewmaps.newLabeledRowViewMap(datum)
                                        .withEmptyBox(12, 16, EXPANDER_GROUP_CLASS)
                                        .withIcon(12, 12, imageHrefFun)
                                )
                                .withChild( // vertical place for name and composite uri label
                                    Viewmaps.newVBoxViewMap(datum)
                                        .withText(toStringFun)
                                        .withChild(
                                            segmentsContentPane = Viewmaps.newHBoxViewMap(datum)
                                                //FIXME: the measuring code for some reason always add this margin
                                                //FIXME: so we are adjusting to this to have URI segments without gaps
                                                //FIXME: this should be removed in the measuring
                                                .withSpacing(-ViewmapUtils.ALL_LABELS_RIGHT_MARGIN)
                                        )
                                )
                        )
                        .withEmptyBox(0, function (datum) {
                            var urlLinks = DiagramSpecifics.getURISegmentBindings(datum);
                            return 5 + DiagramSpecifics.URI_LINK_HGAP * urlLinks.length;
                        })
                        .withChild(
                            restContentPane = Viewmaps.newVBoxViewMap(datum)
                                .withSpacing(10)
                                .withInsets(0, 10, 0, 10)
                        );

                    // first dynamic empty box to form a padding for vertical part of the URL links
                    // then the real content as a right column
                    Viewmaps.newHBoxViewMap(datum)
                        .withSpacing(0)
                        .withEmptyBox(
                            function (d) {
                                var urlLinks = DiagramSpecifics.getURISegmentBindings(datum);
                                return Math.max(0, DiagramSpecifics.URI_LINK_HGAP * (urlLinks.length - 1));
                            }, 0)
                        .withChild(rightColumn)
                        .withDeepContentPaneFor(isURISegment, segmentsContentPane)
                        .withDeepContentPaneFor(isNotURISegment, restContentPane)
                        .asSemanticRoot();
                    break;

                case "URISegment":
                    Viewmaps.newLabeledRowViewMap(datum)
                        .withText(toStringFun)
                        .asSemanticRoot();
                    break;

                default:
                    throw 'Unknown child found: ' + datum.objecttype;
            }
        },

        getImageHrefFun: function (url_prefix) {
            return function (datum) {
                return DiagramSpecifics._getImageHref(datum, url_prefix);
            };
        },

        _getImageHref: function (datum, url_prefix) {
            //FIXME: url_prefix looks innatural here, should be constructor parameter to some module
            //FIXME: partially addressed using getImageHrefFun
            var path = "images/default.png";
            switch (datum.objecttype) {
                case "ObjectResource":
                    path = "images/objectResource.png";
                    break;
                case "CollectionResource":
                    path = "images/collectionResource.png";
                    break;
                case "DataType":
                    path = "images/dataType.png";
                    break;
                case "ReferenceLink":
                    path = "images/referenceLink.png";
                    break;
                case "ReferenceEmbed":
                    path = "images/referenceEmbed.png";
                    break;
                case "Method":
                    path = "images/method.png";
                    break;
                case "Request":
                    path = "images/request.png";
                    break;
                case "QueryParameter":
                    path = "images/queryParameter.png";
                    break;
                case "HeaderParameter":
                    path = "images/headerParameter.png";
                    break;
                case "RequestParameter":
                    path = "images/requestParameter.png";
                    break;
                case "ResourceType":
                    path = "images/response.png";
                    break;
                case "StatusCode":
                    switch (datum.statusCodeGroup) {
                        case "Server Error":
                            path = "images/response_serverError.png";
                            break;
                        case "Client Error":
                            path = "images/response_clientError.png";
                            break;
                        case "Redirection" :
                            path = "images/response_redirection.png";
                            break;
                        case "Success":
                            path = "images/response_success.png";
                            break;
                        case "Informational" :
                            path = "images/response_informational.png";
                            break;
                        case "Default" :
                            path = "images/response_default.png";
                            break;
                        default:
                            path = "images/response.png";
                    }
                    break;
                default :
                    path = "images/default.png";
            }
            return url_prefix + path;
        },

        getContainerBackgroundClass: function (datum) {
            var classes = ["background"];
            switch (datum.objecttype) {
                case "ObjectResource":
                case "CollectionResource":
                    classes.push("resource");
                    break;
                case "DataType":
                    classes.push("dataType");
                    break;
                case "Method":
                    classes.push("method");
                    break;
                case "ReferenceLink":
                    classes.push("referenceLink");
                    break;
                case "Request":
                    classes.push("request");
                    break;
                case "Response":
                    classes.push("response");
                    break;
                case "ReferenceEmbed":
                    classes.push("referenceEmbed");
                    break;
                case "ResourceAPI":
                    //NO-OP
                    break;
                default:
                    classes.push("otherChild");
            }
            return classes.join(' ');
        }
};
