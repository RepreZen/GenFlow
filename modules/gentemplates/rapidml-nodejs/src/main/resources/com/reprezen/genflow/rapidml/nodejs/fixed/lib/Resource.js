// Base class for generated resource handlers, responsible for passing off request 
// processing to the custom implementation class. The implementation class for each
// resource must be in a javascript file in a top-level project folder named "custom". 
// Within that folder its path must be <modelName>/<resourceApiName/<resourceName>.js.
//
// Individual method implementations are expected to return promises.
class Resource {

    constructor(dao) {
    	// cache of already-discovered implementation classes
    	Resource.implClasses = new Map();
    }

    callMethodImpl(resourceName, methodId, enrichments, req, res, next) {
    	let [model, api, resource] = resourceName.split('.');
        if (!Resource.implClasses.has(resourceName)) {
            let implClass = null;
            try {
                Resource.implClasses.set(resourceName, require(`../custom/${model}/${api}/${resource}Impl`));
            } catch (err) {
                // most likely custom class file does not exist so use default impl
                // and don't pointlessly try again later
                console.error(err);
                Resource.implClasses.set(resourceName, null);
            }
        }
        let implClass = Resource.implClasses.get(resourceName);
        if (implClass) {
            let impl = new implClass()
            if (impl[methodId]) {
            	// create a DAO object with this request's locals available, so they
            	// can be provided to DAO event handlers
            	let dao = this.dao.withLocals(Object.assign({}, res.app.locals, res.locals));
            	let result = impl[methodId](dao, enrichments, req, res, next);
            	if (result) {
            		result.then(_ => {
            			if (!res.headersSent) {
            				next();
            			}
            		});
            	} else if (!res.headersSent) {
            		res.status(500).send({errorMessage: 'Unexpeted service error - please contact support'});
            		next(new Error(`Method failed to send a response: req.originalUrl`));
            	}
				return;
            }
        }
        // if we get here, we either couldn't find an impl class, or that class had no
        // implementation of the method invoked by the current request
        throw new Error(`No implementation for method ${this.resourceFQN}.${methodId}`)
    }
}

module.exports = Resource;
