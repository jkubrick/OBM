/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.server.reports;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.CalendarQueryPropertyHandler;
import org.obm.caldav.server.propertyHandler.impl.GetETag;
import org.obm.caldav.server.propertyHandler.impl.ResourceType;
import org.obm.caldav.server.resultBuilder.CalendarQueryResultBuilder;
import org.obm.caldav.server.share.DavComponent;
import org.obm.caldav.server.share.CalDavToken;
import org.obm.caldav.server.share.filter.CompFilter;
import org.obm.caldav.server.share.filter.Filter;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;

/**
 * http://www.webdav.org/specs/rfc4791.html#calendar-query
 * 
 * @author adrienp
 * 
 */
public class CalendarQuery extends ReportProvider {

	private Map<String, CalendarQueryPropertyHandler> properties;

	public CalendarQuery() {
		properties = new HashMap<String, CalendarQueryPropertyHandler>();
		properties.put("getetag", new GetETag());
		properties.put("resourcetype", new ResourceType());
	}

	// Request
	// <?xml version="1.0" encoding="UTF-8"?>
	// <calendar-query xmlns="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
	// <D:prop>
	// <D:getetag/>
	// </D:prop>
	// <filter>
	// <comp-filter name="VCALENDAR">
	// <comp-filter name="VEVENT"/>
	// </comp-filter>
	// </filter>
	// </calendar-query>
	@Override
	public void process(CalDavToken token, IBackend proxy, DavRequest req,
			HttpServletResponse resp, Set<String> requestPropList) {
		logger.info("process(" + token.getLoginAtDomain() + ", req, resp)");

		Filter filter = FilterParser.parse(req.getURI(),req.getXml());
		CompFilter compFilter = filter.getCompFilter();

		Set<CalendarQueryPropertyHandler> propertiesValues = new HashSet<CalendarQueryPropertyHandler>();

		for (String s : requestPropList) {
			CalendarQueryPropertyHandler dph = properties.get(s);
			if (dph != null) {
				propertiesValues.add(dph);
			} else {
				logger.warn("the Property [" + s + "] is not implemented");
			}
		}

		try {
			Document ret = null;
//			Map<String, EventTimeUpdate> events = new HashMap<String, EventTimeUpdate>();
			CompFilter cf = compFilter.getCompFilters().get(0);
			List<DavComponent> comps = proxy.getCalendarService()
					.getAllLastUpdate(token, cf);
//			for (DavComponent event : comps) {
//				String href = req.getHref()
//						+ proxy.getCalendarService().getICSName(event);
//				events.put(href, event);
//			}
			ret = new CalendarQueryResultBuilder().build(req, token, proxy,
					requestPropList, comps, propertiesValues);

			logger.info(DOMUtils.toString(ret));

			resp.setStatus(207); // multi status webdav
			resp.setContentType("text/xml; charset=utf-8");
			DOMUtils.serialise(ret, resp.getOutputStream());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
