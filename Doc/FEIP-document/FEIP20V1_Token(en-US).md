```
Protocol type: FEIP
SerialNumber: 21
ProtocolName: tokey
Version: 1
Description :Issue, manager and transfer tokens on blockchain of freecash.
Authors: C_armX
Language: en-US
CreateDate: 2022-12-22
UpdateDate: 2023-05-25
```
# FEIP21V1_Token(en-US)

## Contents

[Consensus of FEIP](#general-consensus-of-feip)

[Consensus of this protocol](#consensus-of-this-protocol)

[issue](#issue)

[sign](#sign)

[transfer](#transfer)

[destroy](#destroy)


## Consensus of FEIP

1. FEIP type protocols write data into freecash blockchain with OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation writing FEIP data with OP_RETURN needs more than `1cd` consumed.

7. More consensuses of FEIP can be found in `FEIP1_Protocol`.

## Consensuses of this protocol

1. Issue, manager and transfer tokens on blockchain of freecash.

2. ID: `tokenId` is the ID of a token sit which is the txid where the token was issued.

3. Deploy
   
   
4. Mint


5. transfer


6. destroy


7. parameters
   1. 小数位
   2. 可转让
   3. 可增发
   4. 自由铸造

# opreturn
{
   op: deploy, issue，transfer, destroy,close
   tokenId
   name
   description
   consensusId
   capacity
   decimal
   
   transferable
   
   openIssue
      maxAmtPerIssue
      minCddPerIssue
      maxIssuesPerAddr
   
   amount
   issueTo: {"<fid>":"<amt>","<fid>":""<amt>"}
}



# 库
    * token
```json
{
   tokenId
   name
   description
   consensusId
   capacity
   decimal
   
   transferable
   
   openIssue
      maxAmtPerIssue
      minCddPerIssue
      maxIssuesPerAddr
   
   circulating
   deployer
   owners: {
      "<fid>":"<amt>",
      "<fid>":"<amt>"
   }
}
```
```json
{
  "type": "FEIP",
  "sn": 20,
  "ver": 1,
  "name": "Token",
  "data": {
    "tokenId": "exampleTokenId",
    "op": "exampleOp",
    "name": "exampleName",
    "desc": "exampleDescription",
    "consensusId": "exampleConsensusId",
    "capacity": "exampleCapacity",
    "decimal": "2",
    "transferable": "true",
    "closable": "false",
    "openIssue": "true",
    "maxAmtPerIssue": "exampleMaxAmtPerIssue",
    "minCddPerIssue": "exampleMinCddPerIssue",
    "amount": "exampleAmount",
    "issueTo": [{
      "fid": "exampleAddress1",
      "amount": 2
    },
      {
        "fid": "exampleAddress2",
        "amount": 2
      }
    ],
    "transferTo": [{
      "fid": "exampleTransferAddress1",
      "amount": 2
    },
      {
        "fid": "exampleTransferAddress2",
        "amount": 2
      }
    ]
  }
}
```
## deploy

```json

{
  "type": "FEIP",
  "sn": 20,
  "ver": 1,
  "name": "Token",
  "data": {
    "op": "deploy",
    "name": "TestToken",
    "desc": "A test token on freecash.",
    "consensusId": "ce94c4e7c68575d3043aefc1ceee3423320c2ba84a9173d48b5dc71a3e12e6b3",
    "capacity": "100",
    "decimal": "8",
    "transferable": "true",
    "closable": "true"
  }
}
```
## issue
```json

{
  "type": "FEIP",
  "sn": 20,
  "ver": 1,
  "name": "Token",
  "data": {
    "op": "issue",
    "tokenId": "1ec2ce54dc8bf85bf870f701e0a128d77b9f61fb3088cffcc0b8274d4b610e2b",
    "issueTo": [
      {
        "fid": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
        "amount": 3.0000007899
      }
    ]
  }
}


```
## transfer
```json
{
  "type": "FEIP",
  "sn": 20,
  "ver": 1,
  "name": "Token",
  "data": {
    "op": "transfer",
    "tokenId": "1ec2ce54dc8bf85bf870f701e0a128d77b9f61fb3088cffcc0b8274d4b610e2b",
    "transferTo": [
      {
        "fid": "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
        "amount": 1.12345678
      }
    ]
  }
}
```
## destroy
```json
{
  "type": "FEIP",
  "sn": 20,
  "ver": 1,
  "name": "Token",
  "data": {
    "op": "destroy",
    "tokenId": "1ec2ce54dc8bf85bf870f701e0a128d77b9f61fb3088cffcc0b8274d4b610e2b"
  }
}
```

## close
```json
{
  "type": "FEIP",
  "sn": 20,
  "ver": 1,
  "name": "Token",
  "data": {
    "op": "close",
    "tokenId": "1ec2ce54dc8bf85bf870f701e0a128d77b9f61fb3088cffcc0b8274d4b610e2b"
  }
}
```


    * token_holder
```Json
{
    "id": "",
    "fid": "",
    "tokenId": "",
    "balance": "",
    "firstHeight": "",
    "lastHeight": ""
}
```
    * token_history
状态

```json
{
    "tokenId": "74690babcd29d3ebfcfa1c8904cdb293f825be723b1f927f8eb562ab14217999",
    "name": "FreeToken",
    "desc": "The first token on freecash. It's open for everyone.",
    "consensusId": "2a413f66033d2a3b9cc6b963f791928fb7937686d371abd10746f534e93bdd6f",
    "capacity": "10000000000",
    "decimal": "0",
    "transferable": "false",
    "closable": "false",
    "openIssue": "true",
    "maxAmtPerIssue": "1",
    "minCddPerIssue": "1",
    "maxIssuesPerAddr": "1",
    "closed": "false",
    "deployer": "FJYN3D7x4yiLF692WUAe7Vfo2nQpYDNrC7",
    "circulating": 1.0,
    "birthTime": 1709646832,
    "birthHeight": 2151322,
    "lastTxId": "dc7511141f59e22c2267cc5ace51c2d1bed053d87ff6f176fa2218beb2a11187",
    "lastTime": 1709649142,
    "lastHeight": 2151364
}
```
holder
```json
{
    "id": "e7ac8f6b79c62544329a9da3589d59ec9f6a8069c72b2b202e776ea650944d88",
    "fid": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
    "tokenId": "1ec2ce54dc8bf85bf870f701e0a128d77b9f61fb3088cffcc0b8274d4b610e2b",
    "balance": 0.0,
    "firstHeight": 2152032,
    "lastHeight": 2152087
}
```
history

```json
{
    "txId": "3e2a34645c1c4c1ded15215bdb665e590ac5ffa44681383dd238f85c4c399a77",
    "height": 2152128,
    "index": 1,
    "time": 1709694967,
    "signer": "FJYN3D7x4yiLF692WUAe7Vfo2nQpYDNrC7",
    "cdd": 0,
    "tokenId": "1ec2ce54dc8bf85bf870f701e0a128d77b9f61fb3088cffcc0b8274d4b610e2b",
    "op": "close"
}
```