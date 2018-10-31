package com.reprezen.genflow.rapidml.nodejs

import com.fasterxml.jackson.databind.ObjectMapper
import com.reprezen.genflow.api.GenerationException
import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.rapidml.nodejs.NodejsGenerator.Generator
import java.util.List
import java.util.Map

class AppGenerator implements Generator {

	val static builtinDaoAdapters = #{
		'ES' -> './lib/daoAdapters/ES_DAO'
	}

	val static mapper = new ObjectMapper();

	val String metadataFile;
	val IGenTemplateContext context;

	new(String metadataFile, IGenTemplateContext context) {
		this.metadataFile = metadataFile;
		this.context = context;
	}

	override generate() throws GenerationException {
		val port = context.genTargetParameters.get("servicePort") ?: 3000;
		val baseUrl = context.genTargetParameters.get("baseURL");
		val linksPropertyName = context.genTargetParameters.get("linksPropertyName");

		'''
			const url = require('url');
			const express = require('express');
			const winston = require('winston');
			const expressWinston = require('express-winston');
			const fillEnv = require('./lib/fillEnv');
			const fs = require('fs');
			const walk = require('walk');
			const EventEmitter = require('events');
			const http = require('http');
			
			const defaultOptions = {
					port: '«port»',
					baseUrl: '«baseUrl»',
					mainMetadataFile: '«metadataFile»',
					otherMetadataFiles: [
						«FOR mdFile : otherMetadatas SEPARATOR ","»
							'«mdFile»'
						«ENDFOR»
					],
					daoAdapter: '«daoAdapter»',
					daoOptions: «daoAdapterOptions»,
					middlewareModules: [
						«FOR mw : middlewares SEPARATOR ','»
							'«mw»'
						«ENDFOR»
					],
					setupModules: [
						«FOR setup : setupModules SEPARATOR ','»
							'«setup»'
						«ENDFOR»
					]
			}
			
			class App extends EventEmitter {
				constructor(options = {}) {
					super();
					this.options = Object.assign({}, defaultOptions, options);
					this.options = fillEnv(this.options);
				}
				
				setup() {
					this.pSetup = loadMetadata(this.options).then(metadata => {
						this.metadata = metadata;
						this.dao = loadDAO(this.options, this);
						return createApp(this.options, this);
					}).then(app => {
						this.app = app;
						loadSetupModules(this.options, this);
					});
					return this;
				}
				
				run() {
					if (!this.pSetup) {
						setup();
					}
					this.pSetup.then(_ => {
						this.server = new http.createServer(this.app);
						this.server.listen(this.options.port, _ => {
							this.emit('ready', this.options.port);
						});
					}).catch(error => {
						console.error(error);
					});
				}
				
				close() {
					this.server.close();
				}
			}
			
			function loadMetadata(options) {
				const Metadata = require('./lib/Metadata');	let metadata = new Metadata();
				let p = metadata.ingest(options.mainMetadataFile);
				for (let mdfile of options.otherMetadataFiles) {
					p = p.then(_ => metadata.ingest(mdfile));
				}
				p = p.then(_ => metadata.resolve());
				return p.then(_ => metadata);
			}
			
			function loadDAO(options, self) {
				const DataAccessObject = require('./lib/DataAccessObject');
				let adapterClass = require(options.daoAdapter);
				return new DataAccessObject(adapterClass, options.daoOptions, self.metadata, options.baseUrl);
			}
			
			function createApp(options, self) {
				let app = new express();
				app.locals.baseUrl = options.baseUrl;
				app.locals.basePath = url.parse(options.baseUrl).pathname;
				app.locals.daoLimits = self.dao.adapter.constructor.limits;
				app.locals.linksPropertyName = '«linksPropertyName»';
				app.use(expressWinston.logger({
					transports: [new winston.transports.Console()]
				}));
				for (let mw of options.middlewareModules) {
					app.use(require(mw));
				}
				return setupPaths(app, self.dao).then(_ => {
					// this has to go after all the routes have been established
					app.use(expressWinston.errorLogger({
						transports: [new winston.transports.Console({dumpExceptions: true, showStack: true, json:true})]
					}));
					// suppress standard express error logging
					app.use((req,res,next,error) => {});
					return app;
				});
			}
			
			function setupPaths(app, dao) {
				return new Promise((resolve, reject) => {
					walker = walk.walk('handlers');
					walker.on('file', (root, stat, next) => {
						let path = `./${root}/${stat.name}`;
						let handlerClass = require(path);
						new handlerClass(app, dao);
						next();
					});
					walker.on('errors', () => reject('Failed to load resource handlers'));
					walker.on('end', () => resolve());
				});	
			}
			
			function loadSetupModules(options, self) {
				for (let setupModule of options.setupModules) {
					require(setupModule)(self.metadata, self.dao, self.app);
				}
			}
			
			if (require.main === module) {
				let app = new App()
				app.setup();
				app.on('ready', port => {
					console.log(`Listening on port ${port}`);
				});
				app.run();
			}
			
			module.exports = App;
		'''
	}

	def private getDaoAdapter() {
		val adapter = context.genTargetParameters.get('daoAdapter')
		if(builtinDaoAdapters.containsKey(adapter)) builtinDaoAdapters.get(adapter) else adapter
	}

	def private getDaoAdapterOptions() {
		var options = context.genTargetParameters.get('daoAdapterOptions') ?: "{}";
		if(options instanceof Map<?, ?>) mapper.writerWithDefaultPrettyPrinter().writeValueAsString(options);
	}

	def private getOtherMetadatas() {
		context.genTargetParameters.get('additionalMetadata') as List<String> ?: #[]
	}

	def private getMiddlewares() {
		context.genTargetParameters.get('middlewareModules') as List<String> ?: #[]
	}

	def private getSetupModules() {
		context.genTargetParameters.get('setupModules') as List<String> ?: #[]
	}
}
