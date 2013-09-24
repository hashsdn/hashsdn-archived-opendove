/*
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.opendove.odmc.rest.southbound;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.opendove.odmc.IfOpenDoveServiceApplianceCRU;
import org.opendaylight.opendove.odmc.OpenDoveCRUDInterfaces;
import org.opendaylight.opendove.odmc.OpenDoveServiceAppliance;

/**
 * Open DOVE Southbound REST APIs for Domains.<br>
 *
 * <br>
 * <br>
 * Authentication scheme [for now]: <b>HTTP Basic</b><br>
 * Authentication realm : <b>opendaylight</b><br>
 * Transport : <b>HTTP and HTTPS</b><br>
 * <br>
 * HTTPS Authentication is disabled by default. Administrator can enable it in
 * tomcat-server.xml after adding a proper keystore / SSL certificate from a
 * trusted authority.<br>
 * More info :
 * http://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html#Configuration
 *
 */

@Path("/odcs")
public class OpenDoveServiceApplianceSouthbound {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Operation successful"),
            @ResponseCode(code = 201, condition = "Registration Accepted"),
            @ResponseCode(code = 409, condition = "Service Appliance IP Address Conflict"),
            @ResponseCode(code = 500, condition = "Internal Error") })
    public Response dsaRegistration (OpenDoveServiceAppliance appliance) {
        String dsaIP   = appliance.getIP();
        String dsaUUID = appliance.getUUID();

        IfOpenDoveServiceApplianceCRU sbInterface = OpenDoveCRUDInterfaces.getIfDoveServiceApplianceCRU(this);

        if (sbInterface == null) {
            throw new ServiceUnavailableException("OpenDove SB Interface "
                    + RestMessages.SERVICEUNAVAILABLE.toString());
        }

        if (sbInterface.dsaIPExists(dsaIP))
            return Response.status(409).build();
        appliance.initDefaults();
        sbInterface.addDoveServiceAppliance(dsaUUID, appliance);

        return Response.status(201).entity(appliance).build();
    }

}
