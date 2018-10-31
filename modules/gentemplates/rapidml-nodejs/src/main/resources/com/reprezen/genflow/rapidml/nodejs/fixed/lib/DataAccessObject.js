const Metadata = require('./Metadata');
const EventEmitter = require('events');

/**
 * This class provides access to the data store.
 * 
 * The latter is expected to implement four methods: search, getById, getByIds,
 * and getByFieldValues.
 * 
 * These are all methods of this DAO class as well, though there is no
 * superclass/subclass relationship between the DAO class and its adpaters.
 * 
 * The search method is for open-ended queries and exists mostly as a means of
 * directly accessing the data store if needed. The difference between using
 * DAO's search method and directly using the adapter is that
 * adapter-independent processing is not available by the latter route. This
 * includes automatic data enrichment, and publication of DAO events.
 * 
 * Each of these methods has a corresponding enriching method. They are named
 * `eSearch`, `eGetById`, eGetByIds`, and `eGetByFieldValues`. These methods all
 * take the results of the base query and then perform additional queries to get
 * data needed to realize reference properties. Enrichment structures are
 * included in generated method handlers, so custom implementations generally
 * need not consider realization requirements at all.
 */

class DataAccessObject extends EventEmitter {
    constructor(adapterClass, adapterOptions, metadata, baseUrl) {
    	super();
        this.adapter = new adapterClass(adapterOptions, metadata, this);
        this.metadata = metadata;
        this.baseUrl = baseUrl;
    }

    // creates a DAO copy with access to per-request data. This data is primarly
	// useful in event handlers for DAO events.
    withLocals(locals) {
    	let augmentedDao = Object.assign({locals}, this);
    	Object.setPrototypeOf(augmentedDao, this.constructor.prototype);
    	return augmentedDao;
    }
    
    resolveType(type, contextType) {
        return this.metadata.resolveType(type, contextType);
    }
    
    resolveField(field, type) {
        return metadata.resolveField(field, type);
    }
    
    // execute an arbitrary query in the adapter, to obtain objects of the given
	// type.
    // options include things like filter criteria, search phrase, sort
	// criteria, etc.
    search(type, query, options = {}) {
        type = this.resolveType(type);
        emitLoad(type, options, this);
    	return emitLoaded(this.adapter.search(type, query, options), type, this);

    }
    
    // obtain a single object of the given type, with the given id value
    getById(type, id, options = {}) {
        type = this.resolveType(type);
        emitLoad(type, options, this);
        return emitLoaded(this.adapter.getById(type, id, options), type, this);
    }
    
    // obtain a list of objects of the given type, with the given id values
    getByIds(type, ids, options = {}) {
        type = this.resolveType(type);
        emitLoad(type, options, this);
        return emitLoaded(this.adapter.getByIds(type, ids, options), type, this);
    }
    
    // obtian a list of objects of the given type, with any of the given values
	// for the indicate field
    getByFieldValues(type, field, values, options = {}) {
        type = this.resolveType(type);
        emitLoad(type, options, this);
        return emitLoaded(this.adapter.getByFieldValues(type, field, values, options), type, this);
    }
    
    // auto-enriching version of search
    eSearch(query, e, options = {}) {
        let [type, multi, fields, transients, enrichments] = getEnrichmentInfo(e, this.metadata);
        let p = this.search(type, query, Object.assign({fields, transients}, options));
        return p.then((result) => this.enrich(type, multi, result, enrichments, this));
    }
    
    // auto-enriching version of getById
    eGetById(id, e, options = {}) {
        let [type, multi, fields, transients, enrichments] = getEnrichmentInfo(e, this.metadata);
        let p1 = this.getById(type, id, Object.assign({fields, transients}, options));
        let p2 = p1.then((result) => this.enrich(type, multi, [result], enrichments, this));
        return p2.then(objs => objs[0]);
    }
    
    // auto-enriching version of getByIds
    eGetByIds(ids, e, options = {}) {
        let [type, multi, fields, transients, enrichments] = getEnrichmentInfo(e, this.metadata);
        let p = this.getByIds(type, ids, Object.assign({fields, transients}, options));
        return p.then((result) => this.enrich(type, multi, result, enrichments, this));
    }
    
    // auto-enriching version of getByFieldValues
    eGetByFieldValues(field, values, e, options = {}) {
        let [type, multi, fields, transients, enrichments] = getEnrichmentInfo(e, this.metadata);
        let p = this.getByFieldValues(type, field, values, Object.assign({fields, transients}, options));
        return p.then((result) => this.enrich(type, multi, result, enrichments, this));
    }

    // enrich the given objects of the given type, using the given enrichment
	// data
    enrich(type, multi, objects, enrichments = {}, dao) {
        if (objects.length === 0 || Object.keys(enrichments).length === 0) {
            return Promise.resolve(objects);
        }
        // perform all field enrichments in parallel
        let promises = [];
        for (let fieldName in enrichments) {
            let field = this.metadata.resolveField(fieldName, type);
            let [subType, subMulti, subFields, subTransients, subEnrichments, link] =
                getEnrichmentInfo(enrichments[fieldName], this.metadata, type);
            let p = enrichField(type, objects, subMulti, field, subFields, subTransients, link, this).then(([subObjs, map]) => {
            	// recursively enrich objects that were obtained to enrich this
				// field
            	return this.enrich(subType, subMulti, subObjs, subEnrichments, dao).then(() => { 
                    return [field, map, subMulti];
                })
            });
            promises.push(p);
        }
        // wait for all field enrichments to complete
        let all = Promise.all(promises);
        return all.then((results) => {
        	// "stitch" all the enrichments into the original objects, which
			// means setting properties in the incoming objects to the enriching
			// objects. Per-field enrichment results include the enriching
			// objects in a map whose keys are key values contained in the
			// original objects.
        	for (let [field, map, multi] of results) {
            	stitch(objects, field, map, multi);
            	for (let object of objects) {
            		emitEnriched(object, object[field.name], type, field, dao);
            	}
            }
            emitFullyEnriched(objects, type, dao);
            // incoming objects are enriched in-place, so result is same array
			// of objects
            return objects;
        });
    }
}

function getEnrichmentInfo(e, md, contextType) {
	// construct detailed information needed to perform enrichment of a given
	// field, found in the passed in enrichment structure (parameter 'e'). There
	// are some special cases, and "transients" are added for key fields that
	// link referring and referent objects (obtained from manually augmented
	// metadata). Transients are properties that are retrieved from the data
	// store but excluded from actual API responses.
	let [type, multi, fields, enrichments, link] =
        [e.type, e.multi, [...e.fields], e.enrichments, e.link];
    type = md.resolveType(type, contextType);
    let transients = new Set();
    for (refField in enrichments) {
        refField = md.resolveField(refField, type);
        if (refField.name === 'ObjectResourceLink') {
            // special case - "enriching" this will create a link in the current
            // object, and any decorations called for in that link must also
            // occur in the current object
            fields = fields.concat(enrichments[refField.name].fields);
            let params = enrichments[refField.name].link.boundParams;
            for (let param in params) {
            	transients.add(params[param]);
            }
        }
        transients.add(refField.leftKey);
    }
    if (link) {
    	for (param in link.boundParams) {
    		 transients.add(link.boundParams[param]);
    	}
    }
    return [type, multi, fields, transients, enrichments, link];
}

// returns [referrents, map], where referrents is a list of all the referrent
// objects, and map links referrer key ids to referrent
function enrichField(type, objects, subMulti, field, subFields, transients, link, dao) {
    let p;
    
    if (field.name === 'ObjectResourceLink') {
        // ObjectResourceLInk is a special case of a collection object with a
        // message response that is
        // based on an object resource for the same structure (I think :-p ). We
        // don't actually do any ref field embedding
        // (since there's no ref field involved), but we do interpolate a link
        // for each object.
        p = Promise.resolve([objects, null]);
    } else if (subFields.length === 0 && !subMulti) {
        // We do something similar for a single-valued object with no embedded
        // fields, indicating
        // that this is a pure reference link for a collection.
        // The only difference is that the links will go into new objects, not
        // the objects containing the reference.
        // Note that in this case, getEnrichments is guaranteed not to have
        // added any transient fields, since
        // a pure link, not having any embedded fields, cannot possibly have any
        // downstream enrichments.
        let newObjs = [];
        for (let obj of objects) {
            let newObj = {};
            obj[field.name] = newObj;
            newObjs.push(newObj);
        }
        p = Promise.resolve([newObjs, null]);
    } else {
    	// give the event handler a chance to provide enrichment results
		// directly
    	let referrents = emitEnrich(type, objects, field, subFields, dao);
    	if (referrents) {
    		p = Promise.resolve([referrents, mapReferrents(referrents, field.rightKey)]);
    	} else {
    		// if nothing from handler, we do standard automatic enrichment
    		p = (field.via ? enrichViaField : enrichDirectField)(objects, field, subFields, transients, dao);
    	}
    }
    return p.then(result => {
    	// fill in a link if called for in this enrichment
    	if (link) {
            let [objs, _] = result;
            for (let obj of objs) {
                enrichLink(type, obj, link, dao);
            }
        }
        return result;
    });
}

function enrichDirectField(objects, field, subFields, transients, dao){
	// this enrichment is used when the referring object and the referrent
	// object are directly linked by key values. In metadata, the reference
	// field in the referring object type has `leftKey` and `rightKey`
	// properties that identify those key fields in the referring and referrent
	// objects, respectively.
	let referrentIds = new Set(objects.map(o => o[field.leftKey]));
    transients.add(field.rightKey);
    (field.transients || []).map(t => transients.add(t));
    let options = {fields: subFields, transients, order: field.order, filters: field.filters};
    let p = dao.getByFieldValues(field.refType, field.rightKey, [...referrentIds], options);
    return p.then(referrents => {
        let map = mapReferrents(referrents, field.rightKey);
        return [referrents, map];
    });
}

function enrichViaField(objects, field, subFields, transients, dao) {
	// this enrichment is used when the referring object and referrent object
	// are linked via an intermediate "join table". In this case, the metadata
	// for the reference field needs a 'via' property with a `joinType` and its
	// own `leftKey` and `rightKey` properties. A two-stage enrichment occurs,
	// first between referring object and join type (using `leftKey` and
	// `via.leftKey` properties), and then between join type and referrent type
	// (using `via.rightKey` and `rightKey` properties).
	let referrentIds = new Set(objects.map(o => o[field.leftKey]));
    let options = {fields: [field.leftKey, field.rightKey]};
    let viaMap;
    let p1 = dao.getByFieldValues(field.via.joinType, field.via.leftKey, [...referrentIds], options);
    let p2 = p1.then(vias => {
        viaMap = mapReferrents(vias, field.via.rightKey);
        let viaKeys = new Set(vias.map(via => via[field.via.rightKey]));
        transients.add(field.rightKey);
        (field.transients || []).map(t => transients.add(t));
        let options = {fields: subFields, transients, order: field.order, filters: field.filters};
        return dao.getByFieldValues(field.refType, field.via.rightKey, [...viaKeys], options);
    });
    return p2.then(referrents => {
    	// compute an overall map from key values found in referring objects to
		// lists of referrent objects, just as with direct (non-via) enrichment
        let map = mapViaResults(referrents, viaMap, field.via.leftKey, field.rightKey); 
        return [referrents, map];
    })
}

function enrichLink(type, obj, link, dao) {
	// compute a link called for by realization and add it to the object
	let href = link.href;
    let params = link.boundParams;
    // fill in any bound parameters, using properties from the object. It is up
	// to custom code to fill in unbound link params.
    for (name in params) {
        let regexp = new RegExp('/:' + name + '(/|$)', 'g');
        let value = obj[params[name]];
        if (value != null && value != undefined) {
            href = href.replace(regexp, ((match, p1) => '/' + value + p1));
        }
    }
    href = dao.baseUrl + href;
    let linkObject = {[link.rel]: {href}};
    emitEnrichedLink(type, obj, linkObject, dao);
    obj[dao.locals.linksPropertyName] = linkObject;
}

function mapReferrents(referrents, keyField) {
    let map = new Map();
    for (referrent of referrents) {
        let key = referrent[keyField];
        if (map.has(key)) {
            map.get(key).push(referrent);
        } else {
            map.set(key, [referrent]);
        }
    }
    return map;
}

function mapViaResults(referrents, viaMap, leftKey, rightKey) {
    // note that the incoming viaMap is reversed - maps rightKey to leftKey in
    // join table
    let map = new Map();
    for (let referrent of referrents) {
        let rightId = referrent[rightKey];
        for (joinObj of viaMap.get(rightId) || []) {
            let leftId = joinObj[leftKey];
            if (map.has(leftId)) {
                map.get(leftId).push(referrent);
            } else {
                map.set(leftId, [referrent]);
            }
        }
    }
    return map;
}

function stitch(objects, field, map, multi) {
    if (map) {
        for (let obj of objects) {
            let values = map.get(obj[field.leftKey]);
            if (values) {
                obj[field.name] = multi ? values : values[0];
            } else if (multi) {
                obj[field.name] = [];
            }
        }
    }
}

// `load` event emitted when objects are about to be loaded from the dao.
// The options object may be altered as needed to affect the query.
function emitLoad(type, options, dao) {
	dao.emit('load', type, options, dao.locals);
	dao.emit(['load', type.name].join('_'), type, options, dao.locals);
}

// `loaded` event emitted when objects are loaded from the data store. The
// objects can be modified by the handler.
function emitLoaded(objectsPromise, type, dao) {
	return objectsPromise.then(objects => {
		let objectsArray =  Array.isArray(objects) ? objects : [objects];
		dao.emit('loaded', type, objectsArray, dao.locals);
		dao.emit(['loaded', type.name].join('_'), objectsArray, dao.locals);
		return objects;
	});
}

// 'enrich' event emitted when an enrichment is about to be attempted. Handler
// can provide its own values, which will cause automatic enrichment to be
// suppressed. Recursive enrichment still takes place in this case, using the
// handler-provided objects
function emitEnrich(type, objects, field, subFields, dao) {
	let results = {};
	dao.emit('enrich', type, field, objects, subFields, results, dao.locals);
	dao.emit(['enrich', type.name, field.name].join('_'), objects, subFields, results, dao.locals);
	return results.referrents;
}

// 'enriched' event emitted when an enrichment has completed for a given field.
// Handler can modify the enriched object or the objects enriching that field.
function emitEnriched(enrichedObject, enrichingObjects, type, field, dao) {
	dao.emit('enriched', type, field, enrichedObject, enrichingObjects, dao.locals);
	dao.emit(['enriched', type.name, field.name].join('_'), enrichedObject, enrichingObjects, dao.locals);
}

// 'enrichedLink' event emitted when enrichment has created a link.
// Handler can model modify the link
function emitEnrichedLink(type, enrichedObject, link, dao) {
	dao.emit('enrichedLink', type, link, enrichedObject, dao.locals);
	let linkRel = Object.keys(link)[0];
	dao.emit(['enrichedLink', type.name, linkRel].join('_'), link, enrichedObject, dao.locals);
}

// 'fullyEnriched' event emitted when an object has been fully enriched. Handler
// can modify the enriched object.
function emitFullyEnriched(objects, type, dao) {
	let objectsArray = Array.isArray(objects) ? objects : [objects];
	dao.emit('fullyEnriched', type, objectsArray, dao.locals);
	dao.emit(['fullyEnriched', type.name].join('_'), objectsArray, dao.locals);
}

module.exports = DataAccessObject;
