/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.healthcheck.server;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.util.IO;
import org.mortbay.util.ajax.JSON;
import org.obm.filter.Slow;
import org.obm.healthcheck.AbstractHealthCheckTest;
import org.obm.healthcheck.HealthCheckTestEnvOneHandler;
import org.obm.test.GuiceModule;
import org.obm.test.SlowGuiceRunner;

import com.google.common.collect.ImmutableMap;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(HealthCheckTestEnvOneHandler.class)
public class HealthCheckServletOneHandlerTest extends AbstractHealthCheckTest {

	@Test
	public void testRootUrl() throws Exception {
		HttpResponse response = get("/");
		Map<String, String> expected = ImmutableMap.of("method", "GET", "path", "/testing/string");
		
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
		
		Object[] result = (Object[]) JSON.parse(IO.toString(response.getEntity().getContent()));
		
		assertThat(result).containsOnly(expected);
	}

	@Test
	public void testUnknownUrl() throws Exception {
		assertThat(get("/this/surely/isnt/handled").getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
	}
	
	@Test
	public void testGetString() throws Exception {
		HttpResponse response = get("/testing/string");
		
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
		assertThat(IO.toString(response.getEntity().getContent())).isEqualTo("Testing");
	}
	
}
