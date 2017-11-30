3D Protein Lab - Docker Container
=================================

This dockerised version of the 3D Porotein Lab Application is based on the [Tomcat MySQL Docker Containe]() 
created by Manuel de la Peña. 

I have modified the version of Tomcat, fxed some minor bugs and made the DB script setup the required database 
and copy over the WAR file. So that by building this single docker app you will have a standalone version of the 3D Protein Lab application.

## Usage

To create the image `3dproteinlab`, execute the following command on the docker folder:

```shell
docker build -t 3dproteinlab .
```

## Running the App

You could start your image right away, binding to the external ports 8080 

```shell
docker run -d -p 8080:8080 3dproteinlab 
```

However, if you want the DB to persist across runs of the the application (highly recommended) 
then you will need to start the container mapping the mysql directory to a local folder, like this:

```shell
mkdir data
mkdir data/mysql
docker run -d -p 8080:8080 -v $(pwd)/data/mysql:/var/lib/mysql 3dproteinlab
```

#### Notes
* We don't need to expose the mysql port because it is only used internally
* Starting the container the first time will take a long time because the database takes a long time to load.

## Using the App

To use the application by pointing your browser at: 

http://localhost:8080/

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


