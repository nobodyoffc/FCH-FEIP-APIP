# FEIP5V1_Service(en-US)

## Contents

[Consensuses of FEIP](#consensuses-of-feip)

[Consensuses of this protocol](#consensuses-of-this-protocol)

[Publish](#publish)

[Update](#update)

[Stop](#stop)

[Recover](#recover)

[Close](#close)

[Rate](#rate)

[QR Code](#qr-Code)

```
ProtocolType: FEIP
SerialNumber: 2
ProtocolName: Service
Version: 1
Description : Publish and manage service information on Freecash blockchain. 
Author: C_armX, Deisler-JJ_Sboy
Language: en-US
CreateDate: 2022-12-28
UpdateDate: 2023-05-07
```

## Consensuses of FEIP

1. FEIP type protocols write data into freecash blockchain with OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation writing FEIP data with OP_RETURN needs more than `1cd` consumed.

7. More consensuses of FEIP can be found in `FEIP1_Protocol`.

## Consensuses of this protocol

1. This protocol is used to publish program service information on Freecash blockchain. 

2. SID（Service Identity）: The txid when publishing the service is the identity of the service。

3. The publisher can name the service freely. According to `FIPA1V1_Entity`, the FCN(Free Cousensus Name) of the service is `<name>+"@"+"<cid of the publisher>`, e.g., `FCH-Parser@No1_NrC7`.

4. The publisher should ensure its different services having different names.

5. One can only update, stop, or recover its own published services.

6. The publisher can't rate its own services.

7. Stopped or closed services still can be rated.

8. Owner or Owner's master (see FEIP6_Master) can [Close](#close) the service and giving a `closeStatement`.

9. A closed service can never be operated again but be rated.

10. Operations of this protocol write OP_RETURN with the same content structure:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 5|Y|
|ver|int|Version. Fixed: 1|Y|
|name|string|Fixed: "Service"|N|
|pid|string|PID of this protocol. |N|
|did|string|DID of this protocol file. |N|
|data|object|Operation data which is different in different operations. |Y|

## Publish

The publisher sends a tx with the data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|Operation: "publish"|Y|
|stdName|string|the name of the service in English|Y|
|localName|string array|service names in different languages|N|
|desc|string|Description of this service|N|
|types|string array|The types of the service|N|
|urls|string array|URLs describing the service|N|
|waiters|Stringarray|FCH addresses of the waiters to serve users of this protocol.|N|
|protocols|string array|The protocols followed by this service|N|
|params|object|Parameters customized by the service provider.|N|

*  Example of publishing a service

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
		"waiters": ["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"],
		"protocols": ["b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553", "37406e3e45750efccdb060ca2e748f9f026aebb7dadade8e8747340f380edaca"],
		"params": {
			"urlHead": "http://sign.cash/api/",
			"currency": "fch",
			"account": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
			"pricePerRequest": "0.001",
			"minPayment": "10",
			"sessionDays":"365"
		}
	}
}
```

The txid is : 28e85989ce7e3bba56c8179d6dd9b180b23ff48a4ad031c72539d02750659212. It's also the sid of the published service。


## Update

The publisher of a service updates the service information. All fields will be replaced together.

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "update"|Y|
|sid|hex|Txid when publishing the service|Y|
|stdName|string|the name of the service in English|Y|
|localName|string array|service names in different languages|N|
|desc|string|Description of this service|N|
|types|string array|The types of the service|N|
|urls|string array|URLs describing the service|N|
|waiters|Stringarray|FCH addresses of the waiters to serve users of this protocol.|N|
|protocols|string array|The protocols followed by this service|N|
|params|object|Parameters customized by the service provider.|N|

*  Example of updating a service

```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 2,
    "name": "Service",
    "data":{
		"op": "update",
        "sid": "9f1828e227e0a146ff87bc724171f1528c456d151f56811d0e622bb310f08df9",
        "stdName": "freedrive#1",
		"localName": ["飞盘#1","フリスビー#1"],
        "desc": "Free and open cloud storage service",
		"types": ["cloud storage","infrastructure"],
		"urls": ["https://freedrive.com"],
		"waiters": [],
		"protocols": ["b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553", "37406e3e45750efccdb060ca2e748f9f026aebb7dadade8e8747340f380edaca"],
		"params": {
			"urlHead": "http://sign.cash/api/",
			"currency": "fch",
			"account": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
			"pricePerKBytes": "0.001",
			"minPayment": "1",
            "sessionDays": "365"
		}
    }
}
```

## Stop

The owner stops maintaining a service as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|Operation: "stop"|Y|
|sid|hex|Txid when the service was published|Y|


*  Example of stoping a service

```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 1,
    "name": "Service",
    "data":{
		"op":"stop"
        "sid": "9f1828e227e0a146ff87bc724171f1528c456d151f56811d0e622bb310f08df9"
    }
}
```
## Recover

The owner recovers a Stopped service as following:

|field|type|content|required|
|:----|:----|:----|:----|
|sid|hex|Txid when the service was published|Y|
|op|string|Operation: "recover"|Y|

*  Example of stoping a service

```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 1,
    "name": "Service",
    "data":{
		"op":"recover",
        "sid": "9f1828e227e0a146ff87bc724171f1528c456d151f56811d0e622bb310f08df9"
    }
}
```

## Close

The owner or its master close a service permanently, the OP_RETURN of which contains the data as following:

|field|type|content|required|
|:----|:----|:----|:----|
|sid|hex|Txid when the service was published|Y|
|op|string|Operation: "close"|Y|

* Example of closing a service:

```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 1,
    "name": "Service",
    "data":{
		"op":"close",
        "sid": "9f1828e227e0a146ff87bc724171f1528c456d151f56811d0e622bb310f08df9"
    }
}
```

## Rate

Anyone but the owner rate a published service as following:


|field|type|content|required|
|:----|:----|:----|:----|
|sid|hex|Txid when the service was published|Y|
|op|string|operation: "rate"|Y|
|rate|int|Score of rating from 0 to 5|N|

*  Example of rate a service

```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 1,
    "name": "Service",
    "data":{
		"op": "rate",
        "sid": "a4432217ea6b12e4b8095196cf9fe257a0db8e70b4a9acb4b1955d506f8cd1bb",
        "rate": 5
    }
}
```

## QR Code

The QR Code of a published service has fields as following:

```
{
    "meta":"FC",
    "type": "FEIP",
    "sn": 5,
    "ver": 1,
    "data":{
        "sid": "a4432217ea6b12e4b8095196cf9fe257a0db8e70b4a9acb4b1955d506f8cd1bb"
    }
}
```
