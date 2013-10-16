/*
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.opendove.odmc.rest.northbound;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.opendove.odmc.IfOpenDoveServiceApplianceCRU;
import org.opendaylight.opendove.odmc.OpenDoveCRUDInterfaces;
import org.opendaylight.opendove.odmc.rest.OpenDoveServiceApplianceRequest;
import org.opendaylight.opendove.odmc.rest.northbound.OpenDoveSBRestClient;
import org.opendaylight.opendove.odmc.OpenDoveServiceAppliance;

/**
 * Open DOVE Northbound REST APIs for DCS Service Appliance.<br>
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
public class OpenDoveDcsServiceApplianceNorthbound {

    /**
     * Sets/Resets the odcs service role on a service appliance
     *
     * @param saUUID
     *            serivce appliance UUID to modify
     * @return Updated service appliance information
     *
     *         <pre>
     *
     * Example:
     *
     * Request URL:
     * http://localhost:8080/controller/nb/v2/opendove/odcs/uuid/role
     *
     * Request body in JSON:
     * {
     *    "service_appliance": {
     *      "is_DCS": true
     *    }
     * }
     *
     * Response body in JSON:
     * {
     *   "service_appliance": {
     *     "ip_family": 4,
     *     "ip": "10.10.10.1",
     *     "uuid": "uuid",
     *     "dcs_rest_service_port": 1888,
     *     "dgw_rest_service_port": 1888,
     *     "dcs_raw_service_port": 932,
     *     "timestamp": "now",
     *     "build_version": "openDSA-1",
     *     "dcs_config_version": 60,
     *     "canBeDCS": true,
     *     "canBeDGW": true,
     *     "isDCS": true,
     *     "isDGW": false
     *   }
     * }
     * </pre>
     */
	@Path("/{saUUID}/role")
    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Operation successful"),
            @ResponseCode(code = 204, condition = "No content"),
            @ResponseCode(code = 401, condition = "Unauthorized"),
            @ResponseCode(code = 409, condition = "DCS is in a Conflicted State"), 
            @ResponseCode(code = 404, condition = "Not Found"),
            @ResponseCode(code = 500, condition = "Internal Error") 
            })
    public Response nbAssignDcsServiceApplianceRole(
            @PathParam("saUUID") String dsaUUID,
            OpenDoveServiceApplianceRequest request
            ) {
        IfOpenDoveServiceApplianceCRU sbInterface = OpenDoveCRUDInterfaces.getIfDoveServiceApplianceCRU(this);
        if (sbInterface == null) {
            throw new ServiceUnavailableException("OpenDove SB Interface "
                    + RestMessages.SERVICEUNAVAILABLE.toString());
        }
        if (!sbInterface.applianceExists(dsaUUID))
            return Response.status(404).build();
        if (!request.isSingleton())
            return Response.status(400).build();
        OpenDoveServiceAppliance delta = request.getSingleton();
        if (delta.get_isDCS() == null)
            return Response.status(400).build();
        
        OpenDoveServiceAppliance dcsAppliance = sbInterface.getDoveServiceAppliance(dsaUUID);
        if (!dcsAppliance.get_canBeDCS())
            return Response.status(400).build();

        OpenDoveSBRestClient sbRestClient =    new OpenDoveSBRestClient();
        Integer http_response = sbRestClient.assignDcsServiceApplianceRole(dcsAppliance);

        if ( http_response == 200 || http_response == 204 ) {

           // Set the isDCS field.
           Boolean isDCS = true;
           dcsAppliance.set_isDCS(isDCS);

           if (sbInterface.applianceExists(dsaUUID) ) {
               sbInterface.updateDoveServiceAppliance(dsaUUID, dcsAppliance);
           }

           /* Send Updated List of DCS Nodes to All the Nodes that are in Role Assigned State */
           sbRestClient.sendDcsClusterInfo();
        }
        return Response.status(200).entity(new OpenDoveServiceApplianceRequest(sbInterface.getDoveServiceAppliance(dsaUUID))).build();
    }
}
