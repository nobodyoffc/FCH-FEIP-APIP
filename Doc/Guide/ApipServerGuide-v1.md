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
7. FcTools: [https://cid.cash/download/FcTools.jar](https://cid.cash/download/FcTools.jar)

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

* Install docker
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
	$ sudo docker run -dit --name fc_miner --net=host -v /home/newuser/fc_data:/opt/newcoin fc.io:latest /bin/bash
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
- Check if you has been in the container. If not, get into the container:
```
	$ sudo docker exec -it cba65abb2e53 /bin/bash
```
- Stop the container if you want to try again.
```
	$ sudo docker stop cba65abb2e53
```
* Add the same name of newuser within the container
```
	$ useradd newuser
```
* Make sure the user newuser has the same id in `/etc/group` and `/etc/passwd` inside and outside the container. If not, change it:
```
	$ vi /etc/group
	$ vi /etc/passwd
```
* login with newuser
```
	$ su newuser
```
* Start freecash node
```
	$ freecashd -listen=1 -server=1 -datadir=/opt/newcoin -logtimemicros -gen=0 -daemon
	Freecash server starting
```
- "-server=1" makes RPC service started.
- "-gen=0" closed mining.
- To start RPC service, it's necessery to set parameters in ~/fc_data/freecash.conf as below :
```
	server=1
	rpcuser=user
	rpcpassword=password
	rpcallowip=127.0.0.1
```
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
* Wait for your node catching up the newest block.

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
	$ keytool -importcert -file  /home/newuser/es/config/certs/http_ca.crt  -keystore /usr/lib/jvm/java-19-openjdk-amd64/lib/security/cacerts -storepass changeit -alias es
```
## 5. Parse Freecash blockchain
* Copy FchParser.jar to you server:
```
	$ scp FchParser.jar newuser@<server-ip>:/home/newuser/parsers/
```
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

## 6. Parse FEIP protocol data from opreturn0.byte

* Copy FeipParser.jar to the directory where FchParser.jar is
```
	$ scp FeipParser.jar newuser@<server-ip>:/home/newuser/parsers/
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

## 7. Run Redis and ApipManager

* Check if Homebrew exists
```
	$ brew --version
```
* If not, install Homebrew and do what the installer suggested
```
	$ /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```
* Install redis
```
	$ brew install redis
```
* Start redis
```
	$ brew services start redis
```
Or, if you don't want/need a background service you can just run:
```
	$  /home/linuxbrew/.linuxbrew/opt/redis/bin/redis-server /home/linuxbrew/.linuxbrew/etc/redis.conf
```
* Copy ApipManager.jar to the directory where FchParser.jar is in
```
	$ scp ApipManager.jar newuser@<server-ip>:/home/newuser/parsers/
```
* Run ApipManager
```
	$ java -jar ApipManager.jar
```
* List APIs and set nPrices in the first menu
* Manage your service
    - Menu
    - 1 Manage Service
    - 1 Publish New Service: get the publishing json str and write it in blockchain with a TX sent by your fch address.
    - 2 Find service: After you published the service and wait a few minutes, find your service by the address, then save it into redis for you.
    - 3 Show service: Show the detail of your service with json.
* As the nPrice for all APIs and the service were set, ApipManager can be shutdown. 
* If you changed the `params` of the service on blockchain, you need to run ApipManager to reload your service in Manage Service entry.

## 8. Run ApipServer
	
* Install Tomcat

* Download and unzip tomcat
```
	$ curl -O --insecure https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.76/bin/apache-tomcat-9.0.76.tar.gz
	$ tar -xzvf apache-tomcat-9.0.76-deployer.tar.gz
```
* Copy APIP.war to tomcat/webapps/
```
	$ scp APIP.jar newuser@<server-ip>:/home/newuser/tomcat/webapps/
```

* Start tomcat

		- Before start tomcat, you have to run ApipManager.jar to initial some important parameters, such as the password of ElasticSearch client. It's not necessary to keep it running.
```
	$ sudo ./startup.sh
```
- If failed try:
```
	$ chmod +x startup.sh
```
May be, there are more files to be done with "chmod".

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

## 10. POST API list

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