# FEIP16V1_Reputation(en-US)

## Contents

[Consensuses of FEIP](#consensuses-of-feip)

[Consensuses of this protocol](#consensuses-of-this-protocol)


## Summary

```
Protocol type: FEIP
Serial number: 16
Protocol name: Reputation
Version: 1
Description : Rate an address to increase/decrease its reputation.
Author: C_armX, Deisler-JJ_Sboy
Language: en-US
Create date: 2021-02-05
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

1. FEIP16 provides a way for an CID to evaluate other CIDs.

2. Rating someone on chain consumes CoinDays.

3. The rating can be Positive（"good"） or Negative("bad"), and The measure unit of an rating is CoinDays. 

4. The quantity of an rating is all the CoinDays Destoryed(CDD) in the transaction.For example: "100 CoinDays Negative rating" can be marked as "-100cd", and "88 CoinDays positive rating" can be marked as "88cd" or "+88cd".

5. The rater is the first input address,and the ratee is the addrsee of the first output other than OP_RETURN output and rater.

6. Anyone can't rate itself.

7. When rating, the field data.cause is used to record the thing causing the rating in format of "object type:object did" , such as "sid:a246f36574f039460063f626f023c2e13a4d184c89279795539be220e9402643".

8. To rate an address, the OP_RETURN contains:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 16|Y|
|ver|int|Version. Fixed: 1|Y|
|name|string|Fixed: "Reputation"|N|
|pid|string|PID of this protocol. The txid where this protocol was published.|N|
|did|string|DID of this protocol file. The sha256x2 hash of the protocol file in hex.|N|
|data.rate|string|must be "good" or "bad"|Y|
|data.cause|string|The cause of rating. Format: "object type:object did"|N|


The OP_RETURN of which contains the data as follows:

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number. Fixed: 16|Y|
|3|ver|int|Fixed: 1|Y|
|4|name|String|Fixed: "Reputation"|N|
|5|pid|string|The PID of this protocol|N|
|6|data.sign|string|must be "good" or "bad"|Y|
|7|data.cause|string|The cause of rating. Format: "object type:object did"|N|


### Example of positive rating
```

Rater(first Input address): FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv
Retee(first output address): FEk41Kqjar45fLDriztUDTUkdki7mmcjWK
cdd: 1000

OP_RETURN content:
{
    "type": "FEIP",
    "sn": 16,
    "ver": 1,
    "name": "Reputation",
    "pid": "",
    "data":{
        "rate": "good",
        "cause": "sid#a246f36574f039460063f626f023c2e13a4d184c89279795539be220e9402643"
    }
}

FEk41Kqjar45fLDriztUDTUkdki7mmcjWK will get 1000 reputation increasing from FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv's rating.
```

### Example of negative rating
```

Rater(first Input address): FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv
Retee(first output address): FEk41Kqjar45fLDriztUDTUkdki7mmcjWK
cdd: 1000

OP_RETURN content:
{
    "type": "FEIP",
    "sn": 16,
    "ver": 4,
    "name": "Reputation",
    "pid": "",
    "data":{
        "rate": "bad",
        "cause": "sid#a246f36574f039460063f626f023c2e13a4d184c89279795539be220e9402643"
    }
}
FEk41Kqjar45fLDriztUDTUkdki7mmcjWK will get 1000 reputation reducing from FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv's rating.

```