// simple method to transform an arbitrary Javascript object into an instance of 
// model structure, by adding a couple of useful non-enumerable properties

const idSym = Symbol();
const typeSym = Symbol();

function create(type, id, object) {
	Object.defineProperty(object, idSym, {value: id});
	Object.defineProperty(object, '_id', {get: _ => {return this[idSym];}});
	Object.defineProperty(object, typeSym, {value: type});
	Object.defineProperty(object, '_type', {get: _ => {return this[typeSym];}});
	return object;
}

module.exports = {create};