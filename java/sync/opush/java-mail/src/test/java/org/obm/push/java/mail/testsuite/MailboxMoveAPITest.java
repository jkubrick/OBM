package org.obm.push.java.mail.testsuite;

import org.obm.push.java.mail.MailEnvModule;
import org.obm.test.GuiceModule;

@GuiceModule(MailEnvModule.class)
public class MailboxMoveAPITest extends org.obm.push.mail.imap.testsuite.MailboxMoveAPITest {
}
