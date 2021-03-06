/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.freebusy;

import com.google.inject.Module;

/**
 * <p>
 * Classes implementing this module should bind an associated
 * {@link FreeBusyProvider} implementation using
 * {@link com.google.inject.multibindings.Multibinder}.
 * </p>
 * 
 * <p>
 * The associated {@link FreeBusyProvider} implementation will be used to do
 * remote lookup by the FreeBusy servlet.
 * </p>
 */
public interface FreeBusyPluginModule extends Module, Comparable<FreeBusyPluginModule> {
	/**
	 * Returns an integer giving the module a priority. This information
	 * determines the order in which the associated {@link FreeBusyProvider}
	 * implementation will be consulted by the FreeBusy servlet. The higher the
	 * priority the better.
	 * 
	 * @return an integer representing a priority.
	 */
	public int getPriority();
}
