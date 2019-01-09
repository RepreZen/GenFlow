# RealizationRef

## Description

This tests the use of inline 'ObjectRealizationSpecs' within `ReferencePropertyRealization` 
structures.

## Prerequisites

This test suite builds on the `InclusivePropertySet` test

## Tests

### KeysInLevelTwoRefs

<dl>
  <dt>Description</dt>
  <dd>

The realization model defines the following rules:
* Root objects include only reference properties
* Reference properties directly contained in root objects include all properties
* Reference properties in all other references include only key properties

  </dd>
  <dt>RAPID Model</dt>
  <dd>ReferencePropertyRealization_KeysInLevelTwoRefs
  <dt>Assertions</dt>
  <dd>

```
model.tbBSResource.check(errors) [ rzn |
	// BalanceSheetResource 
	rzn.named("BalanceSheet_Root").selects("RootObjects").withProps [ props |
		props.size(3) //
		.includingRef("statement") [ stRzn |
			// BalanceSheet.statement reference FinancialStatement
			stRzn.named("BalanceSheet_Root_statement").withProps [ stProps |
				stProps.size(9).including("statementID", "statementDate", "beginDate", "endDate", "fiscalYear") //
				.includingRef("company") [ coRzn |
					coRzn.named("Company_NonRootRef").selects("NonRootRefObjects").withProps [ coProps |
						coProps.size(1).including("companyID").checkedAll
					]
				].includingRef("balanceSheet") [ bsRzn |
					// FinancialStatement.balanceSheet reference BalanceSheet
					bsRzn.named("BalanceSheet_NonRootRef").selects("NonRootRefObjects").withProps(
						bsProps |
							bsProps.size(1).including("balanceSheetID").checkedAll
					)
				].includingRef("incomeStatement") [ isRzn |
					// FinancialStatement.incomeStatement reference IncomeStatement
					isRzn.named("IncomeStatement_NonRootRef").selects("NonRootRefObjects").withProps [ isProps |
						isProps.size(1).including("incomeStatementID").checkedAll
					]
				].includingRef("cashFlowStatement") [ cfRzn |
					// FinancialStatement.cashFlowStatement reference CashFlowStatement
					cfRzn.named("CashFlowStatement_NonRootRef").selects("NonRootRefObjects").withProps [ cfProps |
						cfProps.size(1).including("cashFlowStatement_ID").checkedAll
					]
				].checkedAll
			]
		].includingRef("accountingMethod") [ acRzn |
			// BalanceSheet.accountingMethod reference AccountingStandard
			acRzn.named("BalanceSheet_Root_accountingMethod").withProps [ amProps |
				amProps.size(4).including("accountingStandardID", "name", "revision", "revisionDate").checkedAll
			]
		].includingRef("comments") [ cmRzn |
			// BalanceSheet.comments reference Comment*
			cmRzn.named("BalanceSheet_Root_comments").withProps [ cmProps |
				cmProps.size(3).including("commentID", "timestamp", "comment").checkedAll
			]
		].checkedAll
	]
]

```

  </dd>
</dl>

