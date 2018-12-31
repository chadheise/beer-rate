#!/bin/bash

# Build the project
mvn -f beerRate/pom.xml clean install

# Create a fat jar
mvn -f beerRate/pom.xml assembly:single

# Rename jar
mkdir /tmp/beerRate
mv beerRate/target/beerRate-0.0.1-SNAPSHOT-jar-with-dependencies.jar /tmp/beerRate/beerRate.jar

# Zip the jar
zip -r beerRate/target/beerRate.zip /tmp/beerRate

# Deploy to AWS elastic beanstalk
# (requires proper setup via eb init and in .elasticbeanstalk/config.yml)
#eb deploy --staged