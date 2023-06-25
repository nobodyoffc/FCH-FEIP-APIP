```
ProtocolType: FEIP
SerialNumber: 2
ProtocolName: Code
Version: 1
Description : Publish and manage code information on Freecash blockchain. 
Authors: C_armX, Deisler-JJ_Sboy
Language: en-US
CreateDate: 2022-12-28
UpdateDate: 2023-05-07
```

# FEIP2V1_Code(en-US)

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


## Consensuses of FEIP

1. FEIP type protocols write data into freecash blockchain with OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation writing FEIP data with OP_RETURN needs more than `1cd` consumed.

7. More consensuses of FEIP can be found in `FEIP1_Protocol`.

## Consensuses of this protocol

1. This protocol is used to publish program code information on Freecash blockchain. 

2. CodeID（Code Identity）: The txid when publishing the code is the identity of the code。

3. The publisher can name the code freely. According to `FIPA1V1_Entity`, the FCN(Free Cousensus Name) of the code is `<name>+"@"+<cid of the publisher>`, e.g., `FCH-Parser@No1_NrC7`.

4. The publisher should ensure its different codes having different names.

5. One can only update, stop, or recover its own published codes.

6. The publisher can't rate its own codes.

7. Stopped or closed codes still can be rated.

8. Owner or Owner's master (see FEIP6_Master) can [Close](#close) the code and giving a `closeStatement`.

9. A closed code can never be operated again.

10. Operations of this protocol write OP_RETURN with the same content structure:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 2|Y|
|ver|int|Version. Fixed: 1|Y|
|name|string|Fixed: "Code"|N|
|pid|string|PID of this protocol. |N|
|did|string|DID of this protocol file. |N|
|data|object|Operation data which is different in different operations. |Y|

## Publish

The publisher sends a tx with the data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|Operation: "publish"|Y|
|name|string|The name of the code in english|Y|
|ver|string|The version of the code in english|Y|
|did|string|The sha256x2 hash of this version code file|Y|
|desc|string|Description of this code|N|
|langs|string array|The program languages of the code|N|
|waiters|Stringarray|FCH addresses of the waiters to serve users of this protocol.|N|
|urls|string array|URLs，the locations to get the code|N|
|protocols|string array|The protocols followed by this code|N|

*  Example of publishing a code

```
{
    "type": "FEIP",
    "sn": 2,
    "ver": 1,
    "name": "Code",
    "data":{
        "op":"publish",
        "name": "FreeChain",
		"ver": "1",
		"did": "fd3f2096a6f2c83cfada751db524d978f798b7a1530c212e0fd9c27a10bf85fb",
        "desc": "The code to parse basic information from the freecash blockchain.",
		"waiters": ["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"],
		"langs":["java"],
        "urls": ["https://github.com/nobodyoffc/FreeChain.git"],
        "protocols":["b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553","37406e3e45750efccdb060ca2e748f9f026aebb7dadade8e8747340f380edaca"]
    }
}
```

The txid is : 28e85989ce7e3bba56c8179d6dd9b180b23ff48a4ad031c72539d02750659212. It's also the codeId of the published code。


## Update

The publisher of a code updates the code information. All fields will be replaced together.

|field|type|content|required|
|:----|:----|:----|:----|
|codeId|hex|Txid when the code was published|Y|
|op|string|Operation: "update"|Y|
|name|string|he name of the code in english|Y|
|ver|string|he version of the code in english|Y|
|did|string|The sha256x2 hash of this version code file|Y|
|desc|string|Description of this code|N|
|langs|string array|The program languages of the code|N|
|urls|string array|URLs，the locations to get the code|N|
|protocols|string array|The protocols followed by this code|N|

*  Example of updating a code

```
{
    "type": "FEIP",
    "sn": 2,
    "ver": 1,
    "name": "Code",
    "data":{
        "codeId": "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151",
        "op":"update",
        "name": "FreeChain",
		"ver": "2",
		"did": "fd3f2096a6f2c83cfada751db524d978f798b7a1530c212e0fd9c27a10bf85fb",
        "desc": "The code to parse basic information from the freecash blockchain.",
		"waiters": ["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"],
		"langs":["java"],
        "urls": ["https://github.com/nobodyoffc/FreeChain.git"],
        "protocols":[]
    }
}
```

## Stop

The owner stops maintaining a code as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|Operation: "stop"|Y|
|codeId|hex|Txid when the code was published|Y|


*  Example of stoping a code

```
{
    "type": "FEIP",
    "sn": 2,
    "ver": 1,
    "name": "Code",
    "data":{
        "codeId": "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151",
        "op":"stop"
    }
}
```
## Recover

The owner recovers a Stopped code as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|Operation: "recover"|Y|
|codeId|hex|Txid when the code was published|Y|


*  Example of stoping a code

```
{
    "type": "FEIP",
    "sn": 2,
    "ver": 1,
    "name": "Code",
    "data":{
        "codeId": "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151",
        "op":"recover"
    }
}
```

## Close

The owner or its master close a code permanently, the OP_RETURN of which contains the data as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|Operation: "close"|Y|
|codeId|hex|Txid when the code was published|Y|


* Example of closing a code:

```
{
    "type": "FEIP",
    "sn": 2,
    "ver": 1,
    "name": "Code",
    "data":{
        "codeId": "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151",
        "op":"close"
    }
}
```

## Rate

Anyone but the owner rate a published code as following:


|field|type|content|required|
|:----|:----|:----|:----|
|codeId|hex|Txid when the code was published|Y|
|op|string|operation: "rate"|Y|
|rate|int|Score of rating from 0 to 5|N|

*  Example of rate a code

```
{
    "type": "FEIP",
    "sn": 2,
    "ver": 1,
    "name": "Code",
    "data":{
        "codeId": "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151",
        "op": "rate",
        "rate": 4
    }
}
```

## QR code

The QR code of a published code has fields as following:

```
{
    "meta":"FC",
    "type": "FEIP",
    "sn": 2,
    "ver": 1,
    "data":{
        "codeId": "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151"
    }
}
```
