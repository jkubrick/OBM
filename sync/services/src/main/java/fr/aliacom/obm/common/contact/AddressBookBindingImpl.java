/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.contact;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.items.AddressBookChangesResponse;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.ContactChangesResponse;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.items.FolderChangesResponse;
import org.obm.sync.server.transactional.Transactional;
import org.obm.sync.services.IAddressBook;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.StoreException;
import fr.aliacom.obm.utils.LogUtils;
import fr.aliacom.obm.utils.ObmHelper;

/**
 * OBM {@link IAddressBook} web service implementation
 */
@Singleton
public class AddressBookBindingImpl implements IAddressBook {

	private static final Log logger = LogFactory.getLog(AddressBookBindingImpl.class);

	private final ContactDao contactDao;
	private final UserDao userDao;
	private final ObmHelper obmHelper;
	private final ContactMerger contactMerger;

	@Inject
	protected AddressBookBindingImpl(ContactDao contactDao, UserDao userDao, ContactMerger contactMerger, ObmHelper obmHelper) {
		this.contactDao = contactDao;
		this.userDao = userDao;
		this.contactMerger = contactMerger;
		this.obmHelper = obmHelper;
	}

	@Override
	@Transactional
	public boolean isReadOnly(AccessToken token, BookType book)	throws AuthFault, ServerFault {
		if (book == BookType.contacts) {
			return false;
		}
		return true;
	}

	@Override
	@Transactional
	public BookType[] listBooks(AccessToken token) {
		return new BookType[] { BookType.contacts, BookType.users };
	}

	@Override
	@Transactional
	public List<AddressBook> listAllBooks(AccessToken token) throws AuthFault, ServerFault {
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			return contactDao.findAddressBooks(con, token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error finding addressbooks ");
		} finally {
			obmHelper.cleanup(con, null, null);
		}
	}
	
	@Override
	@Transactional
	public ContactChangesResponse getSync(AccessToken token, BookType book, Date d)
			throws AuthFault, ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "AddressBook : getSync()");
			return getSync(book, d, token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error find contacts ");
		}
	}

	@Override
	@Transactional
	public AddressBookChangesResponse getAddressBookSync(AccessToken token, Date timestamp)
			throws AuthFault, ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "AddressBook : getAddressBookSync()");
			return getSync(token, timestamp);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error synchronizing contacts ");
		}
	}

	
	private AddressBookChangesResponse getSync(AccessToken token, Date timestamp) throws ServerFault {
		AddressBookChangesResponse response = new AddressBookChangesResponse();
		try {
			response.setContactChanges(getContactsChanges(token, timestamp));
			response.setBooksChanges(getFolderChanges(token, timestamp));
			response.setLastSync(obmHelper.selectNow(obmHelper.getConnection()));
		} catch (Throwable t) {
			logger.error(LogUtils.prefix(token) + t.getMessage(), t);
			throw new ServerFault(t);
		}
		return response;
	}

	private ContactChanges getUsersChanges(AccessToken token, Date timestamp) {
		ContactChanges changes = new ContactChanges();
		changes.setUpdated(userDao.findUpdatedUsers(timestamp, token).getContacts());
		changes.setRemoved(userDao.findRemovalCandidates(timestamp, token));
		return changes;
	}
	
	private ContactChanges getContactsChanges(AccessToken token, Date timestamp) {
		ContactChanges changes = new ContactChanges();
		ContactUpdates updates = contactDao.findUpdatedContacts(timestamp, token);
		changes.setUpdated(updates.getContacts());
		changes.setRemoved(
				Sets.union(updates.getArchived(), 
						contactDao.findRemovalCandidates(timestamp, token)));
		return changes;
	}
	
	private ContactChangesResponse getSync(BookType book, Date timestamp, AccessToken token) throws ServerFault {
		ContactChangesResponse response = new ContactChangesResponse();
		try {
			if (book == BookType.users) {
				response.setChanges(getUsersChanges(token, timestamp));
			} else {
				response.setChanges(getContactsChanges(token, timestamp));
			}
			response.setLastSync(obmHelper.selectNow(obmHelper.getConnection()));
		} catch (Throwable t) {
			logger.error(LogUtils.prefix(token) + t.getMessage(), t);
			throw new ServerFault(t);
		}
		
		return response;
	}
	
	@Override
	@Transactional
	public Contact createContact(AccessToken token, BookType book, Contact contact)
		throws AuthFault, ServerFault {

		try {
			checkContactsAddressBook(token, book);
			
			Contact c = contactDao.createContact(token, contact);
			
			logger.info(LogUtils.prefix(token) + "AddressBook : contact["
					+ c.getFirstname() + " " + c.getLastname() + "] created");
			return c;

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	@Override
	@Transactional
	public Contact createContactWithoutDuplicate(AccessToken token, BookType book, Contact contact)
		throws AuthFault, ServerFault {

		try {
			checkContactsAddressBook(token, book);
			
			contact.setUid(null);

			List<String> duplicates = contactDao.findContactTwinKeys(token, contact);
			if (duplicates != null && !duplicates.isEmpty()) {
				logger.info(LogUtils.prefix(token) + "AddressBook : "
						+ duplicates.size()
						+ " duplicate(s) found for contact ["
						+ contact.getFirstname() + "," + contact.getLastname()
						+ "]");
				Integer contactId = Integer.parseInt(duplicates.get(0));
				contactDao.markUpdated(contactId);
				logger.info(LogUtils.prefix(token) + "Contact["+contactId+"] marked as updated");
				return contactDao.findContact(token, contactId);
			}

			Contact c = contactDao.createContact(token,	contact);
			logger.info(LogUtils.prefix(token) + "AddressBook : contact["
					+ c.getFirstname() + " " + c.getLastname() + "] created");
			return c;

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private void checkContactsAddressBook(AccessToken token, BookType book)
			throws AuthFault, ServerFault, StoreException {
		
		if (isReadOnly(token, book)) {
			throw new StoreException("users not writable");
		}
		
		if (book != BookType.contacts) {
			throw new StoreException("booktype not supported");
		}
	}

	@Override
	@Transactional
	public Contact modifyContact(AccessToken token, BookType book, Contact contact)
		throws AuthFault, ServerFault {

		try {
			checkContactsAddressBook(token, book);
			
			Contact c = modifyContact(token, contact);

			logger.info(LogUtils.prefix(token) + "contact[" + c.getFirstname()
					+ " " + c.getLastname() + "] modified");
			return c;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private Contact modifyContact(AccessToken token, Contact c)
		throws SQLException, FindException {

		Contact previous = contactDao.findContact(token, c.getUid());
		if (previous != null) {
			contactMerger.merge(previous, c);
		} else {
			logger.warn("previous version not found for c.uid: "
					+ c.getUid() + " c.last: " + c.getLastname());
		}
		return contactDao.modifyContact(token, c);
	}
	
	@Override
	@Transactional
	public Contact removeContact(AccessToken token, BookType book, String uid)
			throws AuthFault, ServerFault {

		Integer integerUid = null;
		try {
			integerUid = Integer.valueOf(uid);
		} catch (NumberFormatException e) {
			logger.error(LogUtils.prefix(token) + "contact uid is not an integer", e);
			return null;
		}

		try {
			checkContactsAddressBook(token, book);

			Contact c = contactDao.removeContact(token, integerUid);
			logger.info(LogUtils.prefix(token) + "contact[" + uid
					+ "] removed (archived)");
			return c;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public Contact getContactFromId(AccessToken token, BookType book, String id)
			throws AuthFault, ServerFault {
		if (book != BookType.contacts) {
			return null;
		}

		try {
			int contactId = Integer.parseInt(id);
			return contactDao.findContact(token, contactId);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public KeyList getContactTwinKeys(AccessToken token, BookType book, Contact contact)
		throws AuthFault, ServerFault {

		try {
			checkContactsAddressBook(token, book);
			
			List<String> keys = contactDao.findContactTwinKeys(token, contact);
			return new KeyList(keys);

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public List<Contact> searchContact(AccessToken token, String query, int limit) 
		throws AuthFault, ServerFault {

		try {
			return contactDao.searchContact(token, query, limit);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public FolderChangesResponse getFolderSync(AccessToken token, Date d)
			throws AuthFault, ServerFault {

		try {
			logger.info(LogUtils.prefix(token) + "AddressBook : getFolderSync(" + d + ")");
			FolderChangesResponse sync = getFolderSync(d, token);
			return sync;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e);
		}
	}

	private FolderChangesResponse getFolderSync(Date timestamp, AccessToken token) throws Throwable {
		try {
			FolderChangesResponse response = new FolderChangesResponse();
			response.setFolderChanges(getFolderChanges(token, timestamp));
			response.setLastSync(obmHelper.selectNow(obmHelper.getConnection()));
			return response;
		} catch (Throwable t) {
			throw  new ServerFault(t);
		}
	}

	private FolderChanges getFolderChanges(AccessToken token, Date timestamp) {
		FolderChanges changes = new FolderChanges();
		changes.setUpdated(contactDao.findUpdatedFolders(timestamp, token));
		changes.setRemoved(contactDao.findRemovedFolders(timestamp, token));
		return changes;
	}
	
	@Override
	@Transactional
	public List<Contact> searchContactInGroup(AccessToken token, AddressBook book, String query, int limit) throws AuthFault, ServerFault {

		try {
			return contactDao.searchContact(token, book, query, limit);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public Contact createContactInBook(AccessToken token, int addressBookId,
			Contact contact) throws AuthFault, ServerFault {
		try {
			Contact c = contactDao.createContactInAddressBook(token, contact, addressBookId);
			
			logger.info(LogUtils.prefix(token) + "AddressBook : contact["
					+ c.getFirstname() + " " + c.getLastname() + "] created");
			return c;

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	
	@Override
	@Transactional
	public Contact modifyContactInBook(AccessToken token, int addressBookId,
			Contact contact) throws AuthFault, ServerFault {
		try  {
			return modifyContact(token, contact);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e);
		}
	}
	
	@Override
	@Transactional
	public Contact removeContactInBook(AccessToken token, int addressBookId,
			String uid) throws AuthFault, ServerFault {
		return removeContact(token, BookType.contacts, uid);
	}
	
	@Override
	@Transactional
	public Contact getContactInBook(AccessToken token, int addressBookId,
			String id) throws AuthFault, ServerFault {
		return getContactFromId(token, BookType.contacts, id);
	}

	@Override
	@Transactional
	public boolean unsubscribeBook(AccessToken token, Integer addressBookId) throws AuthFault, ServerFault {
		try {
			return contactDao.unsubscribeBook(token, addressBookId);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
}