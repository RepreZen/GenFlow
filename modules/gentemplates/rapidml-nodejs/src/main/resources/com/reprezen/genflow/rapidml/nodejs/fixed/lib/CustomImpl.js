// Base class for custom resource implementations
//
// This class provides a number of utility methods that can be used
// by method implementations to handle query parameters and to 
// inject values for unbound template parameters in links.
class CustomImpl {
    
    // Inject a value for a template parameter in set of objects.
	// The path indicates the location of the link structure
	// within each object, and rel name identifies the
	// specific link
	fixLinks(req, objs, path, param, value, rel = 'related') {
        doFixLinks(objs, path.split('.'), param, value, rel, req.app.locals);
    }

	// Handle paging parameters, if they exist. Default param names are
	// `pageSize` and
	// `pageNum`. If the DAO imposes a maximum page size, that's enforced.
	// Returns true if the options are valid, with corresponding DAO options set
	// in the options object. Otherwise, appropriate error objects are added to
	// the passed array.
	processPaging(req, options, errors,  msgs = {}, sizeParamName = "pageSize", numParamName = "pageNum", defaultSize = 100, defaultNum = 1) {
        options.paging = {pageSize: defaultSize, pageNum: defaultNum};
        let pageSize = req.query[sizeParamName];
        let pageNum = req.query[numParamName];
        let result = true;
        let maxDepth = req.app.locals.daoLimits.maxQueryDepth;
        
        if (ztrue(pageSize)) {
            let parsed = parseInt(pageSize);
            if (intCheck(pageSize) && parsed >= 1 && (maxDepth === undefined || parsed <= maxDepth+1)) {
                options.paging.pageSize = pageSize;
            } else {
                errors.push({message: msgs.size || `The pageSize  parameter must be a positive integer and must not exceed ${maxDepth+1}`});
                result = false
            }
        }
        if (ztrue(pageNum)) {
            let parsed = parseInt(pageNum);
            if (intCheck(pageNum) && parsed >= 1) {
                options.paging.pageNum = pageNum;
            } else {
                errors.push({message: msgs.num || "The pageNum parameter must be a positive integer"});
                result = false;
            }
        }
        if (maxDepth !== undefined && options.paging.pageSize * (options.paging.pageNum-1) > maxDepth) {
            errors.push({message: msgs.maxDepth || "The requested page exceeds the maximum result depth of ${maxDepth}"});
            result = false;
        }
        return result;
    }
    
	// Handle query parameters with an enumerated set of valid values.
	// Validation is case-insensitive. If the query param passes validation, its
	// value (in the request object) is normalized to a fully upper-cased value.
	// Otherwise an error object is added. DAO options are not affected by this
	// operation.
	validateEnum(req, options, errors, paramName, enumValues, required, msgs = {}) {
        let value = req.query[paramName];
        if (required && !value) {
            errors.push({message:  msgs.required || `The ${paramName} parameter is required`});
            return false;
        } else if (value !== undefined) {
        	req.query[paramName] = value = value.toUpperCase();
        	if (enumValues.indexOf(value) < 0) {
        		errors.push({message: msgs.value || `The ${paramName} parameter must be one of: ${enumValues.join(', ')}`});
        		return false;
        	}
        }
        return true;
    }
    
	// Handle integer parameters, potentially with range limits. Returns true if
	// the
	// value is acceptable, else new error objects will be added to the errors
	// array
	validateInt(req, options, errors, paramName, min, max, required, msgs = {}) {
        let value = req.query[paramName];
        if (required && !value) {
            errors.push({message: msgs.required || `The ${paramName} parameter is required`});
            return false;
        } else if (!value) {
            return true;
        }else if (!intCheck(value)) {
            errors.push({message: msgs.int || `The ${paramName} parameter must be an integer`});
            return false;
        } else {
            value = parseInt(value);
            if (ztrue(min) && value < min || ztrue(max) && value > max) {
                errors.push({message: msgs.range || `The ${paramName} parameter must be ${intCheckRangeMsg(min, max)}`});
                return false;
            } else {
                return true;
            }
           
        }
        return true;
    }
    
	// handle a boolean query parameter. Values 'true' and 1 are both considered
	// true, and 'false' and '0' are considered false. Optionally, an empty
	// param value (meaning the query param appears without a value in the URI)
	// can be interpreted as true.
	//
	// If the param value is acceptable, the value in the request object is
	// normalized to the corresponding Javascript boolean value. Otherwise error
	// objects are added.
	validateBool(req, options, errors, paramName, emptyIsTrue = true, required, msgs = {}) {
        let value = req.query[paramName];
        if (typeof value == 'string') {
            switch(value.toLowerCase()) {
            case '':
                value = !!emptyIsTrue;
                break;
            case '1':
            case 'true':
                value = true;
                break;
            case '0':
            case 'false':
                value = false;
                break;
            default:
                errors.push({message: msgs.value || `The ${paramName} parameter must be either 1 or true (true values) or 0 or false (false values)`});
                return false;
            }
            req.query[paramName] = value;
        } else if (required && !value) {
            errors.push({message: msgs.required || `The ${paramName} parameter must be specified`});
            return false;
        }
        return true;
    }
}


function intCheck(intStr) {
    return !intStr || /^[-+]?[0-9]+$/.test(intStr.trim());
}

function intCheckRangeMsg(min, max){
    if (ztrue(min)&& !ztrue(max)) {
        return `at least ${min}`;
    } else if (ztrue(max) && !ztrue(min)) {
        return `at most ${max}`;
    } else {
        return `between ${min} and ${max}`;
    }
}

function ztrue(x) {
    // zero-truthiness - like truthiness but zero is truthy
    return x || x === 0;
}


function doFixLinks(objs, path, param, value, rel, locals) {
    let regex = new RegExp(`/(:${param})(/|$)`, 'g');
    if (objs) {
        objs = [].concat(objs);
        for (let obj of objs) {
            if (path.length > 0 && path[0].length > 0) {
                doFixLinks(obj[path[0]], path.slice(1), param, value, rel, locals);
            } else {
                let links = obj && obj[locals.linksPropertyName];
                let relNode = links && links[rel];
                if (relNode && relNode.href) {
                    relNode.href = relNode.href.replace(regex, (match, p1, p2) => `/${value}${p2}`);
                }
            }
        }
    }

}

module.exports = CustomImpl;