# Multiplicity

## Description

This tests three areas where multiplicity is considered:
* **`PropertyContext.multiplicity`** matches single- or multi-valued
  properties.
* **`PropertySelector.multiplicity`** is used in `excludedProperties`
  to exclude properties based on multiplicity.  
or non-key properties. 
* **`PropertyRealization.multiplicity`** is used in `includedProperties` 
  to include properties based on multiplicity, and/or to specify 
  the realization of those properties.

## Prerequisites

This test suite builds on the `InclusivePropertySet`, `ExclusivePropertySet`,
`ReferenceLevel`, and `KeyProperties` tests.

## Tests

### Match Multiplicity

<dl>
  <dt>Description</dt>
  <dd>

The realization model defines the following rules:
  Root objects include all properties.
  Single-valued reference properties include only primitive properties. 
  Multi-Valued reference Properties include only key properties.

  </dd>
  <dt>RAPID Model</dt>
  <dd>Multiplicity_Match.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` Match
model.tbBalanceSheetObject.check [
    named("BalanceSheet_Root").selects("RootObjects")
    withProps [
        including("balanceSheetID", "assets", "liabilities", "equity")
        includingRef("statement") [
            named("FinancialStatement_SingleValuedRef").selects("SingleValuedRefObjects")
            withOnlyProps("statementID", "statementDate", "beginDate", "endDate", "fiscalYear")
        ]
        includingRef("accountingMethod") [
            named("AccountingStandard_SingleValuedRef").selects("SingleValuedRefObjects")
            withOnlyProps("accountingStandardID", "name", "revision", "revisionDate")
        ]
        includingRef("comments") [
            named("Comment_MultiValuedRef").selects("MultiValuedRefObjects")
            withOnlyProps("commentID")
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>

### Exclude Single-Valued

<dl>
  <dt>Description</dt>
  <dd>

The realization model defines the following rules:

* Root objects exclude single-valued properties.
* Reference properties include only key properties. 

  </dd>
  <dt>RAPID Model</dt>
  <dd>Multiplicity_ExcludeSingleValued.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ExcludeSingleValued
model.tbBalanceSheetObject.check [
    named("BalanceSheet_Root").selects("RootObjects")
    withProps [
        includingRef("comments") [
            named("Comment_Ref").selects("RefObjects")
            withOnlyProps("commentID")
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>

### Exclude Multi-Valued

<dl>
  <dt>Description</dt>
  <dd>

The realization model defines the following rules:

* Root objects exclude multi-valued properties.
* Reference properties include only key properties. 

  </dd>
  <dt>RAPID Model</dt>
  <dd>Multiplicity_ExcludeMultiValued.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ExcludeMultiValued
model.tbBalanceSheetObject.check [
    named("BalanceSheet_Root").selects("RootObjects")
    withProps [
        including("balanceSheetID", "assets", "liabilities", "equity")
        includingRef("statement") [
            named("FinancialStatement_Ref").selects("RefObjects")
            withOnlyProps("statementID")
        ]
        includingRef("accountingMethod") [
            named("AccountingStandard_Ref").selects("RefObjects")
            withOnlyProps("accountingStandardID")
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>

### Include Single-Valued

<dl>
  <dt>Description</dt>
  <dd>

The realization model defines the following rules:

* Root objects include only single-valued properties.
* Reference properties include only key properties. 

  </dd>
  
  <dt>RAPID Model</dt>
  <dd>Multiplicity_IncludeSingleValued.rapid</dd>
  
  <dt>Assertions</dt>
  <dd>

``` IncludeSingleValued
model.tbBalanceSheetObject.check [
    named("BalanceSheet_Root").selects("RootObjects")
    withProps [
        including("balanceSheetId", "assets", "liabilities", "equity")
        includingRef("statement") [
            named("FinancialStatement_Ref").selects("RefObjects")
            withOnlyProps("statementID")
        ]
        includingRef("accountingMethod") [
            named("AccountingStandard_Ref").selects("RefObjects")
            withOnlyProps("accountingStandardID")
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>

### Include Multi-Valued

<dl>
  <dt>Description</dt>
  <dd>

The realization model defines the following rules:

* Root objects include only multi-valued properties.
* Reference properties include only key properties. 

  </dd>
  
  <dt>RAPID Model</dt>
  <dd>Multiplicity_IncludeMultiValued.rapid</dd>
  
  <dt>Assertions</dt>
  <dd>

``` IncludeMultiValued
model.tbBalanceSheetObject.check [
    named("BalanceSheet_Root").selects("RootObject")
    withProps [
        includingRef("comments") [
            named("Comment_Ref").selects("RefObjets")
            withOnlyProps("commentID")
        ]
        checkedAll
    ]
]
```
