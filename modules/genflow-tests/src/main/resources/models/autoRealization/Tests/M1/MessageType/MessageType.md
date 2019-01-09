# MessageType

## Description

This tests the ability to use `messageType` in realization contexts

## Prerequisites

## Tests

### NoKeysInKeysOut

<dl>
  <dt>Description</dt>
    In this test, a request message on a POST omits key properties, while the response 
    includes only keys
  <dd>

  </dd>
  <dt>RAPID Model</dt>
  <dd>MessageType_KeysInNoKeysOut.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` KeysInNoKeysOut
model.tbBalanceSheetObject.check [
    named("BalanceSheet_Root").selects("RootObjects")
    withProps [
        including("balanceSheetID", "assets", "liabilities", "equity")
        includingRef("statement") [
            bookmark("fsRef")
            named("FinancialStatement_Ref").selects("RefObjects")
            withOnlyProps("statementID")
        ]
        includingRef("accountingMethod") [
            bookmark("amRef")
            named("AccountingStandard_Ref").selects("RefObjects")
            withOnlyProps("accountingStandardID")
        ]
        includingRef("comments") [
            bookmark("cmRef")
            named("Comment_Ref").selects("RefObjects")
            withOnlyProps("commentID")
        ]
        checkedAll
    ]
].thenCheck(model.tbBalanceSheetObject.method("POST").request) [
    named("BalanceSheet_PostReq").selects("POSTRequests")
    withProps [
        including("assets", "liabilities", "equity")
        includingRef("statement") [
            shares("fsRef")
        ]
        includingRef("accountingMethod") [
            shares("amRef")
        ]
        includingRef("comments") [
            shares("cmRef")
        ]
        checkedAll
    ]
].thenCheck(model.tbBalanceSheetObject.method("POST").normalResponse) [
    named("BalanceSheet_PostResp").selects("POST200Response")
    withOnlyProps("balanceSheetID")
]
```

  </dd>
</dl
