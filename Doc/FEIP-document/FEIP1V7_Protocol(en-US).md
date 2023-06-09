# FEIP1V7_Protocol(en-US)

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

```
Protocol type: FEIP
Serial number: 1
Protocol name: Protocol
Version number: 7
Description : Consensuses of freely publishing and managing protocols on the blockchain of freecash.
Author: C_armX, Deisler-JJ_Sboy
Language: en-US
Create date: 2022-02-01
Update date：2023-05-07
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

1. This protocol is used to publish protocol information on Freecash blockchain. 

2. PID（Protocol Identity）: The txid when publishing the protocol is the identity of the protocol。

3. One can send a transaction to publish a protocol by writing data in the format given by [Publish](#publish) .

4. The title of protocol published on chain is in following format: 

`type + serial number + 'V' + version number +'_' + protocol name + '(' + language + ')'`

e.g: `FEIP3V4_CID(en-US)`.

5. According to `FIPA1V1_Entity`, the FCN(Free Cousensus Name) of the protocol is `<title>@<cid of the publisher>`, e.g., `FEIP3V4_CID(en-US)@C_armX`.

6. One can only update its own protocols by writing data in the format given by [Update](#update).

7. One can only stop its own protocols by writing data in the format given by [Stop](#stop).

8. One can rate other's protocols by writing data in the format given by [Rate](#rate).

9. One can't rate its own protocols.

10. Stopped  or closed protocols still can be rated.

11. If the consensuses of a new version is not compatible with the previous version, a new protocol should be released instead of releasing a new version, and the old pid should be given in prePid of the new one.

12. Owner or Owner's master (see FEIP6_Master) can [Close](#close) the protocol and giving a closing statement.

13. A closed protocol can never be operated again.

14. Operations of this protocol write OP_RETURN with the same content structure:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 1|Y|
|ver|int|Version. Fixed: 1|Y|
|name|string|Fixed: "Protocol"|N|
|pid|string|PID of this protocol. |N|
|did|string|DID of this protocol file. |N|
|data|object|Operation data which is different in different operations. |Y|

## Publish

Publish a protocol by send a transaction, the OP_RETURN of which contains the data object as following:

|field|type|content|required|
|:---|:---|:---|:---|
|op|String|Fixed:"publish"|Y|
|type|String|Type of the protocol being published.|Y|
|sn|String|Serial number of the protocol being published,counting from 1.|Y|
|ver|String|Version number of the protocol being published. counting from 1.|Y|
|name|String|Name of the protocol being published.|Y|
|desc|String|Short description of the protocol being published.|N|
|waiters|Stringarray|FCH addresses of the waiters to serve users of this protocol.|N|
|did|string|The double sha256 value of the protocol file being published.|Y|
|lang|String|The language of the protocol being published,formatted with i18n.|N|
|preDid|string|The double sha256 value of the previous protocol file.|N|
|fileUrls|Stringarray|Location URLs to get the protocolfile.|N|

* Example

```
{
    "type": "FEIP",
    "sn": 1,
    "ver": 7,
    "name": "Protocol",
    "data": {
        "op": "publish",
        "type": "FEIP",
        "sn": "3",
        "ver": "4",
        "name": "CID",
        "desc": "Register or unregister a human friendly identity for an address.",
        "waiters": ["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"],
		"did": "1403e5b7100d8e24724f12cd1ea0b722086c02250a7c66b711947f14546cfcfd",
        "lang": "zh-CN",
        "prePid":"",
        "fileUrls": ["https://github.com/freecashorg/FEIP/FEIP3_CID/"]
    }
}
```
## Update
Update a protocol by send a transaction. All field contents will be updated.

|field|type|content|required|
|:---|:---|:---|:---|
|op|String|Fixed:"update"|Y|
|pid|string|The pid of the protocol file being published.|Y|
|type|String|Type of the protocol being published.|Y|
|sn|String|Serial number of the protocol being published,counting from 1.|Y|
|ver|String|Version number of the protocol being published. counting from 1.|Y|
|name|String|Name of the protocol being published.|Y|
|desc|String|Short description of the protocol being published.|N|
|waiters|Stringarray|FCH addresses of the waiters to serve users of this protocol.|N|
|did|string|The double sha256 value of the protocol file being published.|Y|
|lang|String|The language of the protocol being published,formatted with i18n.|N|
|preDid|string|The double sha256 value of the previous protocol file.|N|
|fileUrls|Stringarray|Location URLs to get the protocolfile.|N|


* Example

```
{
    "type": "FEIP",
    "sn": 1,
    "ver": 7,
    "name": "Protocol",
    "pid": "",
    "data": {
		"op": "update",
		"pid": "c50d307c3ac0c193dad6c671ad3cebb881c01c747e03abfeaecc378419739ff4",
        "type": "FEIP",
        "sn": "3",
        "ver": "4",
		"hash": "1403e5b7100d8e24724f12cd1ea0b722086c02250a7c66b711947f14546cfcfd",
        "name": "CID",
        "desc": "Register, update or unregister a human friendly identity for an address.",
        "waiters": ["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"],
        "did": "1403e5b7100d8e24724f12cd1ea0b722086c02250a7c66b711947f14546cfcfd",
		"lang": "en-US",
        "prePid":"",
        "fileUrls": ["https://github.com/freecashorg/FEIP/FEIP3_CID/"]
    }
}
```
## Stop
Stop a protocol by send a transaction, the OP_RETURN of which contains the data as following:

|field|type|content|required|
|:---|:---|:---|:---|
|op|String|Fixed:"stop"|Y|
|pid|string|The pid of the protocol file to be stoped.|Y|

* Example

```
{
    "type": "FEIP",
    "sn": 1,
    "ver": 7,
    "name": "Protocol",
    "pid": "",
    "data": {
		"op": "stop",
        "pid": "c50d307c3ac0c193dad6c671ad3cebb881c01c747e03abfeaecc378419739ff4"
    }
}

```
## Recover

Recover a protocol by send a transaction, the OP_RETURN of which contains the data as following:

|field|type|content|required|
|:---|:---|:---|:---|
|op|String|Fixed:"recover"|Y|
|pid|string|The pid of the protocol file to be stoped.|Y|

* Example

```
{
    "type": "FEIP",
    "sn": 1,
    "ver": 7,
    "name": "Protocol",
    "pid": "",
    "data": {
		"op": "recover",
        "pid": "c50d307c3ac0c193dad6c671ad3cebb881c01c747e03abfeaecc378419739ff4"
    }
}

```

## Close

The owner or its master close a protocol permanently, the OP_RETURN of which contains the data as following:

|field|type|content|required|
|:---|:---|:---|:---|
|op|String|Fixed:"close"|Y|
|pid|string|The pid of the protocol file to be stoped.|Y|

* Example

```
{
    "type": "FEIP",
    "sn": 1,
    "ver": 7,
    "name": "Protocol",
    "pid": "",
    "data": {
		"op": "close",
        "pid": "c50d307c3ac0c193dad6c671ad3cebb881c01c747e03abfeaecc378419739ff4"
    }
}

```

## Rate

Evaluate a protocol by send a transaction, the OP_RETURN of which contains the data as following:

|field|type|content|required|
|:---|:---|:---|:---|
|op|String|Fixed:"rate"|Y|
|pid|string|The pid of the protocol file to be stoped|Y|
|rate|int|Score of rating from 0 to 5|Y|

* Example

```
{
    "type": "FEIP",
    "sn": 1,
    "ver": 7,
    "name": "Protocol",
    "pid": "",
    "data": {
        "op": "rate",
		"pid": "c50d307c3ac0c193dad6c671ad3cebb881c01c747e03abfeaecc378419739ff4",
        "rate": 5
    }
}
```
## QR code

The QR code of a published protocol has fields as following:

```
{
    "meta":"FC",
    "type": "FEIP",
    "sn": 1,
    "ver": 7,
    "name": "Protocol",
    "data":{
        "pid": "c50d307c3ac0c193dad6c671ad3cebb881c01c747e03abfeaecc378419739ff4"
    }
}
```