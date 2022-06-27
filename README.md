

# Food diary app with Alexa


This Alexa app has three components:

1. Alexa Skill
2. MariaDB database on the AWS RDS
3. Lambda function on the AWS



## Running the web service on Amazon Web Services (AWS)

There are several options for running a web service on AWS.

For example, the web service could be run on EC2 (Elastic Cloud Computed), or as a lambda function.
A database could be run on documentDB, DynamoDB, or the Relational Database Service (RDS).

We are using a lambda function with MariaDB on the RDS, largely because they both have a free tier great for piloting:
- Lambda [free tier](https://aws.amazon.com/lambda/pricing/) includes 1M free requests per month and 400,000 GB-seconds of compute time per month.
- RDS [free tier](https://aws.amazon.com/rds/free/) includes 750 hours of a Single-AZ db.t2.micro instance and 20GB storage, plus 20GB backup storage too.

To use AWS the first thing to do is to make a free account [here](aws.amazon.com/).




### General AWS setup


#### Set up a admin IAM user

Instructions are [here](https://docs.aws.amazon.com/IAM/latest/UserGuide/getting-started_create-admin-group.html).


#### Install AWS command line tool

Instructions are [here](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2-mac.html#cliv2-mac-install-gui).


#### Configure credentials:

Fill in the details for the admin user at the prompts of this command:

```bash
aws configure
```

Now we can get the admin user details with this command:

```bash
aws sts get-caller-identity
```



### MariaDB setup on AWS RDS

1. Go to the AWS Management Console and click on RDS.

2. Click on Databases and then Create database. Choose 'easy create' and 'MariaDB'.

This includes a inbound rule for your current IP address. If you want to connect to the database on the command line from other places, you'll need to add other inbound rules for other IPs.

This is a [helpful troubleshooting page](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_Troubleshooting.html#CHAP_Troubleshooting.Connecting).


3. Modify database, and set 'publically accessible' to yes.

When doing any modifications make sure you opt to 'apply immediately', not at the 'next development window'.

4. Check connection

`nc -zv <endpoint> <port>`

You should get the response: "Connection to <endpoint> port <port> [tcp/mysql] succeeded!

Note doing number 3 above, it seems to take a few minutes for this command to work.

5. Connect using db master user (default is admin)

`mysql -h <endpoint> -P <port> -u admin -p`


6. Create DB and tables

```
CREATE DATABASE fooddiary;

USE fooddiary;

CREATE TABLE intake_event_alexa (
	event_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	user_id VARCHAR(300) NOT NULL,
	device_id VARCHAR(300) NOT NULL,
	partial bool,
	creation_timestamp DATETIME NOT NULL,
	breadcrumbs VARCHAR(200),
	intake_time TIME,
	intake_time_of_day VARCHAR(200),
	intake_date DATE
);

CREATE TABLE  intake_element_alexa (
        element_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        event_id INT NOT NULL,
        description VARCHAR(500),
        creation_timestamp DATETIME NOT NULL,
        CONSTRAINT `fk_element_event`
         FOREIGN KEY (event_id) REFERENCES intake_event_alexa (event_id)
         ON DELETE CASCADE
         ON UPDATE CASCADE,
);

```

Export using:

```bash
mysql -h <endpoint> fooddiary -P<port> -u admin -p -e "select * from intake_event_alexa" > intake_event_alexa.tab
mysql -h <endpoint> fooddiary -P<port> -u admin -p -e "select * from intake_element_alexa" > intake_element_alexa.tab

```

7. Add user

```
insert into user (creation_timestamp) values (NOW());
```

Check new user row:

```
select * from user;
```




### Alexa skill setup


a) Create a skill

Go to the Amazon developer console [here](https://developer.amazon.com/alexa/) and 'create a new skill'.


b) Set up interaction model 

Interaction model is made of intents (ways a user can interact with the skill).

Intents each map to a java class in the lambda function code. 
They can have associated slots, components of a users speech that are each put into separate variables for use in the java code.


The skill can be setup using the JSON editor in the Alexa Developer Console. 

To do this drag and drop the JSON file model/fooddiaryskill.json onto the JSON editor.




### AWS Lambda setup

An example of an Alexa lambda function not using the RDS is [here](https://github.com/alexa/skill-sample-java-fact).


#### 1. Create lambda function [here](https://eu-west-2.console.aws.amazon.com/lambda/) with details:

Set the name as 'alexaFoodDiary' and create a new execution role, selecting 'Create a new role with basic Lambda permissions'.

The new role with have a name something like 'alexaFoodDiary-role-XXXXXXXX'.


#### 2. Add VPC access to role

Our lambda function needs particular permissions to access the MariaDB on the RDS. 
We do this by allowing the lambda access to the VPC (shared between the DB and lambda function).

Add AWSLambdaVPCAccessExecutionRole to the new role.

See info [here](https://docs.aws.amazon.com/lambda/latest/dg/lambda-intro-execution-role.html).



NB. There are instructions [here](https://docs.aws.amazon.com/lambda/latest/dg/services-rds-tutorial.html) for creating a 'lambda-vpc-role'.
However, we don't do it this way, instead as above we add AWSLambdaVPCAccessExecutionRole to the lambda default role.
There is also a AmazonRDSFullAccess policy, but this isn't needed since they are in the same VPC.


Useful commands to look at roles:
```bash
aws iam get-role --role-name <role-name>
aws iam list-role-policies --role-name <role-name>
aws iam list-attached-role-policies --role-name <role-name>
```


#### 3. Set trigger

Add trigger, select 'alexa skills kit' and enter the skill ID.

The skill ID can be found using the 'copy skill ID' link in the main skill listing page of the [Alexa Developer console](https://developer.amazon.com/alexa/console/ask).

Info is [here](https://developer.amazon.com/en-US/docs/alexa/custom-skills/host-a-custom-skill-as-an-aws-lambda-function.html#configuring-the-alexa-skills-kit-trigger).


#### 4. Add jar to lambda function

Maven has a file called pom.xml, that stores all the dependencies for the project, so that we can build the java (into a jar and war) using just one simple command. 
In the directory where the pom.xml is run:

```bash
mvn assembly:assembly -DdescriptorId=jar-with-dependencies package
```

Under the Function code heading on the lamdba configuration page, click 'actions' then 'upload a .zip or .jar file'.

The generated jar file is `fooddiaryservlet-jar-with-dependencies.jar` in the `lambda-function/target/` directory.

#### 5. Set handler function

Under the 'Basic settings' section of the lambda configuration page, click edit and set hander function as 
'uk.org.datamining.fooddiaryservlet.FoodDiaryWholeInputSkillHandler'.


#### 6. Set up VPC 

Amazon Virtual Private Cloud (VPC) is a private network of AWS resources that can then talk to each other.

Follow the instructions [here](https://docs.aws.amazon.com/lambda/latest/dg/configuration-vpc.html), to enable lambda function to access our VPC.


#### 7. Set up environment variables in lambda function


Use the following command to set environment variables in the lambda function, replacing [DATABASE-PASSWORD] with the MariaDB password and [DATABASE-ENDPOINT] with the RDS endpoint URL: 

```bash
aws lambda update-function-configuration --function-name alexaFoodDiary --environment "Variables={RDS_FD_PWD=[DATABASE-PASSWORD],RDS_FD_USERNAME=admin,RDS_HOST_NAME=[DATABASE-ENDPOINT]}"
```


#### 8. Set up Alexa Skill

This step sets up the alexa skill in the Alexa Developer Console, so it points to the lambda function.

In the skill set the enpoint to 'AWS Lambda ARN', and add the ARN of the lambda function.





### Other possibly useful things




#### Commands and info found when trying to decide how to provide DB credentials to lambda function.


Create KMS policy and add KMS to policy role:
https://docs.aws.amazon.com/lambda/latest/dg/configuration-envvars.html

https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/AuroraMySQL.Integrating.Authorizing.IAM.KMSCreatePolicy.html



```bash
aws kms create-key --description kms-test --region eu-west-2 
```

We don't provide a key-id because we use a aws managed key (it's free)

```bash
aws ssm put-parameter --name /alexaFoodDiary/value1 --value "TEST" --type SecureString  --region eu-west-2
```

You can view the keys with this command:

```bash
aws kms list-keys
```

and more details with:

```bash
aws kms describe-key --key-id <key-id>
```


https://aws.amazon.com/blogs/security/how-to-securely-provide-database-credentials-to-lambda-functions-by-using-aws-secrets-manager/

https://docs.aws.amazon.com/kms/latest/developerguide/concepts.html




#### RDS proxy

[RDS proxy](https://aws.amazon.com/blogs/compute/using-amazon-rds-proxy-with-aws-lambda/) might be useful in the future. It manages connections between lambda
and RDS to help the RDS manage multiple connection requests.






