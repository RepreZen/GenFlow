/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.services;

import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.modelsolv.reprezen.restapi.ResourceAPI;
import com.modelsolv.reprezen.restapi.ServiceDataResource;
import com.reprezen.genflow.common.graph.ResourceSorter;

public class ResourceSorterServices {
	public static List<ServiceDataResource> sort(List<ServiceDataResource> resources) {
		return new ResourceSorter().sort(resources);
	}

	public static List<ServiceDataResource> sortResources(ResourceAPI resourceAPI) {
		Iterable<ServiceDataResource> resources = Iterables.filter(resourceAPI.getOwnedResourceDefinitions(),
				ServiceDataResource.class);
		return new ResourceSorter().sort(Lists.newArrayList(resources));
	}
}
