# FEIP20V1_P2SH(en-US)

## Contents

[Summary](#summary)

[General consensus of FEIP](#general-consensus-of-feip)

[Consensus of this protocol](#consensus-of-this-protocol)

[Publish](#publish)

## Summary

```
Protocol type: FEIP
Serial number: 20
Protocol name: P2SH
Version: 1
Description : Publish P2SH redeem script by the address from the script hash.
Author: C_armX
Language: en-US
Created date: 2023-01-06
Last modified date：2023-01-14
```

## General consensus of FEIP

1. FEIP type protocols write data of consensus in OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs: ‘ALL’ (value 0x01).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation of writing to freecash blockchain needs more than `1cd` consumed.

## Consensus of this protocol

1. This protocol is used to publish P2SH redeem script by the address from the script hash.
2. The address is Base58check encoded string of the redeem script's SHA256 + RIPEMD160 hash value with prefix '0x05'.

## Publish

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number. Fixed: 20|Y|
|3|ver|int|Fixed: 1|Y|
|4|name|String|Fixed: "P2SH"|N|
|5|pid|string|The PID of this protocol|N|
|6|data.script|string|Redeem script|Y|

* Example

 "3MRS39FX8bpV9CCjJHavw586q6a9Rogpw2"is a 2/3 multi-signature address of public keys :
030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a
02536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f67
03f0145ddf5debc7169952b17b5c6a8a566b38742b6aa7b33b667c0a7fa73762e2

The muti-signature address sends a tx to publish the redeem script:
"5221030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a2102536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f672103f0145ddf5debc7169952b17b5c6a8a566b38742b6aa7b33b667c0a7fa73762e253ae"

```
{
    "type": "FEIP",
    "sn": 20,
    "ver": 1,
    "name": "P2SH",
    "pid": "",
    "data":{
        "script": "5221030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a2102536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f672103f0145ddf5debc7169952b17b5c6a8a566b38742b6aa7b33b667c0a7fa73762e253ae"
        }
}
```