const ESClient = require('elasticsearch').Client;
const StructureInstance = require('../StructureInstance');

class ES_DAO{
	
    constructor(clientOpts, metadata, dao) {
    	this.client = new ESClient(clientOpts);
    	this.metadata = metadata;
    	this.dao = dao;
    	
    	this.esSearch = this.client.search.bind(this.client);
    	this.esGet = this.client.get.bind(this.client);
    	this.esMget = this.client.mget.bind(this.client);
    }
    
    search(type, query, options) {
    	return esQuery(this.esSearch, type, query, options, this);
    }
    
    getById(type, id, options) {
    	return esQuery(this.esGet, type, {id}, options, this).then(result => {
    		return result[0];
    	});
    }
    
    getByIds(type, ids, options) {
    	return esQuery(this.esMget, type, {ids}, options, this);
    }
    
    getByFieldValues(type, field, values, options) {
    	values = values.filter(x => x);
    	if (values.length === 0) {
    		return Promise.resolve([]);
    	} else {
    		options.filters = [{field, type: 'in', args: values}].concat(options.filters);
    		return esQuery(this.esSearch, type, {}, options, this);
    	}
    }
}
    
function esQuery(method, type, query, options, self) {
	try {
		let [esOptions, extractor] = rewriteOptions(options, type, self);
//		console.log(JSON.stringify(esOptions, null, 2));
		return method(Object.assign({}, query, esOptions)).then(data => {
			let hits = extractor(data);
			return hits;
		});
	} catch (err) {
		return Promise.reject(err);
	}
}

function rewriteOptions(options, type, self) {
	let esOptions= {};
    let extractorData = {}
    type = self.metadata.resolveType(type);
    let resolver = {resolve: field => self.metadata.resolveField(field, type)};
    extractorData.typeName = rewriteType(type, esOptions);
    // keep track of fields that need to be renamed after retrieval
    extractorData.fieldAdjustments = rewriteFields(options.fields, options.transients, type, resolver, esOptions);
    rewriteOrder(options.order, resolver, esOptions);
    rewriteSearch(options.search, resolver, esOptions);
    rewriteFilters(options.filters, resolver, esOptions);
    rewritePaging(options.paging, esOptions);
    return [esOptions, extractHits.bind(null, extractorData)];
}

function rewriteType(type, esOptions) {
    esOptions.type = type.dsName || type.name;
    esOptions.index = type.index;
    if (!esOptions.index) {
       throw new Error("DAO query does not supply ElasticSearch 'index' - check metadata overlay");
   }
   return type.name
}

function rewriteFields(fields, transients, type, resolver, esOptions) {
    let props = new Set();
    let renames = new Map();
    if (fields) {
        for (let field of fields) {
        	field = resolver.resolve(field);
            props.add(field.dsName || field.name);
            if (field.dsName && field.dsName !== field.name) {
                renames.set(field.dsName, field.name);
            }
        }
    }
    if (type.transients) {
    	transients = transients || new Set();
    	type.transients.forEach(t => transients.add(t));
    }
    if (transients) {
        for (let transient of transients) {
        	transient = resolver.resolve(transient);
        	let name = transient.dsName || transient.name;
        	// a prop may be listed as both normal and transient,
        	// and in this case it's treated as non-transient 
        	if (!props.has(name)) {
        		props.add(name);
        		renames.set(name, '!');
        	}
        }
    }
    esOptions._source = [...props];
    return renames;
}

function rewriteOrder(order, resolver, esOptions) {
    if (order) {
        let sort = [];
        for (let [field, dir] of order) {
        	field = resolver.resolve(field);
            sort.push((field.dsName || field.name) + ':' + dir.toLowerCase());
        }
        esOptions.sort = sort;
    }
}

function rewriteSearch(search, resolver, esOptions) {
    if (search) {
        let queryItems = [];
        let queryAll = null;
        for (let field of search.fields) {
            if (field === '*') {
                queryAll = [{match: {_all: {query: search.phrase, fuzziness: 'auto'}}}];
            } else {
            	field = resolver.resolve(field);
            	let fieldName = field.dsName || field.name;
                queryItems.push({match: {[fieldName]: {query: search.phrase, fuzziness: 'auto'}}});
            }
        }
        esOptions.body = {query: {bool: {should: queryAll ? queryAll : queryItems}}};
    }
} 


function rewriteFilters(filters, resolver, esOptions) {
    let filterItems = [];
    for (let filter of (filters || []).filter(f=>f)) {
        filterItems.push(createFilter(resolver.resolve(filter.field), filter.type, filter.args));
    }
    if (filterItems.length) {
    	if (esOptions.body) {
    		// If we already have a search query, it must have been added by
    		// `rewriteSearch`, and so must be a `bool` query with a `should`
    		// branch. We'll add our filters to a new `filter` branch and
    		// make sure that at least one `should` clause matches.
    		esOptions.body.query.bool.filter= filterItems;
    		esOptions.body.query.bool.minimum_should_match = 1;
    	} else {
    		// Otherwise we'll submit a pure filter
    		esOptions.body = {filter: {bool: {filter: filterItems}}};
    	}
    }
}

function createFilter(field, type, args) {
	let fieldName = field.dsName || field.name;
	switch(type) {
    case 'missing': return {missing: {field: fieldName}};
    case 'exists': return {exists: {field: fieldName}};
    case 'in': return {terms: {[fieldName]: args}};
    case 'is': return {term: {[fieldName]: args}};
    case 'le': return {range: {[fieldName]: {lte: args}}};
    case 'lt': return {range: {[fieldName]: {lt: args}}};
    case 'ge': return {range: {[fieldName]: {gte: args}}};
    case 'gt': return {range: {[fieldName]: {gt: args}}};
    default:
    	throw new Erorr(`Unknown filter type: ${type}`);
	}
}

function rewritePaging(paging, esOptions) {
	paging = Object.assign({}, {pageSize: 10000, pageNum: 1}, paging);
    esOptions.from = paging.pageSize * (paging.pageNum - 1);
    esOptions.size = paging.pageSize;
}

function extractHits(extractData, esData) {
	if (esData.hits) {
		let hits = esData.hits.hits.map(hit => extractHit(hit, extractData));
		hits._total = esData.hits.total;
		return hits;
	} else if (esData._source) {
		return [extractHit(esData, extractData)];
	} else {
		return [];
	}
}

function extractHit(hit, extractData) {
    let object = hit._source;
    for (let [from, to] of extractData.fieldAdjustments.entries())  {
        // capture value and remove property
        let value = object[from];
        delete object[from];
        if (to === '!') {
            // hide the property
            Object.defineProperty(object, from, {value});
        } else {
            // install under correct name
            object[to] = value;
        }
    }
    return StructureInstance.create(extractData.typeName, hit._id, object);
}

ES_DAO.limits = {
        maxQueryDepth: 9999
}

module.exports = ES_DAO;
/* test-code */
module.exports._test_ = {
        rewriteType,
        rewriteFields,
        rewriteOrder,
        rewriteSearch,
        rewritePaging
};
