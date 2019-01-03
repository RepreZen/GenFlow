# ResourceType

## Description

This tests the ability to use `resourceType` in realization contexts
## Prerequisites

## Tests

### ObjectResourceRefsOnly

<dl>
  <dt>Description</dt>
    In this test, the object resource is realized with only reference properties,
    while the collection resource is reazlied with all properties. Reference
    properties are realized with only key properties, in both cases.
  <dd>

  </dd>
  <dt>RAPID Model</dt>
  <dd>ResourceType_ObjectResourceRefsOnly.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ObjectResourceRefsOnly
model.tbBalanceSheetObject.check [
    named("BalanceSheet_RootObj").selects("RootObjects")
    withProps[
        includingRef("statement") [
            named("FinancialStatement_Ref").selects("RefObjects")
            withOnlyProps("statementID")
        ]
        includingRef("accountingMethod") [
            named("AccountingStandard_Ref").selects("RefObjects")
            withOnlyProps("accountingStandardID")
        ]
        includingRef("comments") [
            named("Comment_Ref").selects("RefObjects")
            withOnlyProps("commentID")

        ]
        checkedAll
    ]
].thenCheck(model.tbBalanceSheetCollection) [
    named("BalanceSheet_RootColl").selects("RootCollections")
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
  
### CollectionResourceRefsOnly

<dl>
  <dt>Description</dt>
  <dd>
	This is the inverse of the first test: Object resource gets all properties,
	while collection resource gets only reference properties.
  </dd>

  <dt>RAPID Model</dt>
  <dd>ResourceType_CollectionResourceRefsOnly.rapid</dd>

  <dt>Assertions</dt>
  <dd>

``` CollectionResourceRefsOnly
model.tbBalanceSheetObject.check [
    named("BalanceSheet_RootObj").selects("RootObjects")
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
        includingRef("comments") [
            named("Comment_Ref").selects("RefObjects")
            withOnlyProps("commentID")
        ]
        checkedAll
    ]
].thenCheck(model.tbBalanceSheetCollection) [
    named("BalanceSheet_RootColl").selects("RootCollections")
    withProps [
        includingRef("statement") [
            named("FinancialStatement_Ref").selects("RefObjects")
            withOnlyProps("statementID")
        ]
        includingRef("accountingMethod") [
            named("AccountingStandard_Ref").selects("RefObjects")
            withOnlyProps("accountingStandardID")
        ]
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
