# 3dProteinLab
### Search Algorithm for Functional Motifs in Protein Structures

This is a java web application that will allow you to search for structural patterns within a 
modified version of the PDB protein structure database.

To run the application you will need to run a MySQL Server and a J2EE application server, such as Tomcat.

## Installation Instructions

1.Install Tomcat V8.5.23
http://www-us.apache.org/dist/tomcat/tomcat-8/v8.5.23/README.html

2.Install MySQL


3.Create a DB with the following credentials


4.Load the Database dump into MySQL


Note: For security reasons if your MySQL sevrer has exposed ports 
we recommend you chnage these DB credentials and recompile the WAR file.


## Docker

We also provide a docker script for building and running the entire application in a container
 
[Install Instructions](docker/README.md)
 

