```
ProtocolType: FEIP
SerialNumber: 19
ProtocolName: Group
Version: 1
Description : Group related actions on Freecash blockchain. 
Authors: C_armX, Deisler-JJ_Sboy
Language: en-US
CreateDate: 2021-04-03
UpdateDate: 2023-05-07
```

# FEIP19V1_Group(en-US)

## Contents

[Consensuses of FEIP](#consensuses-of-feip)

[Consensuses of this protocol](#consensuses-of-this-protocol)

[Create](#create)

[Update](#update)

[Join](#join)

[Leave](#leave)

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

1. Everyone can create groups. The creator has no privilege.

2. The hash of the creating transaction is the group id, called gid.

3. Group information includes name, description, tCDD.

4. Any active member of a group can update the 'name' and 'desc' of the group with consuming CoinDays more than last update operation consumed.

5. Everyone can join any group or leave any group it belonging to.

6. All CDD of operations will be added to 'tCdd' which indicates the hot of the group.

7. If all members left a group, this group is permenently closed.

8. Since block height `2000000`, creating group needs more than `100cd` consumed.

9. Operations of this protocol write OP_RETURN with the same content structure:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 19|Y|
|ver|int|Version. Fixed: 1|Y|
|name|string|Fixed: "Group"|N|
|pid|string|PID of this protocol. |N|
|did|string|DID of this protocol file. |N|
|data|object|Operation data which is different in different operations. |Y|


## Create
Send a tx with the content of op_Return as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "create"|Y|
|name|String|Group name|Y|
|desc|String|Description of this group|Y|

* Example of creating a group

```
{
    "type": "FEIP",
    "sn": 19,
    "ver": 1,
    "name": "Group",
    "pid": "",
    "data":{
        "op": "create",
        "name": "test",
        "desc": "This is a test group"
    }
}
```

## Update
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "create"|Y|
|gid|string|Group ID|Y|
|name|String|Group name|Y|
|desc|String|Description of this group|Y|

* Example of updating group information

```
{
    "type": "FEIP",
    "sn": 19,
    "ver": 1,
    "name": "Group",
    "pid": "",
    "data":{
        "op": "update",
        "gid": "6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114",
        "name": "test1",
        "desc": "This is an updating test."
    }
}
```

## Join

Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "join"|Y|
|gid|string|Group ID|Y|

* Example of joining in a group

```
{
    "type": "FEIP",
    "sn": 19,
    "ver": 1,
    "name": "Group",
    "pid": "",
    "data":{
        "op": "join",
        "gid": "6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114"
    }
}
```

## Leave

Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "leave"|Y|
|gid|string|Group ID|Y|

* Example of leaving a group

```
{
    "type": "FEIP",
    "sn": 19,
    "ver": 1,
    "name": "Group",
    "pid": "",
    "data":{
        "op": "leave",
        "gid": "6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114"
    }
}
```
## QR code

The QR code of a published group has fields as following:

```
{
	"meta":"FC",
    "type": "FEIP",
    "sn": 19,
    "ver": 1,
    "data":{
        "name": "test",
        "gid": "6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114"
    }
}
```