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

package org.obm.push.minig.imap.command;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.base.Splitter;

public class UIDSearchCommand extends Command<MessageSet> {

	// Mon, 7 Feb 1994 21:52:25 -0800

	private SearchQuery sq;

	public UIDSearchCommand(SearchQuery sq) {
		this.sq = sq;
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "UID SEARCH";
		if (!sq.isMatchDeleted()) {
			cmd += " NOT DELETED";
		}
		if (sq.getAfter() != null) {
			DateFormat df = new SimpleDateFormat("d-MMM-yyyy", Locale.ENGLISH);
			cmd += " NOT BEFORE " + df.format(sq.getAfter());
		}
		if (sq.getBefore() != null) {
			DateFormat df = new SimpleDateFormat("d-MMM-yyyy", Locale.ENGLISH);
			cmd += " BEFORE " + df.format(sq.getBefore());
		}
		
		// logger.info("cmd "+cmd);
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void handleResponses(List<IMAPResponse> rs) {
		boolean isOK = isOk(rs);
		data = MessageSet.empty();

		if (isOK) {
			String uidString = null;
			Iterator<IMAPResponse> it = rs.iterator();
			for (int j = 0; j < rs.size() - 1; j++) {
				String resp = it.next().getPayload();
				if (resp.startsWith("* SEARCH ")) {
					uidString = resp;
					break;
				}
			}

			if (uidString != null) {
				// 9 => '* SEARCH '.length
				String uidList = uidString.substring(9);
				Iterable<String> uids = Splitter.on(' ').omitEmptyStrings().split(uidList);
				MessageSet.Builder builder = MessageSet.builder();
				for (String uid : uids) {
					builder.add(Long.valueOf(uid));
				}
				data = builder.build();
			}
		}
	}

}
