var Viewmaps = {

        _Utils: {
            alwaysTrue: function () {
                return true;
            },

            _nextDebugId: 1,

            debugId: function () {
                return this._nextDebugId++;
            }
        },

        $initStatics: function () {
            function $addExtends(SubClass, SuperClass) {
                if (!SubClass.prototype.$extendsSet) {
                    SubClass.prototype = Object.create(SuperClass.prototype);
                    SubClass.prototype.constructor = SubClass;
                    SubClass.prototype.$extendsSet = true;
                }
            }

            if (!this.staticInitDone) {
                this.staticInitDone = true;

                $addExtends(this._Internal.BaseContainerFigure, this._Internal.BaseFigure);
                $addExtends(this._Internal.ImageFigure, this._Internal.BaseFigure);
                $addExtends(this._Internal.LabelFigure, this._Internal.BaseFigure);
                $addExtends(this._Internal.EmptyFigure, this._Internal.BaseFigure);

                this._Internal.$BaseFigure$init(this._Internal.BaseFigure);
                this._Internal.$BaseContainerFigure$init(this._Internal.BaseContainerFigure);
                this._Internal.$EmptyFigure$init(this._Internal.EmptyFigure);
                this._Internal.$ImageFigure$init(this._Internal.ImageFigure);
                this._Internal.$LabelFigure$init(this._Internal.LabelFigure);
            }

            return this;
        },

        newLabeledRowViewMap: function (datum) {
            return this.newHBoxViewMap(datum)
                .withSpacing(5)
                .withMinorAlignment(Draw2D.Alignment.CENTER);
        },

        newVBoxViewMap: function (datum) {
            return new Viewmaps._Internal.BaseContainerFigure(datum, LayoutModule.newColumnLayout(true));
        },

        newHBoxViewMap: function (datum) {
            return new Viewmaps._Internal.BaseContainerFigure(datum, LayoutModule.newColumnLayout(false));
        },

        _Internal: {
            BaseFigure: function (datum) {
                this.datum = datum;
                this.bounds = null;
                this.parentFigure = null;
                this.debugId = Viewmaps._Utils.debugId();
            },

            $BaseFigure$init: function (BaseFigure) {

                BaseFigure.prototype.getLayoutManager = function () {
                    return null;
                };

                BaseFigure.prototype.funOrVal = function (valOrFun) {
                    if (!valOrFun) {
                        return null;
                    }
                    return typeof valOrFun == 'function' ? valOrFun(this.datum) : valOrFun;
                };

                BaseFigure.prototype.setBounds = function (bounds) {
                    this.bounds = bounds;
                    if (this.isSemanticRoot()) {
                        this.datum.width = bounds.getWidth();
                        this.datum.height = bounds.getHeight();
                        this.datum.x = bounds.getX();
                        this.datum.y = bounds.getY();
                    }
                };

                BaseFigure.prototype.asSemanticRoot = function () {
                    if (this.datum.viewmap != null) {
                        throw "Datum: " + this.datum.objecttype + " already have semantic root:";
                    }
                    this.datum.viewmap = this;
                    return this;
                };

                BaseFigure.prototype.isSemanticRoot = function () {
                    return this.datum && this.datum.viewmap === this;
                };

                BaseFigure.prototype.findRootFigure = function () {
                    return this.parentFigure ? this.parentFigure.findRootFigure() : this;
                };

                BaseFigure.prototype.translateLocal2semanticRoot = function (t) {
                    if (!this.parentFigure || !this.bounds) {
                        return t;
                    }
                    if (this.isSemanticRoot()) {
                        return t;
                    }
                    var inParentCoordinates = t.getTranslated(this.bounds.getX(), this.bounds.getY());
                    return this.parentFigure.translateLocal2semanticRoot(inParentCoordinates);
                };

                BaseFigure.prototype.getBoundsInSemanticRootCoordinates = function () {
                    if (!this.bounds) {
                        return null;
                    }
                    //my top-left is (0, 0) in local coordinates
                    var inLocal = this.bounds.getTranslated(-this.bounds.getX(), -this.bounds.getY());
                    return this.translateLocal2semanticRoot(inLocal);
                };
            },

            BaseContainerFigure: function (datum, layoutManager) {
                Viewmaps._Internal.BaseFigure.call(this, datum);

                this.childFigures = [];
                this.insets = null;
                this.layoutManager = layoutManager;
                this.contentPanes = [];
            },

            $BaseContainerFigure$init: function (BaseContainerFigure) {
                BaseContainerFigure.prototype._NO_INSETS = Object.freeze(new Draw2D.Insets(0, 0, 0, 0));

                BaseContainerFigure.prototype.NO_INSETS = function () {
                    return BaseContainerFigure.prototype._NO_INSETS;
                };

                BaseContainerFigure.prototype._registerContentPaneFor = function (childCondition, contentPane) {
                    var entry = {};
                    entry.condition = childCondition;
                    entry.contentPane = contentPane;
                    this.contentPanes.push(entry);
                };

                BaseContainerFigure.prototype.getContentPaneAdjustment = function (datum) {
                    if (!this.isSemanticRoot()) {
                        throw "I am not semantic root: " + datum.viewmap;
                    }
                    var contentPane = this._findContentPaneForSemanticChild(datum);
                    return (contentPane == null || contentPane == this) ? null :
                        contentPane.getBoundsInSemanticRootCoordinates().getTopLeft();
                };

                BaseContainerFigure.prototype.getChildrenFigures = function () {
                    var result = this.childFigures.slice(0);
                    if (this.datum.children) {
                        /*const*/var figureThis = this;
                        /*const*/var semanticRoot = this.datum.viewmap;
                        // semantic root delegates some children to content panes
                        // and hosts all *other* children (those which do NOT have a content pane)
                        this.datum.children.forEach(function (d) {
                            var cPane = semanticRoot._findContentPaneForSemanticChild(d);
                            if (cPane === figureThis || (cPane == null && figureThis === semanticRoot)) {
                                result.push(d);
                            }
                        });
                    }
                    return result;
                };

                BaseContainerFigure.prototype.withChild = function (child) {
                    if (child.parentFigure) {
                        throw "Child figure already has some parent";
                    }
                    child.parentFigure = this;
                    this.childFigures.push(child);
                    return this;
                };

                BaseContainerFigure.prototype.withChildAsSharedContentPane = function (contentPane) {
                    return this.withChildAsContentPaneFor(Viewmaps._Utils.alwaysTrue, contentPane);
                };

                BaseContainerFigure.prototype.withChildAsContentPaneFor = function (childCondition, contentPane) {
                    var resultThis = this.withChild(contentPane);
                    this._registerContentPaneFor(childCondition, contentPane);
                    return resultThis;
                };

                BaseContainerFigure.prototype.withDeepContentPaneFor = function (childCondition, contentPane) {
                    this._registerContentPaneFor(childCondition, contentPane);
                    return this;
                };

                BaseContainerFigure.prototype.withSpacing = function (spacing) {
                    this.getLayoutManager().setSpacing(spacing);
                    return this;
                };

                BaseContainerFigure.prototype.withMinorAlignment = function (alignment) {
                    this.getLayoutManager().setMinorAlignment(alignment);
                    return this;
                };

                BaseContainerFigure.prototype.getInsets = function () {
                    return this.insets || this.NO_INSETS();
                };

                BaseContainerFigure.prototype.withInsets = function (top, left, bottom, right) {
                    this.insets = new Draw2D.Insets(top, left, bottom, right);
                    return this;
                };

                BaseContainerFigure.prototype.withIcon = function (width, height, hrefFun) {
                    var icon = new Viewmaps._Internal.ImageFigure(this.datum, hrefFun)
                        .withWidth(width)
                        .withHeight(height);
                    return this.withChild(icon);
                };

                BaseContainerFigure.prototype.withEmptyBox = function (width, height, cssClass) {
                    var box = new Viewmaps._Internal.EmptyFigure(this.datum, width, height);
                    if (cssClass) {
                        box = box.withClass(cssClass);
                    }
                    return this.withChild(box);
                };

                BaseContainerFigure.prototype.withText = function (textFun) {
                    return this.withChild(
                        new Viewmaps._Internal.LabelFigure(this.datum, textFun)
                            .withClass("label")
                    );
                };

                BaseContainerFigure.prototype.getLayoutManager = function () {
                    return this.layoutManager;
                };

                BaseContainerFigure.prototype.getClientArea = function () {
                    if (!this.bounds) {
                        //FIXME: revisit:
                        throw "I expect bounds already set to me before layouting children";
                    }
                    //FIXME[IMPORTANT]: local coordinates - reconsider secondWalk()
                    var insets = this.getInsets();
                    return new Draw2D.Rectangle(insets.left, insets.top,
                        this.bounds.getWidth() - insets.getWidth(), this.bounds.getHeight() - insets.getHeight());
                };

                BaseContainerFigure.prototype.getPreferredSize = function () {
                    return this.getLayoutManager().getPreferredSize(this, -1, -1);
                };

                BaseContainerFigure.prototype._findContentPaneForSemanticChild = function (child) {
                    var found = CoreUtils.findFirst(this.contentPanes, function (entry) {
                        return entry.condition(child);
                    });
                    return found == null ? null : found.contentPane;
                };

                BaseContainerFigure.prototype.applyToDom = function (dom) {
                    //FIXME: separate getChildren vs getChildrenFigures ?
                    this.getChildrenFigures().forEach(function (next) {
                        if (next instanceof Viewmaps._Internal.BaseFigure) {
                            next.applyToDom(dom);
                        }
                    });
                };
            },

            EmptyFigure: function (datum, width, height) {
                Viewmaps._Internal.BaseFigure.call(this, datum);
                this.widthFun = width;
                this.heightFun = height;
                this.classFun = null;
            },

            $EmptyFigure$init: function (EmptyFigure) {

                EmptyFigure.prototype.applyToDom = function (dom) {
                    var result = d3.select(dom).append("g")
                        .attr("width", this.widthFun)
                        .attr("height", this.heightFun);

                    var boundsInRoot = this.getBoundsInSemanticRootCoordinates();
                    if (boundsInRoot) {
                        result.attr("x", boundsInRoot.getX());
                        result.attr("y", boundsInRoot.getY());
                    }
                    if (this.classFun) {
                        result.attr("class", this.classFun);
                    }
                    return result;
                };

                //FIXME: duplication with LabelFigure
                EmptyFigure.prototype.withClass = function (classFun) {
                    this.classFun = classFun;
                    return this;
                };

                EmptyFigure.prototype.getPreferredSize = function () {
                    var w = this.funOrVal(this.widthFun) || 0;
                    var h = this.funOrVal(this.heightFun) || 0;
                    return new Draw2D.Dimension(w, h);
                };
            },

            ImageFigure: function (datum, hrefFun) {
                Viewmaps._Internal.BaseFigure.call(this, datum);
                this.hrefFun = hrefFun;
                this.widthFun = null;
                this.heightFun = null;
            },

            $ImageFigure$init: function (ImageFigure) {

                ImageFigure.prototype.withWidth = function (widthFun) {
                    this.widthFun = widthFun;
                    return this;
                };

                ImageFigure.prototype.withHeight = function (heightFun) {
                    this.heightFun = heightFun;
                    return this;
                };

                ImageFigure.prototype.getPreferredSize = function () {
                    var w = this.funOrVal(this.widthFun) || 0;
                    var h = this.funOrVal(this.heightFun) || 0;
                    return new Draw2D.Dimension(w, h);
                };

                ImageFigure.prototype.applyToDom = function (dom) {
                    var result = d3.select(dom).append("image");
                    result.attr("xlink:href", this.hrefFun);

                    var boundsInRoot = this.getBoundsInSemanticRootCoordinates();
                    if (boundsInRoot) {
                        result.attr("x", boundsInRoot.getX());
                        result.attr("y", boundsInRoot.getY());
                    }
                    if (this.widthFun) {
                        result.attr("width", this.widthFun);
                    }
                    if (this.heightFun) {
                        result.attr("height", this.heightFun);
                    }
                    return result;
                };
            },

            LabelFigure: function (datum, textFun) {
                Viewmaps._Internal.BaseFigure.call(this, datum);
                this.textFun = textFun;
                this.classFun = null;
            },

            $LabelFigure$init: function (LabelFigure) {

                LabelFigure.prototype.withClass = function (classFun) {
                    this.classFun = classFun;
                    return this;
                };

                LabelFigure.prototype.getPreferredSize = function () {
                    //FIXME: cache text value?
                    var text = this.funOrVal(this.textFun);
                    if (!text) {
                        return new Draw2D.Dimension(0, 0);
                    }
                    var bbox = ViewmapUtils.createSVGtext(text);
                    return new Draw2D.Dimension(bbox.width, bbox.height);
                };

                LabelFigure.prototype.applyToDom = function (dom) {
                    // this.debugDrawBaseLine(dom);

                    var result = d3.select(dom).append("svg:text");

                    // next line does not work in all browsers, otherwise good
                    // result.attr("style", "dominant-baseline:text-after-edge;");

                    // result.attr("filter", "url(#fill_solid_yellow)");

                    if (this.classFun) {
                        result = result.attr("class", this.classFun);
                    }
                    var boundsInRoot = this.getBoundsInSemanticRootCoordinates();
                    //console.log("Label: `" + text + "`: bounds:" + this.bounds.toDebugString() +
                    //    ", in root coordinates: " + boundsInRoot.toDebugString());
                    if (boundsInRoot) {
                        result.attr("x", boundsInRoot.getX());
                        result.attr("y", boundsInRoot.getY());
                        // ```* 0.8``` to adjust the base-line
                        // It is pure-man version of 'style="dominant-baseline:text-after-edge;"
                        // which is unfortunately not cross-browser
                        result.attr("dy", boundsInRoot.getHeight() * 0.8);
                    }

                    result.text(this.textFun);
                    return result;
                };

                LabelFigure.prototype.debugDrawBaseLine = function (dom) {
                    var boundsInRoot = this.getBoundsInSemanticRootCoordinates();
                    if (boundsInRoot) {
                        var topY = boundsInRoot.getY();
                        var bottomY = boundsInRoot.getY() + boundsInRoot.getHeight();
                        d3.select(dom).append("path")
                            .attr("stroke", "green")
                            .attr("fill", "none")
                            .attr("d", "M 0," + bottomY + " 100," + bottomY);
                        d3.select(dom).append("path")
                            .attr("stroke", "red")
                            .attr("fill", "none")
                            .attr("d", "M 0," + topY + " 100," + topY);
                    }
                };

            }
        }

};
Viewmaps.$initStatics();



