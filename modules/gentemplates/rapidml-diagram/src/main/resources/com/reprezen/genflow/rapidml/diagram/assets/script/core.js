d3.custom = {};

var CoreUtils = {
    findFirst: function (arr, condition) {
        var result = null;
        if (arr.length > 0) {
            arr.some(function (next) {
                if (condition(next)) {
                    result = next;
                    return true;
                }
                return false;
            });
        }
        return result;
    }
};