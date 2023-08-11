```
Name: ApipServerGuide
Version: 1.0.0
Description : A guide for building an APIP server 
Authors: C_armX
Language: en-US
CreateDate: 2023-06-09
UpdateDate: 2023-06-15
```

# APIP Server Guide

## 0. Download resources

1. All the documents including APIP, FEIP, and FIPA protocols can be found at [https://github.com/nobodyoffc/FCH-FEIP-APIP/tree/master/Doc ](https://github.com/nobodyoffc/FCH-FEIP-APIP/tree/master/Doc )
2. Freecash node: [https://github.com/freecashorg/freecash/releases ](https://github.com/freecashorg/freecash/releases )
3. FchParser: [https://cid.cash/download/FchParser.jar](https://cid.cash/download/FchParser.jar)
4. FeipParser: [https://cid.cash/download/FeipParser.jar](https://cid.cash/download/FeipParser.jar)
5. ApipManager: [https://cid.cash/download/ApipManager.jar](https://cid.cash/download/ApipManager.jar)
6. ApipServer: [https://cid.cash/download/APIP.war](https://cid.cash/download/APIP.war)
7. fcTools: [https://cid.cash/download/FcTools.jar](https://cid.cash/download/FcTools.jar)

## 1. System

* Required
    - OS: linux or MacOs
    - CPU: 2 cores
    - Memory: 8G
    - Disk: 50G
    - Network: 2M

* Used in test
    - Ubuntu 22.04.2 LTS (GNU/Linux 5.15.0-73-generic x86_64)
    - CPU 8core
    - memory 24G
    - Disk: 256G SSD
    - NetWork 40M

## 2. Install freecash full node

* Install docker: https://get.docker.com
```
	$ sudo curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
```
* run docker 
```
	$ sudo systemctl start docker
	$ docker -v
	Docker version 24.0.2, build cb74dfc
```
* Download fc_miner_docker_1.0.5.zip
```
	$ wget --no-check-certificate https://download.sign.cash/fc_miner_docker_1.0.5.zip
```
* Unzip fc_miner_docker_1.0.5.zip
```
	$ unzip fc_miner_docker_1.0.5.zip
```
* Install fc_miner_1.0.5
```
	$ cd fc_miner_1.0.5
	$ sudo ./install.sh
```
* Create fc_miner container in docker
```
	$ sudo docker run -dit --name fc_miner --net=host -v /home/armx/fc_data:/opt/newcoin fc.io:latest /bin/bash
	cba65abb2e535aa574a51d997c47726db4511c21f01fb23649762f9cc9f2a6aa
```
* Check if the container is started. 
```
	$ sudo docker ps -a
	CONTAINER ID   IMAGE          COMMAND       CREATED              STATUS              PORTS     NAMES
	cba65abb2e53   fc.io:latest   "/bin/bash"   About a minute ago   Up About a minute             fc_miner
```
- Start the container if STATUS is Exited
```
	$ sudo docker start cba65abb2e53
```
- Get into the container:
```
	$ sudo docker exec -it cba65abb2e53 /bin/bash
	root@yourhost:/# 
	
```
* Note: You are using root role. To run ElasticSearch you can't use root and have to create the fc_data directory of freecash with non-root role.
* Add the same name of armx within the container
```
	$ useradd armx
```
* Make sure the user armx has the same id in `/etc/group` and `/etc/passwd` inside and outside the container. If not, change it:
```
	$ vi /etc/group
	$ vi /etc/passwd
```
* Make armx being the owner of directory /opt
```
    $ chown -R armx:armx opt
```
* login with armx
```
	$ su armx
```
* To start freecashRPC service, it's necessary to create freecash.conf.
```
    $ cd opt/newcoin
    $ vi freecash.conf
```

* Write settings below :
```
server=1
rpcuser=user
rpcpassword=password
rpcallowip=127.0.0.1
```


* Start freecash node
```
	$ freecashd -listen=1 -server=1 -datadir=/opt/newcoin -logtimemicros -gen=0 -daemon
	Freecash server starting
```
- "-server=1" makes freecashRPC service started.
- "-gen=0" closed mining.

* Check the blockchain info after a while of the node started 
```
	$ freecash-cli -datadir=/opt/newcoin getblockchaininfo
	{
	  "chain": "main",
	  "blocks": 852,
	  "headers": 80000,
	  "bestblockhash": "00000000001a6556275784d362265bd83d7b2629377a969a78748e6aa5ded69c",
	  "difficulty": 227.9880957234294,
	  "mediantime": 1577869206,
	  "verificationprogress": 1.106386996875033e-06,
	  "initialblockdownload": true,
	  "chainwork": "0000000000000000000000000000000000000000000000000000586cb489e8b2",
	  "size_on_disk": 286570,
	  "pruned": false,
	  "warnings": ""
	}
```
* Check freecashRPC:
```
    $ curl --user user --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblockchaininfo", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/ | cat
    Enter host password for user 'user':password
```

* Wait for your node catching up the newest block.
* If you want to stop freecash node:
```
    # exit
    $ exit
	$ sudo docker ps -a
	$ sodu docker stop containerId
```
* To restart freecash node:
```
    $ sudo docker start cba65abb2e53
    $ sudo docker exec -it cba65abb2e53 /bin/bash
    root@yourhost:/# su armx
    $ freecashd -listen=1 -server=1 -datadir=/opt/newcoin -logtimemicros -gen=0 -daemon
```


## 3. Install ElasticSearch
* Download ES
```
	$ wget --no-check-certificate https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.8.0-linux-x86_64.tar.gz
```
* Unzip
```
	$ tar -zxvf elasticsearch-8.8.0-linux-x86_64.tar.gz
```
* start ES
```
	$ cd elasticsearch-8.8.0/bin
	$ ./elasticsearch
```
* Save the password of user `elastic`

## 4. Install Java
* Install
```
	$ sudo apt install openjdk-19-jre-headless
```
   For CentOs:
```
	$ sudo yum install java-latest-openjdk.x86_64
```
* Find the directory of jvm
```
	$ readlink -f /usr/bin/java
```
or
```
	$ update-alternatives --list java
```
* Import ES certificate into Java
	
- Find the java_HOME directory, assuming it's "/usr/lib/jvm/java-19-openjdk-amd64/".
```
	$ sudo keytool -importcert -file  /home/armx/elasticsearch-8.8.0/config/certs/http_ca.crt  -keystore /usr/lib/jvm/java-19-openjdk-amd64/lib/security/cacerts -storepass changeit -alias es
```
## 5. Run Redis

* install redis
```
    * sudo apt install redis
```

* Install and start redis on CentOs
```
    $ sudo yum install epel-release
    $ sudo yum install redis
    $ sudo systemctl start redis
    $ redis-cli
    > ping
```
* set persistence: find redis.conf and set RDF or AOF to make your data persistently saved on your disk.

## 6. Parse Freecash blockchain
* Copy apip.zip to you server:
```
	$ scp apip.zip armx@<server-ip>:/home/armx/apip/
```
* Unzip apip.zip
* Run FchParser.jar
```
	$ java -jar FchParser.jar
```
* Waiting for all blocks being parsed. Then you can search below indices in ElasticSearch:
		
		- block
		- block_has			//Which transactions are in a block
		- tx
		- tx_has			//Which cashes are in a transaction
		- cash
		- opreturn
		- address
		- p2sh				//p2sh address information
		- block_mark		//Parsing marks to locate blocks in the blk*.dat files 

## 7. Parse FEIP protocol data from opreturn0.byte

* Copy FeipParser.jar to the directory where FchParser.jar is
```
	$ scp FeipParser.jar armx@<server-ip>:/home/armx/parsers/
```
* Run FchParser.jar
```
	$ java -jar FeipParser.jar
```
* Waiting for all OpReturn data being parsed. Then you can search below indices in ElasticSearch:
	
		- cid
		- cid_history
		- reputation_history

		- protocol
		- code
		- service
		- app
		- protocol_history
		- code_history
		- service_history
		- app_history

		- contact
		- mail
		- secret
		- box
		- box_history

		- group
		- team
		- group_history
		- team_history

		- statement
		- proof
		- proof_history

		- parse_mark	//FEIP parsing marks
		- order			//The orders for your APIP service

## Run ApipManager

* Run ApipManager
```
	$ java -jar ApipManager.jar
```
*  Run ApipManager.jar to initial some important parameters.
- Log in ES. The password of ES would have been encrypted and saved in redis.
- Manage your service information by Menu 1 Manage Service
  -     1 Manage Service
  -     2 Publish New Service: get the publishing json str and write it in blockchain with a TX sent by your fch address.
  -     3 Find service: After you published the service and wait a few minutes, find your service by the address, then save it into redis for you.
  -     4 Show service: Show the detail of your service with json.
- Set the nPrice of your APIs by Menu 3 List APIs and Set nPrice
- Set the windowTime by Menu Set windowTime
- Switch on/off freeGet API service by Menu 7 Switch free get APIs
- As long as parameters are set, it's not necessary to keep ApipManager running.
* If you changed the `params` of the service on blockchain, you need to restart ApipManager to reload your service in Manage Service entry.

## 8. Deploy tomcat and APIP.war
* Install Tomcat

* Download and unzip tomcat
```
	$ curl -O --insecure https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.76/bin/apache-tomcat-9.0.76.tar.gz
	$ tar -xzvf apache-tomcat-9.0.76.tar.gz
```

* Copy APIP.war to: apache-tomcat-9.0.76/webapps/

* Start tomcat

```
	$ sudo ./startup.sh
```
- If failed try:
```
	$ chmod +x startup.sh
```
May be, there are more files to be done with "chmod".

## 8. Run ApipServer

* Try your APIP server from a web explorer with URL: 
```
		http://<your doname or ip>:8080/APIP/freeGet/v1/getTotals
```
* Set SSL/TSL for tomcat if you want customers requesting with HTTPS.
		- Generate your keypair for tomcat

```
	$ $JAVA_HOME/bin/keytool -genkey -alias tomcat -keyalg RSA
```
A password will be required. Remember it.
		- Locate the server.xml file in your Tomcat `conf` directory.
		- Look for a <Connector> element with a port attribute, typically set to 8080 for HTTP.
		- Create a new <Connector> element for SSL/TLS just below the existing <Connector>. Update the attributes as follows:
```
		<Connector port="443" protocol="HTTP/1.1"
		   SSLEnabled="true"
		   keystoreFile="/home/<yourhome>/.keystore"
		   keystorePass="the password you set above"
		   ... other attributes ...
		/>
```

   - Shutdown tomcat and startup it again. Now you should be able to request your sevice with:
```
		https://<your doname or ip>/APIP/freeGet/v1/getTotals
```

## 9. Free GET APIs

[1. Get cid or fid information](https://<your domain name or IP>/APIP/freeGet/v1/getFidCid?id=FEk41Kqjar45fLDriztUDTUkdki7mmcjWK)

[2. Get avatar of a fid](https://<your domain name or IP>/APIP/freeGet/v1/getAvatar?fid=FEk41Kqjar45fLDriztUDTUkdki7mmcjWK)

[3. Get cashes(UTXOs) of a fid](https://<your domain name or IP>/APIP/freeGet/v1/getCashes?fid=FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX)

[4. Get service list](https://<your domain name or IP>/APIP/freeGet/v1/getServices)

[5. Get App list](https://<your domain name or IP>/APIP/freeGet/v1/getApps)

[6. Get the count of entries of all indices](https://<your domain name or IP>/APIP/freeGet/v1/getTotals)


## 10. The default ports and allowed IPs:

* freecash node : 8333 for 0.0.0.0, 8332 for 127.0.0.1
* ElasticSearch: 9200 for 127.0.0.1
* Tomcat: 8080 for 0.0.0.0
* Redis : 6379 for 127.0.0.1

## 11. POST SignIn list

* OpenAPI
	- APIP0V1_OpenAPI
		1. signIn		// signIn to get sessionKey.
		2. general		// for all fcdsl queries on a given ES index.
		3. totals       // the totals of all indices in ES
* Blockchain
	- APIP2V1_Blockchain
        1. blockByIds
        2. blockSearch
        3. cashByIds
        4. cashSearch
        5. TxHasByIds
        6. cashValid
        7. txByIds
        8. txSearch
        9. blockHasByIds
        10. opReturnByIds
        11. opReturnSearch
        12. addressByIds
        13. addressSearch
        14. p2shByIds
        15. p2shSearch
* CidInfo
	- APIP3V1_CidInfo
        1. cidInfoByIds
        2. cidByIds
        3. fidCidSeek
        4. cidInfoSearch
        5. cidHistory
        6. homepageHistory
        7. noticeFeeHistory
        8. reputationHistory
        9. nobodys
*  Construct
	- APIP4V1_Protocol
        1. protocolByIds
        2. protocolSearch
        3. protocolOpHistory
        4. protocolRateHistory
	- APIP5V1_Code
        1. codeByIds
        2. codeSearch
        3. codeOpHistory
        4. codeRateHistory
	- APIP6V1_Service
        1. serviceByIds
        2. serviceSearch
        3. serviceOpHistory
        4. serviceRateHistory
	- APIP7V1_App
        1. appByIds
        2. appSearch
        3. appOpHistory
        4. appRateHistory
* Organization
	- APIP8V1_Group
        1. groupByIds
        2. groupSearch
        3. groupOpHistory
        4. groupMembers
        5. groupExMembers
        6. myGroups
	- APIP9V1_Team
        1. teamByIds
        2. teamSearch
        3. teamOpHistory
        4. teamMembers
        5. teamExMembers
        6. teamOtherPersons
        7. myTeams
        8. teamRateHistory
* Personal
	- APIP10V1_Box
        1. boxByIds
        2. boxSearch
        3. boxHistory
	- APIP11V1_Contact
        1. contacts
        2. contactsDeleted
	- APIP12V1_Secret
        1. secrets
        2. secretsDeleted
	- APIP13V1_Mail
        1. mails
        2. mailsDeleted
        3. mailThread
* Public
	- APIP14V1_Proof
        1. proofByIds
        2. proofSearch
        3. proofHistory
	- APIP15V1_Statement
        1. statements
        2. statementSearch
	- APIP17V1_Avatar
        1. avatars
	- APIP18V1_Wallet
        1. unconfirmed
        2. cashValidLive
        3. cashValidForCd
        4. cashValidForPay
        5. decodeRawTx
        6. broadcastTx

## 11. Indices in ES
	
* FCH
	1. block
	2. block_has
	3. tx
	4. tx_has
	5. cash
	6. opreturn
	7. address
	8. p2sh
	9. block_mark
* FEIP
    1. cid
    2. cid_history
    3. reputation_history
    4. protocol
    5. code
    6. service
    7. app
    8. protocol_history
    9. code_history
    10. service_history
    11. app_history
    12. contact
    13. mail
    14. secret
    15. box
    16. box_history
    17. group
    18. team
    19. group_history
    20. team_history
    21. statement
    22. proof
    23. proof_history
    24. parse_mark
* APIP
    1. order