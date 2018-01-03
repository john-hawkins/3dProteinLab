Database files
==============

We have copied the contents of the ProteinLab MySQL database directory, tarred it up and split it with the commands

``
tar -zcvf proteinlab.tar.gz  proteinlab/
split -v 500M proteinlab.tar.gz proteinlab_db_
``

You can recreate the director with the following commands:

``
cat proteinlab_db_* > proteinlab.tar.gz
tar -zxvf proteinlab.tar.gz
``

Inside the scripts directory you will also find the mysql dump file and individual scripts for some of the admin tables
that you might want to refresh independently.
 
