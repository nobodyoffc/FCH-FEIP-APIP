# FEIP15V1_APP(en-US)

## Contents

[Consensuses of FEIP](#consensuses-of-feip)

[Consensuses of this protocol](#consensuses-of-this-protocol)

[Publish](#publish)

[Update](#update)

[Stop](#stop)

[Recover](#recover)

[Close](#close)

[Rate](#rate)

[QR code](#qr-code)

## Summary

```
ProtocolType: FEIP
SerialNumber: 15
ProtocolName:  APP
Version: 1
Description :  Publish and manage APPs on freecash blockchain.
Authors: C_armX
Language: en-US
CreateDate: 2021-11-01
UpdateDate: 2023-01-10
```

## General consensus of FEIP

1. FEIP type protocols write data into freecash blockchain with OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation writing FEIP data with OP_RETURN needs more than `1cd` consumed.

7. More consensuses of FEIP can be found in `FEIP1_Protocol`.

## Consensus of this protocol

1. This protocol is used to publish and manage APPs on freecash blockchain.

2. AID（Application Identity）: The txid of the transaction in which the APP was published.

3. The publisher can name the APP freely. 

4. According to `FIPA1V1_Entity`, the FCN(Free Cousensus Name) of the APP is `<name>+"@"+<cid of the publisher>`, e.g., `FreeSign@No1_NrC7`.

5. The publisher should ensure its different APPs having different stdName.

6. One can only update, stop, or recover its own published APP.

7. The publisher can't rate its own APP.

8. Stopped or closed APPs still can be rated.

9. Owner or Owner's master (see FEIP6_Master) can [Close](#close) the APP and giving a `closeStatement`.

10. A closed APP can never be operated again.

11. Operations of this protocol write OP_RETURN with the same content structure:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 15|Y|
|ver|int|Version. Fixed: 1|Y|
|name|string|Fixed: "APP"|N|
|pid|string|PID of this protocol. |N|
|did|string|DID of this protocol file. |N|
|data|object|Operation data which is different in different operations. |Y|


## Publish

The publisher send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|Operation: "publish"|Y|
|stdName|string|the name of the App in English|Y|
|localName|string array|App names in different languages|N|
|desc|string|Description of this App|N|
|types|string array|The types of the App|N|
|urls|string array|URLs describing the App|N|
|downloads|object array|Download information: os,link,hash. see below.|N|
|waiters|Stringarray|FCH addresses of the waiters to serve users of this protocol.|N|
|protocols|string array|The protocols followed by this App|N|
|services|string array|The SIDs of the services used by this APP|N|
|codes|string array|The COIDs of the codes used by this APP|N|


data.downloads:
|field|type|content|required|
|:----|:----|:----|:----|
|os|String|The name of OS|N|
|link|String|The link to download installing file|N|
|did|String|Double SHA256 hash value of the installing file|N|


* Example of creating an APP

```
{
    "type": "FEIP",
    "sn": 15,
    "ver": 1,
    "name": "APP",
    "data":{
        "op":"publish",
        "stdName": "Signer",
        "localNames": ["飞签","フライング宝くじ"],
        "desc": "Save the private key offline and provide offline signature，and provide other functions.",
		"types":["signer"],
        "urls": ["https://sign.cash"],
        "downloads":[{
			"os":"android",
			"link":"https://sign.cash/download/cryptosigner",
			"did":"2d45fda951ed5c6d621a38266e327ed64e6e582e83ddb8dba3f243caeecdaa8e"
		}
		],
		"waiters": ["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"],
        "protocols":["b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553","37406e3e45750efccdb060ca2e748f9f026aebb7dadade8e8747340f380edaca"],
        "services":["c86e039f466434862585e38c0fd1a11f47dcc07839647a452424503b30f81b39","403d3146bdd1edbd8d71b01ffbad75972e07617971acb767a9bae150d4154dc25"],
        "codes":[""]
	}
}
```
The txid is "f9286689fa3c46ff3463b7b7e482c4e2ea3f23ca8634276eb4753512ff799873". It's AID of this APP。

## update

The publisher of an APP can update the APP information. All fields will be replaced together.

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|Operation: "update"|Y|
|stdName|string|the name of the App in English|Y|
|localName|string array|App names in different languages|N|
|desc|string|Description of this App|N|
|types|string array|The types of the App|N|
|urls|string array|URLs describing the App|N|
|downloads|object array|Download information: os,link,hash. see below.|N|
|waiters|Stringarray|FCH addresses of the waiters to serve users of this protocol.|N|
|protocols|string array|The protocols followed by this App|N|
|services|string array|The SIDs of the services used by this APP|N|
|codes|string array|The COIDs of the codes used by this APP|N|


data.downloads:
|field|type|content|required|
|:----|:----|:----|:----|
|os|String|The name of OS|N|
|link|String|The link to download installing file|N|
|did|String|Double SHA256 hash value of the installing file|N|

* Example of updating an APP

```
{
    "type": "FEIP",
    "sn": 15,
    "ver": 1,
    "name": "APP",
    "data":{
        "aid": "f9286689fa3c46ff3463b7b7e482c4e2ea3f23ca8634276eb4753512ff799873",
        "op":"update",
        "stdName": "Crypto Signer",
        "localNames": ["密签","秘密のサイン"],
		"types":["construct","signer"],
        "desc": "Save the private key offline and provide offline signature，and provide other functions.",
        "urls": ["https://sign.cash"],
		"waiters": [],
        "downloads":[{
			"os":"android",
			"link":"https://sign.cash/download/cryptosigner",
			"hash":"2d45fda951ed5c6d621a38266e327ed64e6e582e83ddb8dba3f243caeecdaa8e"
		}
		],
        "protocols":["b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553","37406e3e45750efccdb060ca2e748f9f026aebb7dadade8e8747340f380edaca"],
        "services":["c86e039f466434862585e38c0fd1a11f47dcc07839647a452424503b30f81b39","403d3146bdd1edbd8d71b01ffbad75972e07617971acb767a9bae150d4154dc25"],
		"codes":[]
	}
}
```

## Stop

The owner can stop maintaining an APP as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|Operation: "stop"|Y|
|codeId|hex|Txid when the app was published|Y|


* Example of stoping an APP

```
{
    "type": "FEIP",
    "sn": 15,
    "ver": 1,
    "name": "APP",
    "data":{
		"op":"stop"
        "aid": "f9286689fa3c46ff3463b7b7e482c4e2ea3f23ca8634276eb4753512ff799873"
    }
}
```
## Recover

The owner can recover a Stopped APP as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|Operation: "recover"|Y|
|codeId|hex|Txid when the app was published|Y|

* Example of stoping an APP

```
{
    "type": "FEIP",
    "sn": 15,
    "ver": 1,
    "name": "APP",
    "data":{
		"op":"recover",
        "aid": "f9286689fa3c46ff3463b7b7e482c4e2ea3f23ca8634276eb4753512ff799873"
    }
}
```

## Close

The owner or its master can close a Stopped APP as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|Operation: "close"|Y|
|codeId|hex|Txid when the code was published|Y|

### Example of stoping an APP

```
{
    "type": "FEIP",
    "sn": 15,
    "ver": 1,
    "name": "APP",
    "data":{
		"op":"close",
        "aid": "f9286689fa3c46ff3463b7b7e482c4e2ea3f23ca8634276eb4753512ff799873"
    }
}
```

## Rate

Anyone but the owner can rate a published APP.


|field|type|content|required|
|:----|:----|:----|:----|
|codeId|hex|Txid when the code was published|Y|
|op|string|operation: "rate"|Y|
|rate|int|Score of rating from 0 to 5|N|

### Example of rate an APP
```
{
    "type": "FEIP",
    "sn": 15,
    "ver": 1,
    "name": "APP",
    "data":{
		"op": "rate",
        "aid": "f9286689fa3c46ff3463b7b7e482c4e2ea3f23ca8634276eb4753512ff799873"
        "rate": 4
    }
}
```

## QR code

The QR code of a published APP has fields as following:

```
{
    "meta":"FC",
    "type": "FEIP",
    "sn": 15,
    "ver": 1,
    "data":{
        "aid": "f9286689fa3c46ff3463b7b7e482c4e2ea3f23ca8634276eb4753512ff799873"
    }
}
```