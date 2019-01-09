# Requiredness

## Description

This tests the ability to use requiredness in property selectors

## Prerequisites

## Tests

### IncludeRequired

<dl>
  <dt>Description</dt>
  The realization defines the following rules:
  * Root objects include non-required properties
  * Reference objects include required properties
  <dd>
The realization defines the following rules:

* Root objects include non-required properties
* Reference objects include required properties

  </dd>

  <dt>RAPID Model</dt>
  <dd>Requiredness_IncludeRequired.rapid</dd>

  <dt>Assertions</dt>
  <dd>

``` IncludeRequired
model.tbBalanceSheetObject.check [
    named("BalanceSheet_Root").selects("RootObjects")
    withProps [
        including("assets", "liabilities", "equity")
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
    ]
]
```

  </dd>
</dl>
  
### ExcludeRequired

<dl>
  <dt>Description</dt>
  <dd>
	This test behaves exactly like IncludeRequiredness, but
	the realization model uses `excludeProperties` instead
	of `includeProperties` to achieve the result.
  </dd>

  <dt>RAPID Model</dt>
  <dd>Requiredness_ExcludeRequired.rapid</dd>

  <dt>Assertions</dt>
  <dd>

``` ExcludeRequired
// This test case behaves exactly like IncludeRequiredness, but uses `excludeProperties` instead of `includeProperties`
testIncludeRequired(model)
```

  </dd>
</dl>
