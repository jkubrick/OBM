-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.8.5 to 0.8.6                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
--
-- Table structure for table 'ObmInfo'
--
CREATE TABLE ObmInfo (
  obminfo_name   varchar(32) NOT NULL default '',
  obminfo_value  varchar(255) default '',
  PRIMARY KEY (obminfo_name)
);

INSERT INTO ObmInfo (obminfo_name, obminfo_value) values ('db_version', '0.8.6');


-------------------------------------------------------------------------------
-- Update Country relations : key is now : iso3166 + lang
-------------------------------------------------------------------------------
-- Company Table

ALTER TABLE Company CHANGE company_country_id company_country_iso3166 char(2) null default '';
UPDATE Company left join Country on company_country_iso3166=country_id
set company_country_iso3166 = country_iso3166;

-- Contact Table

ALTER TABLE Contact CHANGE contact_country_id contact_country_iso3166 char(2) null default '';
UPDATE Contact left join Country on contact_country_iso3166=country_id
set contact_country_iso3166 = country_iso3166;

-- Country table

ALTER TABLE Country DROP COLUMN Country_id;
ALTER TABLE Country DROP PRIMARY KEY;
ALTER TABLE Country ADD PRIMARY KEY (country_iso3166, country_lang);


-------------------------------------------------------------------------------
-- Update Invoice table
-------------------------------------------------------------------------------
-- Maintenance for old database which had different fields order
ALTER TABLE Invoice CHANGE COLUMN invoice_timeupdate invoice_timeupdate timestamp(14) after invoice_id;
ALTER TABLE Invoice CHANGE COLUMN invoice_timecreate invoice_timecreate timestamp(14) after invoice_timeupdate;
ALTER TABLE Invoice CHANGE COLUMN invoice_userupdate invoice_userupdate int(8) after invoice_timecreate;
ALTER TABLE Invoice CHANGE COLUMN invoice_usercreate invoice_usercreate int(8) after invoice_userupdate;

-- New columns
ALTER TABLE Invoice ADD COLUMN invoice_company_id int(8) NOT NULL after invoice_usercreate;
ALTER TABLE Invoice ADD COLUMN invoice_deal_id int(8) DEFAULT NULL after invoice_company_id;
ALTER TABLE Invoice ADD COLUMN invoice_project_id int(8) DEFAULT NULL after invoice_deal_id;

UPDATE Invoice left join DealInvoice on invoice_id=dealinvoice_invoice_id
               left join Deal on dealinvoice_deal_id=deal_id
set invoice_deal_id = deal_id,
invoice_company_id = deal_company_id;

DROP Table IF EXISTS DealInvoice;


-------------------------------------------------------------------------------
-- Update Payment table
-------------------------------------------------------------------------------
-- Maintenance for old database which had different fields order
ALTER TABLE Payment CHANGE COLUMN payment_usercreate payment_usercreate int(8) after payment_userupdate;

-- New columns
ALTER TABLE Payment ADD COLUMN payment_invoice_id int(8) NOT NULL after payment_usercreate;
ALTER TABLE Payment ADD COLUMN payment_company_id int(8) NOT NULL after payment_invoice_id;


UPDATE Payment left join PaymentInvoice on payment_id=paymentinvoice_payment_id
               left join Invoice on paymentinvoice_invoice_id=invoice_id
set payment_invoice_id = invoice_id,
payment_company_id = invoice_company_id;

DROP Table IF EXISTS PaymentInvoice;
