```
Protocol type: FEIP
SerialNumber: 3
ProtocolName: CID
Version: 4
Description : Register or unregister a human friendly identity for an address.
Authors: C_armX, Deisler-JJ_Sboy，Free_Cash
Language: en-US
CreateDate: 2021-02-05
UpdateDate: 2023-05-04
```

# FEIP3V4_CID(en-US)

## Contents

[Consensuses of FEIP](#consensuses-of-feip)

[Consensuses of this protocol](#consensuses-of-this-protocol)

[Register](#register)

[Unregister](#unregister)


## Consensuses of FEIP

1. FEIP type protocols write data into freecash blockchain with OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation writing FEIP data with OP_RETURN needs more than `1cd` consumed.

7. More consensuses of FEIP can be found in `FEIP1_Protocol`.

## Consensuses of this protocol

1. CID(Crypto Identity)：<name>+"_"+<suffix>, e.g., CY_vpAv.

2. Name：user-defined and `cannot`: 1) be `empty`; 2) contain `space`, `@`, `:`, `/`, or `_`.

3. Suffix：the last 4 letters of the address. If the new CID is used by others, increase the length of suffix until the new CID being unique, such as CY_kvpAv.

4. When an address registers a new cid, its previous cid is automatically unregistered.

5. Once a CID is registered by an address, it cannot be registered by other addresses, even if the CID has been unregistered.

6. An address can re-register its unregistered CID.

7. One address can only occupy up to 4 CIDs.

8. Operations of this protocol write OP_RETURN with the same content structure:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 3|Y|
|ver|int|Version. Fixed: 4|Y|
|name|string|Fixed: "CID"|N|
|pid|string|PID of this protocol. The txid where this protocol was published.|N|
|did|string|DID of this protocol file. The sha256x2 hash of the protocol file in hex.|N|
|data|object|Operation data which is different in different operations. |Y|

9. Operations:

## Register

data:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "register"|Y|
|name|string|Nick name given by the user|Y|

* Example

```
{
    "type": "FEIP",
    "sn": 3,
    "ver": 4,
    "name": "CID",
    "data":{
        "op": "register",
        "name": "CY"
    }
}
```
## Unregister

data:

|field|type|content|required|
|:----|:----|:----|:----|
|op|string|operation: "unregister"|Y|

* Example

```
{
    "type": "FEIP",
    "sn": 3,
    "ver": 4,
    "name": "CID",
    "data":{
        "op": "unregister"
    }
}
```