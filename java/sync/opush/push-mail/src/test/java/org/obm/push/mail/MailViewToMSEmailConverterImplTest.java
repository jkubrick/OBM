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

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.data.ParserException;

import org.junit.Before;
import org.junit.Test;
import org.minig.imap.Address;
import org.minig.imap.Envelope;
import org.minig.imap.Flag;
import org.minig.imap.mime.ContentType;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimePart;
import org.obm.DateUtils;
import org.obm.icalendar.ICalendar;
import org.obm.mail.conversation.EmailView;
import org.obm.mail.conversation.EmailViewAttachment;
import org.obm.mail.conversation.EmailViewInvitationType;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.ms.MSEmail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class MailViewToMSEmailConverterImplTest {

	public static class EmailViewFixture {
		long uid = 1l;
		
		boolean answered = false;
		boolean read = false;
		boolean starred = false;
		
		List<Address> from = ImmutableList.of(new Address("from@domain.test")); 
		List<Address> to = ImmutableList.of(new Address("to@domain.test")); 
		List<Address> cc = ImmutableList.of(new Address("cc@domain.test"));
		String subject = "a subject";
		Date date = DateUtils.date("2004-12-14T22:00:00");

		InputStream bodyData = StreamMailTestsUtils.newInputStreamFromString("message data");
		Integer bodyTruncationSize = null;
		ContentType bodyContentType = new ContentType.Builder().contentType("text/plain").build();
		List<EmailViewAttachment> attachments = ImmutableList.of(new EmailViewAttachment("id", subject, "file", 20));
		InputStream attachmentInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("ics/attendee.ics");
		ICalendar iCalendar = null;
		EmailViewInvitationType invitationType = EmailViewInvitationType.REQUEST;
	}

	private EmailViewFixture emailViewFixture;
	
	@Before
	public void setUp() {
		emailViewFixture = new EmailViewFixture();
	}
	
	@Test
	public void testFlagAnsweredPresent() throws IOException, ParserException {
		emailViewFixture.answered = true;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isAnswered()).isTrue();
	}

	@Test
	public void testFlagAnsweredNotPresent() throws IOException, ParserException {
		emailViewFixture.answered = false;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isAnswered()).isFalse();
	}
	
	@Test
	public void testFlagStarredPresent() throws IOException, ParserException {
		emailViewFixture.starred = true;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isStarred()).isTrue();
	}

	@Test
	public void testFlagStarredNotPresent() throws IOException, ParserException {
		emailViewFixture.starred = false;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isStarred()).isFalse();
	}
	
	@Test
	public void testFlagReadPresent() throws IOException, ParserException {
		emailViewFixture.read = true;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isRead()).isTrue();
	}

	@Test
	public void testFlagReadNotPresent() throws IOException, ParserException {
		emailViewFixture.read = false;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isRead()).isFalse();
	}

	@Test
	public void testUid() throws IOException, ParserException {
		emailViewFixture.uid = 54;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getUid()).isEqualTo(54);
	}

	@Test
	public void testHeaderFromNull() throws IOException, ParserException {
		emailViewFixture.from = null;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getFrom()).containsOnly(MSEmailHeader.DEFAULT_FROM_ADDRESS);
	}

	@Test
	public void testHeaderFromEmpty() throws IOException, ParserException {
		emailViewFixture.from = ImmutableList.of(newEmptyAddress());

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getFrom()).containsOnly(newEmptyMSAddress());
	}
	
	@Test
	public void testHeaderFromSingle() throws IOException, ParserException {
		emailViewFixture.from = ImmutableList.of(new Address("from@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getFrom()).containsOnly(new MSAddress("from@domain.test"));
	}
	
	@Test
	public void testHeaderFromMultiple() throws IOException, ParserException {
		emailViewFixture.from = ImmutableList.of(
				new Address("from@domain.test"), new Address("from2@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getFrom()).containsOnly(
				new MSAddress("from@domain.test"), new MSAddress("from2@domain.test"));
	}

	@Test
	public void testHeaderToNull() throws IOException, ParserException {
		emailViewFixture.to = null;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getTo()).isEmpty();
	}

	@Test
	public void testHeaderToEmpty() throws IOException, ParserException {
		emailViewFixture.to = ImmutableList.of(newEmptyAddress());

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getTo()).containsOnly(newEmptyMSAddress());
	}
	
	@Test
	public void testHeaderToSingle() throws IOException, ParserException {
		emailViewFixture.to = ImmutableList.of(new Address("to@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getTo()).containsOnly(new MSAddress("to@domain.test"));
	}
	
	@Test
	public void testHeaderToMultiple() throws IOException, ParserException {
		emailViewFixture.to = ImmutableList.of(
				new Address("to@domain.test"), new Address("to2@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getTo()).containsOnly(
				new MSAddress("to@domain.test"), new MSAddress("to2@domain.test"));
	}

	@Test
	public void testHeaderCcNull() throws IOException, ParserException {
		emailViewFixture.cc = null;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getCc()).isEmpty();
	}

	@Test
	public void testHeaderCcEmpty() throws IOException, ParserException {
		emailViewFixture.cc = ImmutableList.of(newEmptyAddress());

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getCc()).containsOnly(newEmptyMSAddress());
	}
	
	@Test
	public void testHeaderCcSingle() throws IOException, ParserException {
		emailViewFixture.cc = ImmutableList.of(new Address("cc@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getCc()).containsOnly(new MSAddress("cc@domain.test"));
	}
	
	@Test
	public void testHeaderCcMultiple() throws IOException, ParserException {
		emailViewFixture.cc = ImmutableList.of(
				new Address("cc@domain.test"), new Address("cc2@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getCc()).containsOnly(
				new MSAddress("cc@domain.test"), new MSAddress("cc2@domain.test"));
	}
	
	@Test
	public void testHeaderSubjectNull() throws IOException, ParserException {
		emailViewFixture.subject = null;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getSubject()).isEqualTo(MSEmailHeader.DEFAULT_SUBJECT);
	}
	
	@Test
	public void testHeaderSubjectEmpty() throws IOException, ParserException {
		emailViewFixture.subject = "";

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();

		assertThat(convertedMSEmail.getSubject()).isEqualTo(MSEmailHeader.DEFAULT_SUBJECT);
	}
	
	@Test
	public void testHeaderSubject() throws IOException, ParserException {
		emailViewFixture.subject = "a subject";

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getSubject()).isEqualTo("a subject");
	}
	
	@Test
	public void testHeaderDateNull() throws IOException, ParserException {
		emailViewFixture.date = null;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getDate()).isNull();
	}
	
	@Test
	public void testHeaderDate() throws IOException, ParserException {
		emailViewFixture.date = DateUtils.date("2004-12-14T22:00:00");

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getDate()).isEqualTo(DateUtils.date("2004-12-14T22:00:00"));
	}
	
	@Test
	public void testBodyNoTruncation() throws IOException, ParserException {
		emailViewFixture.bodyTruncationSize = null;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getBody().getTruncationSize()).isNull();
	}
	
	@Test
	public void testBodyTruncationValue() throws IOException, ParserException {
		emailViewFixture.bodyTruncationSize = 1512;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getBody().getTruncationSize()).isEqualTo(1512);
	}
	
	@Test
	public void testBodyContentTypePlainText() throws IOException, ParserException {
		emailViewFixture.bodyContentType = new ContentType.Builder().contentType("text/plain").build();

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getBody().getBodyType()).isEqualTo(MSEmailBodyType.PlainText);
	}

	@Test
	public void testWithoutAttachments() throws IOException, ParserException {
		emailViewFixture.attachments = null;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getAttachements()).isEmpty();
	}
	
	@Test
	public void testAttachments() throws IOException, ParserException {
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getAttachements()).hasSize(1);
	}

	@Test
	public void testWithoutMeetingRequest() throws IOException, ParserException {
		emailViewFixture.attachmentInputStream = null;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getMeetingRequest()).isNull();
	}
	
	@Test
	public void testMeetingRequest() throws IOException, ParserException {
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getMeetingRequest()).isNotNull();
	}
	
	@Test
	public void testNoteMessageClass() throws IOException, ParserException {
		emailViewFixture.attachmentInputStream = null;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getMessageClass()).equals(MSMessageClass.NOTE);
	}
	
	@Test
	public void testRequestedMessageClass() throws IOException, ParserException {
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getMessageClass()).equals(MSMessageClass.SCHEDULE_MEETING_REQUEST);
	}
	
	@Test
	public void testCanceledMessageClass() throws IOException, ParserException {
		emailViewFixture.invitationType = EmailViewInvitationType.CANCELED;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getMessageClass()).equals(MSMessageClass.SCHEDULE_MEETING_REQUEST);
	}
	
	private MSEmail makeConversionFromEmailViewFixture() throws IOException, ParserException {
		return new MailViewToMSEmailConverterImpl().convert(newEmailViewFromFixture());
	}

	private void buildICalendar() throws IOException, ParserException {
		if (emailViewFixture.attachmentInputStream != null) {
			emailViewFixture.iCalendar = new ICalendar.Builder().inputStream(emailViewFixture.attachmentInputStream).build();
		}
	}
	
	private EmailView newEmailViewFromFixture() throws IOException, ParserException {
		buildICalendar();
		
		return new EmailView.Builder()
			.uid(emailViewFixture.uid)
			.flags(flagsListFromFixture())
			.envelope(envelopeFromFixture())
			.bodyMimePart(bodyMimePartFromFixture())
			.bodyMimePartData(emailViewFixture.bodyData)
			.bodyTruncation(emailViewFixture.bodyTruncationSize)
			.attachments(emailViewFixture.attachments)
			.iCalendar(emailViewFixture.iCalendar)
			.invitationType(null)
			.build();
	}

	private Collection<Flag> flagsListFromFixture() {
		Builder<Flag> flagsListBuilder = ImmutableSet.<Flag>builder();
		if (emailViewFixture.answered) {
			flagsListBuilder.add(Flag.ANSWERED);
		}
		if (emailViewFixture.starred) {
			flagsListBuilder.add(Flag.FLAGGED);
		}
		if (emailViewFixture.read) {
			flagsListBuilder.add(Flag.SEEN);
		}
		return flagsListBuilder.build();
	}

	private Envelope envelopeFromFixture() {
		return new Envelope.Builder()
			.from(emailViewFixture.from)
			.to(emailViewFixture.to)
			.cc(emailViewFixture.cc)
			.subject(emailViewFixture.subject)
			.date(emailViewFixture.date)
			.build();
	}

	private IMimePart bodyMimePartFromFixture() {
		MimePart mimePart = new MimePart();
		mimePart.setContentType(emailViewFixture.bodyContentType);
		return mimePart;
	}

	public Address newEmptyAddress() {
		return new Address("");
	}
	
	public MSAddress newEmptyMSAddress() {
		return new MSAddress("");
	}
}