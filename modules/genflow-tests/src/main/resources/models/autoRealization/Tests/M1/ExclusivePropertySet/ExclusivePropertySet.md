# Exclusive Property Set

## Description

This test suite introduces exclusive property sets, also called "itemized" 
property sets. These are the equivalent of a RAPID-ML property set declared
`with only properties`. It lists the included properties, along with 
optional realization specifications, which may include cardinality constraints,
primitive property constraints, and recursive reference treatments.

In an exclusive property set, properties are excluded by default; they are 
included if they are explicitly listed in the `includedProperties` list. 
An `excludedProperties` list is not needed, and not allowed. 

## Prerequisites

This test suite builds on the `InclusivePropertySet` tests.

## Tests

### OnlyReferenceProperties

<dl>
  <dt>Description</dt>
  <dd>

The resourceAPI defines contains only one resource, which is `TaxFilingObject`. 
The realizationModel defines only one template, which applies to all references. It 
uses an exclusive property set that includes only reference properties.

This should create a realization for `TaxFiling`, having only a reference to a `Person` 
realization. The Person realization is an empty object, because it has no reference 
properties; therefore all of its properties are excluded.

  </dd>
  <dt>RAPID Model</dt>
  <dd>ExclusivePropertySet_OnlyReference.rapid</dd>
  <dt>Assertions</dt>
  <dd>
  
``` OnlyReference
model.tbTaxFilingObject.check [
    named("TaxFiling_OnlyReference").selects("AllObjects")
    withProps [
        includingRef("taxpayer") [
            named("Person_OnlyReference").selects("AllObjects")
            withNoProps
        ]
        checkedAll
    ]
]
```
  
  </dd>
</dl>


### OnlyPrimitiveProperties

<dl>
  <dt>Description</dt>
  <dd>

The resourceAPI defines contains only one resource, which is `TaxFilingObject`. 
The realizationModel defines only one template, which applies to all references. It 
uses an exclusive property set with _only primitive properties_.

This should create a realization for `TaxFiling`, having only primitive properties.
There should not be a realization for `Person`, because the `taxpayer` reference 
property is excluded, so `Person` is unreachable.

  </dd>
  <dt>RAPID Model</dt>
  <dd>ExclusivePropertySet_OnlyPrimitive.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` OnlyPrimitive
model.tbTaxFilingObject.check [
    named("TaxFiling_OnlyPrimitive").selects("AllObjects")
    withOnlyProps("filingID", "jurisdiction", "year", "period", "currency", "grossIncome", "taxLiability")
]
```

  </dd>
</dl>
