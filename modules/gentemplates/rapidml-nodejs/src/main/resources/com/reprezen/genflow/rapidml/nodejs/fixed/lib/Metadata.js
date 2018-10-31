// this module implements a run-time representation of models relevant 
// to this implementation. Currently only structures are represented, but
// eventually this will be superseded by a full representation of all
// model elements.
//
// The Metadata class parses a YAML file produced by the generator and
// constructs an internal representation that's used heavily by the DAO
// and other modules.
//
// The generated metadata should be loaded first, but then other, 
// similarly-structured YAML files can be loaded on top of the base
// generated data. This can be done for a variety of reasons, some
// principle ones being:
// 
// 1. Augmenting the model data with information needed to tie the model
// to the data source, e.g. by providing column/table names corresponding to
// model elements, providing information about keys for join operations,
// etc.
//
// 2. Adding access-control metadata to support restricted access to methods
// and/or data, based on credentials tied to individual requests

const fs = require('fs');
const merge = require('merge');
const yaml = require('js-yaml');

class Metadata {
    constructor(){
        this.models = {};
    }

    // load metadata from a sequence of yaml files. Each file's data
    // overlays previously loaded data, in the fashion of the NPM `merge`
    // module.
    ingest(files) {
        files = [].concat(files);
        let ingester = function(prior, file) {
            return prior.then(metadata => {
                try {
                    let json = parseJson(file);
                    merge.recursive(metadata, json);
                    return metadata;
                } catch(err) {
                    return Promise.reject(err);
                }
            });
        };
        return [Promise.resolve(this)].concat(files).reduce(ingester);
    }

    // The resolve traverses the metadata and find cross references (e.g.
	// `refType` of a reference property. The references appear initially in the
	// form of a name, but resolution replaces the names with actual object
	// references.
    resolve() {
        // some fixups must be executed after the overall resolving scan of the
		// metadata structure. These fixups, in the form of bound functions, are
		// accumulated in an ephemeral '_fixups' property of the metadata, which
		// is not enumerable and therefore will not be mistaken for a model name
        Object.defineProperty(this, '_fixups', {value: [], configurable: true});
        for (let modelName in this.models) {
            resolveModel(modelName, this);
        }
        for (let fixup of this._fixups) {
            fixup();
        }
        return this;
    }

    // find a model by name
    model(name) {
        return this.models[name];
    }
    
    // resolve a type name to a Structure object. Type name can be fully or
	// partially qualified, and is resolved along the path to the given context
	// type, if provided. E.g. a simple name will resolve to a sibling of the
	// context type.
    resolveType(type, contextType) {
        if (type instanceof Structure) {
            return type;
        }
        let parts = type.split('.');
        let resolved;
        switch (parts.length) {
        case 1:
            resolved = contextType._dataModel.structures[parts[0]];
            break;
        case 2:
            resolved = contextType._dataModel._model.dataModels[parts[0]].structures[parts[1]];
            break;
        case 3:
            resolved = this.models[parts[0]].dataModels[parts[1]].structures[parts[2]];
            break;
        }
        if (resolved) {
            return resolved;
        } else {
            throw new Error(`Unknown type name in metadata: ${type}`);
        }
    }

    // Return the field object of a given name in the given structure type
    resolveField(field, type) {
        if (field instanceof Field) {
            return field;
        } else {
            return type.fields[field] || {name: field};
        }
    }

    // ES6 doesn't have members besides functions as part of its class
	// declaration syntax. Following is part of creating something like Java's
	// inner static class. So e.g. our Model class is accessible via
	// Metadata.Model. There's another part of this construction that occurs
	// outside this class, where we set, for example, MetaData.Model = Model.
    get Model() {
        return this.constructor.Model;
    }
    
    get DataModel() {
        return this.constructor.DataModel;
    }
    
    get Structure() {
        return this.constructor.Structure;
    }
    
    get Field() {
        return this.constructor.Field;
    }
}

// locate a metadata node by its path (array of path components). Maybe create
// an empty object there if it doesn't exist.
function find(metadata, path, create = true) {
    let node = metadata;
    for (let name of path) {
        if (!node[name]) {
            if (create) {
                node[name] = {};
            } else {
                return null;
            }
        }
        node = node[name];
    }
    return node;
}


// Model class - represents a RAPID model, stored as an immediate child of root
// in the metadata structure
class Model {
    constructor(name, data, metadata) {
        data.apis = data.apis || {};
        data.dataModels = data.dataModels || {};
        Object.assign(this, data);
        // make these backpointers unenumerable to make console output of
		// metadata a little cleaner
        Object.defineProperties(this, {_name: {value: name}, _metadata: {value: metadata}});
    }
    
    api(name) {
        return this.apis[name];
    }
    
    dataModel(name) {
        return this.dataModels[name];
    }
}
Metadata.Model = Model;


function resolveModel(name, metadata) {
    metadata.models[name] = new Model(name, metadata.models[name], metadata);
    let model = metadata.models[name];
    for (let apiName in model.apis) {
        resolveApi(model.apis[apiName], model, metadata);
    }
    for (let dmName in model.dataModels) {
        resolveDataModel(dmName, model, metadata);
    }
}

function resolveApi(api, model, metadata) {
    // NOT YET IMPLEMENTED
}


// DataModel class - represents a RAPID DataModel 
class DataModel {
    constructor(name, data, model, metadata) {
        data.structures = data.structures || {};
        data.enums = data.enums || {};
        data.simpleTypes = data.simpleTypes || {};
        Object.assign(this, data);
        Object.defineProperties(this, {_name: {value: name}, _model: {value: model}, _metadata: {value: metadata}});
    }

    structure (name) {
        return this.structures[name];
    }
    
    enum(name) {
        return this.enums[name];
    }
    
    simpleType(name) {
        return this.simpleTypes[name];
    }
}
Metadata.DataModel = DataModel;

function resolveDataModel(name, model, metadata) {
    model.dataModels[name] = new DataModel(name, model.dataModels[name], model, metadata);
    let dm = model.dataModels[name];
    for (let structName in dm.structures) {
        resolveStructure(structName, dm, metadata);
    }
}

// Structure class - represents a RAPID Structure
class Structure {
    constructor(name, data, dataModel, metadata) {
        data.fields = data.fields || {};
        Object.assign(this, data);
        Object.defineProperties(this, {_name: {value: name}, _dataModel: {value: dataModel}, _metadata: {value: metadata}});
    }
    
    field(name) {
        return this.fields[name];
    }
}
Metadata.Structure = Structure;

function resolveStructure(name, dataModel, metadata) {
    dataModel.structures[name] = new Structure(name, dataModel.structures[name], dataModel, metadata);
    let struct = dataModel.structures[name];
    for (let f in struct.fields) {
        resolveField(f, struct, metadata);
    }
}

// Field class - represents a Structure property in a RAPID model
class Field {
    constructor(data, struct, metadata) {
        Object.assign(this, data);
        Object.defineProperties(this, {_struct: {value: struct}, _metadata: {value: metadata}});
    }
}
Metadata.Field = Field;

function resolveField(fieldName, struct, metadata) {
    struct.fields[fieldName] = new Field(struct.fields[fieldName], struct, metadata);
    let field = struct.fields[fieldName]
    if (field.type === 'ref') {
        // can only reliably resolve types appearing in fields after the overall
        // metadata resolution has completed, because we may encounter
        // references to types that have not yet been visited. So we create
        // fixups that will execute at the end of the overall resolve operation.
        metadata._fixups.push(resolveTypesInField.bind(null, field, struct, metadata));
    }
}

function resolveTypesInField(field, contextType, metadata) {
    field.refType = metadata.resolveType(field.refType, contextType);
    if (field.via) {
        field.via.joinType = metadata.resolveType(field.via.joinType, contextType);
    }
}


function parseJson(file) {
    let text = fs.readFileSync(file);
    if (file.endsWith("\.yaml")) {
        return yaml.safeLoad(text, 'utf-8');
    } else {
        return JSON.parse(text);
    }
}

module.exports = Metadata;