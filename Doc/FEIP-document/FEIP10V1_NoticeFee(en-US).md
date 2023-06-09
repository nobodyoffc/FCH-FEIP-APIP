# FEIP10V1_NoticeFee(en-US)

## Contents


[Consensuses of FEIP](#consensuses-of-feip)

[Consensuses of this protocol](#consensuses-of-this-protocol)

```

Protocol type: FEIP
Serial number: 10
Protocol name: NoticeFee
Version: 1
Description : Declare the minimum payment amount with which it is willing to receive the on-chain message notifications.
Author: C_armX
Language: en-US
Create date: 2021-04-26
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

## Consensus of this protocol

1. Notice fee is the minimum payment amount that an address is willing to receive on-chain message notifications. It is used to avoid spam message.

2. When there are multiple inputs, the notice fee is for the address of the first input.

3. When a new notice fee is declared, the old one is automatically cancelled.

4. To declare the notice fee, the OP_RETURN contains:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 10|Y|
|ver|int|Version. Fixed: 1|Y|
|name|string|Fixed: "NoticeFee"|N|
|pid|string|PID of this protocol. The txid where this protocol was published.|N|
|did|string|DID of this protocol file. The sha256x2 hash of the protocol file in hex.|N|
|data.noticeFee|string|Amount of fch in string.|Y|

### Example
```
{
	"type": "FEIP",
	"sn": 10,
	"ver": 1,
	"name": "NoticeFee",
	"data": {
		"noticeFee": "0.0001"
	}
}
```