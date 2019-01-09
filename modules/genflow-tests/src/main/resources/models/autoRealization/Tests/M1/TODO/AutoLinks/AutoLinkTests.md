# AutoLink Tests

## Description

This test suite exercises autoLinking features in the realizationModel.  There are 
several parts of the realizationModel that support this: 

* The `autoLinkableToObjectResource` selector matches any object reference whose type has 
  a corresponding _autoLink objectResource_ in the current scope. A resource may be 
  designated as the autoLink objectResource implicitly, by being the only objectResource
  bound to that type in the current scope; or explicitly, by being the only objectResource
  bound to that type _and_ having a special attribute, which is currently `default`.
  
* The `autoLinkableToCollectionResource` selector behaves the same way, but depends on 
  having an _autoLink collectionResource_ instead of an objectResource.
 
* The `AUTO_LINK_OBJECT_RESOURCE` and `AUTO_LINK_COLLECTION_RESOURCE` constants are two 
  of the allowed values for `TargetResourceType` in a `LinkSpec`. These specify that the 
  target of the link should be the autoLink objectResource or the autoLink
  collectionResource, respectively, for the referenced type. 

    * Multi-valued object references are realized as a collection of objects. If a 
      multi-valued reference includes a link to the `AUTO_LINK_OBJECT_RESOURCE`, 
      each object in the list should include a link to the objectResource.  
      
    * If a multi-valued object reference includes a link to the 
      `AUTO_LINK_COLLECTION_RESOURCE`, the collection itself should contain a single
      link to the collectionResource.  (This may require some additional design and 
      implementation work on our code generators, to ensure that the collection class
      has a member to store links.) 
      
    * If a realizationTemplate includes a link to `AUTO_LINK_OBJECT_RESOURCE` or 
      `AUTO_LINK_COLLECTION_RESOURCE`, where there is no autoLink objectResource or 
      collectionResource, respectively, this should result in a warning, but not an
      error condition. API designers _should_ guard against this warning by setting 
      `autoLinkableToObjectResource` or `autoLinkableToCollectionResource` as preconditions
      in the `appliesTo` property of the realization template. 
    
       
## Tests

### Only ObjectResource Links

 