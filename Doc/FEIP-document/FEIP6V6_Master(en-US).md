# FEIP6V6_Master(en-US)

## Contents

[Consensuses of FEIP](#consensuses-of-feip)

[Consensuses of this protocol](#consensuses-of-this-protocol)


```
Protocol type: FEIP
Serial number: 6
Protocol name: Master
Version number: 6
Description : An address authorize another address as its master.
Author: C_armX
Language: en-US
Create date: 2021-06-23
Update date：2023-05-06

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

1. Once the master address is authorized, it can control all the rights and interests of the original address.

2. An address can't authorize itself.

3. The authorization can't be canceled.

4. An address can only implement a valid master authorization once, and subsequent authorizations are invalid.

5. To authorize an address as master, the OP_RETURN contains:

| field             | type   | content                                                                   | required |
|:------------------|:-------|:--------------------------------------------------------------------------|:---------|
| type              | string | Fixed: "FEIP"                                                             | Y        |
| sn                | int    | Serial number. Fixed: 6                                                   | Y        |
| ver               | int    | Version. Fixed: 1                                                         | Y        |
| name              | string | Fixed: "Master"                                                           | N        |
| pid               | string | PID of this protocol. The txid where this protocol was published.         | N        |
| did               | string | DID of this protocol file. The sha256x2 hash of the protocol file in hex. | N        |
| data.master       | string | The address designated as the master.                                     | Y        |
| data.promise      | string | Fixed:"The master owns all my rights."                                    | Y        |
| data.cipherPriKey | string | The private Key encrypted with master's public key.                       | N        |
| data.alg          | string | The algorithm of encrypting.                                              | N        |

### Example

```
{
    "type": "FEIP",
    "sn": 6,
    "ver": 1,
    "name": "Master",
    "pid": ""
    "data":{
        "master":"FTqiqAyXHnK7uDTXzMap3acvqADK4ZGzts",
        "promise":"The master owns all my rights.",
        "cipherPriKey":"The master owns all my rights.",
        "alg":"eccAes256K1P7@No1_NrC7"
    }
}
```