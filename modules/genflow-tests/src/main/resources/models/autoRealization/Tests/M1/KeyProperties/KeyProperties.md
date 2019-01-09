# Key Properties

## Description

This test suite covers a number of Realization Model features the deal with key 
properties:
* **`RealizationContext.hasKeys`** matches data structures having at least one key 
property. 
* **`PropertySelector.isKey`** is used in `excludedProperties` to exclude key 
or non-key properties. 
* **`PropertyRealization.isKey`** is used in `includedProperties` to include key
or non-key properties, along with optional realization metadata. (realization metadata
is not in the schema yet.)

**Note:** While the metamodel allows any property to be a key, the XText grammar is 
restricted so that only primitive properties can be keys.  Our tests will say within
this restriction.

## Prerequisites

This test suite builds on the `InclusivePropertySet`, `ExclusivePropertySet`, and 
`ReferenceLevel`  tests.


## Tests

### KeyPropertiesIncluded

<dl>
  <dt>Description</dt>
  <dd>

The realization model defines two templates: 
* **`RootObjects`** applies only to root-level objects, and includes all properties. 
* **`ReferencedObjects`** applies to non-root objects, reachable through reference 
  properties, and these only include key properties. 

  </dd>
  <dt>RAPID Model</dt>
  <dd>KeyProperties_IncludeKeys.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` IncludeKeys
model.tbTaxFilingObject.check [
    named("TaxFiling_Root").selects("RootObjects")
    withProps [
        including("filingID", "jurisdiction", "year", "period", "currency", "grossIncome", "taxLiability")
        includingRef("taxpayer") [
            named("Person_Ref").selects("ReferencedObjects")
            withOnlyProps("taxpayerID")
        ]
        checkedAll
    ]
].thenCheck(model.tbTaxFilingObject.get_Method.response(404)) [
    named("ErrorResponse_Root").selects("RootObjects")
    withOnlyProps("errorCode", "httpResponseCode", "message")
]
```

  </dd>
</dl>

### NonKeyPropertiesIncluded

<dl>
  <dt>Description</dt>
  <dd>

The realization model defines two templates: 
* **`RootObjects`** applies only to root-level objects, and includes all properties. 
* **`ReferencedObjects`** applies to non-root objects, reachable through reference 
  properties, and these only include non-key properties. 

  </dd>
  <dt>RAPID Model</dt>
  <dd>KeyProperties_IncludeNonKeys.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` IncludeNonKeys
model.tbTaxFilingObject.check [
    named("TaxFiling_Root").selects("RootObjects")
    withProps [
        including("filingID", "jurisdiction", "year", "period", "currency", "grossIncome", "taxLiability")
        includingRef("taxpayer") [
            named("Person_Ref").selects("ReferencedObjects")
            withOnlyProps("lastName", "firstName", "otherNames")
        ]
        checkedAll
    ]
].thenCheck(model.tbTaxFilingObject.get_Method.response(404)) [
    named("ErrorResponse_Root").selects("RootObjects")
    withOnlyProps("errorCode", "httpResponseCode", "message")
]
```

  </dd>
</dl>

### NonKeyPropertiesExcluded

**Note:** While these `...Excluded` tests yield the same results as their 
`...Included` counterparts specified above, they do so by means of an inclusive 
property set with an excludedProperties list.  This uses the `PropertySelector` 
schema, as opposed to the `PropertyRealization` schema used in the 
includedProperties list. That's why these tests are specified separately from
the `...Included` tests.

<dl>
  <dt>Description</dt>
  <dd>

The realization model defines two templates: 
* **`RootObjects`** applies only to root-level objects, and includes all properties. 
* **`ReferencedObjects`** applies to non-root objects, reachable through reference 
  properties, and these only include key properties. 

  </dd>
  <dt>RAPID Model</dt>
  <dd>KeyProperties_ExcludeNonKeys.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ExcludeNonKeys
model.tbTaxFilingObject.check [
    named("TaxFiling_Root").selects("RootObjects")
    withProps [
        including("filingID", "jurisdiction", "year", "period", "currency", "grossIncome", "taxLiability")
        includingRef("taxpayer") [
            named("Person_Ref").selects("ReferencedObjects")
            withOnlyProps("taxpayerID")
        ]
        checkedAll
    ]
].thenCheck(model.tbTaxFilingObject.get_Method.response(404)) [
    named("ErrorResponse_Root").selects("RootObjects")
    withOnlyProps("errorCode", "httpResponseCode", "message")
]
```

  </dd>
</dl>

### KeyPropertiesExcluded

<dl>
  <dt>Description</dt>
  <dd>

The realization model defines two templates: 
* **`RootObjects`** applies only to root-level objects, and includes all properties. 
* **`ReferencedObjects`** applies to non-root objects, reachable through reference 
  properties, and these include all properties _except_ key properties. 

  </dd>
  <dt>RAPID Model</dt>
  <dd>KeyProperties_ExcludeKeys.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ExcludeKeys
model.tbTaxFilingObject.check [
    named("TaxFiling_Root").selects("RootObjects")
    withProps [
        including("filingID", "jurisdiction", "year", "period", "currency", "grossIncome", "taxLiability")
        includingRef("taxpayer") [
            named("Person_Ref").selects("ReferencedObjects")
            withOnlyProps("lastName", "firstName", "otherNames")
        ]
        checkedAll
    ]
].thenCheck(model.tbTaxFilingObject.get_Method.response(404)) [
    named("ErrorResponse_Root").selects("RootObjects")
    withOnlyProps("errorCode", "httpResponseCode", "message")
]
```

  </dd>
</dl>

### KeysFallbackToAllProperties


<dl>
  <dt>Description</dt>
  <dd>

The realization model defines three templates: 
* **`RootObjects`** applies only to root-level objects, and includes all properties. 
* **`RefsWithKeys`** applies to non-root objects, reachable through reference 
  properties, where the referenced data structure has at least one key property.  
  These only include key properties. 
* **`RefsWithoutKeys`** applies to reference properties, where the referenced 
  data structure has no key properties. These only include all properties. 

  </dd>
  <dt>RAPID Model</dt>
  <dd>KeyProperties_KeysOrAll.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` KeysOrAll
model.tbAccountantObject.check [
    named("Accountant_Root").selects("RootObjects")
    withProps [
        including("employeeID", "lastName", "firstName")
        includingRef("officeAddress") [
            named("Address_Ref").selects("RefsWithoutKeys")
            withOnlyProps("addressLine1", "addressLine2", "city", "stateOrProvince", "postalCode", "country",
                "attentionLine")
        ]
        includingRef("clients") [
            named("Person_Ref").selects("RefsWithKeys")
            withOnlyProps("taxpayerID")
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>

