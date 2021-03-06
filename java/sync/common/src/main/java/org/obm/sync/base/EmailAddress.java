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
package org.obm.sync.base;

import java.io.Serializable;

import org.obm.push.utils.UserEmailParserUtils;
import org.obm.sync.book.IMergeable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class EmailAddress implements IMergeable, Serializable {
	
	public static final String AT = "@";

	public static boolean isEmailAddress(String emailToCheck) {
		return new UserEmailParserUtils().isAddress(emailToCheck);
	}
	
	public static EmailAddress loginAtDomain(String loginAtDomain) {
		UserEmailParserUtils userEmailParserUtils = new UserEmailParserUtils();
		Preconditions.checkArgument(userEmailParserUtils.isAddress(loginAtDomain));
		return new EmailAddress(
				new EmailLogin(userEmailParserUtils.getLogin(loginAtDomain)),
				new DomainName(userEmailParserUtils.getDomain(loginAtDomain)));
	}
	
	public static EmailAddress loginAndDomain(EmailLogin login, DomainName domain) {
		Preconditions.checkArgument(login != null, "login is required");
		Preconditions.checkArgument(domain != null, "domain is required");
		return new EmailAddress(login, domain);
	}

	private final EmailLogin login;
	private final DomainName domain;
	private final String loginAtDomain;

	private EmailAddress(EmailLogin login, DomainName domain) {
		this.login = login;
		this.domain = domain;
		this.loginAtDomain = login.get() + AT + domain.get();
	}

	public String get() {
		return loginAtDomain;
	}

	public EmailLogin getLogin() {
		return login;
	}

	public DomainName getDomain() {
		return domain;
	}

	@Override
	public void merge(IMergeable previous) {
		//do nothing on merge
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(login, domain);
	}
	
	@Override
	public final boolean equals(Object object) {
		if (object instanceof EmailAddress) {
			EmailAddress that = (EmailAddress) object;
			return Objects.equal(this.login, that.login)
				&& Objects.equal(this.domain, that.domain);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("login", login)
			.add("domain", domain)
			.toString();
	}
}
