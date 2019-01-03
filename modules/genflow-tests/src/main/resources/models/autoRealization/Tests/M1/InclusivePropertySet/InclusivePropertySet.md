# Inclusive Property Set

## Description

This test suite addresses the most essential form of realization, which is to represent 
an object reference as an inline object, including all of its properties. 

It relies on a bare minimum of features in the realization model:
* `InclusivePropertySet`
* `excludedProperties`
* `PrimitivePropertySelector`
* `ReferencePropertySelector`

## Tests

### AllProperties

<dl>
  <dt>Description</dt>
  <dd>

 The resourceAPI defines only one resource, which is `TaxFilingObject`. 
 And the realizationModel defines only one template, which applies to all 
 references. 

 This should create one realization for TaxFiling, and another for Person (through the 
 `taxpayer` reference), each having all properties. 

  </dd>
  <dt>RAPID Model</dt>
  <dd>InclusivePropertySet_Simple.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` Simple
model.tbTaxFilingObject.check [
    named("TaxFiling_AllProperties").selects("AllObjectRefsAsInlineObjects")
    withProps [
        including("filingID", "jurisdiction", "year", "period", "currency", "grossIncome", "taxLiability")
        includingRef("taxpayer") [
            named("Person_AllProperties").selects("AllObjectRefsAsInlineObjects")
            withOnlyProps("taxpayerID", "lastName", "firstName", "otherNames")
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>

### AllPropertiesExcludingPrimitive

<dl>
  <dt>Description</dt>
  <dd>

The resourceAPI defines only one resource, which is `TaxFilingObject`. 
The realizationModel defines only one template, which applies to all references. It 
uses an inclusive property set, but excludes all primitive properties.

This should create a realization for `TaxFiling`, having only a reference to a `Person` 
realization.  The Person realization is an empty object, because it has no reference 
properties; therefore all of its properties are excluded.

  </dd>
  <dt>RAPID Model</dt>
  <dd>InclusivePropertySet_ExcludePrimitive.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ExcludePrimitive
model.tbTaxFilingObject.check [
    named("TaxFiling_AllExceptPrimitive").selects("AllObjects")
    withProps [
        includingRef("taxpayer") [
            named("Person_AllExceptPrimitive").selects("AllObjects")
            withNoProps
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>

### AllPropertiesExcludingReferences

<dl>
  <dt>Description</dt>
  <dd>

The resourceAPI defines only one resource, which is `TaxFilingObject`. 
The realizationModel defines only one template, which applies to all references. It 
uses an inclusive property set, but excludes all reference properties.

This should create a realization for `TaxFiling`, having only primitive properties.
There should not be a realization for `Person`, because the `taxpayer` reference 
property is excluded, so `Person` is unreachable.

  </dd>
  <dt>RAPID Model</dt>
  <dd>InclusivePropertySet_ExcludeReference.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ExcludeReference
model.tbTaxFilingObject.check [
    named("TaxFiling_AllExceptReference").selects("AllObjectRefsAsInlineObjects")
    withOnlyProps("filingID", "jurisdiction", "year", "period", "currency", "grossIncome", "taxLiability")
]
```

  </dd>
</dl>
