<%@page import="java.util.ArrayList"%>
<%@page import="petascope.core.ServiceMetadata"%>
<%@page import="petascope.util.Pair"%>
<%@page import="java.util.List"%>
<%@page import="java.util.List"%>
<!--
/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
-->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!-- saved from url=(0071)http://www.quackit.com/html/templates/download/bryantsmith/whiteflower/ -->
<html xmlns="http://www.w3.org/1999/xhtml">

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

            <!-- Bootstrap Core CSS -->
            <link href="/rasdaman/static/wcs-client/assets/components/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet"/>
            <link href="/rasdaman/static/wcs-client/assets/components/angular-bootstrap/ui-bootstrap-csp.css" rel="stylesheet"/>
            <link href="/rasdaman/static/wcs-client/assets/components/jquery-ui/themes/smoothness/jquery-ui.min.css" rel="stylesheet"/>
            <link href="/rasdaman/static/wcs-client/assets/libs/code-prettify/prettify.css" rel="stylesheet"/>
            <link href="/rasdaman/static/wcs-client/assets/components/nvd3/build/nv.d3.min.css" rel="stylesheet"/>
            <link href="http://netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css" rel="stylesheet"/>
            <link href="/rasdaman/static/wcs-client/assets/css/custom.css" rel="stylesheet"/>

            <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
            <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
            <!--[if lt IE 9]>
                <script src="assets/components/html5shiv/dist/html5shiv.min.js"></script>
                <script src="assets/components/respond/dest/respond.min.js"></script>
                <![endif]-->

            <style>
                .panel-heading a:after {
                    font-family: 'Glyphicons Halflings';
                    content: "\e114";
                    float: right;
                    color: grey;
                }

                .panel-heading a.collapsed:after {
                    content: "\e080";
                }

                .center-block {
                    padding: 10px;
                }

                td {
                    padding: 8px;
                    width: 220px;
                }

                input{
                    border:1px solid #B0B0B0 ;
                }
            </style>
            <title>Rasdaman.org - Update Service Indentification & Service Provider Configuration</title>
            <link rel="shortcut icon" type="image/png" href="http://rasdaman.org/chrome/site/favicon.ico" />

            <script>

                // Remove display message after 5 seconds
                setTimeout(fadeOut, 5000);
                function fadeOut() {

                    var retMessage = document.getElementById("retMessage");
                    var htmlstring = retMessage.innerHTML;

                    htmlstring = (htmlstring.trim) ? htmlstring.trim() : htmlstring.replace(/^\s+/, '')
                    if (htmlstring != "")
                    {
                        retMessage.style.display = "none";
                    }

                }

                // Check string is empty
                function isEmpty(str) {
                    return (!str || 0 === str.length);
                }

                // Validate type versions (NOTE: must follow X.X.X where X is a number with a maximum of 2 digits.)
                function validateVersion(version)
                {
                    // Note: comma-separated list of versions (i.e: 1.2.3,3.2.4,2.3.1)
                    // or 1 version (i.e: 1.3.4, without .)
                    if (/^([0-9]*)\.([0-9]+)\.([0-9]+)(\, [0-9]*\.[0-9]+\.[0-9]+)*$/.test(version))
                    {
                        return (true);
                    }
                    alert("Invalid version number (format required: comma-separated list of n.n.n): " + version);
                    return (false);
                }

                // This function will validate onsubmit of form identification
                // Note: title and abstract should not be empty
                //       versions can be multiple and seperate by ", ", i.e: 1.2.3, 3.4.5
                function validateFormIdentification()
                {
                    var c = confirm('Do you want to update *Service Identification* information?');
                    if (c)
                    {
                        // title
                        var title = document.getElementById("serviceTitle").value;
                        if (isEmpty(title))
                        {
                            alert("Title can not be empty.");
                            return false;
                        }

                        // abstract
                        var abstract = document.getElementById("abstract").value;
                        if (isEmpty(abstract))
                        {
                            alert("Abstract can not be empty.");
                            return false;
                        }

                        // versions
                        var value = document.getElementById("serviceTypeVersion").value;
                        var isValid = validateVersion(value);

                        // is valid version X.X.X then allow to submit form
                        if (isValid)
                        {
                            return true;
                        }

                    }
                    return false;
                }

                // This function will validate email service provider address
                function validateEmail(email) {
                    // Note: email can be multiple (i.e: xx@g.com, zzz@b.com)
                    var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))(\, (([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,})))*$/;
                    return re.test(email);
                }


                // This function will validate on submit service provider
                function validateFormServiceProvider()
                {
                    // Some fields can not be empty
                    // Emails can be multiple and separte by ", " (i.e: abc@ac.com, xx2@gg.com)

                    var providerName = document.getElementById("providerName").value;
                    var providerWebsite = document.getElementById("providerWebsite").value;
                    var contactPerson = document.getElementById("contactPerson").value;
                    var positionName = document.getElementById("positionName").value;
                    var cityAddress = document.getElementById("cityAddress").value;
                    var postalCode = document.getElementById("postalCode").value;
                    var country = document.getElementById("country").value;
                    var email = document.getElementById("email").value;

                    var c = confirm('Do you want to update *Service Provider* information?');
                    if (c)
                    {
                        if (isEmpty(providerName))
                        {
                            alert("Provider name cannot be empty.");
                            return false;
                        }

                        if (isEmpty(providerWebsite))
                        {
                            alert("Provider website cannot be empty.");
                            return false;
                        }

                        if (isEmpty(contactPerson))
                        {
                            alert("Contact person cannot be empty.");
                            return false;
                        }

                        if (isEmpty(positionName))
                        {
                            alert("Position name cannot be empty.");
                            return false;
                        }

                        if (isEmpty(cityAddress))
                        {
                            alert("City address cannot be empty.");
                            return false;
                        }

                        if (isEmpty(postalCode))
                        {
                            alert("Postal code cannot be empty.");
                            return false;
                        }

                        if (isEmpty(country))
                        {
                            alert("Country cannot be empty.");
                            return false;
                        }

                        if (isEmpty(email))
                        {
                            alert("Email cannot be empty.");
                            return false;
                        } else if (!validateEmail(email))
                        {
                            alert("Invalid email address (format required: comma-separated list of email addresses): " + email);
                            return false;
                        }

                    }

                    return true;

                }

            </script>

    </head>

    <%
        ServiceMetadata sMeta = (ServiceMetadata) request.getAttribute("sMeta");
    %>

    <body>
        <div id="container">

            <div id="content">
                <br/>
                <h5 style="text-align:center;"> Hint: Please reload Petascope Web page (rasdaman/ows) to see the modification.
                    <a href="?logout=true"><span style="color:red;">[Log out]</span></a></h5>  <br/>

                <h5 style="text-align:center;color:red;" id="retMessage">
                    <%
                        // Message from Server when update data
                        Object ret = request.getAttribute("return");
                        if (ret != null) {
                            out.print(ret.toString());
                        }
                    %>
                </h5>
                <!--------------------------------------- SERVICE IDENTIFICATION ------------------------------------->

                <div class="container">

                    <div class="col-md-12">

                        <div class="panel panel-default" id="panel1">
                            <div class="panel-heading">
                                <h4 class="panel-title">
                                    <a data-toggle="collapse" data-target="#collapseOne" >
                                        Service Identification
                                    </a>
                                </h4>

                            </div>
                            <div id="collapseOne" class="panel-collapse collapse in">
                                <form id="serviceIdentification" method="post" action="?updateIdentification=true" onsubmit="return validateFormIdentification();">

                                    <table style="width:80%; margin: 20px;">

                                        <!-------- Title --->
                                        <tr>
                                            <td><label for="name">Title</label></td>
                                            <td><input type="text" name="serviceTitle" id="serviceTitle"
                                                       <%
                                                           // NOTE: get only 1 title from Description
                                                           String title = sMeta.getIdentification().getDescription().getTitles().get(0);
                                                           if (title == null) {
                                                               title = "";
                                                           }
                                                           out.print("value='" + title + "'");
                                                       %>
                                                       style="width: 475px;" placeholder="Service Title" title="Enter service title" class="required"> </td>
                                        </tr>

                                        <!-------- Abstract --->
                                        <tr>
                                            <td><label for="name">Abstract</label></td>
                                            <td>
                                                <textarea name="abstract" id="abstract" style="margin-left: 0px;" rows="10" cols="57"><%
                                                    String abstractText = sMeta.getIdentification().getDescription().getAbstracts().get(0);
                                                    if (abstractText == null) {
                                                        abstractText = "";
                                                    }
                                                    out.print(abstractText);

                                                    %></textarea>
                                            </td>
                                        </tr>

                                        <!-------- Service type  (NOTE: DO NOT ALLOW TO CHANGE THIS BECAUSE CANNOT LOAD COVERAGES WITH TYPE != OGC WCS)--->
                                        <!--<tr>
                                            <td><label for="name">Service type</label></td>
                                            <td><input type="text" name="serviceType"
                                        <%                                                           // NOTE: get only 1 title from Description
                                            String name = sMeta.getIdentification().getDescription().getTitles().get(0);
                                            if (name == null) {
                                                name = "";
                                            }
                                            out.print("value='" + name + "'");
                                        %>
                                        style="width: 475px;" placeholder="Service type" title="Enter service type" class="required"> </td>
                         </tr>-->


                                        <!-------- Service type version --->
                                        <tr>
                                            <td><label for="name">Service version (X.X.X)</label></td>
                                            <td><input type="text" name="serviceTypeVersion" id="serviceTypeVersion"
                                                       <%
                                                           // NOTE: get multiple versions from TypeVersions
                                                           String serviceTypeVersion = "";
                                                           List<String> versions = sMeta.getIdentification().getTypeVersions();

                                                           int i = 0;
                                                           for (String v : versions) {
                                                               if (i == versions.size() - 1) {
                                                                   serviceTypeVersion = serviceTypeVersion + v;
                                                               } else {
                                                                   serviceTypeVersion = serviceTypeVersion + v + ", ";
                                                               }
                                                               i++;
                                                           }

                                                           out.print("value='" + serviceTypeVersion + "'");
                                                       %>
                                                       style="width: 475px;" placeholder="Service version (x.x.x)" title="Enter service type version" class="required"> </td>
                                        </tr>

                                        <!-------- Submit button --->
                                        <tr>
                                            <td>

                                            </td>
                                            <td>
                                                <input type="submit" name="submit" class="button" id="submit" value="Update" />
                                            </td>
                                        </tr>

                                    </table>

                            </div>
                        </div>
                        </form>
                        <br/>

                        <!-----------------------------------------SERVICE PROVIDER ----------------------------------->

                        <form id="service_provider" method="post" action="?updateProvider=true" onsubmit="return validateFormServiceProvider();">

                            <div style="margin-top: -20px;">

                                <div class="panel panel-default" id="panel2">
                                    <div class="panel-heading">
                                        <h4 class="panel-title">
                                            <a data-toggle="collapse" data-target="#collapseOne" >
                                                Service Provider
                                            </a>
                                        </h4>

                                    </div>
                                    <div id="collapseOne" class="panel-collapse collapse in">
                                        <table style="width:80%; margin: 20px;">

                                            <!-------- Provider name --->
                                            <tr>
                                                <td><label for="name">Provider name</label></td>
                                                <td><input type="text" name="providerName" id="providerName"
                                                           <%
                                                               String providerName = sMeta.getProvider().getName();
                                                               if (providerName == null) {
                                                                   providerName = "";
                                                               }
                                                               // Get Provider Name
                                                               out.print("value='" + providerName + "'");
                                                           %>
                                                           style="width: 475px;" placeholder="Provider name" title="Enter provider name" class="required"> </td>
                                            </tr>

                                            <!-------- Provider web site --->
                                            <tr>
                                                <td><label for="name">Provider web site</label></td>
                                                <td><input type="text" name="providerWebsite" id="providerWebsite"
                                                           <%
                                                               // Get Provider Website
                                                               String providerWebsite = sMeta.getProvider().getSite();
                                                               if (providerWebsite == null) {
                                                                   providerWebsite = "";
                                                               }
                                                               out.print("value='" + providerWebsite + "'");
                                                           %>
                                                           style="width: 475px;" placeholder="Provider website" title="Enter provider website" class="required"> </td>
                                            </tr>

                                            <!-------- Contact person --->
                                            <tr>
                                                <td><label for="name">Contact person</label></td>
                                                <td><input type="text" name="contactPerson" id="contactPerson"
                                                           <%
                                                               // Get Contact Person
                                                               String contactPerson = sMeta.getProvider().getContact().getIndividualName();
                                                               if (contactPerson == null) {
                                                                   contactPerson = "";
                                                               }
                                                               out.print("value='" + contactPerson + "'");
                                                           %>
                                                           style="width: 475px;" placeholder="Contact person" title="Enter contact person" class="required"> </td>
                                            </tr>

                                            <!-------- Position name --->
                                            <tr>
                                                <td><label for="name">Position name</label></td>
                                                <td><input type="text" name="positionName" id="positionName"
                                                           <%
                                                               // Get Position name
                                                               String positionName = sMeta.getProvider().getContact().getPositionName();
                                                               if (positionName == null) {
                                                                   positionName = "";
                                                               }
                                                               out.print("value='" + positionName + "'");
                                                           %>
                                                           style="width: 475px;" placeholder="Position name" title="Enter position name" class="required"> </td>
                                            </tr>

                                            <!-------- Role name --->
                                            <tr>
                                                <td><label for="name">Role name</label></td>
                                                <td>
                                                    <select name="roleID" style="width: 475px; margin-left: 0px;">
                                                        <%
                                                            // Key, Value is from table role_code
                                                            List<Pair> roleList = (List<Pair>) request.getAttribute("roleList");
                                                            for (Pair p : roleList) {
                                                                // Get selected role
                                                                String role = sMeta.getProvider().getContact().getRole();
                                                                String selected = "";

                                                                // Then set the option key to selected (if this entry's value equal selected role)
                                                                if (role.equals(p.snd)) {
                                                                    selected = "selected";
                                                                }
                                                                out.println("<option value='" + p.fst + "'" + selected + ">" + p.snd + "</option>");
                                                            }
                                                        %>
                                                    </select>
                                                </td>
                                            </tr>

                                            <!-------- Email address --->
                                            <tr>
                                                <td><label for="name">Email address</label></td>
                                                <td><textarea name="email" id="email" style="margin-left: 0px;" rows="3" cols="57"><%
                                                    // NOTE: get multiple emails from emailAddresses
                                                    String email = "";
                                                    List<String> emails = new ArrayList<String>(sMeta.getProvider().getContact().getContactInfo().getAddress().getEmailAddresses());

                                                    i = 0;
                                                    for (String e : emails) {
                                                        if (i == emails.size() - 1) {
                                                            email = email + e;
                                                        } else {
                                                            email = email + e + ", ";
                                                        }
                                                        i++;
                                                    }

                                                    out.print(email);
                                                        %></textarea></td>
                                            </tr>

                                            <!-------- Hours of service --->
                                            <tr>
                                                <td><label for="name">Hours of service</label></td>
                                                <td><input type="text" name="hoursOfService"
                                                           <%
                                                               // Get Hours of service from contactInfo
                                                               String hoursOfService = sMeta.getProvider().getContact().getContactInfo().getHoursOfService();
                                                               if (hoursOfService == null) {
                                                                   hoursOfService = "";
                                                               }
                                                               out.print("value='" + hoursOfService + "'");
                                                           %>
                                                           style="width: 475px;" placeholder="Hours of service" title="Enter hours of service" class="required"> </td>
                                            </tr>

                                            <!-------- Contact instructions --->
                                            <tr>
                                                <td><label for="name">Contact instructions</label></td>
                                                <td><textarea name="contactInstructions" style="margin-left: 0px;" rows="3" cols="57"><%
                                                    // Get Contact Instructions from contactInfo
                                                    String contactInstructions = sMeta.getProvider().getContact().getContactInfo().getInstructions();
                                                    if (contactInstructions == null) {
                                                        contactInstructions = "";
                                                    }
                                                    out.print(contactInstructions);
                                                        %></textarea>

                                                </td>
                                            </tr>

                                            <!-------- City address --->
                                            <tr>
                                                <td><label for="name">City address</label></td>
                                                <td><textarea name="cityAddress" id="cityAddress" style="margin-left: 0px;" rows="3" cols="57"><%
                                                    // Get city address from contactAddress
                                                    String city = sMeta.getProvider().getContact().getContactInfo().getAddress().getCity();
                                                    if (city == null) {
                                                        city = "";
                                                    }
                                                    out.print(city);
                                                        %></textarea>
                                                </td>
                                            </tr>

                                            <!-------- Administrative area --->
                                            <tr>
                                                <td><label for="name">Administrative area</label></td>
                                                <td><textarea name="administrativeArea" style="margin-left: 0px;" rows="3" cols="57"><%
                                                    // Get administrative area from contactAddress.administrativeArea
                                                    String administrativeArea = sMeta.getProvider().getContact().getContactInfo().getAddress().getAdministrativeArea();
                                                    if (administrativeArea == null) {
                                                        administrativeArea = "";
                                                    }
                                                    out.print(administrativeArea);
                                                        %></textarea></td>
                                            </tr>


                                            <!-------- Postal code --->
                                            <tr>
                                                <td><label for="name">Postal code</label></td>
                                                <td><input type="text" name="postalCode" id="postalCode"
                                                           <%
                                                               // Get postal code from contactAddress.postalCode
                                                               String postalCode = sMeta.getProvider().getContact().getContactInfo().getAddress().getPostalCode();
                                                               if (postalCode == null) {
                                                                   postalCode = "";
                                                               }
                                                               out.print("value='" + postalCode + "'");
                                                           %>
                                                           style="width: 475px;" placeholder="Postal Code" title="Enter postal code" class="required"> </td>
                                            </tr>

                                            <!-------- Country --->
                                            <tr>
                                                <td><label for="name">Country</label></td>
                                                <td><input type="text" name="country" id="country"
                                                           <%
                                                               // Get country from contactAddress.country
                                                               String country = sMeta.getProvider().getContact().getContactInfo().getAddress().getCountry();
                                                               if (country == null) {
                                                                   country = "";
                                                               }
                                                               out.print("value='" + country + "'");
                                                           %>
                                                           style="width: 475px;" placeholder="Postal Code" title="Enter postal code" class="required"> </td>
                                            </tr>

                                            <!-------- Submit button --->
                                            <tr>
                                                <td>

                                                </td>
                                                <td>
                                                    <input type="submit" name="submit" class="button" id="submit" value="Update"/>
                                                </td>
                                            </tr>

                                        </table>

                                    </div>
                                </div>
                        </form>
                        <!--
                        <div id="footer" style="margin-top:10px;"></div>
                        !-->
                    </div>
                </div>
            </div>

    </body>

    <!--Prettify-->
    <script src="/rasdaman/static/wcs-client/assets/libs/code-prettify/prettify.js"></script>
    <script src="/rasdaman/static/wcs-client/assets/components/jquery/dist/jquery.min.js"></script>
    <script src="/rasdaman/static/wcs-client/assets/components/jquery-ui/jquery-ui.min.js"></script>
    <script src="/rasdaman/static/wcs-client/assets/components/bootstrap/dist/js/bootstrap.min.js"></script>


</html>
