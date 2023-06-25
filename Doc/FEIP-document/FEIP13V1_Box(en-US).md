```
ProtocolName: Box
Description : Define box as the general container of data.
Protocol type: FEIP
SerialNumber: 13
Version: 1
Authors: C_armX
Language: en-US
CreateDate: 2023-04-20
UpdateDate: 2023-05-25
```

# FEIP13V1_Box(en-US)

## Contents

[Consensus of FEIP](#general-consensus-of-feip)

[Consensus of this protocol](#consensus-of-this-protocol)

[create](#create)

[update](#update)

[drop](#drop)

[recover](#recover)

## Consensus of FEIP

1. FEIP type protocols write data into freecash blockchain with OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation writing FEIP data with OP_RETURN needs more than `1cd` consumed.

7. More consensuses of FEIP can be found in `FEIP1_Protocol`.

## Consensuses of this protocol

1. This protocol define box as the general container of data.

2. Bid (Box Identity) is the txid of the transaction where the box is created. 

3. Box can contain a series of DIDs(data identities) or BIDs.

4. A box cannot contain the BID of itself.

5. Operations of this protocol write OP_RETURN with the same content structure:

| field | type    | content                                                                   | required |
|:------|:--------|:--------------------------------------------------------------------------|:---------|
| type  | string  | Fixed: "FEIP"                                                             | Y        |
| sn    | int     | Serial number. Fixed: 13                                                  | Y        |
| ver   | int     | Version. Fixed: 1                                                         | Y        |
| name  | string  | Fixed: "Box"                                                              | N        |
| pid   | string  | PID of this protocol. The txid where this protocol was published.         | N        |
| did   | string  | DID of this protocol file. The sha256x2 hash of the protocol file in hex. | N        |
| data  | object  | Operation data which is different in different operations.                | Y        |

## create
The data object contains:

| field   | type    | content                | required |
|:--------|:--------|:-----------------------|:---------|
| op      | string  | operation: "create"    | Y        |
| name    | string  |                        | N        |
| desc    | string  |                        | N        |
| contain | Object  |                        | Y        |
| alg     | string  | The encrypt algorithm. | Y        |
| cipher  | string  | Encrypted content      | Y        |

* Example

```
{
    "type": "FEIP",
    "sn": 13,
    "ver": 1,
    "name": "Box",
    "data":{
        "op": "create",
        "name": "my box",
        "desc": "my test box",
        "contain": "[{\"FEIP1V7\":\"ccdd9dc1aff5ba8a396101c93a9191900f240e8e7ec214c3ffc54e6c1817da0d\"},{\"FEIP2V1\":\"ede9f8e451bc4e5f1851bba2db36a1a0c77b531471832c4c01af4ad92544f58c\"}]"
    }
}

```
## update
The data object contains:

| field       | type    | content                                | required |
|:------------|:--------|:---------------------------------------|:---------|
| op          | string  | operation: "update"                    | Y        |
| bid         | string  | The txid in which the box was created. | Y        |
| name        | string  |                                        | N        |
| desc        | string  |                                        | N        |
| contain     | object  |                                        | Y        |

* Example

```
{
    "type": "FEIP",
    "sn": 13,
    "ver": 1,
    "name": "Box",
    "data":{
        "op": "update",
        "bid": "d421396c901111fda8442473ad03819e3c7a3e56cf63f1b2c3cb2a480ea3917d",
        "name": "my box",
        "desc": "my updated test box",
        "contain": "[{\"FEIP1V7\":\"ccdd9dc1aff5ba8a396101c93a9191900f240e8e7ec214c3ffc54e6c1817da0d\"},{\"FEIP2V1\":\"ede9f8e451bc4e5f1851bba2db36a1a0c77b531471832c4c01af4ad92544f58c\"}]"
    }
}
```
## drop
The data object contains:

| field number |field name| type                                   |content|required|
|:-------------|:-|:---------------------------------------|:-|:-|
| op           |string| operation: "drop"                      |Y|
| bid          |string| The txid in which the box was created. |Y|

* Example
```
{
    "type": "FEIP",
    "sn": 13,
    "ver": 1,
    "name": "Box",
    "data":{
        "op": "drop",
        "bid": "d421396c901111fda8442473ad03819e3c7a3e56cf63f1b2c3cb2a480ea3917d"
    }
}
```
## recover
The data object contains:

| field number |field name| type                                   |content|required|
|:-------------|:-|:---------------------------------------|:-|:-|
| op           |string| operation: "recover"                   |Y|
| bid          |string| The txid in which the box was created. |Y|

* Example
```
{
    "type": "FEIP",
    "sn": 13,
    "ver": 1,
    "name": "Box",
    "data":{
        "op": "recover",
        "bid": "d421396c901111fda8442473ad03819e3c7a3e56cf63f1b2c3cb2a480ea3917d"
    }
}
```