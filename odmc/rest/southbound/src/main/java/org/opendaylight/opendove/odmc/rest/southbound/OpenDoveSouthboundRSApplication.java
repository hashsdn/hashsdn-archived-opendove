/*
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.opendove.odmc.rest.southbound;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Application;

import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;


/**
 * This class is an instance of javax.ws.rs.core.Application and is used to return the classes
 * that will be instantiated for JAXRS processing. This is necessary
 * because package scanning in jersey doesn't yet work in OSGi environment.
 *
 */
public class OpenDoveSouthboundRSApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
// southbound URIs
        classes.add(OpenDoveDcsChangeVersionSouthbound.class);
        classes.add(OpenDoveDcsServiceApplianceSouthbound.class);
        classes.add(OpenDoveDgwChangeVersionSouthbound.class);
        classes.add(OpenDoveDgwServiceApplianceSouthbound.class);
        classes.add(OpenDoveDomainSouthbound.class);
        classes.add(OpenDoveEGWFwdRuleSouthbound.class);
        classes.add(OpenDoveEGWSNATPoolSouthbound.class);
        classes.add(OpenDoveGwIpv4Southbound.class);
        classes.add(OpenDoveNetworkSouthbound.class);
        classes.add(OpenDoveNetworkSubnetSouthbound.class);
        classes.add(OpenDovePolicySouthbound.class);
        classes.add(OpenDoveSubnetSouthbound.class);
        classes.add(OpenDoveSwitchSouthbound.class);
        classes.add(OpenDoveVGWVNIDMappingSouthbound.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        MOXyJsonProvider moxyJsonProvider = new MOXyJsonProvider();

        moxyJsonProvider.setAttributePrefix("@");
        moxyJsonProvider.setFormattedOutput(true);
        moxyJsonProvider.setIncludeRoot(false);
        moxyJsonProvider.setMarshalEmptyCollections(true);
        moxyJsonProvider.setValueWrapper("$");

        Map<String, String> namespacePrefixMapper = new HashMap<String, String>(1);
        namespacePrefixMapper.put("router", "router");        // FIXME: fill in with XSD
        namespacePrefixMapper.put("provider", "provider");    // FIXME: fill in with XSD
        moxyJsonProvider.setNamespacePrefixMapper(namespacePrefixMapper);
        moxyJsonProvider.setNamespaceSeparator(':');

        HashSet<Object> set = new HashSet<Object>(1);
        set.add(moxyJsonProvider);
        return set;
    }
}
