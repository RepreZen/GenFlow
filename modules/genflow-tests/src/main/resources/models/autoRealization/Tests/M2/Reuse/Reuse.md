# Realization Reuse

## Description

This suite of tests verifies the identity, naming, and reuse of generated
realizations. 

## Tests

### CollectionRoots

<dl>
  <dt>Description</dt>
  <dd>

This test ensures that a single root-level realization is correctly 
shared across two collectionResources. 

  </dd>
  <dt>RAPID Model</dt>
  <dd>Reuse_CollectionRoots.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` CollectionRoots  
model.tbPersonCollection.check [
    bookmark("pc")
    named("Person_Root").selects("RootObjects")
    checkedAllLinks
    withOnlyProps("taxpayerID", "lastName", "firstName", "otherNames")
].thenCheck(model.tbApi.resource("AccountantClients")) [
    shares("pc", "ac")
]
```

  </dd>
</dl>

### ObjectAndMessageRoots

<dl>
  <dt>Description</dt>
  <dd>

This test ensures that a single root-level realization is correctly 
shared across single-valued root contexts, including:
* ObjectResource root-level realization
* Request and response message-level realizations. Message-defined 
  realizations (using the `type` keyword) are always assumed to be
  single-valued.  
  
Note that the resource-level realization in `PersonCollection` 
has an inline realization. This is to avoid the problem described
in https://modelsolv.atlassian.net/browse/ZEN-4084.

  </dd>
  <dt>RAPID Model</dt>
  <dd>Reuse_ObjectAndMessageRoots.rapid</dd>
  <dt>Assertions</dt>
  <dd>
  
``` ObjectAndMessageRoots
model.tbPersonObject.check [
    bookmark("po")
    named("Person_Root").selects("RootObjects")
    withNoLinks
    withOnlyProps("taxpayerID", "lastName", "firstName", "otherNames")
].thenCheck(model.tbPersonObject.get_Method.normalResponse) [
    shares("po", "po-get-200")
].thenCheck(model.tbPersonCollection.method("POST").request) [
    shares("po", "pc-post-req")
].thenCheck(model.tbPersonCollection.method("POST").normalResponse) [
    shares("po", "pc-post-200")
]
```

  </dd>
</dl>

### SingleValueRootsAndRefs

<dl>
  <dt>Description</dt>
  <dd>

This test verifies that a single-valued realization can be shared 
across all single-valued contexts, including:
* ObjectResource root-level realization
* Request and response message-level realizations. Message-defined 
  realizations (using the `type` keyword) are always assumed to be
  single-valued.  
* Single-valued references.

The realization model defines:
* a `${TypeName}_Object` realization that can be used in any 
  single-valued context, and embeds all properties.  
* a `${TypeName}_List` realization that can be used in any multi-valued
  context, and embeds only reference properties. (This rule should not 
  be instantiated in this test case.)

  </dd>
  <dt>RAPID Model</dt>
  <dd>Reuse_SingleValuedRootsAndRefs.rapid</dd>
  <dt>Assertions</dt>
  <dd>
  
``` SingleValuedRootsAndRefs
model.tbPersonObject.check [
    bookmark("po")
    named("Person_Object").selects("SingleValued")
    withNoLinks.withOnlyProps("taxpayerID", "lastName", "firstName", "otherNames")
].thenCheck(model.tbPersonObject.get_Method.normalResponse) [
    shares("po", "po-get-200")
].thenCheck(model.tbTaxFilingObject) [
    named("TaxFiling_Object").selects("SingleValued")
    withProps [
        including("filingID", "jurisdiction", "year", "period", "currency", "grossIncome", "taxLiability")
        includingRef("taxpayer") [
            shares("po", "tfo-taxpayer")
        ]
        checkedAll
    ]
].thenCheck(model.tbPersonCollection.method("POST").request) [
    shares("po", "pc-post-req")
].thenCheck(model.tbPersonCollection.method("POST").normalResponse) [
    shares("po", "pc-post-200")
]
```

  </dd>
</dl>


### MultiValueRootsAndRefs

<dl>
  <dt>Description</dt>
  <dd>

This test verifies that a multi-valued realization can be shared 
across all multi-valued contexts, including:
* CollectionResource root-level realization
* Multi-valued references.

The realization model defines:
* a `${TypeName}_Object` realization that can be used in any 
  single-valued context, and embeds all properties.  
* a `${TypeName}_List` realization that can be used in any multi-valued
  context, and embeds only reference properties.
  
**Note:** I am seeing multiple problems with this in API Studio. If these problems are 
not evident in the generated realization graph, and therefore don't translate into 
failing tests, we need to extract them as separate issues, to be resolved in downstream 
generators:  
* The live documentation view shows the Accountant "clients" reference as a hyperlink to 
  PersonCollection, as if it's using factory-default realization, realizing this reference
  as a hyperlink to the autoLink resource.  This isn't right. 
* In the generated Swagger RXC: 
    * The `Person_List` schema is object-typed, not array-typed. It has only 
      `_rapid_links`, no reference to the `Person_List_item` schema. 
    * The `Person_List_item` schema doesn't define any properties.  
  
  </dd>
  <dt>RAPID Model</dt>
  <dd>Reuse_MultiValuedRootsAndRefs.rapid</dd>
  <dt>Assertions</dt>
  <dd>
  
``` MultiValuedRootsAndRefs
model.tbPersonCollection.check [
    bookmark("pc")
    named("Person_List").selects("MultiValued")
    withNoLinks
    withNoProps
].thenCheck(model.tbPersonCollection.get_Method.normalResponse) [
    shares("pc", "pc-get-200")
].thenCheck(model.tbAccountantObject) [
    named("Accountant_Object").selects("SingleValued")
    withNoLinks
    withProps[
        including("employeeID", "lastName", "firstName")
        includingRef("officeAddress") [
            named("Address_Object").selects("SingleValued")
            withNoLinks
            withOnlyProps("addressLine1", "addressLine2", "city", "stateOrProvince", "postalCode", "country",
                "attentionLine")
        ]
        includingRef("clients")[shares("pc", "ao-clients")]
        checkedAll
    ]
].thenCheck(model.tbAccountantObject) [
    bookmark("ao").named("Accountant_Object").selects("SingleValued").checkedAllLinks.withProps [
        including("employeeID", "lastName", "firstName").includingRef("officeAddress")[].includingRef("clients") [
            shares("pc", "ao-clients")
        ]
    ]
].thenCheck(model.tbAccountantObject.method("PUT").normalResponse) [
    withProps[
        includingRef("clients") [
            shares("pc", "ao-put-200-clients")
        ]
    ]
]
```

  </dd>
</dl>

### SingleAndMultiValuedRoots

<dl>
  <dt>Description</dt>
  <dd>

This test attempts to use single root-level realization 
across single-valued and multi-valued contexts. 

**Note:** Expect this to fail, and do not expect to fix it in the 
current project scope. 

This uses the same realization in single-valued contexts 
(including objectResource and message contexts, which are always handled 
as single-valued), and in and multi-valued contexts (collectionResource). 
The realization may be created and referenced properly in the ZenModel, 
but it will not generate a correct set of schemas in Swagger RXC. This
demonstrates the problem described in 
https://modelsolv.atlassian.net/browse/ZEN-4084


  </dd>
  <dt>RAPID Model</dt>
  <dd>Reuse_SingleAndMultiValuedRoots.rapid</dd>
  <dt>Assertions</dt>
  <dd>
  
``` SingleAndMultiValuedRoots
model.tbPersonObject.check [
    bookmark("po")
    named("Person_Root").selects("RootObjects")
    withNoLinks
    withOnlyProps("taxpayerID", "lastName", "firstName", "otherNames")
].thenCheck(model.tbPersonObject.get_Method.normalResponse) [
    shares("po", "po-get-200")
].thenCheck(model.tbPersonCollection) [
    shares("po", "pc")
].thenCheck(model.tbPersonCollection.method("POST").request) [
    shares("po", "pc-post-req")
].thenCheck(model.tbPersonCollection.method("POST").normalResponse) [
    shares("po", "pc-post-200")
].thenCheck(model.tbApi.resource("AccountantClients")) [
    shares("po", "ac")
].thenCheck(model.tbApi.resource("AccountantClients").method("POST").request) [
    shares("po", "ac")
].thenCheck(model.tbApi.resource("AccountantClients").method("POST").normalResponse) [
    shares("po", "ac")
]
```

  </dd>
</dl>

