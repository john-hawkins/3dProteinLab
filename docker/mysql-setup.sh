mysql -uadmin -p$PASS -e "CREATE DATABASE proteinlab"
mysql -uadmin -p$PASS -e "CREATE USER 'proteinlab'@'localhost' IDENTIFIED BY 'proteinlab';"
mysql -uadmin -p$PASS -e "GRANT ALL PRIVILEGES ON proteinlab.* TO 'proteinlab'@'localhost';"
