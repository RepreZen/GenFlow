# Key Properties

## Description

This test suite tests complex link strategies, including `ConditionalLink`, 
and `LinkSwitch` 

## Prerequisites

* These test makes use of link strategy references to access link strategies 
in the components object (tested independently in `LinkRef` tests).
* These tests make use of `LinkSpec` link strategies

## Tests

### ConditionalLink

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

``` ConditionalLink
model.tbFinancialStatementObject.check [
    named("FinancialStatement_Root").selects("RootObjects")
    withLinks("detail > BalanceSheetObject").checkedAllLinks.withProps [
        including("statementID")
        includingRef("company") [
            bookmark("comp")
            named("Company_Ref").selects("KeyRefWithLinks")
            withNoLinks
            withOnlyProps("companyID")
        ]
        includingRef("balanceSheet") [
            bookmark("bs")
            named("BalanceSheet_Ref").selects("KeyRefWithLinks")
            withLink("detail > FinancialStatementObject")
            withLink("detail > CommentCollection")
            checkedAllLinks
            withProps [
                including("balanceSheetID")
                includingRef("statement") [
                    bookmark("fs")
                    named("FinancialStatement_Ref").selects("KeyRefWithLinks")
                    withOnlyLinks("detail > BalanceSheetObject")
                    withProps [
                        including("statementID")
                        includingRef("company") [
                            shares("comp", "fs-ref")
                        ]
                        includingRef("balanceSheet") [
                            shares("bs", "fs-ref")
                        ]
                        includingRef("incomeStatement") [
                            bookmark("is")
                            named("IncomeStatement_Ref").selects("KeyRefWithLinks")
                            withLinks("detail > FinancialStatementObject")
                            withLinks("detail > CommentCollection")
                            checkedAllLinks
                            withProps [
                                including("incomeStatementID")
                                includingRef("statement") [
                                    shares("fs", "is-ref")
                                ]
                                includingRef("accountingMethod") [
                                    bookmark("acct")
                                    named("AccountingStandard_Ref").selects("KeyRefWithLinks")
                                    withNoLinks
                                    withOnlyProps("accountingStandardID")
                                ]
                                includingRef("comments") [
                                    bookmark("comm")
                                    named("Comment_Ref").selects("KeyRefWithLinks")
                                    withNoLinks
                                    withOnlyProps("commentID")
                                ]
                                checkedAll
                            ]
                        ]
                        includingRef("cashFlowStatement") [
                            bookmark("cf")
                            named("CashFlowStatement_Ref").selects("KeyRefWithLinks")
                            withLinks("detail > FinancialStatementObject")
                            withLinks("detail > CommentCollection")
                            checkedAllLinks
                            withProps [
                                including("cashFlowStatementID")
                                includingRef("statement") [
                                    shares("fs", "cf-ref")
                                ]
                                includingRef("accountingMethod") [
                                    shares("acct", "cf-ref")
                                ]
                                includingRef("comments") [
                                    shares("comm", "cf-ref")
                                ]
                                checkedAll
                            ]
                        ]
                        checkedAll
                    ]
                ]
                includingRef("accountingMethod") [
                    shares("acct", "bs-ref")
                ]
                includingRef("comments") [
                    shares("comm", "bs-ref")
                ]
                checkedAll
            ]
        ]
        includingRef("incomeStatement") [
            shares("is", "fs-obj")
        ]
        includingRef("cashFlowStatement") [
            shares("cf", "fs-obj")
        ]
        checkedAll
    ]
].thenCheck(model.tbBalanceSheetObject) [
    named("BalanceSheet_Root").selects("RootObjects")
    withLinks("detail > FinancialStatementObject")
    withLinks("detail > CommentCollection")
    checkedAllLinks
    withProps [
        including("balanceSheetID")
        includingRef("statement") [
            shares("fs", "bs-obj")
        ]
        includingRef("accountingMethod") [
            shares("acct", "bs-obj")
        ]
        includingRef("comments") [
            shares("comm", "bs-obj")
        ]
    ]
].thenCheck(model.tbCommentCollection) [
    named("Comment_Root").selects("RootObjects")
    withNoLinks
    withOnlyProps("commentID")
]
```

  </dd>
</dl>

### LinkSwitch

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

``` LinkSwitch
model.tbFinancialStatementObject.check [
    named("FinancialStatement_Root").selects("RootObjects")
    withLinks("objectDetail > BalanceSheetObject")
    checkedAllLinks
    withProps[
        including("statementID")
        includingRef("company") [
            named("Company_Ref").selects("RefObjects")
            withNoLinks
            withOnlyProps("companyID")
        ]
        includingRef("balanceSheet") [
            named("BalanceSheet_Ref").selects("RefObjects")
            withNoLinks
            withOnlyProps("balanceSheetID")
        ]
        includingRef("incomeStatement") [
            named("IncomeStatement_Ref").selects("RefObjects")
            withNoLinks
            withOnlyProps("incomeStatementID")
        ]
        includingRef("cashFlowStatement") [
            named("CashFlowStatement_Ref").selects("RefObjects")
            withNoLinks
            withOnlyProps("cashFlowStatementID")
        ]
    ]
].thenCheck(model.tbBalanceSheetObject) [
    named("BalanceSheet_Root").selects("RootObjects")
    withLinks("objectDetail > FinancialStatementObject")
    withLinks("collectionDetail > CommentCollection")
    checkedAllLinks
    withProps[
        including("balanceSheetID")
        includingRef("statement") [
            named("FinancialStatement_Ref").selects("RefObjects")
            withNoLinks
            withOnlyProps("statementID")
        ]
        includingRef("accountingMethod") [
            named("AccountingStandard_Ref").selects("RefObjects")
            withNoLinks
            withOnlyProps("accountingStandardID")
        ]
        includingRef("comments") [
            named("Comment_Ref").selects("RefObjects")
            withNoLinks
            withOnlyProps("commentID")
        ]
        checkedAll
    ]

].thenCheck(model.tbCommentCollection) [
    named("Comment_Root").selects("RootObjects")
    withNoLinks
    withOnlyProps("commentID")
]
```

  </dd>
</dl>
