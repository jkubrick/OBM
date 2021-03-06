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
package org.obm.push.mail.imap;

import java.io.InputStream;

import org.obm.push.mail.mime.MimeAddress;

import com.google.common.io.ByteStreams;
import com.google.inject.Singleton;
import com.sun.mail.imap.IMAPInputStream;
import com.sun.mail.imap.IMAPMessage;

@Singleton
public class MessageInputStreamProviderImpl implements MessageInputStreamProvider {

	private final static int NO_MAX_BYTE_COUNT = -1;
	private final static boolean USE_PEEK = true;
	
	@Override
	public InputStream createMessageInputStream(IMAPMessage messageToFetch, MimeAddress mimePartAddress) {
		return new IMAPInputStream(messageToFetch, address(mimePartAddress), NO_MAX_BYTE_COUNT, USE_PEEK);
	}
	
	@Override
	public InputStream createMessageInputStream(IMAPMessage messageToFetch, MimeAddress mimePartAddress, Integer limit) {
		IMAPInputStream imapInputStream = 
				new IMAPInputStream(messageToFetch, address(mimePartAddress), NO_MAX_BYTE_COUNT, USE_PEEK);
		return ByteStreams.limit(imapInputStream, limit);
	}
	
	private String address(MimeAddress mimeAddress) {
		if (mimeAddress != null) {
			return mimeAddress.getAddress();
		} else {
			return null;
		}
	}	
}
