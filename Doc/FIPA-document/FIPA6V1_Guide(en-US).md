```
FIPA6: Guide
Version: 1
Language: en-US
Author: C_armX
Status: draft
Created date: 2021-04-25
Last modified date：2022-02-12
PID: "unknown"
TXid: 
```

# FIPA6V1_Guide(en-US)

## Contents

[Introduction](#introduction)

[General rules of FIPA type protocols](#general-rules-of-fipa-protocols)

[Rules specific to this protocol](#rules-specific-to-this-protocol)


## Introduction

```

ProtocolType: FIPA
SerialNumber: 6
ProtocolName: Guide
VersionNumber: 1
Description : Define the sender of the first utxo received by an address as its Guide.
Author: C_armX
Language: en-US
Tags: FIPA, guide
PreVersionPid:"unknown"

```

## General consensus of FIPA protocols

FIPA protocols make improvements to the FCH ecosystem. These improvements will not lead to a hard fork of the main network, and will not write information on the blockchain.

## Consensus of this protocol

1. The guide is the address who introduces a new active address into the freecash society.

2. The address of the first input of the transaction by which a new address received its first utxo is the guide of this new address.

3. The addresses with the first utxo from coinbase have no guide.

## Examples

1. FEk41Kqjar45fLDriztUDTUkdki7mmcjWK is the guide of F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW due to the former send the first utxo to the later in transaction ae1c03dd8ca6be465ecdf0e5ff86082a76c37fa83969452009faf5d4d4b9d8d7.

2. FTqiqAyXHnK7uDTXzMap3acvqADK4ZGzts has no guide because it received its first utxo from coinbase in transaction 2fa578c425d4c6872d1ebd6a4b28c6d42a2d3c7826158956b8cd326de789f65a.