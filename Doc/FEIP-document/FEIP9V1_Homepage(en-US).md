# FEIP9V1_Homepages(en-US)

## Contents

[Consensuses of FEIP](#consensuses-of-feip)

[Consensuses of this protocol](#consensuses-of-this-protocol)

[Register](#register)

[Unregister](#unregister)



```

Protocol type: FEIP
Serial number: 9
Protocol name: Homepages
Version: 1
Description : Register home pages path on the freecash blockchain for an address.
Authors: C_armX
Language: en-US
Create date: 2022-10-02
Update date：2023-05-04

```

## Consensuses of FEIP

1. FEIP type protocols write data into freecash blockchain with OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation writing FEIP data with OP_RETURN needs more than `1cd` consumed.

7. More consensuses of FEIP can be found in `FEIP1_FreeProtocol`.

## Consensuses of this protocol

1. An address can register some URLs as its homepages with writing in op_return.

2. The homepages are only registered for the address of the first input.

3. When new homepages are registered, all the old ones are automatically unregistered.

4. Operations of this protocol write OP_RETURN with the same content structure:

|field|type|content|required|
|:----|:----|:----|:----|
|type|String|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 9|Y|
|ver|int|Fixed: 1|Y|
|name|String|Fixed: "Homepages"|N|
|pid|string|PID of this protocol. The txid where this protocol was published.|N|
|did|string|DID of this protocol file. The sha256x2 hash of the protocol file in hex.|N|
|data|object|Operation data which is different in different operations. |Y|

## register

data:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "register"|Y|
|homepages|string array|512|URLs of the homepages|Y|

* Example

```
{
    "type": "FEIP",
    "sn": 9,
    "ver": 1,
    "name": "Homepages",
    "data":{
        "op": "register",
        "homepages": ["https://cid.cash","127.0.0.1:8080"]
    }
}
```
## Unregister

data:

|field|type|content|required|
|:----|:----|:----|:----|
|type|String|Fixed: "FEIP"|Y|
|op|string|operation: "unregister" |Y|

* Example

```
{
    "type": "FEIP",
    "sn": 9,
    "ver": 1,
    "name": "Homepages",
    "data":{
        "op": "unregister"
    }
}
```