/** 
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 */
package com.reprezen.genflow.rapidml.jsonschema

import com.fasterxml.jackson.databind.JsonNode
import com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemeGeneratorTestFixture
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile
import org.junit.Rule
import org.junit.Test

import static org.junit.Assert.*
import static com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemeGeneratorTestFixture.*

@SampleRestFile("realization/Customer_includedPropertiesWCardinality.rapid")
class IncludedPropertiesTest {
	@Rule public JSONSchemeGeneratorTestFixture fixture = new JSONSchemeGeneratorTestFixture()

	@Test def void testIsValidJSONSchema() throws Exception {
		fixture.isValidJsonSchema()
	}

	@Test def void testObjectResourceIncludedProperties() throws Exception {
		var JsonNode objectNode = fixture.getDefinition("CustomerObject")
		assertThat(objectNode, nodeEqualsToYaml('''
type: object
minProperties: 1
properties:
  CustomerID:
    description: CustomerId property
    type: string
  CustomerName:
    type: string
  Orders:
    "$ref": "#/definitions/CustomerObject_Orders"
required:
- CustomerID'''))
	}

	@Test def void testObjectResource_ReferenceEmbed_array() throws Exception {
		var JsonNode objectNode = fixture.getDefinition("CustomerObject_Orders")
		assertThat(objectNode, nodeEqualsToYaml('''
description: "Orders reference"
type: "array"
minItems: 1
items:
  $ref: "#/definitions/CustomerObject_Orders_item"'''))
	}

	@Test def void testObjectResource_ReferenceEmbed_item() throws Exception {
		var JsonNode objectNode = fixture.getDefinition("CustomerObject_Orders_item")
		assertThat(objectNode, nodeEqualsToYaml('''
type: "object"
minProperties: 1
properties:
  OrderID:
    type: "string"
  Quantity:
    type: "number"
  Price:
    type: "number"
  Currency:
    type: "string"
  LineItems:
    $ref: "#/definitions/CustomerObject_Orders_LineItems"'''))
	}

	@Test def void testObjectResource_Nested_ReferenceEmbed() throws Exception {
		var JsonNode objectNode = fixture.getDefinition("CustomerObject_Orders_LineItems")
		assertThat(objectNode, nodeEqualsToYaml('''
type: "array"
minItems: 1
items:
  $ref: "#/definitions/CustomerObject_Orders_LineItems_item"'''))
	}

	@Test def void testObjectResource_Nested_ReferenceEmbed_item() throws Exception {
		var JsonNode objectNode = fixture.getDefinition("CustomerObject_Orders_LineItems_item")
		assertThat(objectNode, nodeEqualsToYaml('''
type: object
minProperties: 1
properties:
  lineItemID:
    type: string
  Product:
    type: object
    minProperties: 1
    properties:
      _links:
        "$ref": "#/definitions/_RapidLinksMap"
      ProductID:
        type: string'''))
	}
	

	@Test def void testRequestObjectRealization() throws Exception {
		var JsonNode objectNode = fixture.getDefinition("CustomerObject_Customer_request")
		assertThat(objectNode, nodeEqualsToYaml('''
type: object
minProperties: 1
properties:
  CustomerID:
    description: CustomerId property
    type: string
  CustomerName:
    type: string
  NotIncludedProp2:
    type: string
  Orders:
    description: Orders reference
    type: array
    minItems: 1
    items:
      "$ref": "#/definitions/OrderLink"
required:
- CustomerName'''))
	}

	@Test def void testOrderObject_link_OrderLink() throws Exception {
//			default linkDescriptor OrderLink
//				OrderID
//				Quantity
//				Price
		var JsonNode objectNode = fixture.getDefinition("OrderLink")
		assertThat(objectNode, nodeEqualsToYaml('''
type: object
minProperties: 1
properties:
  _links:
    "$ref": "#/definitions/_RapidLinksMap"
  OrderID:
    type: string
  Quantity:
    type: number
  Price:
    type: number'''))
	}

	@Test def void testResponseObjectRealization() throws Exception {
		val ordersPropertyNode = 'CustomerObject_Customer_response200_Orders'
		var JsonNode objectNode = fixture.getDefinition("CustomerObject_Customer_response200")
	// Orders is a multi-valued reference with a default OrderObject which has a default linkDescriptor OrderLink
		assertThat(objectNode, nodeEqualsToYaml('''
type: object
minProperties: 1
properties:
  CustomerID:
    description: CustomerId property
    type: string
  CustomerName:
    type: string
  NotIncludedProp1:
    type: string
  Orders:
    description: Orders reference
    type: array
    minItems: 1
    items:
      "$ref": "#/definitions/OrderLink"
required:
- CustomerID'''))
// See testOrderObject_link_OrderLink() for the contents of OrderLink
	}

}