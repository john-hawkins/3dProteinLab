# CREATE A DATABASE FOR THE PROTEIN SEQUENCE AND STRUCTURES
mysql -uadmin -p$PASS -e 'CREATE DATABASE foo CHARACTER SET UTF8'
mysql -uadmin -p$PASS -e "CREATE DATABASE mydb"

# LOAD IT WITH THE DB DUMP FILE
mysql -uadmin -p$PASS mydb < /tmp/dump.sql

# ADD THE USER FOR THE WEB APPLICATION WITH ACCESS
mysql -uadmin -p$PASS -e "CREATE USER 'newuser'@'localhost' IDENTIFIED BY 'password';"
mysql -uadmin -p$PASS -e "GRANT ALL PRIVILEGES ON mydb.* TO 'newuser'@'localhost';"
