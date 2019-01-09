# RealizationRef

## Description

	TBA

## Prerequisites

TBA

## Tests

### KeysInLevelTwoRefs

<dl>
  <dt>Description</dt>
  <dd>
	TBA
  </dd>
  <dt>RAPID Model</dt>
  <dd>RealizationRef_KeysInLevelTwoRefs.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` KeysInLevelTwoRefs
model.tbBalanceSheetObject.check [
    named("BalanceSheet_Root").selects("RootObjects")
    withProps [
        includingRef("statement") [
            named("BalanceSheet_Root_statement")
            withProps [
                including("statementID", "statementDate", "beginDate", "endDate", "fiscalYear")
                includingRef("company") [
                    named("Company_NonRootRef").selects("NonRootRefObjects")
                    withOnlyProps("companyID")
                ]
                includingRef("balanceSheet") [
                    named("BalanceSheet_NonRootRef").selects("NonRootRefObjects")
                    withOnlyProps("balanceSheetID")
                ]
                includingRef("incomeStatement") [
                    named("IncomeStatement_NonRootRef").selects("NonRootRefObjects")
                    withOnlyProps("incomeStatementID")
                ]
                includingRef("cashFlowStatement") [
                    named("CashFlowStatement_NonRootRef").selects("NonRootRefObjects")
                    withOnlyProps("cashFlowStatementID")
                ]
                checkedAll
            ]
        ]
        includingRef("accountingMethod") [
            named("BalanceSheet_Root_accountingMethod")
            withOnlyProps("accountingStandardID", "name", "revision", "revisionDate")
        ]
        includingRef("comments") [
            named("BalanceSheet_Root_comments")
            withOnlyProps("commentID", "timestamp", "comment")
        ]
        checkedAll
    ]
]
```

  </dd>
</dl>


