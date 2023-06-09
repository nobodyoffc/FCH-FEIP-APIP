```
Protocol type: FEIP
SerialNumber: 17
ProtocolName: Secret
Version: 3
Description : Save encrpted secret in the blockchain of FCH.
Authors: C_armX, Deisler-JJ_Sboy，Free_Cash
Language: en-US
CreateDate: 2021-03-07
UpdateDate: 2023-05-22
```

# FEIP17V3_Secret(en-US)

## Contents

[Summary](#summary)

[General consensus of FEIP](#general-consensus-of-feip)

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

1. Save encrpted secret in the blockchain of FCH.

2. Use the public key of the first input address to encrypt the message. 

3. The secret belongs to the first input address of the transaction that added it.

4. Only the address who added the secret can delete or recover it.

5. When updating an secret, just delete it and add a new one.

6. Operations of this protocol write OP_RETURN with the same content structure:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 17|Y|
|ver|int|Version. Fixed: 3|Y|
|name|string|Fixed: "Secret"|N|
|pid|string|PID of this protocol. The txid where this protocol was published.|N|
|did|string|DID of this protocol file. The sha256x2 hash of the protocol file in hex.|N|
|data|object|Operation data which is different in different operations. |Y|

7. Operations:

## Add

When user adds a secret, the data object contains:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "add"|Y|
|alg|string|The encrypt algorithm. "ECC256k1-AES256CBC" is recommended.|Y|
|ciphertext|string|Encrypted message|Y|

* Decrypted data of data.ciphertext

|field|type|content|required|
|:----|:----|:----|:----|
|address|string|The FCH address of the secret|Y|
|note|string|Notes to this secret|N|
|noNoticeFee|boolean|Accept all notices from it regardless notice fee(see FEIP27).|N|
|seeStatement|boolean|Whether see its statements(see FEIP8).|N|
|seeWorks|boolean|Whether see its works(see FEIP8).|N|

* Example

	- The address adding or updating a secret: FEk41Kqjar45fLDriztUDTUkdki7mmcjWK
	- Publickey: 6vU3ZMpwggurw92AUy1Vi6WBxEnBPdjupXGKD7Q5Zcw8yvdJAf
	- Privatekey: L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8
	- OP_RETURN content:

```
{
    "type": "FEIP",
    "sn": 17,
    "ver": 3,
    "name": "Secret",
    "hash": "",
    "data":{
        "op": "add",
        "alg": "ECC256k1-AES256CBC",
        "ciphertext": "Ap1G/crcuoG2WHpSoXIJ88y3pIKfXFSANe5Ht0BURS8NEf8iho8vn7ILXXz3dFWGqQ1dJCMoa5CceJSrmd7hDIKHQO11Xs7s4Fzkdgx/8mCWX/tiLWY24IQpw5csomwfcnINa6V4O0pfUeN0c+9uacLJB38moFz3jX3ameUvVFBEVRZBdPjfOyenmLjPTeq4XhcnB9Lyw4CyzHEtKSZjUcI2UmVhKywA3A9Iwqn+fZ4mczO0/GwoJhsZC+3gmUqKkB4oik1351eSmS9/jeUGioA="
	}
}
```
Decrypted information of data.ciphertext:
```
{
    "type": "password",
    "title": "john@gmail.com",
    "content": "123456",
    "memo": "https://www.gmail.com"
}
```

## Delete

When user deletes a secret, the data object contains:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "delete"|Y|
|secretId|string|The txid in which the secret was added.|Y|


### Example for deleting an item
```
{
    "type": "FEIP",
    "sn": 17,
    "ver": 3,
    "name": "Secret",
    "hash": "",
    "data":{
        "op": "delete",
        "secretId": "fa5c2ca808fb6fabddd6d9b58f0c1467920e5ce5b25da59a26b1b77a33320eed"
    }
}
```

## Recover

When user recovers a deleted secret, the data object contains:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "recover"|Y|
|secretId|string|The txid in which the secret was added.|Y|

### Example for recovering an item
```
{
    "type": "FEIP",
    "sn": 17,
    "ver": 3,
    "name": "Secret",
    "hash": "",
    "data":{
        "op": "recover",
        "secretId": "fa5c2ca808fb6fabddd6d9b58f0c1467920e5ce5b25da59a26b1b77a33320eed"
    }
}
```
