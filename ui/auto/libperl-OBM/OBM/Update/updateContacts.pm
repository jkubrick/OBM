package OBM::Update::updateContacts;

$VERSION = '1.0';

use OBM::Entities::systemEntityIdGetter;
@ISA = ('OBM::Entities::entityIdGetter');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);
use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $parameters ) = @_;

    my $self = bless { }, $class;


    if( !defined($parameters) ) {
        $self->_log( 'paramètres d\'initialisation non définis', 0 );
        return undef;
    }

    $self->{'entitiesFactories'} = {};

    $self->{'incremental'} = $parameters->{'incremental'};
    $self->{'global'} = $parameters->{'global'};

    if( $self->{'global'} ) {
        $self->{'timestamp'} = 0;
    }

    if( $self->{'incremental'} ) {
        $self->{'timestamp'} = 0;
    }

    require OBM::Ldap::ldapServers;
    if( !($self->{'ldapservers'} = OBM::Ldap::ldapServers->instance()) ) {
        $self->_log( 'initialisation du gestionnaire de serveur LDAP impossible', 3 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    $self->{'entitiesFactories'} = undef;
}


sub update {
    my $self = shift;
    my( $domainIdList ) = @_;

    if( !defined($domainIdList) || (ref($domainIdList) ne 'ARRAY') ) {
        $domainIdList = $self->getDomainsId( 0 );
    }

    if( $self->_initEngines() ) {
        $self->_log( 'problème a l\'initialisation des moteurs de mises à jour', 3 );
        return 1;
    }

    if( $self->_initFactories( $domainIdList ) ) {
        $self->_log( 'problème a l\'initialisation des factories d\'entités', 3 );
        return 1;
    }

    if( $self->_deleteDeletedContact() ) {
        return 1;
    }

    for( my $i=0; $i<=$#{$domainIdList}; $i++ ) {
        if( $self->_updateUpdatedDomainContacts( $domainIdList->[$i] ) ) {
            delete( $self->{'entitiesFactories'}->{$domainIdList->[$i]} );
        }
    }

    if( $self->_doUpdate() ) {
        return 1;
    }

    return 0;
}


sub _initEngines {
    my $self = shift;

    require OBM::Ldap::ldapEngine;
    $self->_log( 'initialisation du moteur LDAP', 2 );
    $self->{'engines'}->{'ldapEngine'} = OBM::Ldap::ldapEngine->new();
    if( !defined($self->{'engines'}->{'ldapEngine'}) ) {
        $self->_log( 'erreur à l\'initialisation du moteur LDAP', 1 );
        return 1;
    }elsif( !ref($self->{'ldapEngine'}) ) {
        $self->_log( 'moteur LDAP non démarré', 3 );
        $self->{'ldapEngine'} = undef;
    }

    return 0;
}


sub _programEntitiesFactory {
    my $self = shift;
    my( $factoryProgramming, $domainId ) = @_;

    if( ref($factoryProgramming) ne 'OBM::EntitiesFactory::factoryProgramming' ) {
        $self->_log( 'programmeur de factory incorrect', 3 );
        return 1;
    }

    if( defined($domainId) && $domainId !~ /$regexp_id/ ) {
        $self->_log( 'Id de domain incorrect', 3 );
        return 1;
    }

    if( defined($domainId) ) {
        my $entitiesFactory = $self->{'entitiesFactories'}->{$domainId};
        if( !defined($entitiesFactory) ) {
            $self->_log( 'factory du domaine d\'ID '.$domainId.' incorrecte', 3 );
            return 1;
        }

        if( $entitiesFactory->loadEntities($factoryProgramming) ) {
            $self->_log( 'problème lors de la programmation de la factory du domaine d\'ID '.$domainId, 3 );
            return 1;
        }
    }else {
        my @entitiesFactories = values(%{$self->{'entitiesFactories'}});

        for( my $i=0; $i<=$#entitiesFactories; $i++ ) {
            if( $entitiesFactories[$i]->loadEntities($factoryProgramming) ) {
                $self->_log( 'problème lors de la programmation de la factory du domaine d\'ID '.$domainId, 3 );
                return 1;
            }
        }
    }

    return 0;
}


sub _initFactories {
    my $self = shift;
    my( $domainIdList ) = @_;

    for( my $i=0; $i<=$#{$domainIdList}; $i++ ) {
        require OBM::entitiesFactory;
        $self->_log( 'initialisation de l\'entity factory pour le domaine '.$domainIdList->[$i], 2 );

        my $entitiesFactory;
        if( !( $entitiesFactory = OBM::entitiesFactory->new( 'PROGRAMMABLE', $domainIdList->[$i], undef, undef ) ) ) {
            $self->_log( 'echec de l\'initialisation de l\'entity factory pour le domaine d\'ID '.$domainIdList->[$i], 0 );
            return 1;
        }

        $self->{'entitiesFactories'}->{$domainIdList->[$i]} = $entitiesFactory;
    }

    return 0;
}


sub _updateUpdatedDomainContacts {
    my $self = shift;
    my( $domainId ) = @_;

    if( $self->_deleteDomainContacts( $domainId ) ) {
        return 1;
    }

    if( $self->_updateDomainContacts( $domainId ) ) {
        return 1;
    }

    return 0;
}


sub _deleteDeletedContact {
    my $self = shift;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 0 );
        return 1;
    }

    my $query = 'SELECT deletedcontact_contact_id AS contact_id
                FROM DeletedContact';

    if( $self->{'timestamp'} ) {
        $query .= ' WHERE unix_timestamp(deletedcontact_timestamp) > \''.$self->{'timestamp'}.'\')';
    }
   
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 1;
    }

    my %contactIds;
    while( my $contact = $queryResult->fetchrow_hashref ) {
        $contactIds{$contact->{'contact_id'}} = 1;
    }

    my @contactIds = keys(%contactIds);

    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $programmingObj->setEntitiesType( 'CONTACT' ) || $programmingObj->setUpdateType( 'DELETE' ) || $programmingObj->setEntitiesIds( \@contactIds )) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de f actory', 4 );
        return 1;
    }

    if( $self->_programEntitiesFactory( $programmingObj ) ) {
        $self->_log( 'probleme lors de la programmation  des contacts supprimés, 3' );
        return 1;
    }


    return 0;
}


sub _deleteDomainContacts {
    my $self = shift;
    my( $domainId ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 0 );
        return 1;
    }

    my $query = 'SELECT contact_id
                    FROM Contact
                    WHERE (contact_privacy=1 OR contact_archive=\'1\')
                    AND contact_domain_id=\''.$domainId.'\'';
    if( $self->{'timestamp'} ) {
        $query .= ' AND unix_timestamp(contact_timeupdate) > \''.$self->{'timestamp'}.'\'';
    }

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 1;
    }

    my %contactIds;
    while( my $contact = $queryResult->fetchrow_hashref ) {
        $contactIds{$contact->{'contact_id'}} = 1;
    }

    my @contactIds = keys(%contactIds);

    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $programmingObj->setEntitiesType( 'CONTACT' ) || $programmingObj->setUpdateType( 'DELETE' ) || $programmingObj->setEntitiesIds( \@contactIds )) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de f actory', 4 );
        return 1;
    }

    if( $self->_programEntitiesFactory( $programmingObj, $domainId ) ) {
        $self->_log( 'probleme lors de la programmation  des contacts supprimés', 3 );
        return 1;
    }


    return 0;
}


sub _updateDomainContacts {
    my $self = shift;
    my( $domainId ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 0 );
        return 1;
    }

    my $query = 'SELECT contact_id
                 FROM Contact
                 WHERE  contact_privacy=0
                    AND contact_archive=\'0\'
                    AND contact_domain_id=\''.$domainId.'\'';
    if( $self->{'timestamp'} ) {
        $query .= ' AND unix_timestamp(contact_timeupdate) > \''.$self->{'timestamp'}.'\'';
    }

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 1;
    }

    my %contactIds;
    while( my $contact = $queryResult->fetchrow_hashref ) {
        $contactIds{$contact->{'contact_id'}} = 1;
    }

    my @contactIds = keys(%contactIds);

    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $programmingObj->setEntitiesType( 'CONTACT' ) || $programmingObj->setUpdateType( 'UPDATE_ENTITY' ) || $programmingObj->setEntitiesIds( \@contactIds )) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de f actory', 4 );
        return 1;
    }

    if( $self->_programEntitiesFactory( $programmingObj, $domainId ) ) {
        $self->_log( 'probleme lors de la programmation  des contacts supprimés', 3 );
        return 1;
    }


    return 0;
}


sub _doUpdate {
    my $self = shift;
    my @engines = values(%{$self->{'engines'}});

    while( my( $domainId, $entitiesFactory ) = each(%{$self->{'entitiesFactories'}}) ) {
        if( !defined($entitiesFactory) ) {
            next;
        }

        $self->_log( 'traitement des contacts du domaine d\'ID '.$domainId, 2 );

        while( my $entity = $entitiesFactory->next() ) {
            for( my $i=0; $i<=$#engines; $i++ ) {
                $engines[$i]->update( $entity );
            }
        }
    }

    return 0;
}
