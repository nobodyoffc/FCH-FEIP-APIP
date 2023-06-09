# FEIP5V2_Service(en-US)

## Contents

[Summary](#summary)

[General consensus of FEIP](#general-consensus-of-feip)

[consensus of this protocol](#consensus-of-this-protocol)

[Publish a service](#publish)

[Stop](#stop)

[Recover](#recover)

[Update](#update)

[Close](#close)

[Rate](#rate)

[QR code](#qr-code)


## Summary

```
ProtocolType: FEIP
SerialNumber: 5
ProtocolName: Service
Version: 2
Description :  Publish and manage services on freecash blockchain.
Author: C_armX, Deisler-JJ_Sboy
Language: en-US
CreateDate: 2021-11-01
UpdateDate: 2023-04-09
```

## General consensus of FEIP

1. FEIP type protocols write data of consensus in OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs: ‘ALL’ (value 0x01).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation of writing to freecash blockchain needs more than `1cd` consumed.

## Consensus of this protocol

1. This protocol is used to publish and manage services on freecash blockchain.

2. SID（Service Identity）: The txid of the transaction in which the service was published.

3. The publisher can name the service freely. We can take "[stdName]"+"@"+"[cid/address of the publisher]" to refer to the service, such as "FreeDrive@Free_cash".

4. The publisher should ensure its different services having different stdName.

5. One can only update, stop, or recover its own published service.

6. The publisher can't rate its own service.

7. Stopped or closed services still can be rated.

8. Owner or Owner's master (see FEIP6_Master) can [Close](#close) the service and giving a `closeStatement`.

9. A closed service could never be operated again.

## Publish

To publish protocol, one can send a tx with the content of op_return as following:

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number, Fixed: 5|Y|
|3|ver|int|Fixed: 2|Y|
|4|name|String|Fixed: "Service"|N|
|5|pid|string|The PID of this protocol|N|
|6|did|string|The DID of this protocol file|N|
|7|data.op|string|operation: "publish"|Y|
|8|data.stdName|string|the name of the service in English|Y|
|9|data.localName|string array|service names in different languages|N|
|10|data.desc|string|Description of this service|N|
|11|data.types|string array|The types of the service|N|
|12|data.urls|string array|URLs describing the service|N|
|13|data.pubKeyAdmin|hex|The public key of the FCH identity designated by the publisher for this service|N|
|14|data.protocols|string array|The protocols followed by this service|N|
|15|data.params|structure|Parameters customized by the service provider.|N|

* Example of publishing a service

```
{
	"type": "FEIP",
	"sn": 5,
	"ver": 2,
	"name": "Service",
	"data": {
		"op": "publish",
		"stdName": "freedrive",
		"localName": ["飞盘", "フリスビー"],
		"desc": "Free and open cloud storage service",
		"types": ["cloud storage", "infrastructure"],
		"urls": ["https://freedrive.com"],
		"pubKeyAdmin": "02966dc682850550b1df046f2a03cfe546c4e4cf83f739d1497f6c292fabdad1b4",
		"protocols": ["b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553", "37406e3e45750efccdb060ca2e748f9f026aebb7dadade8e8747340f380edaca"],
		"params": {
			"urlHead": "http://sign.cash/api/",
			"currency": "fch",
			"account": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
			"pricePerRequest": "0.001",
			"minPayment": "10"
		}
	}
}
```

## Stop

The publisher of the protocol sends a tx with the content of op_return as following:

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number, Fixed: 28|Y|
|3|ver|int|Fixed: 2|Y|
|4|name|String|Fixed: "Service"|N|
|5|pid|string|The PID of this protocol|N|
|6|did|string|The DID of this protocol file|N|
|7|data.sid|hex|Txid when publishing the service|Y|
|3|data.op|string|operation: "stop"|Y|

* Example of stopping a service

```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 2,
    "name": "Service",
    "data":{
        "sid": "a246f36574f039460063f626f023c2e13a4d184c89279795539be220e9402643",
        "op": "stop"
    }
}
```

## Recover

The publisher of the protocol send a tx with the content of op_return as following:

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number, Fixed: 28|Y|
|3|ver|int|Fixed: 2|Y|
|4|name|String|Fixed: "Service"|N|
|5|pid|string|The PID of this protocol|N|
|6|did|string|The DID of this protocol file|N|
|7|data.sid|hex|Txid when publishing the service|Y|
|8|data.op|string|operation: "recover"|Y|

* Example of recovering a Service

```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 2,
    "name": "Service",
    "data":{
        "sid": "a246f36574f039460063f626f023c2e13a4d184c89279795539be220e9402643",
        "op": "recover"
    }
}
```

## Update

The publisher of the protocol sends a tx with the content of op_return as following:

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number, Fixed: 5|Y|
|3|ver|int|Fixed: 2|Y|
|4|name|String|Fixed: "Service"|N|
|5|pid|string|The PID of this protocol|N|
|6|did|string|The DID of this protocol file|N|
|7|data.sid|hex|Txid when publishing the service|Y|
|8|data.op|string|operation: "update"|Y|
|9|data.stdName|string|the name of the service in English|Y|
|10|data.localName|string array|service names in different languages|N|
|11|data.desc|string|Description of this service|N|
|12|data.types|string array|The types of the service|N|
|13|data.urls|string array|URLs describing the service|N|
|14|data.pubKeyAdmin|hex|The public key of the FCH identity designated by the publisher for this service|N|
|15|data.protocols|string array|The protocols followed by this service|N|
|16|data.params|structure|Parameters customized by the service provider.|N|

* Example of updating a Service

```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 2,
    "name": "Service",
    "data":{
        "sid": "a246f36574f039460063f626f023c2e13a4d184c89279795539be220e9402643",
        "op": "update",
        "stdName": "freedrive#1",
		"localName": ["飞盘#1","フリスビー#1"],
                "desc": "Free and open cloud storage service",
		"types": ["cloud storage","infrastructure"],
		"urls": ["https://freedrive.com"],
		"pubKeyAdmin": "02966dc682850550b1df046f2a03cfe546c4e4cf83f739d1497f6c292fabdad1b4",
		"protocols": ["b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553", "37406e3e45750efccdb060ca2e748f9f026aebb7dadade8e8747340f380edaca"],
		"params": {
			"urlHead": "http://sign.cash/api/",
			"currency": "fch",
			"account": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
			"pricePerRequest": "0.001",
			"minPayment": "1",
            "sessionDays": "365"
		}
    }
}
```

## Close

The publisher or its master can close the service by sending a tx with the content of op_return as following:

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number, Fixed: 28|Y|
|3|ver|int|Fixed: 2|Y|
|4|name|String|Fixed: "Service"|N|
|5|pid|string|The PID of this protocol|N|
|6|did|string|The DID of this protocol file|N|
|7|data.sid|hex|Txid when publishing the service|Y|
|8|data.op|string|operation: "close"|Y|

* Example of recovering a Service

```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 2,
    "name": "Service",
    "data":{
        "sid": "a246f36574f039460063f626f023c2e13a4d184c89279795539be220e9402643",
        "op": "close"
    }
}
```

## Rate
Anyone but the publisher of the protocol sends a tx with the content of op_return as following:

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number, Fixed: 28|Y|
|3|ver|int|Fixed: 2|Y|
|4|name|String|Fixed: "Service"|N|
|5|pid|string|The PID of this protocol|N|
|6|did|string|The DID of this protocol file|N|
|7|data.sid|hex|Txid when publishing the service|Y|
|8|data.op|string|operation: "rate"|Y|
|9|data.rate|int|Score of rating from 0 to 5|Y|

* Example of Rating a Service

```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 2,
    "name": "Service",
    "data":{
        "sid": "a246f36574f039460063f626f023c2e13a4d184c89279795539be220e9402643",
        "op": "rate",
        "rate": 4
    }
}
```

## QR code

The QR code of a published service has fields as following:

```
{
    "meta": "FC",
    "type": "FEIP",
    "sn": 5,
    "ver": 2,
    "data":{
        "sid":"a246f36574f039460063f626f023c2e13a4d184c89279795539be220e9402643"
    }
}
```
