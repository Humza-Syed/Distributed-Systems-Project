# Enraged_Penguinos
##################################
PREREQUISITE:
SQL Database with username: root and password: password
Create a database: bankaccount
- CREATE DATABASE bank_accounts
- USE bank_accounts

##################################
SETUP:
Start Docker
- run dockerdesktop or other
docker start activemq
- docker run --name=activemq -it -p 8161:8161 -p 61616:61616 rmohr/activemq:latest

##################################
1.Start bank
- mvn spring-boot:run -pl bank
2. Start bookkeeper
- mvn spring-boot:run -pl bookkeeper
3. Start atmmanager
- mvn exec:java -pl atmmanager

--check activemq that following activemq have been created : http://localhost:8161/admin/
	Queues
	- addreessbookrequest
	- atmtoatmmessagequeue
	- bankstatus
	Topic
	- atmtoatmmanagermessagequeue
	- addressbookupdate
	- fulladdressbok

4.Start atm
- mvn spring-boot:run -pl atm
GOTO localhost:8080 SELECT "START" button
5.Get test account details from SQL database
- SELECT * FROM bankaccount
	accountID, PIN , BANK

