


CID

NID
	每个人可以在链上对客体命名，然后以name@cid作为客体的人类可识别身份。
	每个人的命名空间内name唯一，可标记过时，可恢复，不可再次用于命名。采用nid
	删除的命名仍然可以被检索使用，只是标注过时。

ID 为sha256x2("name"+"fid").

命名：
```
{
	"type": "FEIP",
	"sn": 11,
	"ver": 1,
	"name": "NID",
	"data": {
		"op": "add",
		"name": "test",
		"desc": "A ECC/256/AES/sepc-k1 cryptography algorithm in Java. The AES is compatible with OpenSSL. The ECC is of ECDH and based on the lib of org.bouncycastle.",
		"oid": "(pid)00000000000000000000000000000000000000000000000000000000000000"
	}
}
```
停用：
```
{
	"type": "FEIP",
	"sn": 11,
	"ver": 1,
	"name": "NID",
	"data": {
		"op": "stop",
		"name": "test"
	}
}
```

恢复：
```
{
	"type": "FEIP",
	"sn": 11,
	"ver": 1,
	"name": "NID",
	"data": {
		"op": "recover",
		"name": "test"
	}
}
```




```
Protocol type: FEIP
SerialNumber: 11
ProtocolName: NID
Version: 3
Description : Store the encrypted information of addresses for contact on freecash blockchain..
Authors: C_armX, Deisler-JJ_Sboy，Free_Cash
Language: en-US
CreateDate: 2021-05-21
UpdateDate: 2023-05-22
```

# FEIP12V3_Contact(en-US)

## Contents

[Consensus of FEIP](#general-consensus-of-feip)

[Consensus of this protocol](#consensus-of-this-protocol)

[Add](#add)

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

1. Store the encrypted information of addresses for contact on freecash blockchain.

2. Use the public key of the first input address to encrypt the message. 

3. The contact belongs to the first input address of the transaction that added it.

4. Only the address who added the contact can delete or recover it.

5. When updating an contact, just delete it and add a new one.

6. Operations of this protocol write OP_RETURN with the same content structure:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 12|Y|
|ver|int|Version. Fixed: 3|Y|
|name|string|Fixed: "Contact"|N|
|pid|string|PID of this protocol. The txid where this protocol was published.|N|
|did|string|DID of this protocol file. The sha256x2 hash of the protocol file in hex.|N|
|data|object|Operation data which is different in different operations. |Y|

7. Operations:

## Add

When user adds a contact, the data object contains:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "add"|Y|
|alg|string|The encrypt algorithm. "ECC256k1-AES256CBC" is recommended.|Y|
|ciphertext|string|Encrypted message|Y|

* Decrypted data of data.ciphertext

|field|type|content|required|
|:----|:----|:----|:----|
|address|string|The FCH address of the contact|Y|
|note|string|Notes to this contact|N|
|noNoticeFee|boolean|Accept all notices from it regardless notice fee(see FEIP27).|N|
|seeStatement|boolean|Whether see its statements(see FEIP8).|N|
|seeWorks|boolean|Whether see its works(see FEIP8).|N|

* Example

	- The address adding or updating a contact: FEk41Kqjar45fLDriztUDTUkdki7mmcjWK
	- Publickey: 6vU3ZMpwggurw92AUy1Vi6WBxEnBPdjupXGKD7Q5Zcw8yvdJAf
	- Privatekey: L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8
	- OP_RETURN content:

```
{
    "type": "FEIP",
    "sn": 12,
    "ver": 3,
    "name": "Contact",
    "data":{
        "op": "add",
        "alg": "ECC256k1-AES256CBC",
        "ciphertext": "AtrFmWIFjVgOAiJV9ecB0V8vpWeGbc8nZwmJUFjan5zfnLQWLl0NH5Sjh/qWBU849x8yTpn7v6V0Hgdm2zuNGk4flfU2wyYBG2sRFlmagSLZNJQ8T/meD3FX3EGXken+bbG9P6MmSWqWZsAqnx/MtIu/ngXy/+TB6UyyvH3/e1rvPzqfrNKpzVRpcfycUFSlHmm4xU15DA/SZu01PYDUI+AR/x2poKftABu7CxQinEp8bWBARYiDkvsplJLl7h+RJDtg5UgZZlAqG03GdgvmDEWkhHDuYrCKbWpoCILeEilW"
    }
}
```
 
   - Decrypted data of data.ciphertext:

```
{
    "address": "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
    "note": "A public test address.",
    "noNoticeFee": true,
    "seeStatement": true,
    "seeWritings": true
}
```

## Delete

When user deletes a contact, the data object contains:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "delete"|Y|
|contactId|string|The txid in which the contact was added.|Y|


### Example for deleting an item
```
{
    "type": "FEIP",
    "sn": 12,
    "ver": 3,
    "name": "Contact",
    "pid": "",
    "data":{
        "op": "delete",
        "contactId": "009c74b692e7792dea9c6b943e6e46731bb30bf81c5f6399d79c2d43237b60b2"
    }
}
```

## Recover

When user recovers a deleted contact, the data object contains:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "recover"|Y|
|contactId|string|The txid in which the contact was added.|Y|

### Example for recovering an item
```
{
    "type": "FEIP",
    "sn": 12,
    "ver": 3,
    "name": "Contact",
    "data":{
        "op": "recover",
        "contactId": "009c74b692e7792dea9c6b943e6e46731bb30bf81c5f6399d79c2d43237b60b2"
    }
}
```
