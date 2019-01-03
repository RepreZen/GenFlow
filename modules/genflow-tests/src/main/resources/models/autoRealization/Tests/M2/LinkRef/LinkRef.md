# Link References

## Description

`LinkRef` is the `LinkStrategy` subtype that instructs to include the Links as specified in a referenced Realization Rule.
This test suite covers the linkRef feature, and supporting selectors that 
test whether an auto-link resource is available. These "linkability" selectors are 
`autoLinkableToObjectResource` and `autoLinkableToCollectionResource`.

## Tests

### ObjectResource

<dl>
  <dt>Description</dt>
  <dd>

This test copies the behavior of LinkSpec/LinkSpec_ObjectResource, but with 
reusable links defined in the `components` section which are referenced via a `LinkRef`.
The realization model implements the following rules:
* All objects have key properties and reference properties.
* References with an autoLink objectResource will have a link to that.

  </dd>
  <dt>RAPID Model</dt>
  <dd>LinkRef_ObjectResource.rapid</dd>
  <dt>Assertions</dt>
  <dd>

``` ObjectResource
// This model is the same as for LinkSpec_ObjectResource, but the realization 
// model puts link specs in components rather than inlining them. End result 
// should be identical
new LinkSpecTests(myChecker).testObjectResource(model)
```
  </dd>
</dl>

