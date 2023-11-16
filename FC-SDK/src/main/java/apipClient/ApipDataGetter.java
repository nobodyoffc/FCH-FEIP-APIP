package apipClient;

import apipClass.BlockInfo;
import apipClass.CidInfo;
import apipClass.TxInfo;
import apipClass.WebhookInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fchClass.*;
import feipClass.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApipDataGetter {
    public static List<TxInfo> getTxInfoList(Object responseData) {
        Type t = new TypeToken<ArrayList<TxInfo>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }
    public static List<WebhookInfo> getWebhookInfoList(Object responseData) {
        Type t = new TypeToken<ArrayList<WebhookInfo>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    public static List<BlockInfo> getBlockInfoList(Object responseData) {

        Type t = new TypeToken<ArrayList<BlockInfo>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    public static List<CidInfo> getCidInfoList(Object responseData) {
        Type t = new TypeToken<ArrayList<CidInfo>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    public static List<Cash> getCashList(Object responseData) {

        Type t = new TypeToken<ArrayList<Cash>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for Address
    public static List<Address> getAddressList(Object responseData) {
        Type t = new TypeToken<ArrayList<Address>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for Block
    public static List<Block> getBlockList(Object responseData) {
        Type t = new TypeToken<ArrayList<Block>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for BlockHas
    public static List<BlockHas> getBlockHasList(Object responseData) {

        Type t = new TypeToken<ArrayList<BlockHas>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for BlockMark
    public static List<BlockMark> getBlockMarkList(Object responseData) {

        Type t = new TypeToken<ArrayList<BlockMark>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for CashMark
    public static List<CashMark> getCashMarkList(Object responseData) {

        Type t = new TypeToken<ArrayList<CashMark>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for Nobody
    public static List<Nobody> getNobodyList(Object responseData) {

        Type t = new TypeToken<ArrayList<Nobody>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for OpReturn
    public static List<OpReturn> getOpReturnList(Object responseData) {

        Type t = new TypeToken<ArrayList<OpReturn>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for P2SH
    public static List<P2SH> getP2SHList(Object responseData) {

        Type t = new TypeToken<ArrayList<P2SH>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for P2SHRaw
    public static List<P2SHRaw> getP2SHRawList(Object responseData) {

        Type t = new TypeToken<ArrayList<P2SHRaw>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for Tx
    public static List<Tx> getTxList(Object responseData) {

        Type t = new TypeToken<ArrayList<Tx>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for TxHas
    public static List<TxHas> getTxHasList(Object responseData) {

        Type t = new TypeToken<ArrayList<TxHas>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for TxMark
    public static List<TxMark> getTxMarkList(Object responseData) {

        Type t = new TypeToken<ArrayList<TxMark>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for App
    public static List<App> getAppList(Object responseData) {

        Type t = new TypeToken<ArrayList<App>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for AppData
    public static List<AppData> getAppDataList(Object responseData) {

        Type t = new TypeToken<ArrayList<AppData>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for Box
    public static List<Box> getBoxList(Object responseData) {

        Type t = new TypeToken<ArrayList<Box>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for BoxData
    public static List<BoxData> getBoxDataList(Object responseData) {

        Type t = new TypeToken<ArrayList<BoxData>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for Cid
    public static List<Cid> getCidList(Object responseData) {

        Type t = new TypeToken<ArrayList<Cid>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for CidData
    public static List<CidData> getCidDataList(Object responseData) {

        Type t = new TypeToken<ArrayList<CidData>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for Code
    public static List<Code> getCodeList(Object responseData) {
        Type t = new TypeToken<ArrayList<Code>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for CodeData
    public static List<CodeData> getCodeDataList(Object responseData) {
        Type t = new TypeToken<ArrayList<CodeData>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for Contact
    public static List<Contact> getContactList(Object responseData) {

        Type t = new TypeToken<ArrayList<Contact>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for ContactData
    public static List<ContactData> getContactDataList(Object responseData) {

        Type t = new TypeToken<ArrayList<ContactData>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for FcInfo
    public static List<FcInfo> getFcInfoList(Object responseData) {

        Type t = new TypeToken<ArrayList<FcInfo>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for Group
    public static List<Group> getGroupList(Object responseData) {

        Type t = new TypeToken<ArrayList<Group>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for GroupData
    public static List<GroupData> getGroupDataList(Object responseData) {

        Type t = new TypeToken<ArrayList<GroupData>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for HomepageData
    public static List<HomepageData> getHomepageDataList(Object responseData) {

        Type t = new TypeToken<ArrayList<HomepageData>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for Mail
    public static List<Mail> getMailList(Object responseData) {

        Type t = new TypeToken<ArrayList<Mail>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for MailData
    public static List<MailData> getMailDataList(Object responseData) {

        Type t = new TypeToken<ArrayList<MailData>>() {
        }.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with Nid.java
    public static List<Nid> getNidList(Object responseData) {
        Type t = new TypeToken<ArrayList<Nid>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with NidData.java
    public static List<NidData> getNidDataList(Object responseData) {
        Type t = new TypeToken<ArrayList<NidData>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with NobodyData.java
    public static List<NobodyData> getNobodyDataList(Object responseData) {
        Type t = new TypeToken<ArrayList<NobodyData>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with NoticeFeeData.java
    public static List<NoticeFeeData> getNoticeFeeDataList(Object responseData) {
        Type t = new TypeToken<ArrayList<NoticeFeeData>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with Proof.java
    public static List<Proof> getProofList(Object responseData) {
        Type t = new TypeToken<ArrayList<Proof>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with ProofData.java
    public static List<ProofData> getProofDataList(Object responseData) {
        Type t = new TypeToken<ArrayList<ProofData>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with Protocol.java
    public static List<Protocol> getProtocolList(Object responseData) {
        Type t = new TypeToken<ArrayList<Protocol>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with ProtocolData.java
    public static List<ProtocolData> getProtocolDataList(Object responseData) {
        Type t = new TypeToken<ArrayList<ProtocolData>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with ReputationData.java
    public static List<ReputationData> getReputationDataList(Object responseData) {
        Type t = new TypeToken<ArrayList<ReputationData>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with Secret.java
    public static List<Secret> getSecretList(Object responseData) {
        Type t = new TypeToken<ArrayList<Secret>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with SecretData.java
    public static List<SecretData> getSecretDataList(Object responseData) {
        Type t = new TypeToken<ArrayList<SecretData>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with Service.java
    public static List<Service> getServiceList(Object responseData) {
        Type t = new TypeToken<ArrayList<Service>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with ServiceData.java
    public static List<ServiceData> getServiceDataList(Object responseData) {
        Type t = new TypeToken<ArrayList<ServiceData>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with Statement.java
    public static List<Statement> getStatementList(Object responseData) {
        Type t = new TypeToken<ArrayList<Statement>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with StatementData.java
    public static List<StatementData> getStatementDataList(Object responseData) {
        Type t = new TypeToken<ArrayList<StatementData>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with Team.java
    public static List<Team> getTeamList(Object responseData) {
        Type t = new TypeToken<ArrayList<Team>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Replace Code with TeamData.java
    public static List<TeamData> getTeamDataList(Object responseData) {
        Type t = new TypeToken<ArrayList<TeamData>>() {}.getType();
        Gson gson = new Gson();
        return new Gson().fromJson(gson.toJson(responseData), t);
    }

    // Method for CidInfo
    public static Map<String, CidInfo> getCidInfoMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, CidInfo>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for BlockInfo
    public static Map<String, BlockInfo> getBlockInfoMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, BlockInfo>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for TxInfo
    public static Map<String, TxInfo> getTxInfoMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, TxInfo>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for WebhookInfo
    public static Map<String, WebhookInfo> getWebhookInfoMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, WebhookInfo>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for Address
    public static Map<String, Address> getAddressMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Address>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for Block
    public static Map<String, Block> getBlockMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Block>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for BlockHas
    public static Map<String, BlockHas> getBlockHasMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, BlockHas>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for BlockMark
    public static Map<String, BlockMark> getBlockMarkMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, BlockMark>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for Cash
    public static Map<String, Cash> getCashMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Cash>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for CashMark
    public static Map<String, CashMark> getCashMarkMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, CashMark>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for Nobody
    public static Map<String, Nobody> getNobodyMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Nobody>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for OpReturn
    public static Map<String, OpReturn> getOpReturnMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, OpReturn>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for P2SH
    public static Map<String, P2SH> getP2SHMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, P2SH>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for P2SHRaw
    public static Map<String, P2SHRaw> getP2SHRawMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, P2SHRaw>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for Tx
    public static Map<String, Tx> getTxMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Tx>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for TxHas
    public static Map<String, TxHas> getTxHasMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, TxHas>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    // Method for TxMark
    public static Map<String, TxMark> getTxMarkMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, TxMark>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, App> getAppMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, App>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, AppData> getAppDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, AppData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Box> getBoxMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Box>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, BoxData> getBoxDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, BoxData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Cid> getCidMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Cid>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, CidData> getCidDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, CidData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Code> getCodeMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Code>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, CodeData> getCodeDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, CodeData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Contact> getContactMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Contact>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, ContactData> getContactDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, ContactData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, FcInfo> getFcInfoMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, FcInfo>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Group> getGroupMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Group>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, GroupData> getGroupDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, GroupData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, HomepageData> getHomepageDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, HomepageData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Mail> getMailMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Mail>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, MailData> getMailDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, MailData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, MasterData> getMasterDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, MasterData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Nid> getNidMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Nid>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, NidData> getNidDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, NidData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, NobodyData> getNobodyDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, NobodyData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, NoticeFeeData> getNoticeFeeDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, NoticeFeeData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Proof> getProofMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Proof>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, ProofData> getProofDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, ProofData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Protocol> getProtocolMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Protocol>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, ProtocolData> getProtocolDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, ProtocolData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, ReputationData> getReputationDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, ReputationData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Secret> getSecretMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Secret>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, SecretData> getSecretDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, SecretData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Service> getServiceMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Service>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, ServiceData> getServiceDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, ServiceData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Statement> getStatementMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Statement>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, StatementData> getStatementDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, StatementData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, Team> getTeamMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, Team>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }

    public static Map<String, TeamData> getTeamDataMap(Object responseData) {
        Type t = new TypeToken<HashMap<String, TeamData>>() {}.getType();
        return new Gson().fromJson(new Gson().toJson(responseData), t);
    }
}