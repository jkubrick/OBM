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
package org.obm.push.mail;

import org.minig.imap.mime.IMimePart;

import com.google.common.base.Preconditions;


public class FetchInstructions {

	public static class Builder {
		private IMimePart mimePart;
		private Integer truncation;
		
		public Builder() {}
		
		public Builder mimePart(IMimePart mimePart) {
			this.mimePart = mimePart;
			return this;
		}
		
		public Builder truncation(Integer truncation) {
			this.truncation = truncation;
			return this;
		}
		
		public FetchInstructions build() {
			Preconditions.checkNotNull(this.mimePart, "MimePart can't be null.");
			return new FetchInstructions(
					this.mimePart, this.truncation);
		}
	}
	
	private final IMimePart mimePart;
	private final Integer truncation;
	
	private FetchInstructions(IMimePart mimePart, Integer truncation) {
		this.mimePart = mimePart;
		this.truncation = truncation;
	}
	
	public IMimePart getMimePart() {
		return mimePart;
	}
	
	public Integer getTruncation() {
		return truncation;
	}
}