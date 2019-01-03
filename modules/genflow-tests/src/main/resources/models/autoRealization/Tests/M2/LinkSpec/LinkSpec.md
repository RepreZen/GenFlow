# Link Specifications

## Description

`LinkSpec` is the `LinkStrategy` subtype that directly creates a hyperlink. 
This test suite covers the linkSpec feature, and supporting selectors that 
test whether an auto-link resource is available. These "linkability" selectors are 
`autoLinkableToObjectResource` and `autoLinkableToCollectionResource`.

## Tests

### ObjectResource

<dl>
  <dt>Description</dt>
  <dd>

The realization model implements the following rules:
* All objects have key properties and reference properties.
* References with an autoLink objectResource will have a link to that.

  </dd>
  <dt>RAPID Model</dt>
  <dd>LinkSpec_ObjectResource.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ObjectResource
model.tbFinancialStatementObject.check [
    named("FinancialStatement_Root").selects("RootObjects")
    withOnlyLink("detail > BalanceSheetObject")
    withProps [
        including("statementID")
        includingRef("company") [
            bookmark("coRzn")
            named("Company_Ref").selects("KeyRefNoLinks")
            withOnlyProps("companyID")
        ]
        includingRef("balanceSheet") [
            bookmark("bsRzn")
            named("BalanceSheet_Ref").selects("KeyRefWithLinks")
            withOnlyLinks("detail > FinancialStatementObject")
            withProps [
                including("balanceSheetID")
                includingRef("statement") [
                    bookmark("stRzn")
                    named("FinancialStatement_Ref").selects("KeyRefWithLinks")
                    withOnlyLink("detail > BalanceSheetObject")
                    withProps [
                        including("statementID")
                        includingRef("company") [
                            shares("coRzn")
                        ]
                        includingRef("balanceSheet") [
                            shares("bsRzn")
                        ]
                        includingRef("incomeStatement") [
                            bookmark("isRzn")
                            named("IncomeStatement_Ref").selects("KeyRefNoLinks")
                            withProps [
                                including("incomeStatementID")
                                includingRef("statement") [
                                    shares("stRzn")
                                ]
                                includingRef("accountingMethod") [
                                    bookmark("amRzn").named("AccountingStandard_Ref").selects("KeyRefNoLinks")
                                    withOnlyProps("accountingStandardID")
                                ]
                                includingRef("comments") [
                                    bookmark("cmRzn")
                                    named("Comment_Ref").selects("KeyRefNoLinks")
                                    withOnlyProps("commentID")
                                ]
                                checkedAll
                            ]
                        ]
                        includingRef("cashFlowStatement") [
                            bookmark("cfRzn")
                            named("CashFlowStatement_Ref").selects("KeyRefNoLinks")
                            withProps [
                                including("cashFlowStatementID")
                                includingRef("statement") [
                                    shares("stRzn")
                                ]
                                includingRef("accountingMethod") [
                                    shares("amRzn")
                                ]
                                includingRef("comments") [
                                    shares("cmRzn")
                                ]
                                checkedAll
                            ]
                        ]
                        checkedAll
                    ]
                ]
                includingRef("accountingMethod") [
                    shares("amRzn")
                ]
                includingRef("comments") [
                    shares("cmRzn")
                ]
                checkedAll
            ]
        ]
        includingRef("incomeStatement") [
            shares("isRzn")
        ]
        includingRef("cashFlowStatement") [
            shares("cfRzn")
        ]
    ]
].thenCheck(model.tbBalanceSheetObject) [
    named("BalanceSheet_Root").selects("RootObjects")
    withOnlyLinks("detail > FinancialStatementObject")
    withProps [
        including("balanceSheetID")
        includingRef("statement") [
            shares("stRzn")
        ]
        includingRef("accountingMethod") [
            shares("amRzn")
        ]
        includingRef("comments") [
            shares("cmRzn")
        ]
        checkedAll
    ]
]
```
  </dd>
</dl>

### Include Auto-Linkable to ObjectResource

<dl>
  <dt>Description</dt>
  <dd>

This test exercises the linkability selector, which determines whether 
an object has an auto-link ObjectResource available. 

The realization model implements the following rules:
* Root objects will have key properties, and _only_ references to data types that
  have an auto-link objectResource.
* Reference objects will have only key properties, and will have objectResource 
  auto-links.

  </dd>
  <dt>RAPID Model</dt>
  <dd>LinkSpec_IncludeAutoLinkableToOR.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` IncludeAutoLinkableToOR
model.tbFinancialStatementObject.check [
    named("FinancialStatement_Root").selects("RootObjects")
    withOnlyLinks("detail > BalanceSheetObject")
    withProps [
        including("statementID")
        includingRef("balanceSheet") [
            named("BalanceSheet_Ref").selects("References")
            withNoLinks
            withOnlyProps("balanceSheetID")
        ]
        checkedAll
    ]
].thenCheck(model.tbBalanceSheetObject) [
    named("BalanceSheet_Root").selects("RootObjects")
    withOnlyLinks("detail > FinancialStatementObject")
    withProps [
        including("balanceSheetID")
        includingRef("statement") [
            named("FinancialStatement_Ref").selects("References")
            withNoLinks
            withOnlyProps("statementID")
        ]
        checkedAll
    ]
]
```
  </dd>
</dl>

### CollectionResource

<dl>
  <dt>Description</dt>
  <dd>

This test exercises logic related to auto-link collection resources. 

objectResource Root Object (IndexObject) has
  * Multi-valued references, Not auto-linkable to CR
    * Excludes taxFilings
    * Includes people, accountants, balanceSheets

collectionResource Root Objects (CommentCollection, TaxFilingCollection) have
  * key properties
  
Multi-valued reference, not auto-linkable to CR (balanceSheets)
  * key properties
  * multi-valued references, auto-linkable to CR (comments)

Multi-Valued reference, auto-linkaable to CR (comments) has
  * key properties
  * Auto-link to collectionResource

  </dd>
  <dt>RAPID Model</dt>
  <dd>LinkSpec_CollectionResource.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` CollectionResource
model.tbIndexObject.check [
    named("Index_Root").selects("ObjectResourceRoot")
    withNoLinks
    withProps [
        includingRef("people") [
            bookmark("pplRzn")
            named("Person_Ref").selects("MultiRefNotLinkableToCR")
            withNoLinks
            withNoProps
        ]
        includingRef("accountants") [
            named("Accountant_Ref").selects("MultiRefNotLinkableToCR")
            withNoLinks
            withNoProps
        ]
        includingRef("balanceSheets") [
            named("BalanceSheet_Ref").selects("MultiRefNotLinkableToCR")
            withOnlyLink("detail > CommentCollection")
            withProps [
                includingRef("comments") [
                    named("Comment_Ref").selects("MultiRefLinkableToCR")
                    withNoLinks
                    withOnlyProps("commentID")
                ].checkedAll
            ]
        ].checkedAll
    ]
].thenCheck(model.tbCommentCollection) [
    named("Comment_Root").selects("CollectionResourceRoot")
    withNoLinks
    withOnlyProps("commentID")
].thenCheck(model.tbTaxFilingCollection) [
    named("TaxFiling_Root").selects("CollectionResourceRoot")
    withNoLinks
    withOnlyProps("filingID", "year", "period")
]
```
  </dd>
</dl>

