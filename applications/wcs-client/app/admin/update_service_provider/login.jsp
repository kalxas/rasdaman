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
<html xmlns="http://www.w3.org/1999/xhtml">

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></meta>

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
                width: 500px;
                padding: 10px;
            }
        </style>


        <title>Rasdaman.org - Update Service Identification & Service Provider Configuration</title>
        <link rel="shortcut icon" type="image/png" href="http://rasdaman.org/chrome/site/favicon.ico" />

        <script>
            function isEmpty(str) {
                return (!str || 0 === str.length);
            }

            // Check username and password is not empty
            function checkIsEmpty()
            {
                var userName = document.getElementById("userName");
                var passWord = document.getElementById("passWord");

                var valid = false;

                if (isEmpty(userName.value))
                {
                    alert("Petascopedb Username is not empty.");
                } else if (isEmpty(passWord.value))
                {
                    alert("Petascopedb Password is not empty.");
                } else
                {
                    valid = true;
                }
                return valid;
            }

        </script>

    </head>

    <body>



        <div class="container" style="margin-top:10%;">

            <div class="center-block">

                <div class="panel panel-default" id="panel1">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a data-toggle="collapse" data-target="#collapseOne" >
                                Login to petascope admin interface
                            </a>
                        </h4>

                    </div>
                    <div id="collapseOne" class="panel-collapse collapse in">
                        <h5 style="text-align:center;color:red;">
                            <%
                            Object isSuccess = request.getAttribute("isSuccess");
                            if (isSuccess != null && ((Boolean) isSuccess) == false) {
                            out.print("Login unsuccessful (Username or Password is not valid)");
                            }
                            %>
                        </h5>
                        <div class="panel-body" style="margin-left:140px;">

                            <form method="post" action="#">
                                <p>
                                    <input type="text" id="userName" name="userName" value="" placeholder="Username petascopedb">
                                </p>
                                <p>
                                    <input type="password" id="passWord" name="passWord" value="" placeholder="Password petascopedb">
                                </p>
                                <p class="submit" style="margin-top:10px;">
                                    <input type="submit" name="login" value="Login" onclick="return checkIsEmpty();">
                                </p>
                            </form>
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
