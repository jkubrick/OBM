package org.obm.push.minig.imap.testsuite;

import org.obm.push.mail.imap.GuiceModule;
import org.obm.push.minig.imap.ExternalProcessMailEnvModule;

@GuiceModule(ExternalProcessMailEnvModule.class)
public class MailboxMemoryAPITest extends org.obm.push.mail.imap.testsuite.MailboxMemoryAPITest {
}
