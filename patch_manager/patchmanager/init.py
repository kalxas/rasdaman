from trac.core import *
from trac.db import *
from trac.env import IEnvironmentSetupParticipant

last_db_version = 1

class PatchManagerInit(Component):
    implements(IEnvironmentSetupParticipant)

    def environment_created(self):
        pass

    def environment_needs_upgrade(self, db):
        cursor = db.cursor()

        # add column
        try:
            cursor.execute("ALTER TABLE Patches ADD rejected int")
            self.log.info("Added rejected column to Patches table")
        except:
            pass
        return False
        #return self._get_db_version(cursor) != last_db_version

    def upgrade_environment(self, db):
        cursor = db.cursor()

        # Get current database schema version
        db_version = self._get_db_version(cursor)

        # Perform incremental upgrades
        for I in range(db_version + 1, last_db_version):
            script_name  = 'db%i' % (I)
            module = __import__('patchmanager.db.%s' % (script_name),
            globals(), locals(), ['do_upgrade'])
            module.do_upgrade(self.env, cursor)

    def _get_db_version(self, cursor):
        try:
            sql = "SELECT value FROM system WHERE name='patchmanager_version'"
            self.log.debug(sql)
            cursor.execute(sql)
            for row in cursor:
                return int(row[0])
            return -1
        except:
            return -1
