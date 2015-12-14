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
 */
package petascope.wcs2;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import petascope.util.response.MultipartResponse;

import static org.mockito.Mockito.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MultipartResponseTest {

    String responseContents = "";

    @Before
    public void setUp() throws IOException {
        ServletOutputStream os = mock(ServletOutputStream.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(response.getOutputStream()).thenReturn(os);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                responseContents += invocation.getArguments()[0] + "\n";
                return null;
            }
        }).when(os).println(anyString());

        MultipartResponse multipartResponse = new MultipartResponse(response);
        multipartResponse.startPart("ctype1");
        multipartResponse.endPart();
        multipartResponse.startPart("ctype2");
        multipartResponse.endPart();
        multipartResponse.finish();
    }



    @Test
    public void testContentTypes() {
        Assert.assertTrue(responseContents.contains("ctype1"));
        Assert.assertTrue(responseContents.contains("ctype2"));
    }

    @Test
    public void testPartEnd() {
        String[] parts = responseContents.split("--End\n");
        Assert.assertTrue(parts.length == 4);
    }

    @Test
    public void testFinalEnd() {
        Assert.assertTrue(responseContents.contains("--End--"));
    }


}
