from trac.db import Table, Column, Index, DatabaseManager

tables = [
  Table('Patches', key = 'id')[
    Column('id', type = 'integer', auto_increment = True),
    Column('email', 'varchar'),
    Column('subject', 'mediumtext'),
    Column('branch', 'varchar'),
    Column('commit_time', type = 'datetime'),
    Column('submit_time', type = 'datetime'),
    #
  ]
]

def do_upgrade(env, cursor):
    db_connector, _ = DatabaseManager(env)._get_connector()

    # Create tables
    for table in tables:
        for statement in db_connector.to_sql(table):
            cursor.execute(statement)

    # Set database schema version.
    cursor.execute("INSERT INTO system (name, value) VALUES"
      " ('patchmanager_version', '1')")
