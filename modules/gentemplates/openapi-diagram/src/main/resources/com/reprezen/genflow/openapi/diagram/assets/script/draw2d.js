var Draw2D = {
        $initStatics: function () {
            if (!this.staticInitDone) {
                this.staticInitDone = true;

                this.$Location$init(this.Location);
                this.$Dimension$init(this.Dimension);
                this.$Rectangle$init(this.Rectangle);
                this.$Insets$init(this.Insets);
                this.$Transposer$init(this.Transposer);
            }
            return this;
        },

        Location: function (x, y) {
            this.pointX = x;
            this.pointY = y;
        },

        $Location$init: function (Location) {
            Location.prototype.getTransposed = function () {
                return new Draw2D.Location(this.y, this.x);
            };

            Location.prototype.getX = function () {
                return this.pointX;
            };

            Location.prototype.getY = function () {
                return this.pointY;
            };

            Location.prototype.getTranslated = function (dx, dy) {
                return new Draw2D.Location(this.getX() + dx, this.getY() + dy);
            };

        },

        Dimension: function (width, height) {
            this.width = width;
            this.height = height;
        },

        $Dimension$init: function (Dimension) {
            Dimension.prototype.getTransposed = function () {
                return new Draw2D.Dimension(this.height, this.width);
            };

            Dimension.prototype.getWidth = function () {
                return this.width;
            };

            Dimension.prototype.getHeight = function () {
                return this.height;
            };

            Dimension.prototype.expand = function (dw, dh) {
                this.width += dw;
                this.height += dh;
                return this;
            };

            Dimension.prototype.getUnionedWith = function (that) {
                // intentionally using direct fields access, to support other Dimension-like objects passed as `that`
                // specifically the one which is actually returned from ViewmapUtils # createSVGtext
                return new Draw2D.Dimension(
                    Math.max(this.width, that.width), Math.max(this.height, that.height));
            };

            Dimension.prototype.getTranslated = function (dx, dy) {
                return this;
            };
        },

        Rectangle: function (x, y, w, h) {
            this._x = x;
            this._y = y;
            this._w = w;
            this._h = h;
        },

        $Rectangle$init: function (Rectangle) {
            Rectangle.prototype.getTransposed = function () {
                return new Draw2D.Rectangle(this._y, this._x, this._h, this._w);
            };

            Rectangle.prototype.getX = function () {
                return this._x;
            };

            Rectangle.prototype.getY = function () {
                return this._y;
            };

            Rectangle.prototype.getHeight = function () {
                return this._h;
            };

            Rectangle.prototype.getWidth = function () {
                return this._w;
            };

            Rectangle.prototype.getTopLeft = function () {
                return new Draw2D.Location(this._x, this._y);
            };

            Rectangle.prototype.toDebugString = function () {
                return "[" + [this.getX(), this.getY(), this.getWidth(), this.getHeight()].join() + "]";
            };

            Rectangle.prototype.getShrinked = function (insets) {
                if (insets == null) {
                    return this.getCopy();
                }
                return new Draw2D.Rectangle(
                    this._x + insets.left, this._y + insets.top,
                    this._w - insets.getWidth(), this._h - insets.getHeight()
                );
            };

            Rectangle.prototype.getCopy = function () {
                return new Draw2D.Rectangle(this._x, this._y, this._w, this._h);
            };

            Rectangle.prototype.getTranslated = function (dx, dy) {
                return new Draw2D.Rectangle(this._x + dx, this._y + dy, this._w, this._h);
            };
        },

        Insets: function (top, left, bottom, right) {
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
        },

        $Insets$init: function (Insets) {
            Insets.prototype.getHeight = function () {
                return this.top + this.bottom;
            };
            Insets.prototype.getWidth = function () {
                return this.left + this.right;
            };
        },

        Alignment: Object.freeze({
            TOP_LEFT: {debug: "TOP_LEFT"},
            BOTTOM_RIGHT: {debug: "BOTTOM_RIGHT"},
            CENTER: {debug: "CENTER"},
            FILL: {debug: "FILL"}
        }),

        Transposer: function (enabled) {
            this.enabled = enabled;
        },

        $Transposer$init: function (Transposer) {

            Transposer.prototype.transpose = function (t) {
                return (this.enabled) ? t.getTransposed() : t;
            };
        }
};
Draw2D.$initStatics();
