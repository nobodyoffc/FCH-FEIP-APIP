# FEIP4V1_Nobody(en-US)

## Contents

[Summary](#summary)

[General consensus of FEIP](#general-consensus-of-feip)

[Consensus of this protocol](#consensus-of-this-protocol)

[Nobody](#nobody)

## Summary

```
Protocol type: FEIP
Serial number: 4
Protocol name: Nobody
Version: 1
Description : An address nobody all rights by publishing its private key on chain.
Author: C_armX
Language: en-US
Created date: 2023-01-06
Last modified date：2023-01-13
```

## General consensus of FEIP

1. FEIP type protocols write data of consensus in OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs: ‘ALL’ (value 0x01).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation of writing to freecash blockchain needs more than `1cd` consumed.

## Consensus of this protocol

1. This protocol is used to nobody one's all rights by itself.
2. Any address can publish its private key on freecash blockchain to give up all its rights.

## Nobody

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number. Fixed: 4|Y|
|3|ver|int|Fixed: 1|Y|
|4|name|String|Fixed: "Nobody"|N|
|5|pid|string|The PID of this protocol|N|
|6|data.priKey|string|The private key of the signer.|Y|

* Example

FEk41Kqjar45fLDriztUDTUkdki7mmcjWK Nobody its all rights.

```
{
    "type": "FEIP",
    "sn": 4,
    "ver": 1,
    "name": "Nobody",
    "pid": "",
    "data":{
        "priKey": "L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8"
        }
}
```