3D Protein Lab - Docker Container
=================================

This dockerised version of the 3D Porotein Lab Application is based on the [Tomcat MySQL Docker Container]() 
created by Manuel de la Peña. 

I have modified the version of Tomcat, fixed some minor bugs with the MySQL scripts and made the DB script setup the required database 
and copy over the WAR file. So that by building this single docker app you will have a standalone version of the 3D Protein Lab application.

Note: because of the size of the database, you will need go through a few steps once the container is built to get it working (see below).


## Usage

To create the image `3dproteinlab`, execute the following command from inside the docker folder:

```shell
docker build -t 3dproteinlab .
```


## Running the App

You can start your image right away, binding to the external ports 8080 

```shell
docker run -d -p 8080:8080 3dproteinlab 
```
Which is worth doing to check that tomcat is workking.
If you point your browser at http://localhost:8080
You should see the TomCat installation page.

In order for the 3D Protein Lab application to work you are going to need a copy of the database containing
the protien structures and sequences.

The following procedure might seem tedious but we have found it to be the most reliable way to get the DB
working inside the docker container. Largely because the amount of data is too unwieldy to run as an import
inside the build script for the container.

First create a local directory for the data to live in:

```shell
mkdir data
mkdir data/mysql
```

Then start the container but map its internal mysql directory to your new external directory.
This will mean that when the container initialises the db, it will be persisted across runs of the container.


```shell
docker run -d -p 8080:8080 -v $(pwd)/data/mysql:/var/lib/mysql 3dproteinlab
```

Once it has started successfully, shut it down again.

```shell
docker kill <CONTAINER ID>
```

Finally decompress the db archive and copy it into the mysql directory

```shell
cp 3dproteinlab.tar.gz ./data/mysql
cd data/mysql
tar xvzf 3dproteinlab.tar.gz
```

Now start the container again with the local mapping 

```shell
docker run -d -p 8080:8080 -v $(pwd)/data/mysql:/var/lib/mysql 3dproteinlab
```

You should now have a fully functioning version of the app.

Note: We don't need to expose the mysql port because it is only used internally


## Using the App

To use the application by pointing your browser at: 

http://localhost:8080/3dProteinLab


#### Notes
* You will need to have applets enabled in your browser to see the 3d Structures
* You will also need to install the plugin
* The application is perfectly usable without this


## Troubleshooting

Once the container has been built as is running, use the docker process command to get its id number

```shell
docker ps
```

You can then inspect the logs to check that all of the MySQL commands ran successfully.

```shell
docker logs <ID>
``` 

If there are no errors then your container should be running with the data loaded. You can inspect the database by running a bash shell on the container, starting msql and looking at the tables.

```shell
docker exec -it <ID> bash
mysql -uroot
``` 


