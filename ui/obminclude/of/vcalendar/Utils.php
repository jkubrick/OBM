<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



/**
 * Some utils 
 *
 * @package
 * @version $Id:$
 * @copyright Copyright (c) 1997-2007 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr>
  */
class Vcalendar_Utils {

  static function getFileType($file) {
    $handle = fopen($file, 'r');
    while($line = fgets($handle)) {
      if(preg_match('/^\s*VERSION\s*:\s*(.*)$/i',$line,$match)) {
        fclose($handle);
        if(trim($match[1]) == '1.0') {
          return 'vcs';
        } elseif(trim($match[1]) == '2.0') {
          return 'ics';
        }
      }
    }
    fclose($handle);
    return null;
  }
  
  static function entityExist($id, $entity) {
    $fn = $entity.'Exist';
    return self::$fn($id);
  }

  static function userExist($id) {
    $db = new DB_OBM;
    $query = 'SELECT userobm_id From UserObm WHERE userobm_id = '.$id;
    $db->query($query);
    return $db->next_record();
  }

  static function resourceExist($id) {
    $db = new DB_OBM;
    $query = 'SELECT resource_id From Resource WHERE resource_id = '.$id;
    $db->query($query);
    return $db->next_record();
  }  

  static function privatizeEvent($vevent) {
    if($vevent->private) {
      $vevent->reset('summary');
      $vevent->set('summary', $GLOBALS['l_private']);
      $vevent->reset('description');
      $vevent->set('description', $GLOBALS['l_private']);
      $vevent->reset('location');
      $vevent->set('location', $GLOBALS['l_private']);
      $vevent->reset('categories');
      $vevent->set('categories', array($GLOBALS['l_private']));
    }
    return $vevent;
  }
}

