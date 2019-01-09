# Containment

## Description

This test suite covers a number of Realization Model features the deal with 
containment:

* **`ReferencePropertyContext.containment`** matches reference properties 
  having, or not having, specific roles in a containment relationship.
* **`ReferencePropertySelector.containment`** is used in `excludedProperties`
  to exclude reference properties based on containment.  
or non-key properties. 
* **`ReferencePropertyRealization.containment`** is used in `includedProperties` 
  to include reference properties, based on containment, and/or to specify 
  the realization of those reference properties.

## Prerequisites

This test suite builds on the `InclusivePropertySet`, `ExclusivePropertySet`,
`ReferenceLevel`, and `KeyProperties` tests.

## Tests

### ContainingRefs

<dl>
  <dt>Description</dt>
  <dd>

The realization model specifies that:
* Root objects and containing references have all properties.
* Non-containing references have only key properties.

  </dd>
  <dt>RAPID Model</dt>
  <dd>Containment_ContainingRefs.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ContainingRefs
model.api("TaxBlasterAPI").resource("AccountantObject").check [
    named("Accountant_Root").selects("RootObjects")
    withProps [
        including("employeeID", "lastName", "firstName")
        includingRef("officeAddress") [
            named("Address_ContainingRef")
            withOnlyProps("addressLine1", "addressLine2", "city", "stateOrProvince", "postalCode", "country",
                "attentionLine")
        ]
        includingRef("clients") [
            named("Person_NonContainingRef").selects("NonContainingRefs")
            withOnlyProps("taxpayerID")
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>

### ContainerRefs

<dl>
  <dt>Description</dt>
  <dd>

The realization model specifies that:
* Root objects and containing references have all properties.
* Non-containing references have only key properties.
* Non-containment references are empty.

  </dd>
  <dt>RAPID Model</dt>
  <dd>Containment_ContainerRefs.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ContainerRefs
model.tbFinancialStatementObject.check [
    named("FinancialStatement_Root").selects("RootObjects")
    withProps [
        including("statementID", "statementDate", "beginDate", "endDate", "fiscalYear")
        includingRef("company") [
            named("Company_NonContainmentRef").selects("NonContainmentRefs")
            withNoProps
        ]
        includingRef("balanceSheet") [
            named("BalanceSheet_ContainingRef").selects("ContainingRefs")
            withProps [
                including("balanceSheetID", "assets", "liabilities", "equity")
                includingRef("statement") [
                    bookmark("finStmt")
                    named("FinancialStatement_ContainerRef").selects("ContainerRefs")
                    withOnlyProps("statementID")
                ]
                includingRef("accountingMethod") [
                    bookmark("acctMeth")
                    named("AccountingStandard_NonContainmentRef").selects("NonContainmentRefs")
                    withNoProps
                ]
                includingRef("comments") [
                    bookmark("comments")
                    named("Comment_ContainingRef").selects("ContainingRefs")
                    withOnlyProps("commentID", "timestamp", "comment")
                ]
                checkedAll
            ]
        ].includingRef("incomeStatement") [
            named("IncomeStatement_ContainingRef").selects("ContainingRefs")
            withProps [
                including("incomeStatementID", "income", "expenses", "netIncome")
                includingRef("statement") [
                    shares("finStmt")
                ]
                includingRef("accountingMethod") [
                    shares("acctMeth")
                ]
                includingRef("comments") [
                    shares("comments")
                ]
                checkedAll
            ]
        ].includingRef("cashFlowStatement") [
            named("CashFlowStatement_ContainingRef").selects("ContainingRefs")
            withProps [
                including("cashFlowStatementID", "startingCashPosition", "endingCashPosition")
                includingRef("statement") [
                    shares("finStmt")
                ]
                includingRef("accountingMethod") [
                    shares("acctMeth")
                ]
                includingRef("comments") [
                    shares("comments")
                ]
                checkedAll
            ]
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>


### ExcludeContaining

<dl>
  <dt>Description</dt>
  <dd>

The realization model specifies that:
* Root objects include all properties except containing references.
* Reference properties include only primitive properties.

  </dd>
  <dt>RAPID Model</dt>
  <dd>Containment_ExcludeContaining.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ExcludeContaining
model.tbFinancialStatementObject.check [
    named("FinancialStatement_Root").selects("RootObjects")
    withProps [
        including("statementID", "statementDate", "beginDate", "endDate", "fiscalYear")
        includingRef("company") [
            named("Company_Ref").selects("RefObjects")
            withOnlyProps("companyID", "companyName")
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>

### ExcludeContainer

<dl>
  <dt>Description</dt>
  <dd>

The realization model specifies that:
* Root objects include all properties except containing references.
* Reference properties include only primitive properties.

  </dd>
  <dt>RAPID Model</dt>
  <dd>Containment_ExcludeContainer.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ExcludeContainer
model.tbFinancialStatementObject.check [
    named("FinancialStatement_Root").selects("RootObjects")
    withProps [
        including("statementID", "statementDate", "beginDate", "endDate", "fiscalYear")
        includingRef("company") [
            named("Company_NonContainingRef").selects("NonContainingRefs")
            withOnlyProps("companyID", "companyName")
        ]
        includingRef("balanceSheet") [
            named("BalanceSheet_ContainingRef").selects("ContainingRefs")
            withProps [
                including("balanceSheetID", "assets", "liabilities", "equity")
                includingRef("accountingMethod") [
                    bookmark("acctMeth")
                    named("AccountingStandard_NonContainingRef").selects("NonContainingRefs")
                    withOnlyProps("accountingStandardID", "name", "revision", "revisionDate")
                ]
                includingRef("comments") [
                    bookmark("comments")
                    named("Comment_ContainingRef").selects("ContainingRefs")
                    withOnlyProps("commentID", "timestamp", "comment")
                ]
                checkedAll
            ]
        ]
        includingRef("incomeStatement") [
            named("IncomeStatement_ContainingRef").selects("ContainingRefs")
            withProps [
                including("incomeStatementID", "income", "expenses", "netIncome")
                includingRef("accountingMethod") [
                    shares("acctMeth")
                ]
                includingRef("comments") [
                    shares("comments")
                ]
                checkedAll
            ]
        ]
        includingRef("cashFlowStatement") [
            named("CashFlowStatement_ContainingRef").selects("ContainingRefs")
            withProps [
                including("cashFlowStatementID", "startingCashPosition", "endingCashPosition")
                includingRef("accountingMethod") [
                    shares("acctMeth")
                ]
                includingRef("comments") [
                    shares("comments")
                ]
                checkedAll
            ]
        ]
    ]
]
```

  </dd>
</dl>

### ExcludeContainment

### ExcludeNonContaining

### ExcludeNonContainer

### ExcludeNonContainment



### IncludeContaining

<dl>
  <dt>Description</dt>
  <dd>

The realization model specifies that:
* Root objects include primitive properties and containing references.
* Reference properties include only key properties.

  </dd>
  <dt>RAPID Model</dt>
  <dd>Containment_IncludeContaining.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` IncludeContaining
model.tbFinancialStatementObject.check [
    named("FinancialStatement_Root").selects("RootObjects")
    withProps [
        including("statementID", "statementDate", "beginDate", "endDate", "fiscalYear")
        includingRef("balanceSheet") [
            named("BalanceSheet_Ref").selects("RefObjects")
            withOnlyProps("balanceSheetID")
        ]
        includingRef("incomeStatement") [
            named("IncomeStatement_Ref").selects("RefObjects")
            withOnlyProps("incomeStatementID")
        ]
        includingRef("cashFlowStatement") [
            named("CashFlowStatement_Ref").selects("RefObjects")
            withOnlyProps("cashFlowStatementID")
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>

### IncludeContainer
<dl>
  <dt>Description</dt>
  <dd>

The realization model specifies that:
* Root objects include primitive properties and container references.
* Reference properties include only key properties.

  </dd>
  <dt>RAPID Model</dt>
  <dd>Containment_IncludeContainer.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` IncludeContainer
model.tbBalanceSheetObject.check [
    named("BalanceSheet_Root").selects("RootObjects")
    withProps [
        including("balanceSheetID", "assets", "liabilities", "equity")
        includingRef("statement") [
            named("FinancialStatement_Ref").selects("RefObjects")
            withOnlyProps("statementID")
        ]
        checkedAll
    ]
].thenCheck(model.tbIncomeStatementObject) [
    named("IncomeStatement_Root").selects("RootObjects")
    withProps [
        including("incomeStatementID", "income", "expenses", "netIncome")
        includingRef("statement") [
            named("FinancialStatement_Ref").selects("RefObjects")
            withOnlyProps("statementID")
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>

### IncludeContainment

### IncludeNonContaining

### IncludeNonContainer

### IncludeNonContainment