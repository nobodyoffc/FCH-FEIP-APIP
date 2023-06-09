# FEIP17V3_Secret(en-US)

## Contents

[Summary](#summary)

[General consensus of FEIP](#general-consensus-of-feip)

[Consensus of this protocol](#consensus-of-this-protocol)

[Add](#add)

[Delete](#delete)

[Recover](#recover)


## Summary

```
Protocol type: FEIP
Serial number: 17
Protocol name: Secret
Version: 3
Description : Save encrpted important data in the blockchain of FCH.
Author: C_armX, Deisler-JJ_Sboy，Free_Cash
Language: en-US
Created date: 2021-03-07
Last modified date：2023-01-18
```

## General consensus of FEIP

1. FEIP type protocols write data of consensus in OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs: ‘ALL’ (value 0x01).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation of writing to freecash blockchain needs more than `1cd` consumed.

## Consensus of this protocol

1. This protocol helps users to write encrypted personal information in the blockchain of FCH.

2. Use the public key of the first output address to encrypt the message.

3. The secret belongs to the first input address of the transaction that adds it.

4. Only the address who added the secret can delete or recover it.

5. When updating a secret, just delete it and add a new one.


## Add

When user adds a new secret, the OP_RETURN contains the data as follows:

|field number|field name|type|content|required|
|:-|:-|:-|:-|:-|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number. Fixed: 17|Y|
|3|ver|int|Fixed: 3|Y|
|4|name|String|Fixed: "Secret"|N|
|5|pid|hex|Id of this protocol|N|
|6|data.op|string|operation: "add"|Y|
|7|data.alg|string|The encrypt algorithm. "ECC256k1-AES256CBC" is default.|Y|
|8|data.ciphertext|string|Encrypted message|Y|

### Decrypted data of data.ciphertext

|field number|field name|type|content|required|
|:-|:-|:-|:-|:-|
|1|type|string|Customized by the user or App|N|
|2|title|string|Title, account, or other. Depends on "type".|N|
|3|content|string|Text, password, or other. Depends on "type". |Y|
|4|memo|string||N|

### Example for adding a secret

The address of first output: FEk41Kqjar45fLDriztUDTUkdki7mmcjWK
Publickey: 6vU3ZMpwggurw92AUy1Vi6WBxEnBPdjupXGKD7Q5Zcw8yvdJAf
Privatekey: L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8

OP_RETURN content:
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
        "ciphertext": "AjTU0rGQvDxhCs3F5x4Pcz3Bsiiry2LryPcKcPIZ2iDsD68U5he9FkM6AVUzEHTjmfBLkhfFu7rv4fveoyMi5YH+wQoiWDxgs/MYjGZBL/Fuq6XZ6IOCXfWyfwphE4uxhEg5TD9ZBRsrJbNxwbdfee5ev5Gvc8kwYROycs0sAG3rNdoJbEZZ7bs2DqvHbAWdG7w4gYLhP9o+C/xVTZHz7Ks9VHb6i04/1at40etlWXxPWSvkdDWxTtyWSSsY2jrbYjfe+ytXQRTRY4gYQdwg+9s="
        }
}
```
Decrypted information of data.ciphertext:
```
{
    "type": "password",
    "title": "john@gmail.com",
    "content": "123456",
    "memo": "https://www.gmial.com"
}
```

## Delete

When user deletes a secret, the OP_RETURN contains the data as follows:

|field number|field name|type|content|required|
|:-|:-|:-|:-|:-|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number. Fixed: 17|Y|
|3|ver|int|Fixed: 3|Y|
|4|name|String|Fixed: "Secret"|N|
|5|pid|hex|Id of this protocol|N|
|6|data.op|string|operation: "delete"|Y|
|7|data.secretId|string|The txid in which the secret was added.|Y|

### Example for deleting a secret
```
{
    "type": "FEIP",
    "sn": 17,
    "ver": 3,
    "name": "Secret",
    "hash": "",
    "data":{
        "op": "delete",
        "secretId": "61f151ac38370f2349080ad210391f2e9de44fc878954e4eab4d3bcb0dba7a19"
    }
}
```

## Recover

When user recovers a secret, the OP_RETURN contains the data as follows:

|field number|field name|type|content|required|
|:-|:-|:-|:-|:-|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number. Fixed: 17|Y|
|3|ver|int|Fixed: 3|Y|
|4|name|String|Fixed: "Secret"|N|
|5|pid|hex|Id of this protocol|N|
|6|data.op|string|operation: "recover"|Y|
|7|data.secretId|string|The txid in which the secret was added.|Y|

### Example for recovering a secret
```
{
    "type": "FEIP",
    "sn": 17,
    "ver": 3,
    "name": "Secret",
    "hash": "",
    "data":{
        "op": "recover",
        "secretId": "61f151ac38370f2349080ad210391f2e9de44fc878954e4eab4d3bcb0dba7a19"
    }
}
```