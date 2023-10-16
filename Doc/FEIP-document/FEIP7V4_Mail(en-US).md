```
Protocol type: FEIP
SerialNumber: 7
ProtocolName: Mail
Version: 4
Description : Save encrpted mail in the blockchain of FCH.
Authors: C_armX, Deisler-JJ_Sboy，Free_Cash
Language: en-US
CreateDate: 2021-04-03
UpdateDate: 2023-05-22
```

# FEIP7V3_Mail(en-US)

## Contents

[General consensus of FEIP](#general-consensus-of-feip)

[Consensus of this protocol](#consensus-of-this-protocol)

[Add](#send)

[Delete](#delete)

[Recover](#recover)

## Consensuses of FEIP

1. FEIP type protocols write data into freecash blockchain with OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation writing FEIP data with OP_RETURN needs more than `1cd` consumed.

7. More consensuses of FEIP can be found in `FEIP1_Protocol`.

## Consensuses of this protocol

1. This protocol is used to send p2p encrypted mail via the blockchain of FCH.

2. The sender is the first input address. The recipient is the first output address.

3. Only the recipient of a mail can delete or recover the mail, not the sender.

4. Operations of this protocol write OP_RETURN with the same content structure:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 7|Y|
|ver|int|Version. Fixed: 4|Y|
|name|string|Fixed: "Mail"|N|
|pid|string|PID of this protocol. The txid where this protocol was published.|N|
|did|string|DID of this protocol file. The sha256x2 hash of the protocol file in hex.|N|
|data|object|Operation data which is different in different operations. |Y|

7. Operations:

## Send

When user sends a mail, the data object contains:

| field      | type   | content                                                     | required |
|:-----------|:-------|:------------------------------------------------------------|:---------|
| op         | string | operation: "send"                                           | Y        |
| alg        | string | The encrypt algorithm.                                      | Y        |
| cipher     | string | mail encrypted with sender's priKey and recipient's pubKey. | N        |
| cipherSend | string | mail encrypted with the public key of the sender            | N        |
| cipherReci | string | mail encrypted with the public key of the recipient         | N        |
| textId     | string | Double sha256 hash value of the plain text of the mail      | N        |

* Example

	- Sender: FEk41Kqjar45fLDriztUDTUkdki7mmcjWK
	- Sender pubKey: 030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a
	- Recipient: F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW
	- Recipient pubKey: 02536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f67
	- Privatekey: L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8
	- plain text: "This is a mail."
	- OP_RETURN content:

```
{
	"type": "FEIP",
	"sn": 7,
	"ver": 4,
	"name": "Mail",
	"data": {
		"op": "send",
		"alg": "EccAes256BitPay@No1_NrC7",
		"cipherSend": "AtpfHV/b+P2TMG8eFYpwdR7FCOhRjsK5I9UiZBIwUBtBAzi+B/tS5lMv27T9ofORhmSTYRYEfMIaffEwkwtfgEdormE5u4YKnBf001bWPeIQIXEz8HAj7XVt1rYLciJPdQ==",
		"cipherReci": "Avmexwg3OUjOPl7KyAcu1VcAYy1ln90DmqLH/POOPtwDD6PhiitBd/aRjRBZR9Tx9khysbQLHXKFL4BJ6Af0J6iKiDLZXOfvlzdXNM+TzADj0ryOsX9vtVXPsiljS6PjFg==";
		"textId":"c269f3b03960a695e1e2e4ab451bd645c7134d3328a327c98c00ed102f76b451"
	}
}
```

```json

{
	"type": "FEIP",
	"sn": 7,
	"ver": 4,
	"name": "Mail",
	"data": {
		"op": "send",
		"alg": "EccAes256K1P7@No1_NrC7",
		"cipher": "fxBWByu7+7Kbie34w/9ICyWSEFopeMou5+sm/eRhdFw=",
		"textId":"1332f5132b45de03e729e764b38f9a2e4057d6760a8807662331b37ddea936e4"
	}
}

```

## Delete

When user deletes a mail, the data object contains:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "delete"|Y|
|mailId|string|The txid in which the mail was sent.|Y|


### Example for deleting an item
```
{
    "type": "FEIP",
    "sn": 7,
    "ver": 4,
    "name": "Mail",
    "pid": "",
    "data":{
        "op": "delete",
        "mailId": "e51d9e87341bdb4ee497d2252dd1db075accf61df8dccd353ed518a558a066ac"
    }
}
```

## Recover

When user recovers a deleted mail, the data object contains:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "recover"|Y|
|mailId|string|The txid in which the mail was sent.|Y|

### Example for recovering an item
```
{
    "type": "FEIP",
    "sn": 7,
    "ver": 4,
    "name": "Mail",
    "pid": "",
    "data":{
        "op": "recover",
        "mailId": "e51d9e87341bdb4ee497d2252dd1db075accf61df8dccd353ed518a558a066ac"
    }
}
```
