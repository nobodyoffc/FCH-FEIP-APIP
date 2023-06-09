# FreeChain Introduction

## What's FreeChain

FreeChain is a database of Freecash blockchain information.

Besides general blockchain information, some important special information is also reached from FreeChain (see [Features](features)).

The data of FreeChain is parsed from Freecash blockchain data files begining with blk00000.dat, and keeping up to date.

This database is built on ElasticSearch.

The project of FreeChain is wrote with Java.

## Features

1. Take `cash` as the minimum `entity`. It is the output of tx with ID of double sha256 hash value of txid and the index of the output in the tx.
2. A new `cash` born in a tx will be marked as `valid cash`. It was also called `UTXO`(Unspent transaction output) technically. When a cash has been spent, we call it `spent cash`.
3. full-text index of `OpReturn` content.
4. CoinDays(`cd`) for UTXO (Unspent Transaction Output) and address.
5. CoinDays Destroyed (`cdd`) for STXO (Spent Transaction Output), address, tx, block, and OpReturn.
6. `Guide` for address. Guide is the address who bring the new address to freecash world.
7. `BirthHeight` for address, utxo, stxo.
8. `BTC, ETH, LTC, DOGE, TRX` address from the same public key of freecash address.

## Install

If you only want to query information of FreeChain, you need't install FreeChain. You could find a service provider, get an account, and query as showed in [Query Freecash Blockchain](query-Freecash-Blockchain) .

If you want run FreeChain to establish your own database service, you need install it as following:

1. System requirement
* Memory: 6G at lest.
2. Install freecash full node and synchronize all blocks
* Download freecash full node from: https://github.com/freecashorg/freecash
* The default path of block files
	- MacOs: ~/Library/Application Support/freecash/blocks
	- Windows: 
	- docker: ~/fc_data/blocks
* Deal with the conflict of freecash node of Docker, ES and FreeChain
	- Problem: ES can't be install by user root. But the block file of freecash docker node will be belong to root as default. In order to operate ES, FreeChain should run by a new user instead root. You have to create the freecash block files with this new user. To deal with this, you can do as [Install with docker freecash node](install-with-docker-freecash-node)
3. Install Java
4. Install ElasticSearch and run it
	To run a HTTPS ES java client, you need import certification of ES to java as bellow:

```
keytool -importcert -file  [your ES path]/config/certs/http_ca.crt  -keystore [java home path]/lib/security/cacerts -storepass changeit -alias [giving a name whatever]

```

	Without the certification you will meet an error of `javax.net.ssl.SSLHandshakeException: PKIX path building failed:...`
	
5. Run the jar file of FreeChain to parser blocks

## Parse Freecash Blockchain
* config
	To run FreeChain, you need config it at the menu list to input 6.
	If your elasticsearch is in a http mode, you need only input:
	- ip
	- port
	- username(not be used,but input one whatever)
	- block files path (where the blk00000.dat locate in)
	To run FreeChain, you need config it at the menu list to input 6.
	If your elasticsearch is in a http mode, you need only input:
	- ip
	- port
	- username(It's for https ES server. when testing with a http server, just input something whatever.)
	- block files path (where the blk00000.dat locate in)
* restart from interrupt
	If FreeChain interrupted from running, you can restart it. The restarting progress will begin from the height before best height in case some data of last block was damaged in the interruption.
	Some times, it may fail to link to the block chain. You can shut it down and restart again or manual restart from some further height. Generally, a successful restart will link new block to chain within 1 minute.
	If the interruption is due to freecash node shutdown, when you restart freecash node, the last blk file will be reorganized to elimate fork block. So, to recover from such interruption, you need rollback to a height in the blk file before last file. You can find required infomation in "block_mark" index.

## ES Indices
* FreeChain has 7 indices for data service:
	- block 		The metadata of block excluding the TXs.
	- block_has 	Summaries of all the TXs in block.
	- tx			The metadata of TX excluding inputs and outputs.
	- tx_has		Summaries of all inputs and outputs in TX.
	- cash			Details of cash (it's also the input or output of TX).
	- opreturn		Details of OpReturn.
	- address		Details of address.
	There is another index of "block_mark". It's used to parsing block data file. 
* CD field
	- Address and cash has a field named `cd`. It is recalculate every 12 hours. 
## Deal with problems
### Install freecash node with docker
When installing freecash docker node, you should do as below:

1. Create "fc_data" directory with armx if you just installed freecash node and has not run it yet, then skip to 3.
 
2. If you had run freecash with root, copy block dir to armx's home:

```
	$ cp -r /root/fc_data/* /home/armx/fc_data
```
Change the owner to armx

```
	$ chown -R armx:armx /home/armx/fc_data
```
See the result:

```
	$ ls -l /home/armx
	drwxr-xr-x. 3 armx  armx   22 11月  4 12:21 fc_data
```
3. Check and remember the id of armx

```
	$ id armx
	uid=1000(armx) gid=1000(armx) group=100(armx)
```
4. Run docker container

```
	$ docker run -dit --name fc_miner --net=host -v /home/armx/fc_data:/opt/newcoin fc.io:latest /bin/bash
```
	If cointainer has been existed, restart it:
	
```
	$ docker ps -a
```
	Get the id,then:
	
```
	$ docker start [id]
```
	Get into the container:
```
	docker exec -it [id] /bin/bash
```

5. Add the same name of armx within the container

```
	$ useradd armx
```
6. Ensure armx has the same id and password as the armx out container: 

```
	$ vi /etc/group
```
	Find the line where armx locate in, and make sure id is 1000. If not change it.
```
	$ vi /etc/passwd
```
	Find the line where armx locate in, and make sure id is 1000. If not change it.
7. login with armx

```
	$ su armx
```
8. Start freecash node

```
	$ freecashd -listen=1 -server=1 -datadir=/opt/newcoin -logtimemicros -gen=0 -daemon
```
9. Check the blockchain info after a while of the node started 

```
	$ freecash-cli -datadir=/opt/newcoin getblockchaininfo
```
### ResponseException: [HTTP/1.1 429 Too Many Requests]

When rollbacking, you may encount this error. To deal with it, set your ElasticSearch as following.

```
PUT _all/_settings
{
  "index.blocks.read_only_allow_delete": null
}
```
## Data request example

Get the information of a address:

```
https://154.221.22.19:9200/address/_doc/FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv

Response:
{"_index":"address","_id":"FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv","_version":70,"_seq_no":607880,"_primary_term":1,"found":true,"_source":{"id":"FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv","balance":254118369,"income":428331192962,"expend":428077074593,"guide":"FQXH8X7mwzg6K4WAH8CAGwq2c1eWpcUqej","birthHeight":19773,"lastHeight":164474,"cdd":62,"cd":0,"utxo":30}}
```

Get the information of a block:

```
https://154.221.22.19:9200/block/_doc/0000000000001d7f6215b20c3723e43475bdc4464ed9358e65c2f85bd1ee5db7

Response:
{"_index":"block","_id":"0000000000001d7f6215b20c3723e43475bdc4464ed9358e65c2f85bd1ee5db7","_version":1,"_seq_no":1000,"_primary_term":1,"found":true,"_source":{"size":260,"height":1000,"version":"20000000","preId":"000000000005959261b0364cdc4b47a5631c325348a36c6604ff94c398ef4a98","merkleRoot":"ffd3ef54da94500c808aaa433b8cc5606788da83502df1fb6cd5ef30f51da751","time":1577869350,"diffTarget":456812732,"nonce":1258916810,"txCount":1,"id":"0000000000001d7f6215b20c3723e43475bdc4464ed9358e65c2f85bd1ee5db7","inValueT":0,"outValueT":5000000000,"fee":0,"cdd":0}}
```

by No1_NrC7
