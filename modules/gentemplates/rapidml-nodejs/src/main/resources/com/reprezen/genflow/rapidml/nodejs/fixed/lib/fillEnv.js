// function used in the app builder to inject environment
// variables into configuration parameters as specified in the GenTarget.
// Any string in the passed structure is filled by replacing anything of
// the form `${<name>}` with the value of the named environment var.
function fillEnv(obj) {
    let regex = /\$\{([a-z0-9_]+)\}/gi;
    switch(typeof obj) {
    case 'string':
        obj = obj.replace(regex, (match, p1) => process.env[p1] );
        break;
    case 'object':
        if (Array.isArray(obj)) {
            obj = obj.map(x => fillEnv(x));
        }
        else {
            for (key in obj) {
                obj[key] = fillEnv(obj[key]);
            }
        }
        break;
    }
    return obj;
    
}

module.exports = fillEnv;