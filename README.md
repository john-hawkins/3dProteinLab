3D Protein Lab
==============
### Search Algorithm for Functional Motifs in Protein Structures

This is a java web application that will allow you to search for structural patterns within a 
modified version of the PDB protein structure database.

To run the application you will need to run a MySQL Server and a J2EE application server, such as Tomcat.

## Installation Instructions

#### 1. Install Tomcat V8.5.23
http://www-us.apache.org/dist/tomcat/tomcat-8/v8.5.23/README.html

#### 2. Install MySQL


#### 3. Create a proteinlb DB and a user  with the following credentials

* dbname: proteinlab 
* user: proteinlab 
* pass: proteinlab


```shell
mysql -uadmin -p$PASS -e "CREATE USER 'proteinlab'@'localhost' IDENTIFIED BY 'proteinlab';"
mysql -uadmin -p$PASS -e "CREATE DATABASE proteinlab"
mysql -uadmin -p$PASS -e "GRANT ALL PRIVILEGES ON proteinlab.* TO 'proteinlab'@'localhost';"
```


#### 4. Load the Database dump into MySQL

The DB dump file contains SQL statements to recreate all required tables into the DB.

You first need to decompress it

```shell
tar .xvzf proteinlab.sql.tar.gz
mysql -uadmin -p$PASS proteinlab < proteinlab.sql
```

Note: For security reasons if your MySQL sevrer has exposed ports 
we recommend you change these DB credentials and recompile the WAR file.

You can change the DB credentials in this file and then recomplile the WAR.
[DB Config File](3DProteinLab/WebContent/WEB-INF/conf/database.properties) 


## Docker

We also provide a docker script for building and running the entire application in a container
 
[Install Instructions](docker/README.md)
 

## Build Instructions
 
I recommend getting the latest version and compiling using Java 8, even though the code base was originally meant for Java 7.

Install [Eclipse Oxygen for Java EE Applications](https://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/oxygen1a)

You will need to download the following libraries (also included in the lib folder) and add them to your project

* BioJava Structure
* Mysql Connector Jar

Create a new project pointing at the 3DProteinLab directory.

Modify whichever files your need to.

Compile and Export as a War file.


